package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.renderers.OxygenZoneRenderer;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Packet to sync oxygen zone visualization from server to client
 */
public record OxygenVisualizationPacket(boolean enabled, List<BlockPos> oxygenZones) implements CustomPacketPayload {

    public static final Type<OxygenVisualizationPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "oxygen_visualization")
    );

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, OxygenVisualizationPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, OxygenVisualizationPacket::enabled,
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), OxygenVisualizationPacket::oxygenZones,
        OxygenVisualizationPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the client side
     */
    public static void handle(OxygenVisualizationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Update the oxygen zone renderer on the client
            OxygenZoneRenderer renderer = OxygenZoneRenderer.getInstance();
            renderer.setRenderingEnabled(packet.enabled);

            if (packet.enabled && packet.oxygenZones != null) {
                Set<BlockPos> zones = new HashSet<>(packet.oxygenZones);
                renderer.updateOxygenZones(zones);
                AdAstraMekanized.LOGGER.debug("Client received oxygen visualization update: {} zones", zones.size());
            } else {
                renderer.clearOxygenZones();
                AdAstraMekanized.LOGGER.debug("Client cleared oxygen visualization");
            }
        });
    }
}