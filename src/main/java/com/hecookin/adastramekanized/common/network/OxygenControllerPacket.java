package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import com.hecookin.adastramekanized.common.items.OxygenNetworkController;
import com.hecookin.adastramekanized.common.menus.OxygenControllerMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for oxygen controller GUI actions
 */
public record OxygenControllerPacket(ActionType action, int distributorIndex, boolean boolValue, String stringValue)
    implements CustomPacketPayload {

    public enum ActionType {
        TOGGLE_DISTRIBUTOR,
        REMOVE_DISTRIBUTOR,
        RENAME_DISTRIBUTOR,
        MASTER_TOGGLE,
        CLEAR_ALL,
        UPDATE_STATUS,
        REMOTE_TOGGLE // Direct toggle from controller item
    }

    public static final Type<OxygenControllerPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "oxygen_controller")
    );

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, OxygenControllerPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.idMapper(i -> ActionType.values()[i], ActionType::ordinal), OxygenControllerPacket::action,
        ByteBufCodecs.INT, OxygenControllerPacket::distributorIndex,
        ByteBufCodecs.BOOL, OxygenControllerPacket::boolValue,
        ByteBufCodecs.STRING_UTF8, OxygenControllerPacket::stringValue,
        OxygenControllerPacket::new
    );

    // Convenience constructors
    public static OxygenControllerPacket toggleDistributor(int index) {
        return new OxygenControllerPacket(ActionType.TOGGLE_DISTRIBUTOR, index, false, "");
    }

    public static OxygenControllerPacket removeDistributor(int index) {
        return new OxygenControllerPacket(ActionType.REMOVE_DISTRIBUTOR, index, false, "");
    }

    public static OxygenControllerPacket renameDistributor(int index, String name) {
        return new OxygenControllerPacket(ActionType.RENAME_DISTRIBUTOR, index, false, name);
    }

    public static OxygenControllerPacket masterToggle(boolean enableAll) {
        return new OxygenControllerPacket(ActionType.MASTER_TOGGLE, -1, enableAll, "");
    }

    public static OxygenControllerPacket clearAll() {
        return new OxygenControllerPacket(ActionType.CLEAR_ALL, -1, false, "");
    }

    public static OxygenControllerPacket updateStatus() {
        return new OxygenControllerPacket(ActionType.UPDATE_STATUS, -1, false, "");
    }

    public static OxygenControllerPacket remoteToggle(boolean enable) {
        return new OxygenControllerPacket(ActionType.REMOTE_TOGGLE, -1, enable, "");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the server side
     */
    public static void handle(OxygenControllerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Handle remote toggle directly (not through GUI)
                if (packet.action == ActionType.REMOTE_TOGGLE) {
                    handleRemoteToggle(player, packet.boolValue);
                    return;
                }

                // Handle GUI actions
                if (player.containerMenu instanceof OxygenControllerMenu menu) {
                    switch (packet.action) {
                        case TOGGLE_DISTRIBUTOR -> {
                            menu.handleButtonClick(0, packet.distributorIndex);
                            // Apply the change immediately
                            applyDistributorToggle(player, menu.getLinkData(), packet.distributorIndex);
                        }
                        case REMOVE_DISTRIBUTOR -> {
                            menu.handleButtonClick(1, packet.distributorIndex);
                        }
                        case RENAME_DISTRIBUTOR -> {
                            menu.handleRename(packet.distributorIndex, packet.stringValue);
                        }
                        case MASTER_TOGGLE -> {
                            menu.handleMasterToggle(packet.boolValue);
                            // Apply to all distributors
                            applyMasterToggle(player, menu.getLinkData(), packet.boolValue);
                        }
                        case CLEAR_ALL -> {
                            menu.handleClearAll();
                        }
                        case UPDATE_STATUS -> {
                            // Update statuses and send back to client
                            OxygenNetworkController.updateDistributorStatuses(player.level(), menu.getLinkData());
                        }
                    }
                }
            }
        });
    }

    private static void handleRemoteToggle(ServerPlayer player, boolean enable) {
        // Find controller in player's inventory
        var controller = player.getMainHandItem();
        if (!(controller.getItem() instanceof OxygenNetworkController)) {
            controller = player.getOffhandItem();
            if (!(controller.getItem() instanceof OxygenNetworkController)) {
                // Search inventory
                for (var stack : player.getInventory().items) {
                    if (stack.getItem() instanceof OxygenNetworkController) {
                        controller = stack;
                        break;
                    }
                }
            }
        }

        if (controller.getItem() instanceof OxygenNetworkController) {
            DistributorLinkData linkData = OxygenNetworkController.getOrCreateLinkData(controller);
            applyMasterToggle(player, linkData, enable);
            OxygenNetworkController.saveLinkData(controller, linkData);
        }
    }

    private static void applyDistributorToggle(ServerPlayer player, DistributorLinkData linkData, int index) {
        if (index < 0 || index >= linkData.getLinkedDistributors().size()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        DistributorLinkData.LinkedDistributor link = linkData.getLinkedDistributors().get(index);
        BlockPos pos = link.getPos();

        if (level.isLoaded(pos)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ImprovedOxygenDistributor distributor) {
                distributor.setManuallyDisabled(!link.isEnabled());
                AdAstraMekanized.LOGGER.info("Remote toggled distributor at {} to {}", pos, link.isEnabled());
            }
        }
    }

    private static void applyMasterToggle(ServerPlayer player, DistributorLinkData linkData, boolean enableAll) {
        ServerLevel level = player.serverLevel();

        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            link.setEnabled(enableAll);
            BlockPos pos = link.getPos();

            if (level.isLoaded(pos)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ImprovedOxygenDistributor distributor) {
                    distributor.setManuallyDisabled(!enableAll);
                    AdAstraMekanized.LOGGER.info("Remote {} distributor at {}", enableAll ? "enabled" : "disabled", pos);
                }
            }
        }
    }
}