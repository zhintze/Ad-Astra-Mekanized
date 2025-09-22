package com.hecookin.adastramekanized.common.dimensions;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.performance.PerformanceMonitor;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetData;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetRegistry;
import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Enhanced runtime dimension registry with lazy loading and LRU management.
 * Integrates with DynamicPlanetRegistry for efficient dimension management.
 */
public class EnhancedRuntimeDimensionRegistry {

    private static final EnhancedRuntimeDimensionRegistry INSTANCE = new EnhancedRuntimeDimensionRegistry();

    // Performance constants
    private static final int MAX_LOADED_DIMENSIONS = 10;
    private static final long DIMENSION_UNLOAD_DELAY = 5 * 60 * 1000; // 5 minutes
    private static final long MAINTENANCE_INTERVAL = 2 * 60 * 1000; // 2 minutes

    // Tracking maps
    private final Map<ResourceKey<Level>, ServerLevel> templateMapping = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, Long> dimensionAccessTimes = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, DynamicPlanetData> dimensionToPlanet = new ConcurrentHashMap<>();

    private MinecraftServer server;
    private boolean initialized = false;
    private long lastMaintenanceTime = 0;

    private EnhancedRuntimeDimensionRegistry() {
        // Private constructor for singleton
    }

    public static EnhancedRuntimeDimensionRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the enhanced registry
     */
    public void initialize(MinecraftServer server) {
        if (initialized) {
            AdAstraMekanized.LOGGER.warn("EnhancedRuntimeDimensionRegistry already initialized");
            return;
        }

        this.server = server;
        this.initialized = true;
        this.lastMaintenanceTime = System.currentTimeMillis();

        AdAstraMekanized.LOGGER.info("EnhancedRuntimeDimensionRegistry initialized with lazy loading (max {} dimensions)", MAX_LOADED_DIMENSIONS);
    }

    /**
     * Get or create a dimension for a dynamic planet
     */
    public ServerLevel getOrCreateDynamicDimension(DynamicPlanetData planetData) {
        if (!initialized) {
            AdAstraMekanized.LOGGER.error("Cannot get/create dimension: registry not initialized");
            return null;
        }

        try (var timer = PerformanceMonitor.getInstance().startOperation("dimension_creation")) {
            ResourceKey<Level> planetDimensionKey = planetData.getDimensionKey();

            // Check if we have a template mapping for this planet
            ServerLevel mappedLevel = templateMapping.get(planetDimensionKey);
            if (mappedLevel != null) {
                updateDimensionAccess(planetDimensionKey, planetData);
                return mappedLevel;
            }

            // Perform maintenance before creating new mapping
            performMaintenance();

            // Check if we can load more dimensions
            if (templateMapping.size() >= MAX_LOADED_DIMENSIONS) {
                AdAstraMekanized.LOGGER.info("Maximum dimensions mapped ({}), triggering LRU unload", MAX_LOADED_DIMENSIONS);
                unloadLeastRecentlyUsedDimension();
            }

            // Create new dimension mapping using template
            return createDimensionMapping(planetData);
        }
    }

    /**
     * Create dimension mapping from template
     */
    private ServerLevel createDimensionMapping(DynamicPlanetData planetData) {
        try {
            AdAstraMekanized.LOGGER.info("Creating template-based dimension mapping for planet: {} (type: {})",
                planetData.getDisplayName(), planetData.getEffectsType());

            // Validate planet data
            if (planetData == null) {
                AdAstraMekanized.LOGGER.error("Cannot create dimension mapping: planetData is null");
                return null;
            }

            if (planetData.getEffectsType() == null) {
                AdAstraMekanized.LOGGER.error("Cannot create dimension mapping: effectsType is null for planet {}",
                    planetData.getDisplayName());
                return null;
            }

            // Use existing dimension as template based on effects type
            ResourceKey<Level> templateKey = getTemplateDimensionKey(planetData.getEffectsType());
            if (templateKey == null) {
                AdAstraMekanized.LOGGER.error("No template dimension key found for effects type: {}",
                    planetData.getEffectsType());
                return null;
            }

            ServerLevel templateLevel = server.getLevel(templateKey);
            if (templateLevel == null) {
                AdAstraMekanized.LOGGER.error("Template dimension not found: {} for effects type: {}",
                    templateKey.location(), planetData.getEffectsType());
                return null;
            }

            // Map the planet dimension to the template dimension
            ResourceKey<Level> planetDimensionKey = planetData.getDimensionKey();
            templateMapping.put(planetDimensionKey, templateLevel);

            updateDimensionAccess(planetDimensionKey, planetData);

            AdAstraMekanized.LOGGER.info("Successfully mapped planet {} to template dimension {}",
                planetData.getDisplayName(), templateKey.location());

            return templateLevel;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to create template dimension mapping for planet {}: {}",
                planetData.getDisplayName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get template dimension key based on effects type
     */
    private ResourceKey<Level> getTemplateDimensionKey(DimensionEffectsType effectsType) {
        return switch (effectsType) {
            case MOON_LIKE -> Level.END; // Airless environment
            case ASTEROID_LIKE -> Level.END; // Airless environment like moon
            case VOLCANIC -> Level.NETHER; // Hot environment
            case ICE_WORLD -> Level.OVERWORLD; // Cold but atmospheric
            case GAS_GIANT -> Level.NETHER; // Dense atmosphere
            case ROCKY -> Level.OVERWORLD; // Earth-like
            case ALTERED_OVERWORLD -> Level.OVERWORLD; // Earth-like environment
        };
    }

    /**
     * Update dimension access time for LRU tracking
     */
    private void updateDimensionAccess(ResourceKey<Level> dimensionKey, DynamicPlanetData planetData) {
        long currentTime = System.currentTimeMillis();
        dimensionAccessTimes.put(dimensionKey, currentTime);
        dimensionToPlanet.put(dimensionKey, planetData);

        // Update planet access time in registry
        if (planetData != null) {
            DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
            registry.markDimensionLoaded(planetData.getPlanetId());
        }
    }

    /**
     * Unload least recently used dimension
     */
    private void unloadLeastRecentlyUsedDimension() {
        if (dimensionAccessTimes.isEmpty()) {
            return;
        }

        // Find least recently used dimension
        ResourceKey<Level> lruDimension = dimensionAccessTimes.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);

        if (lruDimension != null && templateMapping.containsKey(lruDimension)) {
            DynamicPlanetData planetData = dimensionToPlanet.get(lruDimension);

            AdAstraMekanized.LOGGER.info("Unloading LRU dimension mapping: {} (planet: {})",
                lruDimension.location(),
                planetData != null ? planetData.getDisplayName() : "Unknown");

            // Remove from our tracking
            templateMapping.remove(lruDimension);
            dimensionAccessTimes.remove(lruDimension);
            dimensionToPlanet.remove(lruDimension);

            // Update planet registry
            if (planetData != null) {
                DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
                registry.markDimensionUnloaded(planetData.getPlanetId());
            }
        }
    }

    /**
     * Perform periodic maintenance
     */
    private void performMaintenance() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastMaintenanceTime < MAINTENANCE_INTERVAL) {
            return;
        }

