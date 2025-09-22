package com.hecookin.adastramekanized.common.teleportation;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.planets.PlanetManager;
import com.hecookin.adastramekanized.common.worldgen.PlanetChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Simplified teleportation system for static planets
 */
public class PlanetTeleportationSystem {

    private static final PlanetTeleportationSystem INSTANCE = new PlanetTeleportationSystem();

    private MinecraftServer server;
    private final Map<String, Long> teleportCache = new ConcurrentHashMap<>();

    private PlanetTeleportationSystem() {
        // Private constructor for singleton
    }

    public static PlanetTeleportationSystem getInstance() {
        return INSTANCE;
    }

    public void initialize(MinecraftServer server) {
        this.server = server;
        AdAstraMekanized.LOGGER.info("Planet teleportation system initialized");
    }

    public void clearCache() {
        teleportCache.clear();
        AdAstraMekanized.LOGGER.info("Teleportation cache cleared");
    }

    /**
     * Teleport result enumeration
     */
    public enum TeleportResult {
        SUCCESS,
        PLANET_NOT_FOUND,
        DIMENSION_NOT_LOADED,
        PLAYER_ERROR,
        SYSTEM_ERROR
    }

    /**
     * Teleport to any planet by ID
     */
    public CompletableFuture<TeleportResult> teleportToAnyPlanet(ServerPlayer player, ResourceLocation planetId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlanetRegistry registry = PlanetRegistry.getInstance();
                Planet planet = registry.getPlanet(planetId);

                if (planet == null) {
                    AdAstraMekanized.LOGGER.warn("Planet not found: {}", planetId);
                    return TeleportResult.PLANET_NOT_FOUND;
                }

                PlanetManager manager = PlanetManager.getInstance();
                if (!manager.isPlanetDimensionLoaded(planetId)) {
                    AdAstraMekanized.LOGGER.warn("Planet dimension not loaded: {}", planetId);
                    return TeleportResult.DIMENSION_NOT_LOADED;
                }

                ServerLevel targetLevel = manager.getPlanetLevel(planetId);
                if (targetLevel == null) {
                    AdAstraMekanized.LOGGER.error("Could not get level for planet: {}", planetId);
                    return TeleportResult.SYSTEM_ERROR;
                }

                // Find safe spawn position
                Vec3 spawnPos = findSafeSpawnPosition(targetLevel);

                // Execute teleportation
                boolean success = executeTeleportation(player, targetLevel, spawnPos);

                if (success) {
                    AdAstraMekanized.LOGGER.info("Successfully teleported {} to planet {}",
                        player.getName().getString(), planet.displayName());
                    return TeleportResult.SUCCESS;
                } else {
                    return TeleportResult.SYSTEM_ERROR;
                }

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Teleportation failed for player {} to planet {}",
                    player.getName().getString(), planetId, e);
                return TeleportResult.SYSTEM_ERROR;
            }
        });
    }

    /**
     * Find a safe spawn position in the target level
     */
    private Vec3 findSafeSpawnPosition(ServerLevel level) {
        // Start at world spawn coordinates
        BlockPos spawnPos = level.getSharedSpawnPos();

        // Try to find a safe surface position, trying multiple nearby locations
        BlockPos surfacePos = findSafeSurfaceNearby(level, spawnPos);

        // Add some height for safety and center on block
        return new Vec3(surfacePos.getX() + 0.5, surfacePos.getY() + 2.0, surfacePos.getZ() + 0.5);
    }

    /**
     * Find a safe surface position, trying multiple locations around the spawn point
     */
    private BlockPos findSafeSurfaceNearby(ServerLevel level, BlockPos center) {
        // Try the center position first
        BlockPos centerSurface = findActualSurface(level, center);
        if (isValidSurfacePosition(level, centerSurface)) {
            return centerSurface;
        }

        // Try positions in expanding circles around the center
        int[] offsets = {0, 5, 10, 16}; // Try at spawn, then 5, 10, and 16 blocks away

        for (int radius : offsets) {
            if (radius == 0) continue; // Already tried center

            // Try 8 positions around the circle
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4.0; // 45-degree increments
                int x = center.getX() + (int) (radius * Math.cos(angle));
                int z = center.getZ() + (int) (radius * Math.sin(angle));

                BlockPos testPos = new BlockPos(x, center.getY(), z);
                BlockPos surface = findActualSurface(level, testPos);

                if (isValidSurfacePosition(level, surface)) {
                    AdAstraMekanized.LOGGER.debug("Found safe surface at distance {} from spawn: ({}, {}, {})",
                        radius, surface.getX(), surface.getY(), surface.getZ());
                    return surface;
                }
            }
        }

        // If no safe position found, return the center surface anyway
        AdAstraMekanized.LOGGER.warn("No safe surface found near spawn, using center position");
        return centerSurface;
    }

    /**
     * Find the actual surface position using custom logic that works with planet terrain
     */
    private BlockPos findActualSurface(ServerLevel level, BlockPos startPos) {
        int x = startPos.getX();
        int z = startPos.getZ();

        // Try multiple methods to find the surface

        // Method 1: Use PlanetChunkGenerator surface calculation if available
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        if (chunkGenerator instanceof PlanetChunkGenerator planetGenerator) {
            try {
                // Use the planet chunk generator's base height calculation
                int surfaceHeight = planetGenerator.getBaseHeight(x, z,
                    net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                    level, level.getChunkSource().randomState());
                BlockPos planetSurface = new BlockPos(x, surfaceHeight + 1, z); // +1 for air above surface

                if (isValidSurfacePosition(level, planetSurface)) {
                    AdAstraMekanized.LOGGER.debug("Found surface using PlanetChunkGenerator at Y={}", planetSurface.getY());
                    return planetSurface;
                }
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.debug("PlanetChunkGenerator surface calculation failed: {}", e.getMessage());
            }
        }

        // Method 2: Try standard heightmap
        try {
            BlockPos heightmapSurface = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, startPos);
            if (isValidSurfacePosition(level, heightmapSurface)) {
                AdAstraMekanized.LOGGER.debug("Found surface using heightmap at Y={}", heightmapSurface.getY());
                return heightmapSurface;
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Heightmap failed, using custom surface finding: {}", e.getMessage());
        }

        // Method 3: Manual surface search for planets with custom terrain
        BlockPos customSurface = findSurfaceByScanning(level, x, z);
        if (customSurface != null) {
            AdAstraMekanized.LOGGER.debug("Found surface using custom scan at Y={}", customSurface.getY());
            return customSurface;
        }

        // Method 4: Fallback to a reasonable height
        int fallbackY = Math.max(level.getSeaLevel() + 10, 80);
        AdAstraMekanized.LOGGER.warn("Using fallback surface height Y={} for position ({}, {})", fallbackY, x, z);
        return new BlockPos(x, fallbackY, z);
    }

    /**
     * Scan from top to bottom to find the actual surface
     */
    private BlockPos findSurfaceByScanning(ServerLevel level, int x, int z) {
        // Start from a reasonable height and scan downward
        int maxY = Math.min(level.getMaxBuildHeight() - 10, 200);
        int minY = Math.max(level.getMinBuildHeight() + 10, -50);

        // Scan downward to find the first solid block
        for (int y = maxY; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos below = pos.below();

            // Check if this position has a solid block below and air above
            if (!level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir() &&
                level.getBlockState(pos.above(2)).isAir()) {
                // Found a solid surface with air above
                return pos.above(); // Return the air block above the surface
            }
        }

        // If no surface found, try scanning upward from minimum height
        for (int y = minY; y <= maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);

            if (!level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir()) {
                return pos.above();
            }
        }

        return null; // No surface found
    }

    /**
     * Check if a position is a valid surface for teleportation
     */
    private boolean isValidSurfacePosition(ServerLevel level, BlockPos pos) {
        try {
            // Check that there's solid ground below and air above
            BlockPos below = pos.below();

            boolean hasGround = !level.getBlockState(below).isAir();
            boolean hasAirAbove = level.getBlockState(pos).isAir() &&
                                  level.getBlockState(pos.above()).isAir();

            return hasGround && hasAirAbove;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Execute the actual teleportation
     */
    private boolean executeTeleportation(ServerPlayer player, ServerLevel targetLevel, Vec3 position) {
        try {
            // Teleport the player
            player.teleportTo(targetLevel, position.x, position.y, position.z,
                player.getYRot(), player.getXRot());

            return true;
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to execute teleportation for player {}",
                player.getName().getString(), e);
            return false;
        }
    }

    /**
     * Get all available planets for teleportation
     */
    public List<Planet> getAllAvailablePlanets() {
        PlanetRegistry registry = PlanetRegistry.getInstance();
        if (!registry.isDataLoaded()) {
            return List.of();
        }
        return List.copyOf(registry.getAllPlanets());
    }

    /**
     * Check if a planet is available for teleportation
     */
    public boolean isPlanetAvailable(ResourceLocation planetId) {
        PlanetManager manager = PlanetManager.getInstance();
        return manager.isPlanetDimensionLoaded(planetId);
    }
}