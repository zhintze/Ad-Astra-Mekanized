package com.hecookin.adastramekanized.common.worldgen;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetData;
import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import com.hecookin.adastramekanized.common.planets.CelestialType;
import com.hecookin.adastramekanized.common.planets.PlanetDiscoveryService;
import com.hecookin.adastramekanized.common.dimensions.DimensionJsonGenerator;
import com.hecookin.adastramekanized.api.planets.Planet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Generates random planets at world creation time, before the server starts.
 * This ensures all planets are available in the dimension registry when the world loads.
 *
 * Process:
 * 1. Hook into ServerAboutToStartEvent
 * 2. Generate N random planets with full configurations
 * 3. Create all dimension JSON files in the world's datapack
 * 4. Allow normal world loading to proceed with new planets available
 */
public class WorldStartPlanetGenerator {

    private static final WorldStartPlanetGenerator INSTANCE = new WorldStartPlanetGenerator();

    private boolean hasGenerated = false;
    private final List<GeneratedPlanetInfo> generatedPlanets = new ArrayList<>();

    /**
     * Register event handlers for world start planet generation
     */
    public static void register() {
        NeoForge.EVENT_BUS.register(INSTANCE);
        AdAstraMekanized.LOGGER.info("WorldStartPlanetGenerator registered for world creation events");
    }

    /**
     * Main event handler - generates planets when server is starting (after worlds are loaded)
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();

        // Only generate once per world
        if (hasGenerated) {
            AdAstraMekanized.LOGGER.debug("Planets already generated for this world, skipping");
            return;
        }

        AdAstraMekanized.LOGGER.info("Starting world creation planet generation...");

        try {
            // Validate configuration before proceeding
            if (!PlanetGenerationConfig.validateConfiguration()) {
                AdAstraMekanized.LOGGER.error("Invalid planet generation configuration, skipping planet generation");
                return;
            }

            // Generate planets synchronously before world loads
            generatePlanetsForWorld(server);
            hasGenerated = true;

            AdAstraMekanized.LOGGER.info("Successfully generated {} planets for new world",
                generatedPlanets.size());

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to generate planets at world start", e);
        }
    }

    /**
     * Generate all planets for the world
     */
    private void generatePlanetsForWorld(MinecraftServer server) {
        AdAstraMekanized.LOGGER.info("Generating {} random planets for world creation",
            PlanetGenerationConfig.PLANET_COUNT);

        // Create randomizer based on world seed (with fallback if overworld not available yet)
        long worldSeed;
        try {
            worldSeed = server.overworld().getSeed();
        } catch (Exception e) {
            // Fallback: generate seed from world name or use system time
            String worldName = server.getWorldPath(LevelResource.ROOT).getFileName().toString();
            worldSeed = worldName.hashCode() + System.currentTimeMillis();
            AdAstraMekanized.LOGGER.warn("Could not get world seed, using fallback: {} for world: {}",
                worldSeed, worldName);
        }

        PlanetRandomizer randomizer = new PlanetRandomizer(worldSeed,
            PlanetGenerationConfig.USE_SEED_BASED_GENERATION);

        NameGenerator nameGenerator = new NameGenerator(randomizer);
        // Initialize the unique file dimension generator
        DimensionJsonGenerator dimensionGenerator = new DimensionJsonGenerator(server);

        // Generate each planet
        for (int i = 0; i < PlanetGenerationConfig.PLANET_COUNT; i++) {
            try {
                GeneratedPlanetInfo planetInfo = generateSinglePlanet(server, randomizer,
                    nameGenerator, dimensionGenerator, i);
                generatedPlanets.add(planetInfo);

                AdAstraMekanized.LOGGER.debug("Generated planet {}/{}: {} ({})",
                    i + 1, PlanetGenerationConfig.PLANET_COUNT,
                    planetInfo.name(), planetInfo.effectsType());

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to generate planet {} of {}",
                    i + 1, PlanetGenerationConfig.PLANET_COUNT, e);
            }
        }

        // Log generation summary
        logGenerationSummary();

        // CRITICAL: Register generated planets with discovery service for unified access
        PlanetDiscoveryService discoveryService = PlanetDiscoveryService.getInstance();
        discoveryService.initialize(server);

        // Register each generated planet individually
        for (GeneratedPlanetInfo planetInfo : generatedPlanets) {
            // Convert DynamicPlanetData to Planet API object
            Planet planet = planetInfo.toPlanet();
            if (planet != null) {
                discoveryService.registerGeneratedPlanet(planet);
            } else {
                AdAstraMekanized.LOGGER.error("Failed to convert generated planet to Planet API: {}", planetInfo.name());
            }
        }

        // Also trigger discovery for any file-based planets
        discoveryService.discoverAllPlanets();

