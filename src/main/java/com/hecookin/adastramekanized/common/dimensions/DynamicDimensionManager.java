package com.hecookin.adastramekanized.common.dimensions;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.worldgen.PlanetChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages dynamic creation of planetary dimensions at runtime.
 *
 * This system creates Minecraft dimensions on-demand when players attempt to travel
 * to planets, using the planet configuration data to generate appropriate world settings.
 */
public class DynamicDimensionManager {

    private static final DynamicDimensionManager INSTANCE = new DynamicDimensionManager();

    private final Map<ResourceKey<Level>, ServerLevel> createdDimensions = new ConcurrentHashMap<>();
    private MinecraftServer server;

    private DynamicDimensionManager() {
        // Private constructor for singleton
    }

    public static DynamicDimensionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize with server instance
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        AdAstraMekanized.LOGGER.info("DynamicDimensionManager initialized");
    }

    /**
     * Get or create a dimension for the given planet using runtime registry
     */
    public ServerLevel getOrCreatePlanetDimension(ResourceKey<Level> dimensionKey, String planetName) {
        // First check if dimension already exists in server
        ServerLevel existingLevel = server.getLevel(dimensionKey);
        if (existingLevel != null) {
            AdAstraMekanized.LOGGER.info("Found existing dimension: {}", dimensionKey.location());
            return existingLevel;
        }

        // Check our created dimensions cache
        if (createdDimensions.containsKey(dimensionKey)) {
            AdAstraMekanized.LOGGER.info("Using cached created dimension: {}", dimensionKey.location());
            return createdDimensions.get(dimensionKey);
        }

        // Get planet data
        ResourceLocation planetId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetName);
        Planet planet = PlanetRegistry.getInstance().getPlanet(planetId);

        if (planet == null) {
            AdAstraMekanized.LOGGER.error("Planet not found in registry: {}", planetId);
            return null;
        }

        AdAstraMekanized.LOGGER.info("Creating runtime dimension for planet: {}", planet.displayName());

        // Use RuntimeDimensionRegistry to create dimension at runtime
        RuntimeDimensionRegistry runtimeRegistry = RuntimeDimensionRegistry.getInstance();
        runtimeRegistry.initialize(server);

        ServerLevel serverLevel = runtimeRegistry.registerAndCreateDimension(dimensionKey, planet);
        if (serverLevel != null) {
            createdDimensions.put(dimensionKey, serverLevel);
            AdAstraMekanized.LOGGER.info("Successfully created runtime dimension for planet: {} at {}",
                planet.displayName(), dimensionKey.location());
            return serverLevel;
        } else {
            AdAstraMekanized.LOGGER.error("Failed to create runtime dimension for planet: {}", planet.displayName());
            return null;
        }
    }

    /**
     * Create dimension type from planet data
     */
    private Holder<DimensionType> createDimensionType(Planet planet) {
        try {
            // Get the dimension type registry
            Registry<DimensionType> dimensionTypeRegistry = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);

            // Try to get existing dimension type from datapack first
            ResourceLocation dimensionTypeLocation = planet.dimension().dimensionType();
            ResourceKey<DimensionType> dimensionTypeKey = ResourceKey.create(Registries.DIMENSION_TYPE, dimensionTypeLocation);
            var existingType = dimensionTypeRegistry.getHolder(dimensionTypeKey);
            if (existingType.isPresent()) {
                AdAstraMekanized.LOGGER.info("Using existing dimension type: {}", dimensionTypeLocation);
                return existingType.get();
            }

            // If not found, create a basic dimension type based on planet properties
            AdAstraMekanized.LOGGER.info("Creating dynamic dimension type for planet: {}", planet.displayName());

            // Determine base dimension characteristics from planet
            boolean hasSkylight = planet.atmosphere().hasAtmosphere();
            boolean hasCeiling = false; // Most planets don't have bedrock ceiling
            float ambientLight = planet.dimension().ambientLight();

            // Adjust properties based on planet type
            if (planet.id().getPath().equals("moon")) {
                hasSkylight = false; // No atmosphere
                ambientLight = 0.0f; // Space is dark
            } else if (planet.id().getPath().equals("mars")) {
                hasSkylight = true; // Thin atmosphere
                ambientLight = 0.1f; // Dimmer than Earth
            }

            // Use overworld dimension type as base (this is a simplified approach)
            ResourceKey<DimensionType> overworldKey = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.withDefaultNamespace("overworld"));
            var overworldType = dimensionTypeRegistry.getHolder(overworldKey);
            if (overworldType.isPresent()) {
                return overworldType.get();
            }

            AdAstraMekanized.LOGGER.warn("Could not create dimension type for planet: {}", planet.displayName());
            return null;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error creating dimension type for planet {}: {}", planet.displayName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Create chunk generator from planet data
     */
    private ChunkGenerator createChunkGenerator(Planet planet, Registry<Biome> biomeRegistry) {
        try {
            // Get noise settings registry
            Registry<NoiseGeneratorSettings> noiseSettingsRegistry = getNoiseSettingsRegistry();

            // Try to get planet-specific noise settings from datapack
            ResourceLocation noiseSettingsLocation = planet.dimension().chunkGenerator();
            ResourceKey<NoiseGeneratorSettings> noiseKey = ResourceKey.create(Registries.NOISE_SETTINGS, noiseSettingsLocation);
            var noiseSettings = noiseSettingsRegistry.getHolder(noiseKey);

            if (!noiseSettings.isPresent()) {
                // Try to get our custom noise settings
                String planetName = planet.id().getPath();
                ResourceLocation planetNoiseLocation = ResourceLocation.fromNamespaceAndPath(
                    AdAstraMekanized.MOD_ID, planetName);
                ResourceKey<NoiseGeneratorSettings> planetNoiseKey = ResourceKey.create(Registries.NOISE_SETTINGS, planetNoiseLocation);
                noiseSettings = noiseSettingsRegistry.getHolder(planetNoiseKey);
            }

            if (!noiseSettings.isPresent()) {
                // Fall back to overworld noise settings
                ResourceKey<NoiseGeneratorSettings> overworldNoiseKey = ResourceKey.create(Registries.NOISE_SETTINGS, ResourceLocation.withDefaultNamespace("overworld"));
                noiseSettings = noiseSettingsRegistry.getHolder(overworldNoiseKey);
                if (noiseSettings.isPresent()) {
                    AdAstraMekanized.LOGGER.info("Using overworld noise settings for planet: {}", planet.displayName());
                } else {
                    AdAstraMekanized.LOGGER.error("Could not find any noise settings for planet: {}", planet.displayName());
                    return null;
                }
            } else {
                AdAstraMekanized.LOGGER.info("Using planet-specific noise settings for: {}", planet.displayName());
            }

            // Create biome source - for now use single biome
            var planetBiomeLocation = ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID, planet.id().getPath() + "_plains");
            ResourceKey<Biome> planetBiomeKey = ResourceKey.create(Registries.BIOME, planetBiomeLocation);
            var planetBiome = biomeRegistry.getHolder(planetBiomeKey);

            if (!planetBiome.isPresent()) {
                // Fall back to plains biome
                ResourceKey<Biome> plainsKey = ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("plains"));
                planetBiome = biomeRegistry.getHolder(plainsKey);
                if (!planetBiome.isPresent()) {
                    AdAstraMekanized.LOGGER.error("Could not find biome for planet: {}", planet.displayName());
                    return null;
                }
                AdAstraMekanized.LOGGER.info("Using plains biome fallback for planet: {}", planet.displayName());
            } else {
                AdAstraMekanized.LOGGER.info("Using planet-specific biome for: {}", planet.displayName());
            }

            // Create biome source
            BiomeSource biomeSource = new FixedBiomeSource(planetBiome.get());

            // Create our custom planet chunk generator with generation settings
            return new PlanetChunkGenerator(
                biomeSource,
                planet.generation(),
                planet.id()
            );

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error creating chunk generator for planet {}: {}", planet.displayName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get registry access for dimension creation
     */
    private Registry<NoiseGeneratorSettings> getNoiseSettingsRegistry() {
        return server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS);
    }

    /**
     * Clean up created dimensions
     */
    public void cleanup() {
        createdDimensions.clear();
        AdAstraMekanized.LOGGER.info("DynamicDimensionManager cleaned up");
    }

    /**
     * Get status information
     */
    public DimensionManagerStatus getStatus() {
        return new DimensionManagerStatus(
            server != null,
            createdDimensions.size(),
            server != null ? server.levelKeys().size() : 0
        );
    }

    public record DimensionManagerStatus(
        boolean initialized,
        int createdDimensions,
        int totalServerDimensions
    ) {}

    /**
     * Create a new ServerLevel for the planet dimension
     */
    private ServerLevel createServerLevel(ResourceKey<Level> dimensionKey, LevelStem levelStem, Planet planet) {
        try {
            AdAstraMekanized.LOGGER.info("Creating ServerLevel for dimension: {}", dimensionKey.location());

            // For now, dynamic server level creation in runtime is complex in NeoForge 1.21
            // This is a simplified approach that focuses on getting the system working

            // The actual ServerLevel creation requires complex registry manipulation
            // and world data management that is typically done during server startup

            // Instead, let's try a different approach - check if the dimension is available
            // in the server but just not loaded yet

            AdAstraMekanized.LOGGER.warn("Dynamic ServerLevel creation requires advanced registry manipulation");
            AdAstraMekanized.LOGGER.warn("This feature will be fully implemented in the next development phase");

            return null;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to create ServerLevel for dimension {}: {}",
                dimensionKey.location(), e.getMessage(), e);
            return null;
        }
    }
}