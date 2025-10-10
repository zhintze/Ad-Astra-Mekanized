package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.constants.RocketConstants;
import com.hecookin.adastramekanized.common.entities.vehicles.Lander;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.hecookin.adastramekanized.common.registry.ModEntityTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet sent from client to server when player selects a planet to land on.
 * Triggers landing animation sequence by spawning a Lander entity.
 */
public record ServerboundLandPacket(String planetId, boolean tryPreviousLocation) implements CustomPacketPayload {

    public static final Type<ServerboundLandPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "land")
    );

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, ServerboundLandPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ServerboundLandPacket::planetId,
        ByteBufCodecs.BOOL, ServerboundLandPacket::tryPreviousLocation,
        ServerboundLandPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the server side - initiates landing sequence.
     * For regular planets: spawns a Lander entity for descent.
     * For Earth/Overworld: special case handling.
     */
    public static void handle(ServerboundLandPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Entity vehicle = player.getVehicle();
            if (!(vehicle instanceof Rocket rocket)) return;

            ResourceLocation planetId = ResourceLocation.parse(packet.planetId);

            if (planetId.equals(ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"))) {
                ResourceKey<Level> overworldKey = Level.OVERWORLD;
                ServerLevel overworld = player.server.getLevel(overworldKey);
                if (overworld != null) {
                    handlePlanetLanding(player, rocket, overworld, planetId);
                }
                return;
            }

            Planet planet = PlanetRegistry.getInstance().getPlanet(planetId);

            if (planet == null) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Planet not found!"),
                    false
                );
                return;
            }

            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, planet.dimension().dimensionType());
            ServerLevel targetLevel = player.server.getLevel(dimensionKey);
            if (targetLevel == null) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Planet dimension not loaded!"),
                    false
                );
                return;
            }

            handlePlanetLanding(player, rocket, targetLevel, planetId);
        });
    }

    /**
     * Handle planet landing with lander entity.
     * Works for all planets including Earth's Orbit.
     */
    private static void handlePlanetLanding(ServerPlayer player, Rocket rocket, ServerLevel targetLevel, ResourceLocation planetId) {
        // Earth Orbit special handling: always land at station coordinates
        boolean isEarthOrbit = planetId.getPath().equals("earth_orbit");
        BlockPos targetPos;
        int landingHeight;

        if (isEarthOrbit) {
            // Earth Orbit: spawn at station coordinates (0, 0) at Y=600, centered at 0.5, 0.5
            targetPos = new BlockPos(0, 0, 0);
            landingHeight = 600;
            AdAstraMekanized.LOGGER.info("Player {} landing at Earth Orbit station: {}", player.getName().getString(), targetPos);
        } else {
            // Other planets: use saved coordinates if available, otherwise current position
            BlockPos savedPos = com.hecookin.adastramekanized.common.util.LaunchCoordinateTracker.getLaunchCoordinates(player, targetLevel.dimension());

            if (savedPos != null) {
                // Use saved launch coordinates (X and Z)
                targetPos = savedPos;
                AdAstraMekanized.LOGGER.info("Player {} landing at saved coordinates: {}", player.getName().getString(), targetPos);
            } else {
                // No saved coordinates, use current position
                targetPos = player.blockPosition();
                AdAstraMekanized.LOGGER.info("Player {} landing at current position: {}", player.getName().getString(), targetPos);
            }
            landingHeight = RocketConstants.ATMOSPHERE_LEAVE_HEIGHT;
        }

        Vec3 landingPos = new Vec3(targetPos.getX() + 0.5, landingHeight, targetPos.getZ() + 0.5);

        // Stop riding for teleportation
        player.stopRiding();

        // Teleport player to target dimension
        ServerPlayer teleportedPlayer = teleportToDimension(player, targetLevel, landingPos);

        // Create Lander entity in target dimension
        Lander lander = ModEntityTypes.LANDER.get().create(targetLevel);
        if (lander == null) {
            AdAstraMekanized.LOGGER.error("Failed to create lander entity for player {}", player.getName().getString());
            return;
        }

        lander.setPos(landingPos);
        targetLevel.addFreshEntity(lander);

        // Transfer rocket inventory to lander (slots 1-10)
        var rocketInventory = rocket.inventory();
        var landerInventory = lander.inventory();
        for (int i = 0; i < rocketInventory.getContainerSize(); i++) {
            landerInventory.setItem(i + 1, rocketInventory.getItem(i));
        }

        // Store rocket as item in lander inventory (slot 0)
        landerInventory.setItem(0, rocket.getDropStack());

        // Player rides the lander
        teleportedPlayer.startRiding(lander);

        // Remove old rocket entity
        rocket.discard();

        // Grant planet visited advancement
        grantPlanetVisitedAdvancement(teleportedPlayer, planetId);

        AdAstraMekanized.LOGGER.info("Player {} landing on planet {} in lander", player.getName().getString(), planetId);
    }

    /**
     * Teleport player to another dimension.
     * Returns the player entity in the target dimension (may be different instance).
     */
    private static ServerPlayer teleportToDimension(ServerPlayer player, ServerLevel targetLevel, Vec3 pos) {
        player.teleportTo(targetLevel, pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
        return player.server.getPlayerList().getPlayer(player.getUUID());
    }

    /**
     * Grant the planet visited advancement to a player.
     * Advancement ID format: adastramekanized:planets/visited_<planet_path>
     */
    private static void grantPlanetVisitedAdvancement(ServerPlayer player, ResourceLocation planetId) {
        // Convert planet ID to advancement ID
        // minecraft:overworld -> adastramekanized:planets/visited_overworld
        // adastramekanized:mars -> adastramekanized:planets/visited_mars
        String planetPath = planetId.getPath();
        ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID,
            "planets/visited_" + planetPath
        );

        // Get the advancement
        net.minecraft.advancements.AdvancementHolder advancement = player.server.getAdvancements().get(advancementId);

        if (advancement == null) {
            AdAstraMekanized.LOGGER.debug("No advancement found for planet: {}", planetId);
            return;
        }

        // Grant all criteria for the advancement
        net.minecraft.advancements.AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);

        if (!progress.isDone()) {
            for (String criterion : progress.getRemainingCriteria()) {
                player.getAdvancements().award(advancement, criterion);
            }
            AdAstraMekanized.LOGGER.info("Granted planet visited advancement for {} to player {}",
                planetPath, player.getName().getString());
        }
    }
}
