package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.world.SpaceStationSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Handles fall-out teleportation and space station spawning for Earth's Orbit dimension.
 * When players fall below Y=0, teleports them back to Earth (Overworld) at Y=600 at corresponding X/Z coordinates.
 * Also triggers automatic space station spawning when players first enter the dimension.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class EarthOrbitFalloutHandler {

    private static final ResourceLocation EARTH_ORBIT_DIM =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "earth_orbit");

    private static final int FALLOUT_Y_THRESHOLD = 0;
    private static final int OVERWORLD_SPAWN_Y = 600;

    /**
     * Trigger space station spawning when players enter Earth's Orbit
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getLevel().isClientSide()) return;

        // Check if player is entering Earth's Orbit
        ResourceLocation currentDim = player.level().dimension().location();
        if (!currentDim.equals(EARTH_ORBIT_DIM)) return;

        AdAstraMekanized.LOGGER.info("Player {} entered Earth's Orbit, triggering structure spawn check", player.getName().getString());

        // Trigger space station spawning (only spawns once per world)
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            SpaceStationSpawner.trySpawnStation(serverLevel);
        }
    }

    /**
     * Monitor players for fall-out condition.
     * Uses PlayerTickEvent instead of EntityTickEvent for better performance (only fires for players).
     */
    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Check if player is in Earth's Orbit dimension
        ResourceLocation currentDim = player.level().dimension().location();
        if (!currentDim.equals(EARTH_ORBIT_DIM)) return;

        // Check if player has fallen below threshold
        if (player.getY() < FALLOUT_Y_THRESHOLD) {
            teleportToEarth(player);
        }
    }

    private static void teleportToEarth(ServerPlayer player) {
        ServerLevel earth = player.server.getLevel(Level.OVERWORLD);
        if (earth == null) {
            AdAstraMekanized.LOGGER.error("Cannot teleport player - Earth (Overworld) dimension not found!");
            return;
        }

        // Keep X/Z coordinates, set Y to atmospheric re-entry height
        double x = player.getX();
        double z = player.getZ();
        double y = OVERWORLD_SPAWN_Y;

        AdAstraMekanized.LOGGER.info("Player {} fell out of Earth's Orbit, teleporting back to Earth at ({}, {}, {})",
            player.getName().getString(), x, y, z);

        // Teleport player
        player.teleportTo(earth, x, y, z, player.getYRot(), player.getXRot());

        // Reset fall distance to prevent fall damage
        player.resetFallDistance();
    }
}
