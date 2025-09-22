package com.hecookin.adastramekanized.common.dimensions;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.performance.PerformanceMonitor;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetData;
import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import com.hecookin.adastramekanized.api.planets.Planet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime dimension manager for creating real dimensions for dynamic planets.
 * Replaces the template mapping system with actual dimension generation.
 */
public class RuntimeDimensionManager {

    private static final RuntimeDimensionManager INSTANCE = new RuntimeDimensionManager();

    // Constants for server limits
    private static final int MAX_LOADED_DIMENSIONS = 10; // Maximum simultaneous loaded dynamic dimensions
    private static final long DIMENSION_CLEANUP_DELAY = 10 * 60 * 1000; // 10 minutes before cleanup

    // Tracking
    private final Map<ResourceKey<Level>, ServerLevel> loadedDimensions = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, Long> dimensionAccessTimes = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, DynamicPlanetData> dimensionPlanetData = new ConcurrentHashMap<>();

    private MinecraftServer server;
    private boolean initialized = false;

    private RuntimeDimensionManager() {
        // Private constructor for singleton
    }

    public static RuntimeDimensionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the runtime dimension manager
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        this.initialized = true;

        AdAstraMekanized.LOGGER.info("RuntimeDimensionManager initialized - max {} simultaneous dimensions",
            MAX_LOADED_DIMENSIONS);
    }

    /**
     * Get or check for a dynamic planet dimension.
     * Dimensions are now expected to be generated during world creation and loaded naturally by NeoForge.
     */
    public ServerLevel getOrCreateDynamicDimension(DynamicPlanetData planetData) {
        if (!initialized) {
            AdAstraMekanized.LOGGER.error("RuntimeDimensionManager not initialized");
            return null;
        }

        try (var timer = PerformanceMonitor.getInstance().startOperation("dynamic_dimension_lookup")) {
            ResourceKey<Level> dimensionKey = planetData.getDimensionKey();

            // Check if dimension is already tracked
            ServerLevel existingLevel = loadedDimensions.get(dimensionKey);
            if (existingLevel != null) {
                updateAccessTime(dimensionKey);
                return existingLevel;
            }

            // Check if dimension exists in server registry (loaded naturally by NeoForge)
            existingLevel = server.getLevel(dimensionKey);
            if (existingLevel != null) {
                // Dimension was found - track it
                loadedDimensions.put(dimensionKey, existingLevel);
                updateAccessTime(dimensionKey);
                dimensionPlanetData.put(dimensionKey, planetData);

                AdAstraMekanized.LOGGER.info("Found existing dimension for planet: {} ({})",
                    planetData.getDisplayName(), dimensionKey.location());
                return existingLevel;
            }

            // Dimension not found - this indicates the datapack files weren't generated properly
            // or the world needs a restart for NeoForge to load the new dimensions
            AdAstraMekanized.LOGGER.warn("Dimension not found for planet: {} ({}). " +
                "Dimension files may need to be generated or the world restarted.",
                planetData.getDisplayName(), dimensionKey.location());

            return null;
        }
    }



    /**
     * Update dimension access time for LRU tracking
     */
    private void updateAccessTime(ResourceKey<Level> dimensionKey) {
        dimensionAccessTimes.put(dimensionKey, System.currentTimeMillis());
    }

    /**
     * Perform cleanup if we're approaching limits
     */
    private void performCleanupIfNeeded() {
        if (loadedDimensions.size() >= MAX_LOADED_DIMENSIONS) {
            AdAstraMekanized.LOGGER.info("Approaching dimension limit ({}), performing LRU cleanup",
                MAX_LOADED_DIMENSIONS);
            cleanupOldestDimension();
        }
    }

    /**
     * Clean up the least recently used dimension
     */
    private void cleanupOldestDimension() {
        long currentTime = System.currentTimeMillis();
        ResourceKey<Level> oldestDimension = null;
        long oldestTime = currentTime;

        for (Map.Entry<ResourceKey<Level>, Long> entry : dimensionAccessTimes.entrySet()) {
            long accessTime = entry.getValue();
            if (accessTime < oldestTime && (currentTime - accessTime) > DIMENSION_CLEANUP_DELAY) {
                oldestTime = accessTime;
                oldestDimension = entry.getKey();
            }
        }

        if (oldestDimension != null) {
            unloadDimension(oldestDimension);
        }
    }

    /**
     * Unload a dimension (remove from tracking, but don't destroy server dimension)
     */
    private void unloadDimension(ResourceKey<Level> dimensionKey) {
        loadedDimensions.remove(dimensionKey);
        dimensionAccessTimes.remove(dimensionKey);
        dimensionPlanetData.remove(dimensionKey);

        AdAstraMekanized.LOGGER.info("Unloaded dimension from tracking: {}", dimensionKey.location());
    }

    /**
     * Get statistics about loaded dimensions
     */
    public DimensionStats getStats() {
        return new DimensionStats(
            loadedDimensions.size(),
            MAX_LOADED_DIMENSIONS,
            dimensionPlanetData.size()
        );
    }

    /**
     * Shutdown and cleanup
     */
    public void shutdown() {
        loadedDimensions.clear();
        dimensionAccessTimes.clear();
        dimensionPlanetData.clear();
        initialized = false;

        AdAstraMekanized.LOGGER.info("RuntimeDimensionManager shutdown complete");
    }

    /**
     * Statistics record
     */
    public record DimensionStats(
        int loadedDimensions,
        int maxDimensions,
        int trackedPlanets
    ) {}
}