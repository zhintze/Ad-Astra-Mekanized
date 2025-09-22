package com.hecookin.adastramekanized.common.planets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.planets.StaticPlanetDefinitions.PlanetDefinition;
import com.hecookin.adastramekanized.common.planets.StaticPlanetDefinitions.PlanetType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Generates static JSON files for all 50 predefined planets using Ad Astra patterns
 */
public class StaticPlanetGenerator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String RESOURCES_PATH = "src/main/resources/data/adastramekanized/";

    /**
     * Generate all planet data files
     */
    public static void generateAllPlanets() {
        AdAstraMekanized.LOGGER.info("Generating static data for 50 planets...");

        // Create directories
        createDirectories();

        // Generate files for each planet
        for (PlanetDefinition planet : StaticPlanetDefinitions.ALL_PLANETS) {
            try {
                generatePlanetData(planet);
                generateDimensionData(planet);
                generateDimensionType(planet);
                generateNoiseSettings(planet);
                generateBiome(planet);

                AdAstraMekanized.LOGGER.debug("Generated data for planet: {}", planet.displayName);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to generate data for planet: {}", planet.displayName, e);
            }
        }

        AdAstraMekanized.LOGGER.info("Completed generating data for {} planets", StaticPlanetDefinitions.ALL_PLANETS.size());
    }

    private static void createDirectories() {
        new File(RESOURCES_PATH + "planets").mkdirs();
        new File(RESOURCES_PATH + "dimension").mkdirs();
        new File(RESOURCES_PATH + "dimension_type").mkdirs();
        new File(RESOURCES_PATH + "worldgen/noise_settings").mkdirs();
        new File(RESOURCES_PATH + "worldgen/biome").mkdirs();
    }

    /**
     * Generate planet data JSON (properties and characteristics)
     */
    private static void generatePlanetData(PlanetDefinition planet) throws IOException {
        JsonObject planetJson = new JsonObject();

        planetJson.addProperty("id", planet.getId().toString());
        planetJson.addProperty("display_name", planet.displayName);

        // Properties
        JsonObject properties = new JsonObject();
        properties.addProperty("gravity", planet.gravity);
        properties.addProperty("temperature", planet.temperature);
        properties.addProperty("day_length", 24.0f); // Standard day
        properties.addProperty("orbit_distance", 1000 + planet.name.hashCode() % 5000);
        properties.addProperty("has_rings", false);
        properties.addProperty("moon_count", planet.type == PlanetType.GAS_GIANT_MOON ? 0 : (planet.name.hashCode() % 4));
        planetJson.add("properties", properties);

        // Atmosphere
        JsonObject atmosphere = new JsonObject();
        atmosphere.addProperty("has_atmosphere", planet.hasAtmosphere);
        atmosphere.addProperty("breathable", planet.breathable);
        atmosphere.addProperty("pressure", planet.hasAtmosphere ? 0.8f + (planet.name.hashCode() % 100) / 100.0f : 0.0f);
        atmosphere.addProperty("oxygen_level", planet.breathable ? 0.21f : (planet.hasAtmosphere ? 0.01f : 0.0f));
        atmosphere.addProperty("type", planet.hasAtmosphere ? (planet.breathable ? "NORMAL" : "THIN") : "NONE");
        planetJson.add("atmosphere", atmosphere);

        // Dimension reference
        JsonObject dimension = new JsonObject();
        dimension.addProperty("dimension_type", planet.getId().toString());
        dimension.addProperty("biome_source", planet.getId() + "_biome_source");
        dimension.addProperty("chunk_generator", planet.getId() + "_generator");
        dimension.addProperty("is_orbital", false);
        dimension.addProperty("sky_color", planet.skyColor);
        dimension.addProperty("fog_color", planet.fogColor);
        dimension.addProperty("ambient_light", planet.type == PlanetType.HOT_WORLD ? 0.3f : 0.1f);
        planetJson.add("dimension", dimension);

        // Rendering settings
        JsonObject rendering = createRenderingSettings(planet);
        planetJson.add("rendering", rendering);

        writeJsonFile(RESOURCES_PATH + "planets/" + planet.name + ".json", planetJson);
    }

    /**
     * Generate dimension JSON with unique seeded noise
     */
    private static void generateDimensionData(PlanetDefinition planet) throws IOException {
        JsonObject dimensionJson = new JsonObject();

        dimensionJson.addProperty("type", planet.getId().toString());

        JsonObject generator = new JsonObject();
        generator.addProperty("type", "minecraft:noise");
        generator.addProperty("settings", planet.getId().toString());

        // Biome source with unique parameters
        JsonObject biomeSource = createBiomeSource(planet);
        generator.add("biome_source", biomeSource);

        dimensionJson.add("generator", generator);

        writeJsonFile(RESOURCES_PATH + "dimension/" + planet.name + ".json", dimensionJson);
    }

    /**
     * Generate dimension type with planet-specific settings
     */
    private static void generateDimensionType(PlanetDefinition planet) throws IOException {
        JsonObject dimensionType = new JsonObject();

        dimensionType.addProperty("ambient_light", planet.type == PlanetType.HOT_WORLD ? 0.3f : 0.1f);
        dimensionType.addProperty("effects", planet.getId().toString());
        dimensionType.addProperty("has_ceiling", false);
        dimensionType.addProperty("has_skylight", true);
        dimensionType.addProperty("height", 384);
        dimensionType.addProperty("min_y", -64);
        dimensionType.addProperty("natural", true);
        dimensionType.addProperty("coordinate_scale", 1.0f);
        dimensionType.addProperty("bed_works", true);
        dimensionType.addProperty("respawn_anchor_works", false);
        dimensionType.addProperty("has_raids", planet.breathable);
        dimensionType.addProperty("logical_height", 384);
        dimensionType.addProperty("infiniburn", "minecraft:infiniburn_overworld");

        // Monster spawn settings
        JsonObject monsterSpawn = new JsonObject();
        monsterSpawn.addProperty("type", "minecraft:uniform");
        JsonObject spawnValue = new JsonObject();
        spawnValue.addProperty("max_inclusive", 7);
        spawnValue.addProperty("min_inclusive", 0);
        monsterSpawn.add("value", spawnValue);
        dimensionType.add("monster_spawn_light_level", monsterSpawn);

        writeJsonFile(RESOURCES_PATH + "dimension_type/" + planet.name + ".json", dimensionType);
    }

    /**
     * Generate high-quality noise settings with unique seed
     */
    private static void generateNoiseSettings(PlanetDefinition planet) throws IOException {
        JsonObject noiseSettings = new JsonObject();

        noiseSettings.addProperty("sea_level", getSeaLevel(planet));
        noiseSettings.addProperty("disable_mob_generation", false);
        noiseSettings.addProperty("aquifers_enabled", planet.type == PlanetType.GAS_GIANT_MOON);
        noiseSettings.addProperty("ore_veins_enabled", true);
        noiseSettings.addProperty("legacy_random_source", false);

        // Default blocks
        JsonObject defaultBlock = new JsonObject();
        defaultBlock.addProperty("Name", planet.defaultBlock);
        noiseSettings.add("default_block", defaultBlock);

        JsonObject defaultFluid = new JsonObject();
        defaultFluid.addProperty("Name", planet.type == PlanetType.GAS_GIANT_MOON ? "minecraft:water" : "minecraft:air");
        noiseSettings.add("default_fluid", defaultFluid);

        // Noise configuration with unique seed
        JsonObject noise = new JsonObject();
        noise.addProperty("min_y", -64);
        noise.addProperty("height", 384);
        noise.addProperty("size_horizontal", getHorizontalSize(planet));
        noise.addProperty("size_vertical", getVerticalSize(planet));
        noiseSettings.add("noise", noise);

        // Noise router with planet-specific density functions
        JsonObject noiseRouter = createNoiseRouter(planet);
        noiseSettings.add("noise_router", noiseRouter);

        // Surface rule
        JsonObject surfaceRule = createSurfaceRule(planet);
        noiseSettings.add("surface_rule", surfaceRule);

        // Empty spawn target for now
        noiseSettings.add("spawn_target", new JsonArray());

        writeJsonFile(RESOURCES_PATH + "worldgen/noise_settings/" + planet.name + ".json", noiseSettings);
    }

    /**
     * Generate biome with planet-specific ores and mobs
     */
    private static void generateBiome(PlanetDefinition planet) throws IOException {
        JsonObject biome = new JsonObject();

        biome.addProperty("downfall", planet.hasAtmosphere ? 0.4f : 0.0f);
        biome.addProperty("temperature", Math.max(0.0f, Math.min(2.0f, (planet.temperature + 100) / 200.0f)));
        biome.addProperty("has_precipitation", planet.hasAtmosphere && planet.temperature > -50);

        // Effects
        JsonObject effects = new JsonObject();
        effects.addProperty("sky_color", planet.skyColor);
        effects.addProperty("fog_color", planet.fogColor);
        effects.addProperty("water_color", getWaterColor(planet));
        effects.addProperty("water_fog_color", getWaterFogColor(planet));
        biome.add("effects", effects);

        // Features (ore generation)
        JsonArray features = new JsonArray();
        for (int i = 0; i < 10; i++) {
            JsonArray layer = new JsonArray();
            if (i == 6) { // Underground ores layer
                for (String ore : planet.ores) {
                    layer.add("minecraft:" + ore.replace("minecraft:", ""));
                }
            }
            features.add(layer);
        }
        biome.add("features", features);

        // Spawners (mobs)
        JsonObject spawners = new JsonObject();
        JsonArray monsters = new JsonArray();
        for (String mob : planet.mobs) {
            JsonObject spawner = new JsonObject();
            spawner.addProperty("type", mob);
            spawner.addProperty("weight", 100);
            spawner.addProperty("minCount", 1);
            spawner.addProperty("maxCount", 4);
            monsters.add(spawner);
        }
        spawners.add("monster", monsters);

        // Empty arrays for other spawn categories
        spawners.add("creature", new JsonArray());
        spawners.add("ambient", new JsonArray());
        spawners.add("water_creature", new JsonArray());
        spawners.add("water_ambient", new JsonArray());
        spawners.add("underground_water_creature", new JsonArray());
        spawners.add("axolotls", new JsonArray());

        biome.add("spawners", spawners);

        writeJsonFile(RESOURCES_PATH + "worldgen/biome/" + planet.name + "_plains.json", biome);
    }

    // Helper methods for planet-specific generation
    private static int getSeaLevel(PlanetDefinition planet) {
        return switch (planet.type) {
            case GAS_GIANT_MOON -> 63;
            case ICE_WORLD -> 32;
            case HOT_WORLD -> 0;
            default -> 48;
        };
    }

    private static int getHorizontalSize(PlanetDefinition planet) {
        return switch (planet.type) {
            case EXTREME_WORLD -> 2;
            case GAS_GIANT_MOON -> 1;
            default -> 1;
        };
    }

    private static int getVerticalSize(PlanetDefinition planet) {
        return switch (planet.type) {
            case HOT_WORLD, EXTREME_WORLD -> 3;
            default -> 2;
        };
    }

    private static int getWaterColor(PlanetDefinition planet) {
        return switch (planet.type) {
            case ICE_WORLD -> 0x3F76E4;
            case HOT_WORLD -> 0xFF6B47;
            case EXTREME_WORLD -> 0x905957;
            default -> 0x4159204;
        };
    }

    private static int getWaterFogColor(PlanetDefinition planet) {
        return switch (planet.type) {
            case ICE_WORLD -> 0x050533;
            case HOT_WORLD -> 0x4C0000;
            case EXTREME_WORLD -> 0x2A0A0A;
            default -> 0x050533;
        };
    }

    // Complex helper methods for JSON generation
    private static JsonObject createRenderingSettings(PlanetDefinition planet) {
        JsonObject rendering = new JsonObject();

        // Sky settings
        JsonObject sky = new JsonObject();
        sky.addProperty("sky_color", planet.skyColor);
        sky.addProperty("sunrise_color", adjustColor(planet.skyColor, 30));
        sky.addProperty("custom_sky", true);
        sky.addProperty("has_stars", true);
        sky.addProperty("star_count", 5000 + (planet.name.hashCode() % 20000));
        sky.addProperty("star_brightness", 0.2f + (planet.name.hashCode() % 100) / 100.0f);
        sky.addProperty("star_visibility", planet.hasAtmosphere ? "night_only" : "constant");
        rendering.add("sky", sky);

        // Fog settings
        JsonObject fog = new JsonObject();
        fog.addProperty("fog_color", planet.fogColor);
        fog.addProperty("has_fog", planet.hasAtmosphere);
        fog.addProperty("fog_density", planet.hasAtmosphere ? 0.1f + (planet.name.hashCode() % 50) / 100.0f : 0.0f);
        fog.addProperty("near_plane", 5.0f + (planet.name.hashCode() % 20));
        fog.addProperty("far_plane", 100.0f + (planet.name.hashCode() % 200));
        rendering.add("fog", fog);

        // Celestial bodies
        JsonObject celestialBodies = new JsonObject();

        JsonObject sun = new JsonObject();
        sun.addProperty("texture", "minecraft:textures/environment/sun.png");
        sun.addProperty("scale", 0.5f + (planet.name.hashCode() % 100) / 100.0f);
        sun.addProperty("color", adjustColor(planet.skyColor, -20));
        sun.addProperty("visible", true);
        celestialBodies.add("sun", sun);

        // Add moons if planet has them
        JsonArray moons = new JsonArray();
        int moonCount = planet.name.hashCode() % 4;
        for (int i = 0; i < moonCount; i++) {
            JsonObject moon = new JsonObject();
            moon.addProperty("texture", "minecraft:textures/environment/moon_phases.png");
            moon.addProperty("scale", 0.2f + (i * 0.1f));
            moon.addProperty("color", adjustColor(planet.skyColor, 50));
            moon.addProperty("horizontal_position", -1.0f + (i * 0.5f));
            moon.addProperty("vertical_position", 0.5f + (i * 0.2f));
            moon.addProperty("moves_with_time", true);
            moon.addProperty("visible", true);
            moons.add(moon);
        }
        celestialBodies.add("moons", moons);
        celestialBodies.add("visible_planets", new JsonArray());

        rendering.add("celestial_bodies", celestialBodies);

        // Weather
        JsonObject weather = new JsonObject();
        weather.addProperty("has_clouds", planet.hasAtmosphere && planet.temperature > -100);
        weather.addProperty("has_rain", planet.hasAtmosphere && planet.temperature > 0);
        weather.addProperty("has_snow", planet.hasAtmosphere && planet.temperature < 0);
        weather.addProperty("has_storms", planet.type == PlanetType.EXTREME_WORLD);
        weather.addProperty("rain_acidity", planet.type == PlanetType.HOT_WORLD ? 0.8f : 0.0f);
        rendering.add("weather", weather);

        // Particles
        JsonObject particles = new JsonObject();
        particles.addProperty("has_dust", !planet.hasAtmosphere || planet.type == PlanetType.AIRLESS_BODY);
        particles.addProperty("has_ash", planet.type == PlanetType.HOT_WORLD);
        particles.addProperty("has_spores", planet.type == PlanetType.GAS_GIANT_MOON);
        particles.addProperty("has_snowfall", planet.type == PlanetType.ICE_WORLD);
        particles.addProperty("particle_density", 0.1f + (planet.name.hashCode() % 50) / 100.0f);
        particles.addProperty("particle_color", planet.fogColor);
        rendering.add("particles", particles);

        return rendering;
    }

    private static JsonObject createBiomeSource(PlanetDefinition planet) {
        JsonObject biomeSource = new JsonObject();
        biomeSource.addProperty("type", "minecraft:fixed");
        biomeSource.addProperty("biome", planet.getId() + "_plains");
        return biomeSource;
    }

    private static JsonObject createNoiseRouter(PlanetDefinition planet) {
        JsonObject router = new JsonObject();
        Random random = new Random(planet.seed);

        // Barrier noise
        JsonObject barrier = createNoiseFunction("minecraft:aquifer_barrier",
            0.8f + random.nextFloat() * 0.4f, 0.6f + random.nextFloat() * 0.4f);
        router.add("barrier", barrier);

        // Fluid level floodedness
        JsonObject floodedness = createNoiseFunction("minecraft:aquifer_fluid_level_floodedness",
            0.9f + random.nextFloat() * 0.2f, 0.9f + random.nextFloat() * 0.2f);
        router.add("fluid_level_floodedness", floodedness);

        // Temperature and vegetation (shifted noise)
        JsonObject temperature = createShiftedNoise("minecraft:temperature", 0.25f);
        JsonObject vegetation = createShiftedNoise("minecraft:vegetation", 0.25f);
        router.add("temperature", temperature);
        router.add("vegetation", vegetation);

        // Terrain shaping
        JsonObject continents = createNoiseFunction("minecraft:continentalness", 0.8f + random.nextFloat() * 0.4f, 0.0f);
        JsonObject erosion = createNoiseFunction("minecraft:erosion", 0.9f + random.nextFloat() * 0.2f, 0.0f);
        JsonObject ridges = createNoiseFunction("minecraft:ridge", 1.2f + random.nextFloat() * 0.3f, 0.0f);

        router.add("continents", continents);
        router.add("erosion", erosion);
        router.add("ridges", ridges);

        // Depth calculation
        JsonObject depth = new JsonObject();
        depth.addProperty("type", "minecraft:add");
        depth.addProperty("argument1", "minecraft:y");
        JsonObject depthConstant = new JsonObject();
        depthConstant.addProperty("type", "minecraft:constant");
        depthConstant.addProperty("argument", 0.1f + random.nextFloat() * 0.1f);
        depth.add("argument2", depthConstant);
        router.add("depth", depth);

        // Final density with planet-specific parameters
        JsonObject finalDensity = createFinalDensity(planet, random);
        router.add("final_density", finalDensity);

        // Initial density without jaggedness
        JsonObject initialDensity = createInitialDensity(planet, random);
        router.add("initial_density_without_jaggedness", initialDensity);

        // Ore vein settings
        router.addProperty("vein_toggle", 0);
        router.addProperty("vein_ridged", 0);
        router.addProperty("vein_gap", 0);

        return router;
    }

    private static JsonObject createSurfaceRule(PlanetDefinition planet) {
        JsonObject surfaceRule = new JsonObject();
        surfaceRule.addProperty("type", "minecraft:sequence");

        JsonArray sequence = new JsonArray();

        // Bedrock layer
        JsonObject bedrockRule = new JsonObject();
        bedrockRule.addProperty("type", "minecraft:condition");

        JsonObject bedrockCondition = new JsonObject();
        bedrockCondition.addProperty("type", "minecraft:vertical_gradient");
        bedrockCondition.addProperty("random_name", "minecraft:bedrock_floor");
        JsonObject trueAt = new JsonObject();
        trueAt.addProperty("above_bottom", 0);
        JsonObject falseAt = new JsonObject();
        falseAt.addProperty("above_bottom", 5);
        bedrockCondition.add("true_at_and_below", trueAt);
        bedrockCondition.add("false_at_and_above", falseAt);
        bedrockRule.add("if_true", bedrockCondition);

        JsonObject bedrockResult = new JsonObject();
        bedrockResult.addProperty("type", "minecraft:block");
        JsonObject bedrockState = new JsonObject();
        bedrockState.addProperty("Name", "minecraft:bedrock");
        bedrockResult.add("result_state", bedrockState);
        bedrockRule.add("then_run", bedrockResult);

        sequence.add(bedrockRule);

        // Surface layer rule
        JsonObject surfaceLayerRule = new JsonObject();
        surfaceLayerRule.addProperty("type", "minecraft:condition");

        JsonObject surfaceCondition = new JsonObject();
        surfaceCondition.addProperty("type", "minecraft:on_floor");
        surfaceLayerRule.add("if_true", surfaceCondition);

        JsonObject surfaceResult = new JsonObject();
        surfaceResult.addProperty("type", "minecraft:block");
        JsonObject surfaceState = new JsonObject();
        surfaceState.addProperty("Name", planet.surfaceBlock);
        surfaceResult.add("result_state", surfaceState);
        surfaceLayerRule.add("then_run", surfaceResult);

        sequence.add(surfaceLayerRule);

        surfaceRule.add("sequence", sequence);
        return surfaceRule;
    }

    // Helper methods for complex noise generation
    private static JsonObject createNoiseFunction(String noise, float xzScale, float yScale) {
        JsonObject noiseFunc = new JsonObject();
        noiseFunc.addProperty("type", "minecraft:noise");
        noiseFunc.addProperty("noise", noise);
        noiseFunc.addProperty("xz_scale", xzScale);
        noiseFunc.addProperty("y_scale", yScale);
        return noiseFunc;
    }

    private static JsonObject createShiftedNoise(String noise, float scale) {
        JsonObject shifted = new JsonObject();
        shifted.addProperty("type", "minecraft:shifted_noise");
        shifted.addProperty("noise", noise);
        shifted.addProperty("xz_scale", scale);
        shifted.addProperty("y_scale", 0.0f);
        shifted.addProperty("shift_x", "minecraft:shift_x");
        shifted.addProperty("shift_y", 0);
        shifted.addProperty("shift_z", "minecraft:shift_z");
        return shifted;
    }

    private static JsonObject createFinalDensity(PlanetDefinition planet, Random random) {
        JsonObject finalDensity = new JsonObject();
        finalDensity.addProperty("type", "minecraft:interpolated");

        JsonObject argument = new JsonObject();
        argument.addProperty("type", "minecraft:blend_density");

        JsonObject add = new JsonObject();
        add.addProperty("type", "minecraft:add");

        JsonObject mul = new JsonObject();
        mul.addProperty("type", "minecraft:mul");

        // Y-clamped gradient (safe values well below world height)
        JsonObject gradient = new JsonObject();
        gradient.addProperty("type", "minecraft:y_clamped_gradient");
        gradient.addProperty("from_y", 200 + random.nextInt(40));
        gradient.addProperty("to_y", 280 + random.nextInt(20)); // Safe: well below 320
        gradient.addProperty("from_value", 1);
        gradient.addProperty("to_value", 0);
        mul.add("argument1", gradient);

        JsonObject caveNoise = new JsonObject();
        caveNoise.addProperty("type", "minecraft:add");
        JsonObject cheeseNoise = createNoiseFunction("minecraft:cave_cheese",
            0.5f + random.nextFloat() * 0.2f, 0.9f + random.nextFloat() * 0.2f);
        caveNoise.add("argument1", cheeseNoise);
        caveNoise.addProperty("argument2", 10.0f + random.nextFloat() * 8.0f);
        mul.add("argument2", caveNoise);

        add.add("argument1", mul);
        add.addProperty("argument2", -5.0f - random.nextFloat() * 5.0f);

        argument.add("argument", add);
        finalDensity.add("argument", argument);

        return finalDensity;
    }

    private static JsonObject createInitialDensity(PlanetDefinition planet, Random random) {
        JsonObject initialDensity = new JsonObject();
        initialDensity.addProperty("type", "minecraft:mul");
        initialDensity.addProperty("argument1", 2.0f + random.nextFloat() * 2.0f);

        JsonObject quarterNeg = new JsonObject();
        quarterNeg.addProperty("type", "minecraft:quarter_negative");

        JsonObject mul = new JsonObject();
        mul.addProperty("type", "minecraft:mul");

        JsonObject yAdd = new JsonObject();
        yAdd.addProperty("type", "minecraft:add");
        yAdd.addProperty("argument1", "minecraft:y");
        JsonObject yConstant = new JsonObject();
        yConstant.addProperty("type", "minecraft:constant");
        yConstant.addProperty("argument", random.nextFloat() * 0.2f);
        yAdd.add("argument2", yConstant);
        mul.add("argument1", yAdd);

        JsonObject cache2d = new JsonObject();
        cache2d.addProperty("type", "minecraft:cache_2d");
        JsonObject jaggedNoise = createNoiseFunction("minecraft:jagged",
            0.7f + random.nextFloat() * 0.3f, 0.0f);
        cache2d.add("argument", jaggedNoise);
        mul.add("argument2", cache2d);

        quarterNeg.add("argument", mul);
        initialDensity.add("argument2", quarterNeg);

        return initialDensity;
    }

    private static int adjustColor(int color, int adjustment) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = Math.max(0, Math.min(255, r + adjustment));
        g = Math.max(0, Math.min(255, g + adjustment));
        b = Math.max(0, Math.min(255, b + adjustment));

        return (r << 16) | (g << 8) | b;
    }

    private static void writeJsonFile(String path, JsonObject json) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            GSON.toJson(json, writer);
        }
    }
}