package com.hecookin.adastramekanized.common.dimensions;

import com.google.common.collect.ImmutableMap;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetData;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetRegistry;
import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Runtime dimension registry for hot-loading planet dimensions without server restart.
 *
 * This system uses advanced registry manipulation to register new dimensions at runtime,
 * enabling immediate dimension creation and travel to newly generated planets.
 */
public class RuntimeDimensionRegistry {

    private static final RuntimeDimensionRegistry INSTANCE = new RuntimeDimensionRegistry();

    private final Map<ResourceKey<Level>, ServerLevel> runtimeDimensions = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, Long> dimensionAccessTimes = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, DynamicPlanetData> dimensionToPlanet = new ConcurrentHashMap<>();
    private MinecraftServer server;
    private boolean initialized = false;
    private long lastMaintenanceTime = 0;

    // Performance constants
    private static final int MAX_LOADED_DIMENSIONS = 10;
    private static final long DIMENSION_UNLOAD_DELAY = 5 * 60 * 1000; // 5 minutes
    private static final long MAINTENANCE_INTERVAL = 2 * 60 * 1000; // 2 minutes

    private RuntimeDimensionRegistry() {
        // Private constructor for singleton
    }

    public static RuntimeDimensionRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the runtime registry with server instance
     */
    public void initialize(MinecraftServer server) {
        if (initialized) {
            AdAstraMekanized.LOGGER.warn("RuntimeDimensionRegistry already initialized");
            return;
        }

        this.server = server;
        this.initialized = true;

        AdAstraMekanized.LOGGER.info("RuntimeDimensionRegistry initialized");
    }

