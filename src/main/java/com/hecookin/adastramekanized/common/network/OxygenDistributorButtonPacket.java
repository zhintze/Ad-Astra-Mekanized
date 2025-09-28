package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet to handle oxygen distributor GUI button clicks
 */
public record OxygenDistributorButtonPacket(BlockPos pos, ButtonType buttonType, int value) implements CustomPacketPayload {

    public enum ButtonType {
        POWER,
        VISIBILITY,
        COLOR
    }

    public static final Type<OxygenDistributorButtonPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "oxygen_distributor_button")
    );

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, OxygenDistributorButtonPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, OxygenDistributorButtonPacket::pos,
        ByteBufCodecs.idMapper(i -> ButtonType.values()[i], ButtonType::ordinal), OxygenDistributorButtonPacket::buttonType,
        ByteBufCodecs.INT, OxygenDistributorButtonPacket::value,
        OxygenDistributorButtonPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the server side
     */
    public static void handle(OxygenDistributorButtonPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Validate the player can interact with this position
                if (player.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) > 64) {
                    AdAstraMekanized.LOGGER.warn("Player {} tried to interact with oxygen distributor too far away", player.getName().getString());
                    return;
                }

                BlockEntity be = player.level().getBlockEntity(packet.pos);
                if (be instanceof ImprovedOxygenDistributor distributor) {
                    switch (packet.buttonType) {
                        case POWER -> {
                            distributor.setManuallyDisabled(packet.value == 0);  // value=0 means OFF (disabled)
                            AdAstraMekanized.LOGGER.debug("Player {} toggled oxygen distributor power to {}",
                                player.getName().getString(), packet.value != 0 ? "ON" : "OFF");
                        }
                        case VISIBILITY -> {
                            distributor.setOxygenBlockVisibility(packet.value != 0);
                            AdAstraMekanized.LOGGER.debug("Player {} toggled oxygen visibility to {}", player.getName().getString(), packet.value != 0);
                        }
                        case COLOR -> {
                            distributor.setOxygenBlockColor(packet.value);
                            AdAstraMekanized.LOGGER.debug("Player {} changed oxygen color to index {}", player.getName().getString(), packet.value);
                        }
                    }
                }
            }
        });
    }
}