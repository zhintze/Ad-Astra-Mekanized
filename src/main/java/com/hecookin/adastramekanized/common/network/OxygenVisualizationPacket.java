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
public record OxygenVisualizationPacket(BlockPos distributorPos, Set<BlockPos> oxygenZones, boolean visible, int colorIndex) implements CustomPacketPayload {

    public static final Type<OxygenVisualizationPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "oxygen_visualization")
    );

    // Alternative constructors for compatibility
    public OxygenVisualizationPacket(boolean enabled, List<BlockPos> zones) {
        this(BlockPos.ZERO, zones != null ? new HashSet<>(zones) : new HashSet<>(), enabled, 0);
    }

    public OxygenVisualizationPacket(BlockPos distributorPos, Set<BlockPos> oxygenZones, boolean visible) {
        this(distributorPos, oxygenZones, visible, 0);
    }

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, OxygenVisualizationPacket> CODEC = StreamCodec.of(
        OxygenVisualizationPacket::encode,
        OxygenVisualizationPacket::decode
    );

    private static void encode(ByteBuf buf, OxygenVisualizationPacket packet) {
        // Write distributor position
        buf.writeLong(packet.distributorPos.asLong());

        // Write visibility flag
        buf.writeBoolean(packet.visible);

        // Write color index
        buf.writeByte(packet.colorIndex);

        // Write zone count and positions
        buf.writeInt(packet.oxygenZones.size());
        for (BlockPos pos : packet.oxygenZones) {
            buf.writeLong(pos.asLong());
        }
    }

    private static OxygenVisualizationPacket decode(ByteBuf buf) {
        // Read distributor position
        BlockPos distributorPos = BlockPos.of(buf.readLong());

        // Read visibility flag
        boolean visible = buf.readBoolean();

        // Read color index
        int colorIndex = buf.readByte();

        // Read zone positions
        int zoneCount = buf.readInt();
        Set<BlockPos> zones = new HashSet<>();
        for (int i = 0; i < zoneCount; i++) {
            zones.add(BlockPos.of(buf.readLong()));
        }

        return new OxygenVisualizationPacket(distributorPos, zones, visible, colorIndex);
    }

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

            // Enable global rendering if any distributor is visible
            renderer.setRenderingEnabled(true);

            if (packet.visible && packet.oxygenZones != null && !packet.oxygenZones.isEmpty()) {
                // Update zones for this specific distributor with color
                renderer.updateDistributorZones(packet.distributorPos, packet.oxygenZones, packet.colorIndex);
                renderer.setDistributorVisibility(packet.distributorPos, true);
                AdAstraMekanized.LOGGER.debug("Client received oxygen visualization for distributor at {}: {} zones, color {}",
                    packet.distributorPos, packet.oxygenZones.size(), packet.colorIndex);
            } else {
                // Clear or hide zones for this distributor
                if (packet.distributorPos.equals(BlockPos.ZERO)) {
                    // Legacy clear all
                    renderer.clearAllZones();
                    AdAstraMekanized.LOGGER.debug("Client cleared all oxygen visualization");
                } else {
                    // Just hide this distributor's zones
                    renderer.setDistributorVisibility(packet.distributorPos, false);
                    AdAstraMekanized.LOGGER.debug("Client hiding oxygen visualization for distributor at {}",
                        packet.distributorPos);
                }
            }
        });
    }
}