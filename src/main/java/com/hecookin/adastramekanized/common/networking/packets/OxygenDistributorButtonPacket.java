package com.hecookin.adastramekanized.common.networking.packets;

import com.hecookin.adastramekanized.common.blockentities.machines.MekanismBasedOxygenDistributor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.hecookin.adastramekanized.AdAstraMekanized;

public record OxygenDistributorButtonPacket(BlockPos pos, ButtonType buttonType, boolean state) implements CustomPacketPayload {

    public static final Type<OxygenDistributorButtonPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "oxygen_distributor_button")
    );

    public static final StreamCodec<FriendlyByteBuf, OxygenDistributorButtonPacket> STREAM_CODEC = StreamCodec.of(
        OxygenDistributorButtonPacket::write,
        OxygenDistributorButtonPacket::read
    );

    public enum ButtonType {
        POWER,
        VISIBILITY
    }

    public static void write(FriendlyByteBuf buf, OxygenDistributorButtonPacket packet) {
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.buttonType);
        buf.writeBoolean(packet.state);
    }

    public static OxygenDistributorButtonPacket read(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ButtonType type = buf.readEnum(ButtonType.class);
        boolean state = buf.readBoolean();
        return new OxygenDistributorButtonPacket(pos, type, state);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity be = serverPlayer.level().getBlockEntity(pos);
                if (be instanceof MekanismBasedOxygenDistributor distributor) {
                    switch (buttonType) {
                        case POWER -> distributor.setActive(state);
                        case VISIBILITY -> distributor.setOxygenBlockVisibility(state);
                    }
                    be.setChanged();
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}