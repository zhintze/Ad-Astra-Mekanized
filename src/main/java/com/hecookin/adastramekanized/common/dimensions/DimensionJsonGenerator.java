package com.hecookin.adastramekanized.common.dimensions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.planets.DynamicPlanetData;
import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Generates complete dimension JSON files for dynamic planets.
 * Creates dimension_type, dimension, noise_settings, and biome files.
 */
public class DimensionJsonGenerator {

    private final MinecraftServer server;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path worldDatapackPath;

    public DimensionJsonGenerator(MinecraftServer server) {
        this.server = server;
        this.worldDatapackPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR)
            .resolve("data").resolve(AdAstraMekanized.MOD_ID);
    }

    /**
     * Generate all necessary dimension files for a dynamic planet
     */
    public boolean generateDimensionFiles(DynamicPlanetData planetData) {
        try {
            String planetId = planetData.getPlanetId().getPath(); // e.g., "planet_001"

            // Create directory structure
            createDirectoryStructure();

            // Generate all 4 required files
            boolean success = true;
            success &= generateDimensionTypeFile(planetId, planetData);
            success &= generateDimensionFile(planetId, planetData);
            success &= generateNoiseSettingsFile(planetId, planetData);
            success &= generateBiomeFile(planetId, planetData);

            if (success) {
                AdAstraMekanized.LOGGER.info("Generated all dimension files for planet: {} ({})",
                    planetData.getDisplayName(), planetId);
            } else {
                AdAstraMekanized.LOGGER.error("Failed to generate some dimension files for planet: {}",
                    planetData.getDisplayName());
            }

            return success;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Exception generating dimension files for planet: {}",
                planetData.getDisplayName(), e);
            return false;
        }
    }

    /**
     * Create the necessary directory structure
     */
    private void createDirectoryStructure() throws IOException {
        Files.createDirectories(worldDatapackPath.resolve("dimension_type"));
        Files.createDirectories(worldDatapackPath.resolve("dimension"));
        Files.createDirectories(worldDatapackPath.resolve("worldgen").resolve("noise_settings"));
        Files.createDirectories(worldDatapackPath.resolve("worldgen").resolve("biome"));
    }

    /**
     * Generate dimension_type/planet_xxx.json
     */
    private boolean generateDimensionTypeFile(String planetId, DynamicPlanetData planetData) {
        try {
            JsonObject dimensionType = new JsonObject();

            // Basic dimension properties based on planet type
            DimensionEffectsType effectsType = planetData.getEffectsType();
            dimensionType.addProperty("ultrawarm", effectsType == DimensionEffectsType.VOLCANIC);
            dimensionType.addProperty("natural", false);
            dimensionType.addProperty("coordinate_scale", 1.0);
            dimensionType.addProperty("has_skylight", true);
            dimensionType.addProperty("has_ceiling", false);

            // Ambient light based on planet type
            double ambientLight = switch (effectsType) {
                case MOON_LIKE -> 0.1; // Very dark like Moon
                case ASTEROID_LIKE -> 0.0; // Completely airless and dark
                case ICE_WORLD -> 0.4; // Dim like winter
                case VOLCANIC -> 1.0; // Bright from lava
                case GAS_GIANT -> 0.6; // Filtered light through atmosphere
                case ROCKY -> 0.8; // Mars-like
                case ALTERED_OVERWORLD -> 0.0; // Normal Earth-like lighting
            };
            dimensionType.addProperty("ambient_light", ambientLight);

            dimensionType.add("fixed_time", null);
            dimensionType.addProperty("monster_spawn_light_level", 0);
            dimensionType.addProperty("monster_spawn_block_light_limit", 0);
            dimensionType.addProperty("min_y", -64);
            dimensionType.addProperty("height", 384);
            dimensionType.addProperty("logical_height", 384);
            dimensionType.addProperty("infiniburn", "#minecraft:infiniburn_overworld");
            dimensionType.addProperty("effects", AdAstraMekanized.MOD_ID + ":" + planetId);
            dimensionType.addProperty("respawn_anchor_works", false);
            dimensionType.addProperty("has_raids", false);
            dimensionType.addProperty("bed_works", false);
            dimensionType.addProperty("piglin_safe", false);

            // Write file
            Path filePath = worldDatapackPath.resolve("dimension_type").resolve(planetId + ".json");
            writeJsonToFile(dimensionType, filePath);

            return true;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to generate dimension_type file for {}", planetId, e);
            return false;
        }
    }

    /**
     * Generate dimension/planet_xxx.json
     */
    private boolean generateDimensionFile(String planetId, DynamicPlanetData planetData) {
        try {
            JsonObject dimension = new JsonObject();

            dimension.addProperty("type", AdAstraMekanized.MOD_ID + ":" + planetId);

            JsonObject generator = new JsonObject();
            generator.addProperty("type", "minecraft:noise");

            JsonObject biomeSource = new JsonObject();
            biomeSource.addProperty("type", "minecraft:fixed");
            biomeSource.addProperty("biome", AdAstraMekanized.MOD_ID + ":" + planetId + "_plains");
            generator.add("biome_source", biomeSource);

            generator.addProperty("settings", AdAstraMekanized.MOD_ID + ":" + planetId);
            dimension.add("generator", generator);

            // Write file
            Path filePath = worldDatapackPath.resolve("dimension").resolve(planetId + ".json");
            writeJsonToFile(dimension, filePath);

            return true;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to generate dimension file for {}", planetId, e);
            return false;
        }
    }

    /**
     * Generate worldgen/noise_settings/planet_xxx.json
     */
    private boolean generateNoiseSettingsFile(String planetId, DynamicPlanetData planetData) {
        try {
            JsonObject noiseSettings = new JsonObject();

            noiseSettings.addProperty("sea_level", 63);

            // Mob generation based on planet type
            DimensionEffectsType effectsType = planetData.getEffectsType();
            boolean disableMobs = switch (effectsType) {
                case MOON_LIKE -> true; // Airless = no mobs
                case ASTEROID_LIKE -> true; // Airless = no mobs
                case ICE_WORLD, ROCKY, GAS_GIANT, VOLCANIC -> false; // Allow mobs
                case ALTERED_OVERWORLD -> false; // Earth-like = allow mobs
            };
            noiseSettings.addProperty("disable_mob_generation", disableMobs);

            noiseSettings.addProperty("aquifers_enabled", effectsType != DimensionEffectsType.MOON_LIKE);
            noiseSettings.addProperty("ore_veins_enabled", true);
            noiseSettings.addProperty("legacy_random_source", false);

            // Default block based on planet type
            JsonObject defaultBlock = new JsonObject();
            String defaultBlockName = switch (effectsType) {
                case MOON_LIKE -> "adastramekanized:moon_stone";
                case ASTEROID_LIKE -> "adastramekanized:moon_stone"; // Similar to moon
                case ROCKY -> "adastramekanized:mars_stone";
                case ICE_WORLD -> "minecraft:packed_ice";
                case VOLCANIC -> "minecraft:blackstone";
                case GAS_GIANT -> "minecraft:stone"; // TODO: Add gas giant stone
                case ALTERED_OVERWORLD -> "minecraft:stone"; // Earth-like stone
            };
            defaultBlock.addProperty("Name", defaultBlockName);
            noiseSettings.add("default_block", defaultBlock);

            JsonObject defaultFluid = new JsonObject();
            defaultFluid.addProperty("Name", "minecraft:air");
            noiseSettings.add("default_fluid", defaultFluid);

            // Noise configuration (simplified for now)
            JsonObject noise = new JsonObject();
            noise.addProperty("min_y", -64);
            noise.addProperty("height", 384);
            noise.addProperty("size_horizontal", 1);
            noise.addProperty("size_vertical", 2);
            noiseSettings.add("noise", noise);

            // Simplified noise router (using overworld base)
            JsonObject noiseRouter = new JsonObject();
            noiseRouter.addProperty("barrier", 0);
            noiseRouter.addProperty("fluid_level_floodedness", 0);
            noiseRouter.addProperty("fluid_level_spread", 0);
            noiseRouter.addProperty("lava", 0);
            noiseRouter.addProperty("temperature", 0);
            noiseRouter.addProperty("vegetation", 0);
            noiseRouter.addProperty("continents", 0);
            noiseRouter.addProperty("erosion", 0);
            noiseRouter.addProperty("depth", 0);
            noiseRouter.addProperty("ridges", 0);
            noiseRouter.addProperty("initial_density_without_jaggedness", "minecraft:overworld/depth");
            noiseRouter.addProperty("final_density", "minecraft:overworld/depth");
            noiseRouter.addProperty("vein_toggle", 0);
            noiseRouter.addProperty("vein_ridged", 0);
            noiseRouter.addProperty("vein_gap", 0);
            noiseSettings.add("noise_router", noiseRouter);

            // Empty spawn target
            noiseSettings.add("spawn_target", gson.toJsonTree(new String[0]));

            // Surface rule
            JsonObject surfaceRule = new JsonObject();
            surfaceRule.addProperty("type", "minecraft:sequence");

            // Surface block based on planet type
            String surfaceBlockName = switch (effectsType) {
                case MOON_LIKE -> "adastramekanized:moon_stone";
                case ASTEROID_LIKE -> "adastramekanized:moon_stone"; // Similar to moon
                case ROCKY -> "adastramekanized:mars_stone";
                case ICE_WORLD -> "minecraft:snow_block";
                case VOLCANIC -> "minecraft:magma_block";
                case GAS_GIANT -> "minecraft:stone"; // TODO: Add gas giant surface
                case ALTERED_OVERWORLD -> "minecraft:grass_block"; // Earth-like grass surface
            };

            JsonObject[] sequence = new JsonObject[1];
            sequence[0] = new JsonObject();
            sequence[0].addProperty("type", "minecraft:condition");

            JsonObject ifTrue = new JsonObject();
            ifTrue.addProperty("type", "minecraft:above_preliminary_surface");
            sequence[0].add("if_true", ifTrue);

            JsonObject thenRun = new JsonObject();
            thenRun.addProperty("type", "minecraft:block");
            JsonObject resultState = new JsonObject();
            resultState.addProperty("Name", surfaceBlockName);
            thenRun.add("result_state", resultState);
            sequence[0].add("then_run", thenRun);

            surfaceRule.add("sequence", gson.toJsonTree(sequence));
            noiseSettings.add("surface_rule", surfaceRule);

            // Write file
            Path filePath = worldDatapackPath.resolve("worldgen").resolve("noise_settings").resolve(planetId + ".json");
            writeJsonToFile(noiseSettings, filePath);

            return true;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to generate noise_settings file for {}", planetId, e);
            return false;
        }
    }

    /**
     * Generate worldgen/biome/planet_xxx_plains.json
     */
    private boolean generateBiomeFile(String planetId, DynamicPlanetData planetData) {
        try {
            JsonObject biome = new JsonObject();

            DimensionEffectsType effectsType = planetData.getEffectsType();

            biome.addProperty("has_precipitation", effectsType == DimensionEffectsType.ICE_WORLD);

            // Temperature based on planet type
            float temperature = switch (effectsType) {
                case MOON_LIKE -> -1.0f; // Extremely cold
                case ASTEROID_LIKE -> -1.0f; // Extremely cold like moon
                case ICE_WORLD -> -0.8f; // Very cold
                case ROCKY -> -0.87f; // Mars-like cold
                case GAS_GIANT -> 0.2f; // Mild due to atmosphere
                case VOLCANIC -> 2.0f; // Very hot
                case ALTERED_OVERWORLD -> 0.8f; // Earth-like temperate
            };
            biome.addProperty("temperature", temperature);
            biome.addProperty("temperature_modifier", "none");
            biome.addProperty("downfall", effectsType == DimensionEffectsType.ICE_WORLD ? 0.8f : 0.0f);

            // Visual effects based on planet type
            JsonObject effects = new JsonObject();

            // Colors based on planet type
            int[] colors = switch (effectsType) {
                case MOON_LIKE -> new int[]{7829367, 4159204, 329011, 7829367, 6316128, 6316128}; // Gray tones
                case ASTEROID_LIKE -> new int[]{5592405, 4159204, 329011, 5592405, 6316128, 6316128}; // Darker gray tones
                case ROCKY -> new int[]{12697814, 4159204, 329011, 12697814, 10387789, 10387789}; // Mars colors
                case ICE_WORLD -> new int[]{11393254, 4020182, 329011, 11393254, 4020182, 4020182}; // Blue/white
                case VOLCANIC -> new int[]{3344392, 4159204, 329011, 3344392, 2651799, 2651799}; // Dark/red
                case GAS_GIANT -> new int[]{9474192, 4159204, 329011, 9474192, 5797459, 5797459}; // Purple/blue
                case ALTERED_OVERWORLD -> new int[]{12638463, 4159204, 329011, 7907327, 5467731, 5467731}; // Earth-like colors
            };

            effects.addProperty("fog_color", colors[0]);
            effects.addProperty("water_color", colors[1]);
            effects.addProperty("water_fog_color", colors[2]);
            effects.addProperty("sky_color", colors[3]);
            effects.addProperty("grass_color", colors[4]);
            effects.addProperty("foliage_color", colors[5]);

            JsonObject moodSound = new JsonObject();
            moodSound.addProperty("sound", "minecraft:ambient.cave");
            moodSound.addProperty("tick_delay", 6000);
            moodSound.addProperty("block_search_extent", 8);
            moodSound.addProperty("offset", 2.0);
            effects.add("mood_sound", moodSound);

            biome.add("effects", effects);

            // Empty spawners for now (can be customized later)
            JsonObject spawners = new JsonObject();
            spawners.add("monster", gson.toJsonTree(new Object[0]));
            spawners.add("creature", gson.toJsonTree(new Object[0]));
            spawners.add("ambient", gson.toJsonTree(new Object[0]));
            spawners.add("axolotls", gson.toJsonTree(new Object[0]));
            spawners.add("underground_water_creature", gson.toJsonTree(new Object[0]));
            spawners.add("water_creature", gson.toJsonTree(new Object[0]));
            spawners.add("water_ambient", gson.toJsonTree(new Object[0]));
            spawners.add("misc", gson.toJsonTree(new Object[0]));
            biome.add("spawners", spawners);

            // Empty spawn costs
            biome.add("spawn_costs", new JsonObject());

            // Empty carvers
            JsonObject carvers = new JsonObject();
            carvers.add("air", gson.toJsonTree(new Object[0]));
            biome.add("carvers", carvers);

            // Empty features (10 empty arrays)
            Object[][] features = new Object[10][0];
            biome.add("features", gson.toJsonTree(features));

            // Write file
            Path filePath = worldDatapackPath.resolve("worldgen").resolve("biome").resolve(planetId + "_plains.json");
            writeJsonToFile(biome, filePath);

            return true;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to generate biome file for {}", planetId, e);
            return false;
        }
    }

    /**
     * Write JSON object to file
     */
    private void writeJsonToFile(JsonObject jsonObject, Path filePath) throws IOException {
        String jsonString = gson.toJson(jsonObject);
        Files.writeString(filePath, jsonString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        AdAstraMekanized.LOGGER.debug("Generated dimension file: {}", filePath);
    }
}