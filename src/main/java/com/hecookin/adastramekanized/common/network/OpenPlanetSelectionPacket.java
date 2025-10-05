package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet sent from server to client to open the planet selection screen.
 * Contains list of available planets for travel.
 */
public record OpenPlanetSelectionPacket(List<String> planetIds) implements CustomPacketPayload {

    public static final Type<OpenPlanetSelectionPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "open_planet_selection")
    );

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, OpenPlanetSelectionPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
        OpenPlanetSelectionPacket::planetIds,
        OpenPlanetSelectionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the client side
     */
    public static void handle(OpenPlanetSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Get planet data from registry
            PlanetRegistry registry = PlanetRegistry.getInstance();
            List<Planet> planets = new ArrayList<>();

            for (String planetId : packet.planetIds) {
                Planet planet = registry.getPlanet(ResourceLocation.parse(planetId));
                if (planet != null) {
                    planets.add(planet);
                }
            }

            // Open the planet selection screen
            Minecraft.getInstance().setScreen(
                new com.hecookin.adastramekanized.client.screens.PlanetSelectionScreen(planets)
            );
        });
    }
}