        AdAstraMekanized.LOGGER.debug("Performing EnhancedRuntimeDimensionRegistry maintenance");

        // Remove dimensions that haven't been accessed recently
        List<ResourceKey<Level>> toRemove = dimensionAccessTimes.entrySet().stream()
            .filter(entry -> currentTime - entry.getValue() > DIMENSION_UNLOAD_DELAY)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        for (ResourceKey<Level> dimensionKey : toRemove) {
            DynamicPlanetData planetData = dimensionToPlanet.get(dimensionKey);
            AdAstraMekanized.LOGGER.info("Auto-unloading inactive dimension mapping: {} (planet: {})",
                dimensionKey.location(),
                planetData != null ? planetData.getDisplayName() : "Unknown");

            templateMapping.remove(dimensionKey);
            dimensionAccessTimes.remove(dimensionKey);
            dimensionToPlanet.remove(dimensionKey);

            // Update planet registry
            if (planetData != null) {
                DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
                registry.markDimensionUnloaded(planetData.getPlanetId());
            }
        }

        lastMaintenanceTime = currentTime;
    }

    /**
     * Check if a dimension is managed by this registry
     */
    public boolean hasDimensionMapping(ResourceKey<Level> dimensionKey) {
        return templateMapping.containsKey(dimensionKey);
    }

    /**
     * Get mapped dimension for planet
     */
    public ServerLevel getMappedDimension(ResourceKey<Level> dimensionKey) {
        if (templateMapping.containsKey(dimensionKey)) {
            // Update access time
            DynamicPlanetData planetData = dimensionToPlanet.get(dimensionKey);
            if (planetData != null) {
                updateDimensionAccess(dimensionKey, planetData);
            }
            return templateMapping.get(dimensionKey);
        }
        return null;
    }

    /**
     * Remove dimension mapping
     */
    public boolean removeDimensionMapping(ResourceKey<Level> dimensionKey) {
        boolean removed = templateMapping.remove(dimensionKey) != null;
        if (removed) {
            dimensionAccessTimes.remove(dimensionKey);
            DynamicPlanetData planetData = dimensionToPlanet.remove(dimensionKey);

            if (planetData != null) {
                DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
                registry.markDimensionUnloaded(planetData.getPlanetId());
            }

            AdAstraMekanized.LOGGER.info("Removed dimension mapping: {}", dimensionKey.location());
        }
        return removed;
    }

    /**
     * Clear all mappings
     */
    public void clearAll() {
        int count = templateMapping.size();

        // Update planet registry for all mapped dimensions
        for (DynamicPlanetData planetData : dimensionToPlanet.values()) {
            if (planetData != null) {
                DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
                registry.markDimensionUnloaded(planetData.getPlanetId());
            }
        }

        templateMapping.clear();
        dimensionAccessTimes.clear();
        dimensionToPlanet.clear();

        AdAstraMekanized.LOGGER.info("Cleared {} dimension mappings", count);
    }

    /**
     * Shutdown the registry
     */
    public void shutdown() {
        if (!initialized) return;

        AdAstraMekanized.LOGGER.info("Shutting down EnhancedRuntimeDimensionRegistry...");

        clearAll();
        server = null;
        initialized = false;

        AdAstraMekanized.LOGGER.info("EnhancedRuntimeDimensionRegistry shutdown complete");
    }

    /**
     * Get registry status
     */
    public RegistryStatus getStatus() {
        return new RegistryStatus(
            initialized,
            server != null,
            templateMapping.size(),
            dimensionToPlanet.size(),
            MAX_LOADED_DIMENSIONS
        );
    }

    public record RegistryStatus(
        boolean initialized,
        boolean hasServer,
        int mappedDimensions,
        int trackedPlanets,
        int maxDimensions
    ) {}
}