package com.hecookin.adastramekanized.common.planets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hecookin.adastramekanized.AdAstraMekanized;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Central planet generation system using proven TerraBlender approach.
 * Replaces StaticPlanetGenerator with builder pattern and Moon-based generation.
 */
public class PlanetMaker {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String RESOURCES_PATH = "src/main/resources/data/adastramekanized/";
    private static final List<PlanetBuilder> PLANETS = new ArrayList<>();

    /**
     * Start building a new planet with the given name
     */
    public static PlanetBuilder planet(String name) {
        return new PlanetBuilder(name);
    }

    /**
     * Generate all configured planets using runData integration
     */
    public static void generateAllPlanets() {
        AdAstraMekanized.LOGGER.info("Generating planets using PlanetMaker system...");

        createDirectories();

        for (PlanetBuilder planet : PLANETS) {
            try {
                generatePlanetFiles(planet);
                AdAstraMekanized.LOGGER.info("Generated planet: {}", planet.name);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to generate planet: {}", planet.name, e);
            }
        }

        AdAstraMekanized.LOGGER.info("Completed generating {} planets", PLANETS.size());

        AdAstraMekanized.LOGGER.info("Planet generation uses multi-noise biome sources for variety");
    }


    /**
     * Create multi-noise biome source for a planet
     */
    private static JsonObject createBiomePreset(PlanetBuilder planet) {
        JsonObject biomeSource = new JsonObject();
        biomeSource.addProperty("type", "minecraft:multi_noise");

        // Create biome entries for this planet
        JsonArray biomes = new JsonArray();

        switch (planet.name.toLowerCase()) {
            case "moon":
                // Moon biomes using existing cold/barren biomes
                biomes.add(createBiomeEntry("minecraft:frozen_peaks",
                    -0.8f, -0.9f, 0.4f, 0.2f, 0.0f, 0.1f)); // Cold, dry, highlands (lunar highlands)
                biomes.add(createBiomeEntry("minecraft:snowy_slopes",
                    -0.6f, -0.8f, -0.2f, 0.3f, -0.5f, -0.1f)); // Cold, dry, lowlands (lunar maria)
                break;
            case "mars":
                // Mars biomes using existing desert/cold biomes
                biomes.add(createBiomeEntry("minecraft:badlands",
                    -0.2f, -0.7f, 0.6f, -0.1f, 0.5f, 0.0f)); // Cool, dry, elevated (mars highlands)
                biomes.add(createBiomeEntry("minecraft:desert",
                    0.1f, -0.6f, 0.2f, 0.4f, -0.3f, -0.1f)); // Warmer, dry, valleys (mars valleys)
                biomes.add(createBiomeEntry("minecraft:frozen_peaks",
                    -0.9f, -0.8f, 0.8f, 0.0f, 0.2f, 0.3f)); // Very cold, polar (mars polar)
                break;
            default:
                // Default single biome for other planets
                biomes.add(createBiomeEntry("minecraft:plains",
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
                break;
        }

        biomeSource.add("biomes", biomes);
        return biomeSource;
    }

    /**
     * Create a biome entry with climate parameters
     */
    private static JsonObject createBiomeEntry(String biome, float temperature, float humidity,
                                              float continentalness, float erosion, float depth, float weirdness) {
        JsonObject entry = new JsonObject();
        entry.addProperty("biome", biome);

        JsonObject parameters = new JsonObject();
        parameters.addProperty("temperature", temperature);
        parameters.addProperty("humidity", humidity);
        parameters.addProperty("continentalness", continentalness);
        parameters.addProperty("erosion", erosion);
        parameters.addProperty("depth", depth);
        parameters.addProperty("weirdness", weirdness);
        parameters.addProperty("offset", 0.0f);

        entry.add("parameters", parameters);
        return entry;
    }

    /**
     * Fluent builder for planet configuration
     */
    public static class PlanetBuilder {
        private final String name;

        // Unique seed for this planet's terrain generation
        private long seed;

        // Noise configuration (the 6 parameters we modified + important ones)
        private float continentalScale = 1.0f;
        private float erosionScale = 0.25f;
        private float ridgeScale = 0.25f;
        private float heightVariation1 = 0.4f;
        private float heightVariation2 = 0.05f;
        private float heightVariation3 = 0.1f;
        private float heightVariation4 = 0.05f;

        // Surface configuration
        private String surfaceBlock = "minecraft:stone";
        private String subsurfaceBlock = "minecraft:cobblestone";
        private String deepBlock = "minecraft:stone";

        // Basic planet properties
        private int seaLevel = 63;
        private boolean disableMobGeneration = false;
        private boolean aquifersEnabled = false;
        private boolean oreVeinsEnabled = true;
        private String defaultFluid = "minecraft:air";

        // Dimension properties
        private int skyColor = 0x78A7FF;
        private int fogColor = 0xC0D8FF;
        private boolean hasAtmosphere = true;
        private float ambientLight = 0.1f;

        private PlanetBuilder(String name) {
            this.name = name;
            // Generate unique seed from planet name hash + constant offset for deterministic results
            this.seed = name.hashCode() + 1000000L; // Offset to avoid negative hash values being too small
        }

        // Noise parameter configuration methods
        public PlanetBuilder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public PlanetBuilder continentalScale(float scale) {
            this.continentalScale = scale;
            return this;
        }

        public PlanetBuilder erosionScale(float scale) {
            this.erosionScale = scale;
            return this;
        }

        public PlanetBuilder ridgeScale(float scale) {
            this.ridgeScale = scale;
            return this;
        }

        public PlanetBuilder heightVariation(float var1, float var2, float var3, float var4) {
            this.heightVariation1 = var1;
            this.heightVariation2 = var2;
            this.heightVariation3 = var3;
            this.heightVariation4 = var4;
            return this;
        }

        // Surface configuration methods
        public PlanetBuilder surfaceBlock(String block) {
            this.surfaceBlock = block;
            return this;
        }

        public PlanetBuilder subsurfaceBlock(String block) {
            this.subsurfaceBlock = block;
            return this;
        }

        public PlanetBuilder deepBlock(String block) {
            this.deepBlock = block;
            return this;
        }

        // Basic properties
        public PlanetBuilder seaLevel(int level) {
            this.seaLevel = level;
            return this;
        }

        public PlanetBuilder disableMobGeneration(boolean disable) {
            this.disableMobGeneration = disable;
            return this;
        }

        public PlanetBuilder aquifersEnabled(boolean enabled) {
            this.aquifersEnabled = enabled;
            return this;
        }

        public PlanetBuilder oreVeinsEnabled(boolean enabled) {
            this.oreVeinsEnabled = enabled;
            return this;
        }

        public PlanetBuilder defaultFluid(String fluid) {
            this.defaultFluid = fluid;
            return this;
        }

        // Visual properties
        public PlanetBuilder skyColor(int color) {
            this.skyColor = color;
            return this;
        }

        public PlanetBuilder fogColor(int color) {
            this.fogColor = color;
            return this;
        }

        public PlanetBuilder hasAtmosphere(boolean atmosphere) {
            this.hasAtmosphere = atmosphere;
            return this;
        }

        public PlanetBuilder ambientLight(float light) {
            this.ambientLight = light;
            return this;
        }

        /**
         * Generate this planet and add it to the generation queue
         */
        public PlanetBuilder generate() {
            PLANETS.add(this);
            return this;
        }
    }

    private static void createDirectories() {
        new File(RESOURCES_PATH + "planets").mkdirs();
        new File(RESOURCES_PATH + "dimension").mkdirs();
        new File(RESOURCES_PATH + "dimension_type").mkdirs();
        new File(RESOURCES_PATH + "worldgen/noise_settings").mkdirs();
    }

    /**
     * Generate all files for a planet using Moon's proven TerraBlender approach
     */
    private static void generatePlanetFiles(PlanetBuilder planet) throws IOException {
        generatePlanetData(planet);
        generateDimensionData(planet);
        generateDimensionType(planet);
        generateNoiseSettings(planet);
    }

    /**
     * Generate planet data JSON (basic planet properties)
     */
    private static void generatePlanetData(PlanetBuilder planet) throws IOException {
        JsonObject planetJson = new JsonObject();

        planetJson.addProperty("id", "adastramekanized:" + planet.name);
        planetJson.addProperty("display_name", capitalizeFirst(planet.name));

        // Basic properties
        JsonObject properties = new JsonObject();
        properties.addProperty("gravity", 1.0f);
        properties.addProperty("temperature", 20.0f);
        properties.addProperty("day_length", 24.0f);
        properties.addProperty("orbit_distance", 1000);
        properties.addProperty("has_rings", false);
        properties.addProperty("moon_count", 0);
        planetJson.add("properties", properties);

        // Atmosphere
        JsonObject atmosphere = new JsonObject();
        atmosphere.addProperty("has_atmosphere", planet.hasAtmosphere);
        atmosphere.addProperty("breathable", planet.hasAtmosphere);
        atmosphere.addProperty("pressure", planet.hasAtmosphere ? 1.0f : 0.0f);
        atmosphere.addProperty("oxygen_level", planet.hasAtmosphere ? 0.21f : 0.0f);
        atmosphere.addProperty("type", planet.hasAtmosphere ? "NORMAL" : "NONE");
        planetJson.add("atmosphere", atmosphere);

        // Dimension reference
        JsonObject dimension = new JsonObject();
        dimension.addProperty("dimension_type", "adastramekanized:" + planet.name);
        dimension.addProperty("biome_source", planet.name + "_biome_source");
        dimension.addProperty("chunk_generator", planet.name + "_generator");
        dimension.addProperty("is_orbital", false);
        dimension.addProperty("sky_color", planet.skyColor);
        dimension.addProperty("fog_color", planet.fogColor);
        dimension.addProperty("ambient_light", planet.ambientLight);
        planetJson.add("dimension", dimension);

        writeJsonFile(RESOURCES_PATH + "planets/" + planet.name + ".json", planetJson);
    }

    /**
     * Generate dimension JSON following Moon's TerraBlender pattern
     */
    private static void generateDimensionData(PlanetBuilder planet) throws IOException {
        JsonObject dimensionJson = new JsonObject();

        dimensionJson.addProperty("type", "adastramekanized:" + planet.name);

        JsonObject generator = new JsonObject();
        generator.addProperty("type", "minecraft:noise");
        generator.addProperty("settings", "adastramekanized:" + planet.name);

        // Use multi-noise biome source for planet-specific biome variety
        JsonObject biomeSource = createBiomePreset(planet);
        generator.add("biome_source", biomeSource);

        dimensionJson.add("generator", generator);

        writeJsonFile(RESOURCES_PATH + "dimension/" + planet.name + ".json", dimensionJson);
    }

    /**
     * Generate dimension type following Moon's pattern
     */
    private static void generateDimensionType(PlanetBuilder planet) throws IOException {
        JsonObject dimensionType = new JsonObject();

        dimensionType.addProperty("ambient_light", planet.ambientLight);
        dimensionType.addProperty("effects", "adastramekanized:" + planet.name);
        dimensionType.addProperty("has_ceiling", false);
        dimensionType.addProperty("has_skylight", true);
        dimensionType.addProperty("height", 384);
        dimensionType.addProperty("min_y", -64);
        dimensionType.addProperty("natural", true);
        dimensionType.addProperty("coordinate_scale", 1.0f);
        dimensionType.addProperty("bed_works", true);
        dimensionType.addProperty("respawn_anchor_works", false);
        dimensionType.addProperty("has_raids", planet.hasAtmosphere);
        dimensionType.addProperty("logical_height", 384);
        dimensionType.addProperty("infiniburn", "#minecraft:infiniburn_overworld");
        dimensionType.addProperty("ultrawarm", false);
        dimensionType.addProperty("piglin_safe", false);
        dimensionType.addProperty("monster_spawn_light_level", 0);
        dimensionType.addProperty("monster_spawn_block_light_limit", 0);

        writeJsonFile(RESOURCES_PATH + "dimension_type/" + planet.name + ".json", dimensionType);
    }

    /**
     * Generate noise settings using exact Moon pattern with configurable parameters
     */
    private static void generateNoiseSettings(PlanetBuilder planet) throws IOException {
        JsonObject noiseSettings = new JsonObject();

        noiseSettings.addProperty("sea_level", planet.seaLevel);
        noiseSettings.addProperty("disable_mob_generation", planet.disableMobGeneration);
        noiseSettings.addProperty("aquifers_enabled", planet.aquifersEnabled);
        noiseSettings.addProperty("ore_veins_enabled", planet.oreVeinsEnabled);
        noiseSettings.addProperty("legacy_random_source", false);

        // Default blocks
        JsonObject defaultBlock = new JsonObject();
        defaultBlock.addProperty("Name", planet.deepBlock);
        noiseSettings.add("default_block", defaultBlock);

        JsonObject defaultFluid = new JsonObject();
        defaultFluid.addProperty("Name", planet.defaultFluid);
        noiseSettings.add("default_fluid", defaultFluid);

        // Noise configuration (Moon's working pattern)
        JsonObject noise = new JsonObject();
        noise.addProperty("min_y", -64);
        noise.addProperty("height", 384);
        noise.addProperty("size_horizontal", 2);
        noise.addProperty("size_vertical", 1);
        noiseSettings.add("noise", noise);

        // Noise router using Moon's exact working pattern with configurable parameters
        JsonObject noiseRouter = createNoiseRouter(planet);
        noiseSettings.add("noise_router", noiseRouter);

        // Surface rule using Moon's pattern with configurable blocks
        JsonObject surfaceRule = createSurfaceRule(planet);
        noiseSettings.add("surface_rule", surfaceRule);

        // Empty spawn target
        noiseSettings.add("spawn_target", new JsonArray());

        writeJsonFile(RESOURCES_PATH + "worldgen/noise_settings/" + planet.name + ".json", noiseSettings);
    }

    /**
     * Get seed-based variation for a parameter
     */
    private static float getSeedVariation(PlanetBuilder planet, String parameterName, float baseValue, float variationRange) {
        // Create deterministic variation based on seed and parameter name
        long paramSeed = planet.seed ^ parameterName.hashCode();
        float variation = (float) Math.sin(paramSeed * 0.00001) * variationRange;
        return baseValue + variation;
    }

    /**
     * Create noise router using Moon's exact working pattern with configurable parameters and seed variations
     */
    private static JsonObject createNoiseRouter(PlanetBuilder planet) {
        JsonObject router = new JsonObject();

        // Basic required fields (Moon's values)
        router.addProperty("barrier", 0);
        router.addProperty("fluid_level_floodedness", 0);
        router.addProperty("fluid_level_spread", 0);
        router.addProperty("lava", 0);
        router.addProperty("temperature", 0);
        router.addProperty("vegetation", 0);

        // Continents with configurable scale and seed variation
        JsonObject continents = new JsonObject();
        continents.addProperty("type", "minecraft:shifted_noise");
        continents.addProperty("noise", "minecraft:continentalness");
        continents.addProperty("xz_scale", getSeedVariation(planet, "continental_scale", planet.continentalScale, planet.continentalScale * 0.3f));
        continents.addProperty("y_scale", 1);
        continents.addProperty("shift_x", "minecraft:shift_x");
        continents.addProperty("shift_y", 0);
        continents.addProperty("shift_z", "minecraft:shift_z");
        router.add("continents", continents);

        // Erosion with configurable scale and seed variation
        JsonObject erosion = new JsonObject();
        erosion.addProperty("type", "minecraft:shifted_noise");
        erosion.addProperty("noise", "minecraft:erosion");
        erosion.addProperty("xz_scale", getSeedVariation(planet, "erosion_scale", planet.erosionScale, planet.erosionScale * 0.4f));
        erosion.addProperty("y_scale", 0);
        erosion.addProperty("shift_x", "minecraft:shift_x");
        erosion.addProperty("shift_y", 0);
        erosion.addProperty("shift_z", "minecraft:shift_z");
        router.add("erosion", erosion);

        router.addProperty("depth", 0);

        // Ridges with configurable scale and seed variation
        JsonObject ridges = new JsonObject();
        ridges.addProperty("type", "minecraft:shifted_noise");
        ridges.addProperty("noise", "minecraft:ridge");
        ridges.addProperty("xz_scale", getSeedVariation(planet, "ridge_scale", planet.ridgeScale, planet.ridgeScale * 0.5f));
        ridges.addProperty("y_scale", 0);
        ridges.addProperty("shift_x", "minecraft:shift_x");
        ridges.addProperty("shift_y", 0);
        ridges.addProperty("shift_z", "minecraft:shift_z");
        router.add("ridges", ridges);

        // Initial density with configurable height variations
        router.add("initial_density_without_jaggedness", createInitialDensity(planet));

        // Final density with configurable height variations
        router.add("final_density", createFinalDensity(planet));

        // Ore vein settings
        router.addProperty("vein_toggle", 0);
        router.addProperty("vein_ridged", 0);
        router.addProperty("vein_gap", 0);

        return router;
    }

    /**
     * Create initial density using Moon's pattern with configurable parameters
     */
    private static JsonObject createInitialDensity(PlanetBuilder planet) {
        JsonObject initialDensity = new JsonObject();
        initialDensity.addProperty("type", "minecraft:add");

        JsonObject argument1 = new JsonObject();
        argument1.addProperty("type", "minecraft:mul");

        JsonObject yGradient = new JsonObject();
        yGradient.addProperty("type", "minecraft:y_clamped_gradient");
        yGradient.addProperty("from_y", -64);
        yGradient.addProperty("to_y", 320);
        yGradient.addProperty("from_value", 1.5);
        yGradient.addProperty("to_value", -1.5);
        argument1.add("argument1", yGradient);

        argument1.addProperty("argument2", 0.64);
        initialDensity.add("argument1", argument1);

        JsonObject argument2 = new JsonObject();
        argument2.addProperty("type", "minecraft:add");
        argument2.addProperty("argument1", -0.234375);

        JsonObject mulArgument2 = new JsonObject();
        mulArgument2.addProperty("type", "minecraft:mul");

        JsonObject addArgument1 = new JsonObject();
        addArgument1.addProperty("type", "minecraft:add");

        JsonObject mulContinentalness = new JsonObject();
        mulContinentalness.addProperty("type", "minecraft:mul");

        JsonObject continentalnessNoise = new JsonObject();
        continentalnessNoise.addProperty("type", "minecraft:shifted_noise");
        continentalnessNoise.addProperty("noise", "minecraft:continentalness");
        continentalnessNoise.addProperty("xz_scale", 0.25);
        continentalnessNoise.addProperty("y_scale", 0);
        continentalnessNoise.addProperty("shift_x", "minecraft:shift_x");
        continentalnessNoise.addProperty("shift_y", 0);
        continentalnessNoise.addProperty("shift_z", "minecraft:shift_z");
        mulContinentalness.add("argument1", continentalnessNoise);

        mulContinentalness.addProperty("argument2", getSeedVariation(planet, "height_var1", planet.heightVariation1, planet.heightVariation1 * 0.25f));
        addArgument1.add("argument2", mulContinentalness);

        JsonObject mulErosion = new JsonObject();
        mulErosion.addProperty("type", "minecraft:mul");

        JsonObject erosionNoise = new JsonObject();
        erosionNoise.addProperty("type", "minecraft:shifted_noise");
        erosionNoise.addProperty("noise", "minecraft:erosion");
        erosionNoise.addProperty("xz_scale", 0.25);
        erosionNoise.addProperty("y_scale", 0);
        erosionNoise.addProperty("shift_x", "minecraft:shift_x");
        erosionNoise.addProperty("shift_y", 0);
        erosionNoise.addProperty("shift_z", "minecraft:shift_z");
        mulErosion.add("argument1", erosionNoise);

        mulErosion.addProperty("argument2", getSeedVariation(planet, "height_var2", planet.heightVariation2, planet.heightVariation2 * 0.25f));
        addArgument1.add("argument1", mulErosion);

        mulArgument2.add("argument1", addArgument1);
        mulArgument2.addProperty("argument2", 0.175);

        argument2.add("argument2", mulArgument2);
        initialDensity.add("argument2", argument2);

        return initialDensity;
    }

    /**
     * Create final density using Moon's pattern with configurable parameters
     */
    private static JsonObject createFinalDensity(PlanetBuilder planet) {
        JsonObject finalDensity = new JsonObject();
        finalDensity.addProperty("type", "minecraft:interpolated");

        JsonObject argument = new JsonObject();
        argument.addProperty("type", "minecraft:blend_density");

        JsonObject blendArgument = new JsonObject();
        blendArgument.addProperty("type", "minecraft:add");

        JsonObject argument1 = new JsonObject();
        argument1.addProperty("type", "minecraft:mul");

        JsonObject yGradient = new JsonObject();
        yGradient.addProperty("type", "minecraft:y_clamped_gradient");
        yGradient.addProperty("from_y", -64);
        yGradient.addProperty("to_y", 320);
        yGradient.addProperty("from_value", 1.5);
        yGradient.addProperty("to_value", -1.5);
        argument1.add("argument1", yGradient);

        argument1.addProperty("argument2", 0.64);
        blendArgument.add("argument1", argument1);

        JsonObject argument2 = new JsonObject();
        argument2.addProperty("type", "minecraft:add");
        argument2.addProperty("argument1", -0.234375);

        JsonObject mulArgument2 = new JsonObject();
        mulArgument2.addProperty("type", "minecraft:mul");

        JsonObject addArgument1 = new JsonObject();
        addArgument1.addProperty("type", "minecraft:add");

        JsonObject mulContinentalness = new JsonObject();
        mulContinentalness.addProperty("type", "minecraft:mul");

        JsonObject continentalnessNoise = new JsonObject();
        continentalnessNoise.addProperty("type", "minecraft:shifted_noise");
        continentalnessNoise.addProperty("noise", "minecraft:continentalness");
        continentalnessNoise.addProperty("xz_scale", 0.25);
        continentalnessNoise.addProperty("y_scale", 0);
        continentalnessNoise.addProperty("shift_x", "minecraft:shift_x");
        continentalnessNoise.addProperty("shift_y", 0);
        continentalnessNoise.addProperty("shift_z", "minecraft:shift_z");
        mulContinentalness.add("argument1", continentalnessNoise);

        mulContinentalness.addProperty("argument2", getSeedVariation(planet, "height_var3", planet.heightVariation3, planet.heightVariation3 * 0.25f));
        addArgument1.add("argument2", mulContinentalness);

        JsonObject mulErosion = new JsonObject();
        mulErosion.addProperty("type", "minecraft:mul");

        JsonObject erosionNoise = new JsonObject();
        erosionNoise.addProperty("type", "minecraft:shifted_noise");
        erosionNoise.addProperty("noise", "minecraft:erosion");
        erosionNoise.addProperty("xz_scale", 0.25);
        erosionNoise.addProperty("y_scale", 0);
        erosionNoise.addProperty("shift_x", "minecraft:shift_x");
        erosionNoise.addProperty("shift_y", 0);
        erosionNoise.addProperty("shift_z", "minecraft:shift_z");
        mulErosion.add("argument1", erosionNoise);

        mulErosion.addProperty("argument2", getSeedVariation(planet, "height_var4", planet.heightVariation4, planet.heightVariation4 * 0.25f));
        addArgument1.add("argument1", mulErosion);

        mulArgument2.add("argument1", addArgument1);
        mulArgument2.addProperty("argument2", 0.175);

        argument2.add("argument2", mulArgument2);
        blendArgument.add("argument2", argument2);

        argument.add("argument", blendArgument);
        finalDensity.add("argument", argument);

        return finalDensity;
    }

    /**
     * Create surface rule using Moon's proven pattern with configurable blocks
     */
    private static JsonObject createSurfaceRule(PlanetBuilder planet) {
        JsonObject surfaceRule = new JsonObject();
        surfaceRule.addProperty("type", "minecraft:sequence");

        JsonArray sequence = new JsonArray();

        // Surface layer (top block)
        JsonObject surfaceLayer = new JsonObject();
        surfaceLayer.addProperty("type", "minecraft:condition");

        JsonObject surfaceCondition = new JsonObject();
        surfaceCondition.addProperty("type", "minecraft:above_preliminary_surface");
        surfaceLayer.add("if_true", surfaceCondition);

        JsonObject surfaceResult = new JsonObject();
        surfaceResult.addProperty("type", "minecraft:block");
        JsonObject surfaceState = new JsonObject();
        surfaceState.addProperty("Name", planet.surfaceBlock);
        surfaceResult.add("result_state", surfaceState);
        surfaceLayer.add("then_run", surfaceResult);

        sequence.add(surfaceLayer);

        // Subsurface layer
        JsonObject subsurfaceLayer = new JsonObject();
        subsurfaceLayer.addProperty("type", "minecraft:condition");

        JsonObject subsurfaceCondition = new JsonObject();
        subsurfaceCondition.addProperty("type", "minecraft:water");
        subsurfaceCondition.addProperty("offset", -1);
        subsurfaceCondition.addProperty("surface_depth_multiplier", 0);
        subsurfaceCondition.addProperty("add_stone_depth", false);
        subsurfaceLayer.add("if_true", subsurfaceCondition);

        JsonObject subsurfaceResult = new JsonObject();
        subsurfaceResult.addProperty("type", "minecraft:block");
        JsonObject subsurfaceState = new JsonObject();
        subsurfaceState.addProperty("Name", planet.subsurfaceBlock);
        subsurfaceResult.add("result_state", subsurfaceState);
        subsurfaceLayer.add("then_run", subsurfaceResult);

        sequence.add(subsurfaceLayer);

        // Deep layer (default)
        JsonObject deepLayer = new JsonObject();
        deepLayer.addProperty("type", "minecraft:block");
        JsonObject deepState = new JsonObject();
        deepState.addProperty("Name", planet.deepBlock);
        deepLayer.add("result_state", deepState);

        sequence.add(deepLayer);

        surfaceRule.add("sequence", sequence);
        return surfaceRule;
    }

    private static String capitalizeFirst(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static void writeJsonFile(String path, JsonObject json) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            GSON.toJson(json, writer);
        }
    }
}