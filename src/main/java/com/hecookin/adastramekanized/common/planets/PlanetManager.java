package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server-side planet management system.
 *
 * Handles planet loading, data management, and integration with the server lifecycle.
 * Coordinates between planet data loading and dimension registration.
 */
public class PlanetManager {

    private static final PlanetManager INSTANCE = new PlanetManager();

    private final PlanetRegistry registry = PlanetRegistry.getInstance();
    private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "AdAstraMekanized-PlanetLoader"));

    private volatile boolean initialized = false;
    private MinecraftServer server;

    private PlanetManager() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance
     */
    public static PlanetManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the planet manager with server instance
     */
    public void initialize(MinecraftServer server) {
        if (initialized) {
            AdAstraMekanized.LOGGER.warn("PlanetManager already initialized");
            return;
        }

        this.server = server;
        this.initialized = true;

        AdAstraMekanized.LOGGER.info("PlanetManager initialized");
    }

    /**
     * Load all planet data synchronously during server startup
     *
     * @return CompletableFuture that completes when loading is done
     */
    public CompletableFuture<Void> loadPlanetData() {
        if (!initialized) {
            AdAstraMekanized.LOGGER.error("Cannot load planet data: PlanetManager not initialized");
            return CompletableFuture.completedFuture(null);
        }

        // Run synchronously during server startup to avoid thread pool termination issues
        try {
                AdAstraMekanized.LOGGER.info("Starting planet data loading...");

                // Clear existing data
                registry.clearAll();

                // Load planet data using PlanetDataLoader
                PlanetDataLoader loader = new PlanetDataLoader(server);
                loader.loadAllPlanets();

                // Static planet system - no discovery service needed

                // Validate loaded data
                var invalidPlanets = registry.validateAllPlanets();
                if (!invalidPlanets.isEmpty()) {
                    AdAstraMekanized.LOGGER.warn("Found {} invalid planets: {}",
                            invalidPlanets.size(), invalidPlanets);
                }

                // Set default planet if not set
                if (registry.getDefaultPlanet() == null) {
                    ResourceLocation earthId = ResourceLocation.fromNamespaceAndPath(
                            AdAstraMekanized.MOD_ID, "earth");
                    if (registry.planetExists(earthId)) {
                        registry.setDefaultPlanet(earthId);
                    }
                }

                registry.markDataLoaded();

                AdAstraMekanized.LOGGER.info("Planet data loading completed. {} planets loaded",
                        registry.getPlanetCount());

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to load planet data", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Reload all planet data
     */
    public CompletableFuture<Void> reloadPlanetData() {
        AdAstraMekanized.LOGGER.info("Reloading planet data...");
        return loadPlanetData();
    }

    /**
     * Get planet by ID with server-side validation
     */
    public Planet getPlanet(ResourceLocation planetId) {
        Planet planet = registry.getPlanet(planetId);
        if (planet != null && !planet.isValid()) {
            AdAstraMekanized.LOGGER.warn("Retrieved invalid planet: {}", planetId);
            return null;
        }
        return planet;
    }

    /**
     * Check if a dimension exists for the given planet
     */
    public boolean isPlanetDimensionLoaded(ResourceLocation planetId) {
        if (server == null) return false;

        Planet planet = registry.getPlanet(planetId);
        if (planet == null) return false;

        ResourceLocation dimensionId = planet.getDimensionLocation();
        ResourceKey<Level> dimensionKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionId);
        ServerLevel level = server.getLevel(dimensionKey);
        return level != null;
    }

    /**
     * Get server level for a planet dimension
     */
    public ServerLevel getPlanetLevel(ResourceLocation planetId) {
        if (server == null) return null;

        Planet planet = registry.getPlanet(planetId);
        if (planet == null) return null;

        ResourceLocation dimensionId = planet.getDimensionLocation();
        ResourceKey<Level> dimensionKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionId);
        return server.getLevel(dimensionKey);
    }

    /**
     * Shutdown the planet manager
     */
    public void shutdown() {
        if (!initialized) return;

        AdAstraMekanized.LOGGER.info("Shutting down PlanetManager...");

        asyncExecutor.shutdown();
        registry.clearAll();
        initialized = false;
        server = null;

        AdAstraMekanized.LOGGER.info("PlanetManager shutdown complete");
    }

    /**
     * Check if the manager is ready for operations
     */
    public boolean isReady() {
        return initialized && registry.isDataLoaded();
    }

    /**
     * Get the planet registry instance
     */
    public PlanetRegistry getRegistry() {
        return registry;
    }

    /**
     * Get manager status for debugging
     */
    public ManagerStatus getStatus() {
        return new ManagerStatus(
                initialized,
                server != null,
                registry.isDataLoaded(),
                registry.getPlanetCount()
        );
    }

    /**
     * Manager status record
     */
    public record ManagerStatus(
            boolean initialized,
            boolean hasServer,
            boolean dataLoaded,
            int planetCount
    ) {}

    // Event handlers for server lifecycle

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PlanetManager manager = getInstance();
        manager.initialize(event.getServer());

        // Load planet data asynchronously
        manager.loadPlanetData().whenComplete((result, throwable) -> {
            if (throwable != null) {
                AdAstraMekanized.LOGGER.error("Failed to load planet data on server start", throwable);
            } else {
                AdAstraMekanized.LOGGER.info("Planet system ready");
            }
        });
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        getInstance().shutdown();
    }
}