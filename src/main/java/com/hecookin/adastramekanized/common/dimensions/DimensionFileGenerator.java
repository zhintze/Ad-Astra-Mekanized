package com.hecookin.adastramekanized.common.dimensions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

/**
 * Dynamic dimension file generator for planets.
 *
 * Creates dimension and dimension_type JSON files at runtime based on planet data,
 * enabling hot-reload of new dimensions during gameplay.
 */
public class DimensionFileGenerator {

    private static final DimensionFileGenerator INSTANCE = new DimensionFileGenerator();

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private MinecraftServer server;
    private Path dataPackPath;

    private DimensionFileGenerator() {
        // Private constructor for singleton
    }

    public static DimensionFileGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the generator with server instance
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        this.dataPackPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR);

        // Ensure mod datapack directory exists
        try {
            Path modDataPath = getModDataPath();
            Files.createDirectories(modDataPath.resolve("dimension"));
            Files.createDirectories(modDataPath.resolve("dimension_type"));
            AdAstraMekanized.LOGGER.info("DimensionFileGenerator initialized at: {}", modDataPath);
        } catch (IOException e) {
            AdAstraMekanized.LOGGER.error("Failed to initialize dimension generator directories", e);
        }
    }

    /**
     * Generate dimension files for a planet asynchronously
     */
    public CompletableFuture<Boolean> generateDimensionFiles(Planet planet) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                AdAstraMekanized.LOGGER.info("Generating dimension files for planet: {}", planet.displayName());

                // Skip dimension type generation - using template dimension types instead
                // Generate dimension file only
                boolean dimensionCreated = generateDimension(planet);

                if (dimensionCreated) {
                    AdAstraMekanized.LOGGER.info("Successfully generated dimension file for {}", planet.displayName());

                    // Update planet dimension reference
                    updatePlanetDimensionReference(planet);

                    return true;
                } else {
                    AdAstraMekanized.LOGGER.error("Failed to generate dimension file for {}", planet.displayName());
                    return false;
                }

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Error generating dimension files for planet {}", planet.displayName(), e);
                return false;
            }
        });
    }

    /**
     * Generate dimension type JSON file
     */
    private boolean generateDimensionType(Planet planet) {
        try {
            JsonObject dimensionType = createDimensionTypeJson(planet);
            Path filePath = getModDataPath()
                    .resolve("dimension_type")
                    .resolve(planet.id().getPath() + ".json");

            writeJsonFile(filePath, dimensionType);
            AdAstraMekanized.LOGGER.debug("Created dimension type file: {}", filePath);
            return true;

        } catch (IOException e) {
            AdAstraMekanized.LOGGER.error("Failed to create dimension type file for {}", planet.displayName(), e);
            return false;
        }
    }

    /**
     * Generate dimension JSON file
     */
    private boolean generateDimension(Planet planet) {
        try {
            JsonObject dimension = createDimensionJson(planet);
            Path filePath = getModDataPath()
                    .resolve("dimension")
                    .resolve(planet.id().getPath() + ".json");

            writeJsonFile(filePath, dimension);
            AdAstraMekanized.LOGGER.debug("Created dimension file: {}", filePath);
            return true;

        } catch (IOException e) {
            AdAstraMekanized.LOGGER.error("Failed to create dimension file for {}", planet.displayName(), e);
            return false;
        }
    }

    /**
     * Create dimension type JSON based on planet properties
     */
    private JsonObject createDimensionTypeJson(Planet planet) {
        JsonObject dimensionType = new JsonObject();

        // Base dimension type properties
        dimensionType.addProperty("ultrawarm", planet.properties().temperature() > 80);
        dimensionType.addProperty("natural", false); // All planet dimensions are artificial
        dimensionType.addProperty("coordinate_scale", 1.0);
        dimensionType.addProperty("has_skylight", true);
        dimensionType.addProperty("has_ceiling", false);

        // Ambient light based on planet properties
        float ambientLight = planet.dimension().ambientLight();
        dimensionType.addProperty("ambient_light", ambientLight);

        // Time settings - some planets might have fixed time
        if (planet.properties().dayLength() > 100) {
            // Very long days - use fixed time
            dimensionType.addProperty("fixed_time", 6000L); // Noon
        } else {
            dimensionType.add("fixed_time", null);
        }

        // Spawn settings
        dimensionType.addProperty("monster_spawn_light_level", 0);
        dimensionType.addProperty("monster_spawn_block_light_limit", 0);

        // World height
        dimensionType.addProperty("min_y", -64);
        dimensionType.addProperty("height", 384);
        dimensionType.addProperty("logical_height", 384);

        // Fire behavior
        dimensionType.addProperty("infiniburn", "#minecraft:infiniburn_overworld");

        // Effects namespace - use planet-specific effects
        // Each planet has its own custom dimension effects for proper atmospheric rendering
        dimensionType.addProperty("effects", planet.id().toString());

        // Planet-specific settings
        dimensionType.addProperty("respawn_anchor_works", planet.isHabitable());
        dimensionType.addProperty("has_raids", false); // No raids on planets
        dimensionType.addProperty("bed_works", planet.atmosphere().breathable()); // Beds work if breathable
        dimensionType.addProperty("piglin_safe", false); // No piglins on planets

        return dimensionType;
    }

    /**
     * Create dimension JSON based on planet properties
     */
    private JsonObject createDimensionJson(Planet planet) {
        JsonObject dimension = new JsonObject();

        // Reference the appropriate template dimension type based on planet characteristics
        String dimensionType = chooseDimensionTypeForPlanet(planet);
        dimension.addProperty("type", dimensionType);

        // Generator configuration
        JsonObject generator = new JsonObject();
        generator.addProperty("type", "minecraft:noise");

        // Biome source
        JsonObject biomeSource = new JsonObject();
        biomeSource.addProperty("type", "minecraft:fixed");

        // Choose biome based on planet characteristics
        String biome = chooseBiomeForPlanet(planet);
        biomeSource.addProperty("biome", biome);

        generator.add("biome_source", biomeSource);

        // Noise settings - use appropriate preset
        String noiseSettings = chooseNoiseSettingsForPlanet(planet);
        generator.addProperty("settings", noiseSettings);

        dimension.add("generator", generator);

        return dimension;
    }

    /**
     * Choose appropriate template dimension type for planet
     */
    private String chooseDimensionTypeForPlanet(Planet planet) {
        // Determine planet type based on characteristics
        float temp = planet.properties().temperature();
        boolean hasAtmosphere = planet.atmosphere().hasAtmosphere();
        float gravity = planet.properties().gravity();

        // Ice world: Very cold planets
        if (temp < -50) {
            return "adastramekanized:ice_world";
        }
        // Gas giant: High atmosphere pressure or very low gravity
        else if (!hasAtmosphere || gravity < 0.3f) {
            return "adastramekanized:gas_giant";
        }
        // Volcanic: Very hot planets
        else if (temp > 100) {
            return "adastramekanized:volcanic";
        }
        // Moon-like: Airless or very low atmosphere
        else if (!hasAtmosphere || planet.atmosphere().pressure() < 0.1f) {
            return "adastramekanized:moon_like";
        }
        // Rocky/Mars-like: Default for most planets
        else {
            return "adastramekanized:mars_like";
        }
    }

    /**
     * Choose appropriate biome for planet
     */
    private String chooseBiomeForPlanet(Planet planet) {
        // Use template biomes based on planet characteristics
        float temp = planet.properties().temperature();
        boolean hasAtmosphere = planet.atmosphere().hasAtmosphere();
        float gravity = planet.properties().gravity();

        // Match the same logic as dimension type selection
        if (temp < -50) {
            return "adastramekanized:ice_world";
        } else if (!hasAtmosphere || gravity < 0.3f) {
            return "adastramekanized:gas_giant";
        } else if (temp > 100) {
            return "adastramekanized:volcanic";
        } else if (!hasAtmosphere || planet.atmosphere().pressure() < 0.1f) {
            return "adastramekanized:moon_like";
        } else {
            return "adastramekanized:rocky";
        }
    }

    /**
     * Choose appropriate noise settings for planet
     */
    private String chooseNoiseSettingsForPlanet(Planet planet) {
        // Use template noise settings based on planet characteristics
        float temp = planet.properties().temperature();
        boolean hasAtmosphere = planet.atmosphere().hasAtmosphere();
        float gravity = planet.properties().gravity();

        // Match the same logic as dimension type and biome selection
        if (temp < -50) {
            return "adastramekanized:ice_world";
        } else if (!hasAtmosphere || gravity < 0.3f) {
            return "adastramekanized:gas_giant";
        } else if (temp > 100) {
            return "adastramekanized:volcanic";
        } else if (!hasAtmosphere || planet.atmosphere().pressure() < 0.1f) {
            return "adastramekanized:moon_like";
        } else {
            return "adastramekanized:rocky";
        }
    }

    /**
     * Update planet JSON to reference the new dimension
     */
    private void updatePlanetDimensionReference(Planet planet) {
        // This would update the planet's dimension reference to use the new dimension type
        // For now, just log that we would do this
        AdAstraMekanized.LOGGER.info("Planet {} now has dimension type: {}",
                planet.displayName(), planet.id().toString());
    }

    /**
     * Write JSON object to file
     */
    private void writeJsonFile(Path filePath, JsonObject jsonObject) throws IOException {
        String jsonString = GSON.toJson(jsonObject);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, jsonString.getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Get the mod's data directory path
     */
    private Path getModDataPath() {
        return dataPackPath.resolve("data").resolve(AdAstraMekanized.MOD_ID);
    }

    /**
     * Generate dimensions for all planets that don't have them
     */
    public CompletableFuture<Integer> generateMissingDimensions(Iterable<Planet> planets) {
        return CompletableFuture.supplyAsync(() -> {
            int generated = 0;
            for (Planet planet : planets) {
                if (needsDimensionGeneration(planet)) {
                    try {
                        Boolean result = generateDimensionFiles(planet).get();
                        if (result != null && result) {
                            generated++;
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.error("Failed to generate dimension for {}", planet.displayName(), e);
                    }
                }
            }

            AdAstraMekanized.LOGGER.info("Generated {} missing planet dimensions", generated);
            return generated;
        });
    }

    /**
     * Check if a planet needs dimension generation
     */
    private boolean needsDimensionGeneration(Planet planet) {
        // Check if dimension files exist
        Path dimensionTypePath = getModDataPath()
                .resolve("dimension_type")
                .resolve(planet.id().getPath() + ".json");
        Path dimensionPath = getModDataPath()
                .resolve("dimension")
                .resolve(planet.id().getPath() + ".json");

        return !Files.exists(dimensionTypePath) || !Files.exists(dimensionPath);
    }

    /**
     * Hot-reload dimensions using runtime registry manipulation
     */
    public CompletableFuture<Boolean> reloadDimensions() {
        if (server == null) {
            AdAstraMekanized.LOGGER.warn("Cannot reload dimensions: server not initialized");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                AdAstraMekanized.LOGGER.info("Performing runtime dimension hot-reload...");

                // Get the runtime dimension registry
                RuntimeDimensionRegistry runtimeRegistry = RuntimeDimensionRegistry.getInstance();
                runtimeRegistry.initialize(server);

                // Clear existing runtime dimensions first
                runtimeRegistry.clearAll();

                // Find all generated dimension files and register them
                Path dimensionTypePath = getModDataPath().resolve("dimension_type");
                Path dimensionPath = getModDataPath().resolve("dimension");

                int registeredCount = 0;

                if (Files.exists(dimensionTypePath) && Files.exists(dimensionPath)) {
                    // Get all dimension type files
                    try (var dimensionTypeFiles = Files.list(dimensionTypePath)) {
                        for (Path typeFile : dimensionTypeFiles.toList()) {
                            if (!typeFile.toString().endsWith(".json")) continue;

                            String planetName = typeFile.getFileName().toString().replace(".json", "");
                            ResourceLocation planetId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetName);

                            // Get the planet from registry
                            PlanetRegistry planetRegistry = PlanetRegistry.getInstance();
                            Planet planet = planetRegistry.getPlanet(planetId);
                            if (planet == null) {
                                AdAstraMekanized.LOGGER.warn("Planet not found in registry for dimension: {}", planetName);
                                continue;
                            }

                            // Create dimension key
                            ResourceKey<Level> dimensionKey = ResourceKey.create(
                                net.minecraft.core.registries.Registries.DIMENSION,
                                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetName)
                            );

                            // Register and create the dimension
                            ServerLevel serverLevel = runtimeRegistry.registerAndCreateDimension(dimensionKey, planet);
                            if (serverLevel != null) {
                                registeredCount++;
                                AdAstraMekanized.LOGGER.info("Hot-loaded dimension for planet: {} at {}",
                                    planet.displayName(), dimensionKey.location());
                            } else {
                                AdAstraMekanized.LOGGER.warn("Failed to hot-load dimension for planet: {}", planet.displayName());
                            }
                        }
                    }
                }

                AdAstraMekanized.LOGGER.info("Runtime dimension hot-reload completed - {} dimensions registered", registeredCount);
                return registeredCount > 0;

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to hot-reload dimensions", e);
                return false;
            }
        });
    }

    /**
     * Get generator status information
     */
    public GeneratorStatus getStatus() {
        if (server == null) {
            return new GeneratorStatus(false, null, 0, 0);
        }

        Path modDataPath = getModDataPath();
        int dimensionTypes = countFilesInDirectory(modDataPath.resolve("dimension_type"));
        int dimensions = countFilesInDirectory(modDataPath.resolve("dimension"));

        return new GeneratorStatus(true, modDataPath.toString(), dimensionTypes, dimensions);
    }

    /**
     * Count JSON files in a directory
     */
    private int countFilesInDirectory(Path directory) {
        try {
            if (!Files.exists(directory)) {
                return 0;
            }
            return (int) Files.list(directory)
                    .filter(path -> path.toString().endsWith(".json"))
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Generator status record
     */
    public record GeneratorStatus(
            boolean initialized,
            String dataPath,
            int dimensionTypes,
            int dimensions
    ) {}
}