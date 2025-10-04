package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.entities.vehicles.Vehicle;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for synchronizing vehicle control inputs (movement, steering) from client to server.
 * Adapted from Ad Astra's vehicle control system.
 */
public record VehicleControlPacket(float xxa, float zza) implements CustomPacketPayload {

    public static final Type<VehicleControlPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "vehicle_control")
    );

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, VehicleControlPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, VehicleControlPacket::xxa,
        ByteBufCodecs.FLOAT, VehicleControlPacket::zza,
        VehicleControlPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the server side
     */
    public static void handle(VehicleControlPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.getVehicle() instanceof Vehicle vehicle) {
                    vehicle.updateInput(packet.xxa, packet.zza);
                }
            }
        });
    }
}