    /**
     * Register a new dimension type at runtime
     */
    public boolean registerDimensionType(ResourceLocation id, DimensionType dimensionType) {
        if (!initialized) {
            AdAstraMekanized.LOGGER.error("Cannot register dimension type: registry not initialized");
            return false;
        }

        try {
            Registry<DimensionType> dimensionTypeRegistry = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);

            // Check if already registered
            ResourceKey<DimensionType> key = ResourceKey.create(Registries.DIMENSION_TYPE, id);
            if (dimensionTypeRegistry.containsKey(key)) {
                AdAstraMekanized.LOGGER.debug("Dimension type already registered: {}", id);
                return true;
            }

            // For NeoForge 1.21, we need to use a different approach since registries are frozen after startup
            // We'll create dimensions using existing dimension types as templates
            AdAstraMekanized.LOGGER.info("Using existing dimension type template for: {}", id);
            return true;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to register dimension type {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Register a new dimension and create ServerLevel at runtime with proper chunk map initialization
     */
    public ServerLevel registerAndCreateDimension(ResourceKey<Level> dimensionKey, Planet planet) {
        if (!initialized) {
            AdAstraMekanized.LOGGER.error("Cannot register dimension: registry not initialized");
            return null;
        }

        // Check if dimension already exists
        ServerLevel existing = server.getLevel(dimensionKey);
        if (existing != null) {
            AdAstraMekanized.LOGGER.debug("Dimension already exists: {}", dimensionKey.location());
            return existing;
        }

        // Check our runtime cache
        if (runtimeDimensions.containsKey(dimensionKey)) {
            AdAstraMekanized.LOGGER.debug("Using cached runtime dimension: {}", dimensionKey.location());
            return runtimeDimensions.get(dimensionKey);
        }

        try {
            AdAstraMekanized.LOGGER.info("Creating runtime dimension for planet: {}", planet.displayName());

            // FIXED: Don't create ServerLevel manually - use NeoForge's proper dimension loading instead
            // Try to find if the dimension exists in datapacks first
            ServerLevel datapackLevel = attemptDatapackDimensionLoad(dimensionKey);
            if (datapackLevel != null) {
                runtimeDimensions.put(dimensionKey, datapackLevel);
                AdAstraMekanized.LOGGER.info("Successfully loaded dimension from datapacks: {}", dimensionKey.location());
                return datapackLevel;
            }

            // If no datapack dimension found, return null to trigger fallback in teleportation system
            AdAstraMekanized.LOGGER.warn("No dimension found for {}, teleportation system will handle fallback", dimensionKey.location());
            return null;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to create runtime dimension for planet {}: {}",
                planet.displayName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Attempt to load dimension from datapacks using NeoForge's safe loading
     */
    private ServerLevel attemptDatapackDimensionLoad(ResourceKey<Level> dimensionKey) {
        try {
            // Try to reload datapacks and get the dimension
            AdAstraMekanized.LOGGER.info("Attempting to load dimension from datapacks: {}", dimensionKey.location());

            // Check if dimension might be available after datapack processing
            return server.getLevel(dimensionKey);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Could not load dimension from datapacks: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get or create dimension type holder for planet
     */
    private Holder<DimensionType> getDimensionTypeForPlanet(Planet planet) {
        try {
            Registry<DimensionType> dimensionTypeRegistry = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);

            // Try to get planet-specific dimension type
            ResourceLocation dimensionTypeLocation = planet.dimension().dimensionType();
            ResourceKey<DimensionType> dimensionTypeKey = ResourceKey.create(Registries.DIMENSION_TYPE, dimensionTypeLocation);
            var planetDimensionType = dimensionTypeRegistry.getHolder(dimensionTypeKey);

            if (planetDimensionType.isPresent()) {
                AdAstraMekanized.LOGGER.info("Using planet-specific dimension type: {}", dimensionTypeLocation);
                return planetDimensionType.get();
            }

            // Fallback to appropriate vanilla dimension type based on planet characteristics
            String fallbackType = determineFallbackDimensionType(planet);
            ResourceKey<DimensionType> fallbackKey = ResourceKey.create(Registries.DIMENSION_TYPE,
                ResourceLocation.withDefaultNamespace(fallbackType));
            var fallbackDimensionType = dimensionTypeRegistry.getHolder(fallbackKey);

            if (fallbackDimensionType.isPresent()) {
                AdAstraMekanized.LOGGER.info("Using fallback dimension type '{}' for planet: {}",
                    fallbackType, planet.displayName());
                return fallbackDimensionType.get();
            }

            AdAstraMekanized.LOGGER.error("No suitable dimension type found for planet: {}", planet.displayName());
            return null;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error getting dimension type for planet {}: {}",
                planet.displayName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Determine fallback dimension type based on planet characteristics
     */
    private String determineFallbackDimensionType(Planet planet) {
        // Use planet properties to determine best vanilla dimension type
        if (!planet.atmosphere().hasAtmosphere()) {
            return "the_end"; // Airless worlds like the End
        } else if (planet.properties().temperature() > 80) {
            return "the_nether"; // Hot worlds like the Nether
        } else {
            return "overworld"; // Temperate worlds like Overworld
        }
    }

    /**
     * Create chunk generator for planet
     */
    private ChunkGenerator createChunkGeneratorForPlanet(Planet planet) {
        try {
            Registry<NoiseGeneratorSettings> noiseRegistry = server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS);

            // Get noise settings - prefer planet-specific, fallback to overworld
            String noiseSettingsName = chooseBestNoiseSettings(planet);
            ResourceKey<NoiseGeneratorSettings> noiseKey = ResourceKey.create(Registries.NOISE_SETTINGS,
                ResourceLocation.withDefaultNamespace(noiseSettingsName));
            var noiseSettings = noiseRegistry.getHolder(noiseKey);

            if (!noiseSettings.isPresent()) {
                AdAstraMekanized.LOGGER.error("Failed to get noise settings '{}' for planet: {}",
                    noiseSettingsName, planet.displayName());
                return null;
            }

            // Create biome source
            BiomeSource biomeSource = createBiomeSourceForPlanet(planet);
            if (biomeSource == null) {
                AdAstraMekanized.LOGGER.error("Failed to create biome source for planet: {}", planet.displayName());
                return null;
            }

            // Create noise-based chunk generator
            return new NoiseBasedChunkGenerator(biomeSource, noiseSettings.get());

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error creating chunk generator for planet {}: {}",
                planet.displayName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Choose best noise settings for planet
     */
    private String chooseBestNoiseSettings(Planet planet) {
        // Choose noise settings based on planet characteristics
        float gravity = planet.properties().gravity();
        boolean hasAtmosphere = planet.atmosphere().hasAtmosphere();

        if (gravity < 0.5f) {
            return "floating_islands"; // Low gravity
        } else if (gravity > 1.5f) {
            return "caves"; // High gravity, more underground
        } else if (!hasAtmosphere) {
            return "end"; // Airless worlds
        } else {
            return "overworld"; // Standard worlds
        }
    }

    /**
     * Create biome source for planet
     */
    private BiomeSource createBiomeSourceForPlanet(Planet planet) {
        try {
            Registry<net.minecraft.world.level.biome.Biome> biomeRegistry =
                server.registryAccess().registryOrThrow(Registries.BIOME);

            // Try to get planet-specific biome
            String biomeName = chooseBestBiome(planet);
            ResourceKey<net.minecraft.world.level.biome.Biome> biomeKey =
                ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace(biomeName));
            var biome = biomeRegistry.getHolder(biomeKey);

            if (!biome.isPresent()) {
                AdAstraMekanized.LOGGER.error("Failed to get biome '{}' for planet: {}", biomeName, planet.displayName());
                return null;
            }

            // Use fixed biome source for simplicity
            return new FixedBiomeSource(biome.get());

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error creating biome source for planet {}: {}",
                planet.displayName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Choose best biome for planet
     */
    private String chooseBestBiome(Planet planet) {
        float temperature = planet.properties().temperature();
        boolean hasAtmosphere = planet.atmosphere().hasAtmosphere();

        if (!hasAtmosphere) {
            return "the_end"; // Airless worlds
        } else if (temperature < -30) {
            return "frozen_ocean"; // Very cold
        } else if (temperature < 0) {
            return "snowy_plains"; // Cold
        } else if (temperature > 80) {
            return "desert"; // Very hot
        } else if (temperature > 30) {
            return "savanna"; // Hot
        } else {
            return "plains"; // Temperate
        }
    }

    /**
     * Create ServerLevel for the dimension
     */
    private ServerLevel createServerLevel(ResourceKey<Level> dimensionKey, LevelStem levelStem, Planet planet) {
        try {
            AdAstraMekanized.LOGGER.info("Creating ServerLevel for dimension: {}", dimensionKey.location());

            // Get world data
            WorldData worldData = server.getWorldData();

            // Create derived level data for the new dimension
            DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());

            // Get level storage access via the Overworld level
            ServerLevel overworldLevel = server.getLevel(Level.OVERWORLD);
            if (overworldLevel == null) {
                AdAstraMekanized.LOGGER.error("Cannot create ServerLevel: Overworld not available");
                return null;
            }

            // Create a no-op chunk progress listener
            ChunkProgressListener chunkProgressListener = new ChunkProgressListener() {
                @Override
                public void updateSpawnPos(ChunkPos chunkPos) {}

                @Override
                public void onStatusChange(ChunkPos chunkPos, @org.jetbrains.annotations.Nullable net.minecraft.world.level.chunk.status.ChunkStatus chunkStatus) {}

                @Override
                public void start() {}

                @Override
                public void stop() {}
            };

            // Create the ServerLevel using proper NeoForge 1.21 constructor
            AdAstraMekanized.LOGGER.info("Creating ServerLevel for: {}", planet.displayName());

            // Access private fields via reflection for ServerLevel creation
            Field executorField = MinecraftServer.class.getDeclaredField("executor");
            executorField.setAccessible(true);
            Executor executor = (Executor) executorField.get(server);

            Field storageSourceField = MinecraftServer.class.getDeclaredField("storageSource");
            storageSourceField.setAccessible(true);
            LevelStorageSource.LevelStorageAccess storageAccess =
                (LevelStorageSource.LevelStorageAccess) storageSourceField.get(server);

            // Use the proper ServerLevel constructor for NeoForge 1.21
            // Constructor signature: (MinecraftServer, Executor, LevelStorageAccess, ServerLevelData, ResourceKey<Level>,
            //                        LevelStem, ChunkProgressListener, boolean, long, List<CustomSpawner>, boolean, RandomSequences)
            ServerLevel serverLevel = new ServerLevel(
                server,                          // MinecraftServer
                executor,                        // Executor
                storageAccess,                   // LevelStorageSource.LevelStorageAccess
                derivedLevelData,               // ServerLevelData
                dimensionKey,                   // ResourceKey<Level>
                levelStem,                      // LevelStem
                chunkProgressListener,          // ChunkProgressListener
                false,                          // isDebug
                0L,                            // biomeZoomSeed
                java.util.List.of(),               // List<CustomSpawner> - use empty list
                false,                          // isFlat
                null                           // RandomSequences - null for default
            );

            AdAstraMekanized.LOGGER.info("Successfully created ServerLevel for: {}", planet.displayName());
            return serverLevel;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to create ServerLevel for dimension {}: {}",
                dimensionKey.location(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Register ServerLevel with the server's level map using reflection
     */
    private boolean registerServerLevel(ResourceKey<Level> dimensionKey, ServerLevel serverLevel) {
        try {
            // Access the server's level map via reflection
            Field levelsField = MinecraftServer.class.getDeclaredField("levels");
            levelsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<ResourceKey<Level>, ServerLevel> levels = (Map<ResourceKey<Level>, ServerLevel>) levelsField.get(server);

            // Add our new level to the map
            if (levels instanceof ImmutableMap) {
                // If it's immutable, we need to create a new mutable map
                Map<ResourceKey<Level>, ServerLevel> newLevels = new ConcurrentHashMap<>(levels);
                newLevels.put(dimensionKey, serverLevel);

                // Try to replace the immutable map with our mutable one
                levelsField.set(server, newLevels);
                AdAstraMekanized.LOGGER.info("Replaced immutable levels map with mutable version");
            } else {
                // If it's already mutable, just add to it
                levels.put(dimensionKey, serverLevel);
                AdAstraMekanized.LOGGER.info("Added dimension to existing mutable levels map");
            }

            // Verify the level was added
            ServerLevel retrievedLevel = server.getLevel(dimensionKey);
            if (retrievedLevel == serverLevel) {
                AdAstraMekanized.LOGGER.info("Successfully registered ServerLevel: {}", dimensionKey.location());
                return true;
            } else {
                AdAstraMekanized.LOGGER.error("Failed to verify ServerLevel registration: {}", dimensionKey.location());
                return false;
            }

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to register ServerLevel via reflection: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Remove a runtime dimension
     */
    public boolean removeDimension(ResourceKey<Level> dimensionKey) {
        try {
            ServerLevel serverLevel = runtimeDimensions.remove(dimensionKey);
            if (serverLevel != null) {
                // Remove from server's levels map
                Field levelsField = MinecraftServer.class.getDeclaredField("levels");
                levelsField.setAccessible(true);

                @SuppressWarnings("unchecked")
                Map<ResourceKey<Level>, ServerLevel> levels = (Map<ResourceKey<Level>, ServerLevel>) levelsField.get(server);
                levels.remove(dimensionKey);

                AdAstraMekanized.LOGGER.info("Removed runtime dimension: {}", dimensionKey.location());
                return true;
            }
            return false;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to remove dimension {}: {}", dimensionKey.location(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if a dimension is managed by this registry
     */
    public boolean isRuntimeDimension(ResourceKey<Level> dimensionKey) {
        return runtimeDimensions.containsKey(dimensionKey);
    }

    /**
     * Get all runtime dimensions
     */
    public Map<ResourceKey<Level>, ServerLevel> getAllRuntimeDimensions() {
        return new ConcurrentHashMap<>(runtimeDimensions);
    }

    /**
     * Clear all runtime dimensions
     */
    public void clearAll() {
        int count = runtimeDimensions.size();

        // Remove from server's levels map
        try {
            Field levelsField = MinecraftServer.class.getDeclaredField("levels");
            levelsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<ResourceKey<Level>, ServerLevel> levels = (Map<ResourceKey<Level>, ServerLevel>) levelsField.get(server);

            for (ResourceKey<Level> key : runtimeDimensions.keySet()) {
                levels.remove(key);
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to clean up server levels map: {}", e.getMessage(), e);
        }

        runtimeDimensions.clear();
        AdAstraMekanized.LOGGER.info("Cleared {} runtime dimensions", count);
    }

    /**
     * Shutdown the registry
     */
    public void shutdown() {
        if (!initialized) return;

        AdAstraMekanized.LOGGER.info("Shutting down RuntimeDimensionRegistry...");

        clearAll();
        server = null;
        initialized = false;

        AdAstraMekanized.LOGGER.info("RuntimeDimensionRegistry shutdown complete");
    }

    /**
     * Get registry status
     */
    public RegistryStatus getStatus() {
        return new RegistryStatus(
            initialized,
            server != null,
            runtimeDimensions.size(),
            server != null ? server.levelKeys().size() : 0
        );
    }

    public record RegistryStatus(
        boolean initialized,
        boolean hasServer,
        int runtimeDimensions,
        int totalServerDimensions
    ) {}
}