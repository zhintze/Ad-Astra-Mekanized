package com.hecookin.adastramekanized.common.dimensions;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages planet dimensions and their integration with Minecraft's dimension system.
 *
 * Handles dynamic dimension registration, level access, and dimension lifecycle management.
 */
public class PlanetDimensionManager {

    private static final PlanetDimensionManager INSTANCE = new PlanetDimensionManager();

    private final Map<ResourceLocation, ResourceKey<Level>> planetDimensions = new ConcurrentHashMap<>();
    private final Set<ResourceLocation> registeredPlanets = ConcurrentHashMap.newKeySet();

    private MinecraftServer server;
    private boolean initialized = false;

    private PlanetDimensionManager() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance
     */
    public static PlanetDimensionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the dimension manager with server instance
     */
    public void initialize(MinecraftServer server) {
        if (initialized) {
            AdAstraMekanized.LOGGER.warn("PlanetDimensionManager already initialized");
            return;
        }

        this.server = server;
        this.initialized = true;

        AdAstraMekanized.LOGGER.info("PlanetDimensionManager initialized");
    }

    /**
     * Register dimensions for all planets in the registry
     */
    public void registerPlanetDimensions() {
        if (!initialized) {
            AdAstraMekanized.LOGGER.error("Cannot register dimensions: manager not initialized");
            return;
        }

        PlanetRegistry registry = PlanetRegistry.getInstance();
        int registeredCount = 0;

        for (Planet planet : registry.getAllPlanets()) {
            if (registerPlanetDimension(planet)) {
                registeredCount++;
            }
        }

        AdAstraMekanized.LOGGER.info("Registered {} planet dimensions", registeredCount);
    }

    /**
     * Register a dimension for a specific planet
     */
    public boolean registerPlanetDimension(Planet planet) {
        if (!initialized || planet == null) {
            return false;
        }

        ResourceLocation planetId = planet.id();

        // Skip if already registered
        if (registeredPlanets.contains(planetId)) {
            AdAstraMekanized.LOGGER.debug("Planet dimension already registered: {}", planetId);
            return true;
        }

        try {
            // Create dimension key
            ResourceKey<Level> dimensionKey = createDimensionKey(planet);

            // Store the mapping
            planetDimensions.put(planetId, dimensionKey);
            registeredPlanets.add(planetId);

            AdAstraMekanized.LOGGER.debug("Registered dimension for planet: {} -> {}", planetId, dimensionKey.location());
            return true;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to register dimension for planet {}: {}", planetId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Unregister a planet dimension
     */
    public boolean unregisterPlanetDimension(ResourceLocation planetId) {
        if (!registeredPlanets.contains(planetId)) {
            return false;
        }

        planetDimensions.remove(planetId);
        registeredPlanets.remove(planetId);

        AdAstraMekanized.LOGGER.debug("Unregistered dimension for planet: {}", planetId);
        return true;
    }

    /**
     * Get the dimension key for a planet
     */
    public ResourceKey<Level> getDimensionKey(ResourceLocation planetId) {
        return planetDimensions.get(planetId);
    }

    /**
     * Get the server level for a planet
     */
    public ServerLevel getPlanetLevel(ResourceLocation planetId) {
        if (server == null) {
            return null;
        }

        ResourceKey<Level> dimensionKey = planetDimensions.get(planetId);
        if (dimensionKey == null) {
            return null;
        }

        return server.getLevel(dimensionKey);
    }

    /**
     * Check if a planet has a registered dimension
     */
    public boolean hasDimension(ResourceLocation planetId) {
        return planetDimensions.containsKey(planetId);
    }

    /**
     * Check if a planet dimension is currently loaded
     */
    public boolean isDimensionLoaded(ResourceLocation planetId) {
        ServerLevel level = getPlanetLevel(planetId);
        return level != null;
    }

    /**
     * Get all registered planet dimensions
     */
    public Map<ResourceLocation, ResourceKey<Level>> getAllPlanetDimensions() {
        return new HashMap<>(planetDimensions);
    }

    /**
     * Get the planet ID from a dimension key
     */
    public ResourceLocation getPlanetFromDimension(ResourceKey<Level> dimensionKey) {
        return planetDimensions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(dimensionKey))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if a dimension key belongs to a planet
     */
    public boolean isPlanetDimension(ResourceKey<Level> dimensionKey) {
        return planetDimensions.containsValue(dimensionKey);
    }

    /**
     * Create a dimension key for a planet
     */
    private ResourceKey<Level> createDimensionKey(Planet planet) {
        ResourceLocation dimensionLocation = planet.getDimensionLocation();
        return ResourceKey.create(Registries.DIMENSION, dimensionLocation);
    }

    /**
     * Clear all registered dimensions (for cleanup/reload)
     */
    public void clearAll() {
        int count = planetDimensions.size();
        planetDimensions.clear();
        registeredPlanets.clear();

        AdAstraMekanized.LOGGER.info("Cleared {} planet dimensions", count);
    }

    /**
     * Shutdown the dimension manager
     */
    public void shutdown() {
        if (!initialized) return;

        AdAstraMekanized.LOGGER.info("Shutting down PlanetDimensionManager...");

        clearAll();
        server = null;
        initialized = false;

        AdAstraMekanized.LOGGER.info("PlanetDimensionManager shutdown complete");
    }

    /**
     * Get manager status for debugging
     */
    public DimensionManagerStatus getStatus() {
        return new DimensionManagerStatus(
                initialized,
                server != null,
                planetDimensions.size(),
                registeredPlanets.size()
        );
    }

    /**
     * Manager status record
     */
    public record DimensionManagerStatus(
            boolean initialized,
            boolean hasServer,
            int dimensionCount,
            int registeredPlanets
    ) {}

    /**
     * Validate all planet dimensions
     */
    public ValidationResult validateDimensions() {
        if (!initialized) {
            return new ValidationResult(false, "Manager not initialized", 0, 0);
        }

        PlanetRegistry registry = PlanetRegistry.getInstance();
        int validCount = 0;
        int totalCount = 0;

        for (ResourceLocation planetId : registeredPlanets) {
            totalCount++;

            // Check if planet still exists in registry
            Planet planet = registry.getPlanet(planetId);
            if (planet == null) {
                AdAstraMekanized.LOGGER.warn("Dimension registered for non-existent planet: {}", planetId);
                continue;
            }

            // Check if dimension key is valid
            ResourceKey<Level> dimensionKey = planetDimensions.get(planetId);
            if (dimensionKey == null) {
                AdAstraMekanized.LOGGER.warn("Missing dimension key for planet: {}", planetId);
                continue;
            }

            validCount++;
        }

        boolean isValid = validCount == totalCount;
        String message = String.format("Validation complete: %d/%d dimensions valid", validCount, totalCount);

        return new ValidationResult(isValid, message, validCount, totalCount);
    }

    /**
     * Validation result record
     */
    public record ValidationResult(
            boolean isValid,
            String message,
            int validCount,
            int totalCount
    ) {}
}