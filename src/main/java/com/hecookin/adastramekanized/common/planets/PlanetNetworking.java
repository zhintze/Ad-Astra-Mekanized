package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handles networking for planet data synchronization between server and client.
 *
 * Manages sending planet registry data to clients when they join and
 * handling registry updates.
 */
public class PlanetNetworking {

    private static final String PROTOCOL_VERSION = "1";

    /**
     * Register network handlers
     */
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(AdAstraMekanized.MOD_ID)
                .versioned(PROTOCOL_VERSION);

        registrar.playToClient(
                PlanetSyncPacket.TYPE,
                PlanetSyncPacket.STREAM_CODEC,
                PlanetNetworking::handlePlanetSync
        );

        registrar.playToClient(
                PlanetUpdatePacket.TYPE,
                PlanetUpdatePacket.STREAM_CODEC,
                PlanetNetworking::handlePlanetUpdate
        );

        AdAstraMekanized.LOGGER.debug("Registered planet networking handlers");
    }

    /**
     * Send all planet data to a player
     */
    public static void sendPlanetDataToPlayer(ServerPlayer player) {
        PlanetRegistry registry = PlanetRegistry.getInstance();
        Collection<Planet> planets = registry.getAllPlanets();

        if (!planets.isEmpty()) {
            PlanetSyncPacket packet = new PlanetSyncPacket(List.copyOf(planets));
            PacketDistributor.sendToPlayer(player, packet);

            AdAstraMekanized.LOGGER.debug("Sent {} planets to player {}", planets.size(), player.getName().getString());
        }
    }

    /**
     * Send planet data to all players
     */
    public static void sendPlanetDataToAll() {
        PlanetRegistry registry = PlanetRegistry.getInstance();
        Collection<Planet> planets = registry.getAllPlanets();

        if (!planets.isEmpty()) {
            PlanetSyncPacket packet = new PlanetSyncPacket(List.copyOf(planets));
            PacketDistributor.sendToAllPlayers(packet);

            AdAstraMekanized.LOGGER.debug("Sent {} planets to all players", planets.size());
        }
    }

    /**
     * Send a single planet update to all players
     */
    public static void sendPlanetUpdate(Planet planet) {
        PlanetUpdatePacket packet = new PlanetUpdatePacket(planet, PlanetUpdatePacket.Action.ADD_OR_UPDATE);
        PacketDistributor.sendToAllPlayers(packet);

        AdAstraMekanized.LOGGER.debug("Sent planet update for: {}", planet.id());
    }

    /**
     * Send planet removal to all players
     */
    public static void sendPlanetRemoval(ResourceLocation planetId) {
        // Create a minimal planet for removal (only ID matters)
        Planet dummyPlanet = new Planet(
                planetId, "",
                new Planet.PlanetProperties(1.0f, 0.0f, 24.0f, 100, false, 0),
                new Planet.AtmosphereData(false, false, 0.0f, 0.0f, Planet.AtmosphereType.NONE),
                new Planet.DimensionSettings(
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "fixed"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "flat"),
                        false, 0, 0, 0.0f
                )
        );

        PlanetUpdatePacket packet = new PlanetUpdatePacket(dummyPlanet, PlanetUpdatePacket.Action.REMOVE);
        PacketDistributor.sendToAllPlayers(packet);

        AdAstraMekanized.LOGGER.debug("Sent planet removal for: {}", planetId);
    }

    // Packet handlers

    private static void handlePlanetSync(PlanetSyncPacket packet, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            PlanetRegistry registry = PlanetRegistry.getInstance();

            // Clear existing client data
            registry.clearAll();

            // Register all planets from packet
            int successCount = 0;
            for (Planet planet : packet.planets()) {
                if (registry.registerPlanet(planet)) {
                    successCount++;
                }
            }

            registry.markDataLoaded();
            registry.markClientSynced();

            AdAstraMekanized.LOGGER.info("Received planet sync: {}/{} planets registered",
                    successCount, packet.planets().size());
        });
    }

    private static void handlePlanetUpdate(PlanetUpdatePacket packet, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            PlanetRegistry registry = PlanetRegistry.getInstance();

            switch (packet.action()) {
                case ADD_OR_UPDATE -> {
                    if (registry.registerPlanet(packet.planet())) {
                        AdAstraMekanized.LOGGER.debug("Updated planet: {}", packet.planet().id());
                    } else {
                        AdAstraMekanized.LOGGER.warn("Failed to update planet: {}", packet.planet().id());
                    }
                }
                case REMOVE -> {
                    Planet removed = registry.unregisterPlanet(packet.planet().id());
                    if (removed != null) {
                        AdAstraMekanized.LOGGER.debug("Removed planet: {}", packet.planet().id());
                    }
                }
            }
        });
    }

    // Packet classes

    /**
     * Packet for synchronizing all planet data to client
     */
    public record PlanetSyncPacket(List<Planet> planets) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<PlanetSyncPacket> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planet_sync"));

        public static final StreamCodec<FriendlyByteBuf, PlanetSyncPacket> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.fromCodec(Planet.CODEC)),
                        packet -> List.copyOf(packet.planets()),
                        list -> new PlanetSyncPacket(List.copyOf(list))
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Packet for individual planet updates
     */
    public record PlanetUpdatePacket(Planet planet, Action action) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<PlanetUpdatePacket> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planet_update"));

        public static final StreamCodec<FriendlyByteBuf, PlanetUpdatePacket> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.fromCodec(Planet.CODEC),
                        PlanetUpdatePacket::planet,
                        Action.STREAM_CODEC,
                        PlanetUpdatePacket::action,
                        PlanetUpdatePacket::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public enum Action {
            ADD_OR_UPDATE,
            REMOVE;

            public static final StreamCodec<FriendlyByteBuf, Action> STREAM_CODEC =
                    StreamCodec.of(
                            (buf, action) -> buf.writeEnum(action),
                            buf -> buf.readEnum(Action.class)
                    );
        }
    }
}