        AdAstraMekanized.LOGGER.info("Registered {} generated planets with discovery service", generatedPlanets.size());
    }

    /**
     * Generate a single planet with all attributes and files
     */
    private GeneratedPlanetInfo generateSinglePlanet(MinecraftServer server,
                                                   PlanetRandomizer randomizer,
                                                   NameGenerator nameGenerator,
                                                   DimensionJsonGenerator dimensionGenerator,
                                                   int planetIndex) {

        // Create planet-specific randomizer for deterministic generation
        PlanetRandomizer planetRandomizer = randomizer.forPlanet(planetIndex);

        // Generate core attributes
        DimensionEffectsType effectsType = planetRandomizer.generateEffectsType();
        CelestialType celestialType = planetRandomizer.generateCelestialType();
        String planetName = nameGenerator.generateUniquePlanetName(planetIndex, effectsType);

        // Generate physical properties
        PlanetPhysicalProperties physicalProps = generatePhysicalProperties(planetRandomizer);

        // Generate atmosphere
        PlanetRandomizer.AtmosphereProperties atmosphere = planetRandomizer.generateAtmosphere();

        // Generate ore configuration
        List<PlanetRandomizer.OreSpawnConfig> oreConfig = planetRandomizer.generateOreConfiguration();

        // Generate mob configuration
        PlanetRandomizer.MobSpawnConfig mobConfig = planetRandomizer.generateMobConfiguration();

        // Create planet ID
        ResourceLocation planetId = ResourceLocation.fromNamespaceAndPath(
            AdAstraMekanized.MOD_ID, String.format("generated_%03d", planetIndex + 1));

        // Create DynamicPlanetData for dimension generation
        DynamicPlanetData planetData = createDynamicPlanetData(planetId, planetName, effectsType,
            celestialType, physicalProps, atmosphere);

        // Dimension files will be generated when the planet JSON file is created

        // Generate correct planet JSON file using Planet codec
        GeneratedPlanetInfo planetInfo = new GeneratedPlanetInfo(
            planetId, planetName, effectsType, celestialType, physicalProps,
            atmosphere, oreConfig, mobConfig, true
        );
        generatePlanetJsonFile(server, planetInfo, dimensionGenerator);

        AdAstraMekanized.LOGGER.info("Generated complete planet: {} with {} ores and {} mobs",
            planetName, oreConfig.size(),
            mobConfig.mobType() == PlanetRandomizer.MobType.NONE ? 0 : mobConfig.mobTypes().size());

        return planetInfo;
    }

    /**
     * Generate physical properties for a planet
     */
    private PlanetPhysicalProperties generatePhysicalProperties(PlanetRandomizer randomizer) {
        return new PlanetPhysicalProperties(
            randomizer.generateGravity(),
            randomizer.generateTemperature(),
            randomizer.generateDayLength(),
            randomizer.generateOrbitDistance()
        );
    }

    /**
     * Create DynamicPlanetData from generated attributes
     */
    private DynamicPlanetData createDynamicPlanetData(ResourceLocation planetId, String planetName,
                                                    DimensionEffectsType effectsType,
                                                    CelestialType celestialType,
                                                    PlanetPhysicalProperties physicalProps,
                                                    PlanetRandomizer.AtmosphereProperties atmosphere) {

        DynamicPlanetData planetData = new DynamicPlanetData(
            planetId, planetName, effectsType, celestialType,
            physicalProps.gravity(), physicalProps.temperature(), physicalProps.dayLength(),
            physicalProps.orbitDistance(), atmosphere.hasAtmosphere(), atmosphere.breathable(),
            atmosphere.pressure()
        );

        return planetData;
    }

    /**
     * Generate planet JSON file using Planet codec serialization
     */
    private void generatePlanetJsonFile(MinecraftServer server, GeneratedPlanetInfo planetInfo, DimensionJsonGenerator dimensionGenerator) {
        try {
            // Convert to Planet API object
            Planet planet = planetInfo.toPlanet();
            if (planet == null) {
                AdAstraMekanized.LOGGER.error("Failed to convert GeneratedPlanetInfo to Planet for JSON generation: {}", planetInfo.name());
                return;
            }

            // Serialize using Planet codec
            com.mojang.serialization.DataResult<com.google.gson.JsonElement> result =
                Planet.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, planet);

            if (result.isError()) {
                AdAstraMekanized.LOGGER.error("Failed to serialize planet {}: {}", planet.displayName(), result.error());
                return;
            }

            com.google.gson.JsonElement jsonElement = result.getOrThrow();

            // Create planet JSON file path
            Path worldDatapackPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR)
                .resolve("data").resolve(AdAstraMekanized.MOD_ID).resolve("planets");

            java.nio.file.Files.createDirectories(worldDatapackPath);

            Path planetJsonFile = worldDatapackPath.resolve(planet.id().getPath() + ".json");

            // Write pretty-printed JSON
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(jsonElement);

            java.nio.file.Files.writeString(planetJsonFile, jsonString,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);

            AdAstraMekanized.LOGGER.info("Generated planet JSON file: {}", planetJsonFile);

            // Generate dimension file for the planet using DynamicPlanetData
            generateDimensionFileForPlanet(planetInfo, dimensionGenerator);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to generate planet JSON file for {}: {}", planetInfo.name(), e.getMessage(), e);
        }
    }

    /**
     * Generate dimension file for a planet using the DimensionJsonGenerator
     */
    private void generateDimensionFileForPlanet(GeneratedPlanetInfo planetInfo, DimensionJsonGenerator dimensionGenerator) {
        try {
            // Create DynamicPlanetData from GeneratedPlanetInfo
            DynamicPlanetData planetData = createDynamicPlanetData(
                planetInfo.planetId(),
                planetInfo.name(),
                planetInfo.effectsType(),
                planetInfo.celestialType(),
                planetInfo.physicalProperties(),
                planetInfo.atmosphereProperties()
            );

            // Generate dimension files synchronously (we need this during world startup)
            boolean success = dimensionGenerator.generateDimensionFiles(planetData);

            if (success) {
                AdAstraMekanized.LOGGER.info("Successfully generated dimension files for planet: {}", planetInfo.name());
            } else {
                AdAstraMekanized.LOGGER.error("Failed to generate dimension files for planet: {}", planetInfo.name());
            }

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Exception generating dimension files for planet {}: {}", planetInfo.name(), e.getMessage(), e);
        }
    }

    /**
     * Log summary of generation results
     */
    private void logGenerationSummary() {
        AdAstraMekanized.LOGGER.info("=== Planet Generation Summary ===");
        AdAstraMekanized.LOGGER.info("Total planets generated: {}", generatedPlanets.size());

        // Count by effect type
        Map<DimensionEffectsType, Integer> effectCounts = new HashMap<>();
        Map<CelestialType, Integer> celestialCounts = new HashMap<>();
        Map<PlanetRandomizer.MobType, Integer> mobCounts = new HashMap<>();

        for (GeneratedPlanetInfo planet : generatedPlanets) {
            effectCounts.merge(planet.effectsType(), 1, Integer::sum);
            celestialCounts.merge(planet.celestialType(), 1, Integer::sum);
            mobCounts.merge(planet.mobConfiguration().mobType(), 1, Integer::sum);
        }

        AdAstraMekanized.LOGGER.info("Effect types: {}", effectCounts);
        AdAstraMekanized.LOGGER.info("Celestial types: {}", celestialCounts);
        AdAstraMekanized.LOGGER.info("Mob distributions: {}", mobCounts);

        // Log some example planet names
        AdAstraMekanized.LOGGER.info("Example planets: {}",
            generatedPlanets.stream()
                .limit(5)
                .map(GeneratedPlanetInfo::name)
                .toList());

        AdAstraMekanized.LOGGER.info("=== Generation Complete ===");
    }

    /**
     * Get list of all generated planets (for debugging/admin commands)
     */
    public List<GeneratedPlanetInfo> getGeneratedPlanets() {
        return new ArrayList<>(generatedPlanets);
    }

    /**
     * Reset generation state (for testing)
     */
    public void reset() {
        hasGenerated = false;
        generatedPlanets.clear();
    }

    /**
     * Get the singleton instance
     */
    public static WorldStartPlanetGenerator getInstance() {
        return INSTANCE;
    }

    // ========== RECORD CLASSES ==========

    /**
     * Physical properties of a generated planet
     */
    public record PlanetPhysicalProperties(
        float gravity,          // Gravity multiplier (1.0 = Earth)
        float temperature,      // Temperature in Celsius
        float dayLength,        // Day length in hours
        int orbitDistance       // Distance from star in millions of km
    ) {}

    /**
     * Complete information about a generated planet
     */
    public record GeneratedPlanetInfo(
        ResourceLocation planetId,                                    // Unique identifier
        String name,                                                 // Display name
        DimensionEffectsType effectsType,                           // Terrain/atmosphere type
        CelestialType celestialType,                                // Sky/celestial configuration
        PlanetPhysicalProperties physicalProperties,                // Physical attributes
        PlanetRandomizer.AtmosphereProperties atmosphereProperties, // Atmosphere details
        List<PlanetRandomizer.OreSpawnConfig> oreConfiguration,    // Ore generation setup
        PlanetRandomizer.MobSpawnConfig mobConfiguration,          // Mob spawning setup
        boolean dimensionFilesGenerated                            // Whether files were created
    ) {
        /**
         * Convert to Planet API object for discovery service registration
         */
        public Planet toPlanet() {
            try {
                // Convert atmosphere data
                Planet.AtmosphereType atmosphereType = determineAtmosphereType();
                Planet.AtmosphereData atmosphere = new Planet.AtmosphereData(
                    atmosphereProperties.hasAtmosphere(),
                    atmosphereProperties.breathable(),
                    atmosphereProperties.pressure(),
                    atmosphereProperties.breathable() ? 0.21f : 0.0f, // Earth-like oxygen if breathable
                    atmosphereType
                );

                // Convert planet properties
                Planet.PlanetProperties properties = new Planet.PlanetProperties(
                    physicalProperties.gravity(),
                    physicalProperties.temperature(),
                    physicalProperties.dayLength(),
                    physicalProperties.orbitDistance(),
                    false, // hasRings - not generated yet
                    0      // moonCount - not generated yet
                );

                // Create dimension settings based on effects type
                Planet.DimensionSettings dimension = createDimensionSettings();

                // Create default generation and rendering settings
                com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings generation =
                    com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings.createEarthlike();

                com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering rendering =
                    com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering.createDefault();

                return new Planet(planetId, name, properties, atmosphere, dimension, generation, rendering);

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to convert GeneratedPlanetInfo to Planet: {}", e.getMessage(), e);
                return null;
            }
        }

        private Planet.AtmosphereType determineAtmosphereType() {
            if (!atmosphereProperties.hasAtmosphere()) {
                return Planet.AtmosphereType.NONE;
            }

            float pressure = atmosphereProperties.pressure();
            if (pressure < 0.3f) {
                return Planet.AtmosphereType.THIN;
            } else if (pressure > 2.0f) {
                return Planet.AtmosphereType.THICK;
            } else if (atmosphereProperties.breathable()) {
                return Planet.AtmosphereType.NORMAL;
            } else {
                return Planet.AtmosphereType.TOXIC;
            }
        }

        private Planet.DimensionSettings createDimensionSettings() {
            // Create dimension type based on effects type
            String dimensionTypeName = switch (effectsType) {
                case ROCKY -> "mars_like";
                case ICE_WORLD -> "ice_world";
                case VOLCANIC -> "volcanic";
                case GAS_GIANT -> "gas_giant";
                case MOON_LIKE -> "moon_like";
                case ASTEROID_LIKE -> "moon_like";
                case ALTERED_OVERWORLD -> "mars_like";
            };

            ResourceLocation dimensionType = ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID, dimensionTypeName);

            ResourceLocation chunkGenerator = ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID, "planet");

            ResourceLocation biomeSource = ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID, effectsType.name().toLowerCase());

            int skyColor = determineSkyColor();
            int fogColor = determineFogColor();
            float ambientLight = determineAmbientLight();

            return new Planet.DimensionSettings(
                dimensionType,
                biomeSource,
                chunkGenerator,
                false, // isOrbital
                skyColor,
                fogColor,
                ambientLight
            );
        }

        private int determineSkyColor() {
            if (!atmosphereProperties.hasAtmosphere()) {
                return 0x000000; // Black space
            }

            return switch (effectsType) {
                case ROCKY -> 0xD2691E; // Sandy brown
                case ICE_WORLD -> 0xE0FFFF; // Light cyan
                case VOLCANIC -> 0xFF4500; // Orange red
                case GAS_GIANT -> 0x4682B4; // Steel blue
                case MOON_LIKE -> 0x000000; // Black
                case ASTEROID_LIKE -> 0x000000; // Black
                case ALTERED_OVERWORLD -> 0x87CEEB; // Sky blue
            };
        }

        private int determineFogColor() {
            if (!atmosphereProperties.hasAtmosphere()) {
                return 0x000000; // Black
            }

            return switch (effectsType) {
                case ROCKY -> 0xDEB887; // Burlywood
                case ICE_WORLD -> 0xF0F8FF; // Alice blue
                case VOLCANIC -> 0x8B0000; // Dark red
                case GAS_GIANT -> 0x6495ED; // Cornflower blue
                case MOON_LIKE -> 0x696969; // Dim gray
                case ASTEROID_LIKE -> 0x696969; // Dim gray
                case ALTERED_OVERWORLD -> 0xB0C4DE; // Light steel blue
            };
        }

        private float determineAmbientLight() {
            if (!atmosphereProperties.hasAtmosphere()) {
                return 0.0f; // No atmosphere means no ambient light
            }

            return switch (effectsType) {
                case ROCKY -> 0.3f;
                case ICE_WORLD -> 0.4f; // Ice reflects light
                case VOLCANIC -> 0.2f; // Ash and smoke reduce light
                case GAS_GIANT -> 0.1f; // Dense atmosphere
                case MOON_LIKE -> 0.0f; // Usually no atmosphere
                case ASTEROID_LIKE -> 0.0f; // No atmosphere
                case ALTERED_OVERWORLD -> 0.9f; // Earth-like lighting
            };
        }
    }
}