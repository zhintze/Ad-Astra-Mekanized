package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.worldgen.NameGenerator;
import com.hecookin.adastramekanized.common.worldgen.PlanetRandomizer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Generates random planets with all necessary JSON files and Java classes
 * for complete NeoForge dimension integration.
 */
public class RandomPlanetGenerator {

    private static RandomPlanetGenerator instance;
    private final Gson gson;
    private final NameGenerator nameGenerator;
    private final PlanetRandomizer planetRandomizer;

    public RandomPlanetGenerator() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.planetRandomizer = new PlanetRandomizer(System.currentTimeMillis(), false);
        this.nameGenerator = new NameGenerator(planetRandomizer);
    }

    public static synchronized RandomPlanetGenerator getInstance() {
        if (instance == null) {
            instance = new RandomPlanetGenerator();
        }
        return instance;
    }

    /**
     * Generate the specified number of random planets with all necessary files
     */
    public CompletableFuture<GenerationResult> generateRandomPlanets(int amount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                AdAstraMekanized.LOGGER.info("Starting generation of {} random planets", amount);

                Path resourcesRoot = getResourcesRoot();
                if (resourcesRoot == null) {
                    return new GenerationResult(false, "Failed to locate resources directory", 0);
                }

                List<GeneratedPlanetData> generatedPlanets = new ArrayList<>();

                for (int i = 0; i < amount; i++) {
                    GeneratedPlanetData planetData = generateSinglePlanet(i, resourcesRoot);
                    if (planetData != null) {
                        generatedPlanets.add(planetData);
                        AdAstraMekanized.LOGGER.debug("Generated planet {}/{}: {}", i + 1, amount, planetData.name());
                    } else {
                        AdAstraMekanized.LOGGER.warn("Failed to generate planet {}/{}", i + 1, amount);
                    }
                }

                AdAstraMekanized.LOGGER.info("Successfully generated {} planets", generatedPlanets.size());
                return new GenerationResult(true, "Generated " + generatedPlanets.size() + " planets", generatedPlanets.size());

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to generate planets", e);
                return new GenerationResult(false, "Generation failed: " + e.getMessage(), 0);
            }
        });
    }

    /**
     * Generate a single planet with all required files
     */
    private GeneratedPlanetData generateSinglePlanet(int index, Path resourcesRoot) {
        try {
            // Generate basic planet data
            DimensionEffectsType effectsType = DimensionEffectsType.getRandomType();
            String planetName = nameGenerator.generateUniquePlanetName(index, effectsType);
            String planetId = nameToId(planetName);

            PlanetRandomizer planetRand = planetRandomizer.forPlanet(index);

            // Generate planet properties using Ad Astra ranges with variation
            PlanetProperties properties = generatePlanetProperties(planetRand, effectsType);
            AtmosphereData atmosphere = generateAtmosphereData(planetRand, effectsType);
            RenderingData rendering = generateRenderingData(planetRand, effectsType, properties);

            // Create all JSON files
            createPlanetJson(resourcesRoot, planetId, planetName, properties, atmosphere, rendering);
            createDimensionTypeJson(resourcesRoot, planetId);
            createBiomeJson(resourcesRoot, planetId, effectsType, properties);
            createNoiseSettingsJson(resourcesRoot, planetId, effectsType);

            // Create Java dimension effects class
            createDimensionEffectsClass(planetId, effectsType);

            return new GeneratedPlanetData(planetName, planetId, effectsType);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to generate planet at index {}", index, e);
            return null;
        }
    }

    /**
     * Generate randomized planet properties based on Ad Astra ranges
     */
    private PlanetProperties generatePlanetProperties(PlanetRandomizer rand, DimensionEffectsType type) {
        DimensionEffectsType.PhysicalProperties defaults = type.getDefaultPhysics();

        // Gravity: 0.5f to 12.0f with type-based defaults
        float gravity = rand.randomFloat(
            Math.max(0.05f, defaults.gravity() - 0.3f),
            Math.min(12.0f, defaults.gravity() + 0.8f)
        );

        // Extreme outliers (5% chance)
        if (rand.randomBoolean(0.05f)) {
            gravity = rand.randomFloat(0.01f, 15.0f);
        }

        // Temperature: -270°C to 500°C with type-based bias
        float temperature = rand.randomFloat(
            defaults.temperature() - 50f,
            defaults.temperature() + 100f
        );

        // Extreme temperature outliers (5% chance)
        if (rand.randomBoolean(0.05f)) {
            temperature = rand.randomFloat(-270f, 500f);
        }

        // Day length: 0.5 to 2000 hours with variation
        float dayLength = rand.randomFloat(
            Math.max(0.5f, defaults.dayLength() * 0.3f),
            defaults.dayLength() * 3.0f
        );

        // Orbit distance: 50 to 5000 million km
        int orbitDistance = rand.randomInt(50, 5000);

        // Rings and moons
        boolean hasRings = rand.randomBoolean(0.15f); // 15% chance
        int moonCount = rand.randomInt(0, Math.min(5, type == DimensionEffectsType.GAS_GIANT ? 8 : 3));

        return new PlanetProperties(gravity, temperature, dayLength, orbitDistance, hasRings, moonCount);
    }

    /**
     * Generate atmosphere data with randomization
     */
    private AtmosphereData generateAtmosphereData(PlanetRandomizer rand, DimensionEffectsType type) {
        DimensionEffectsType.AtmosphericProperties defaults = type.getDefaultAtmosphere();

        boolean hasAtmosphere = defaults.hasAtmosphere();
        boolean breathable = defaults.breathable();

        // Add some variation to breathable atmospheres (rare)
        if (hasAtmosphere && !breathable && rand.randomBoolean(0.02f)) {
            breathable = true; // 2% chance of finding breathable atmosphere
        }

        float pressure = defaults.pressure();
        if (hasAtmosphere) {
            // Vary pressure ±50%
            pressure *= rand.randomFloat(0.5f, 1.5f);
        }

        float oxygenLevel = defaults.oxygenLevel();
        if (hasAtmosphere) {
            // Vary oxygen ±30%
            oxygenLevel *= rand.randomFloat(0.7f, 1.3f);
            oxygenLevel = Math.max(0.0f, Math.min(1.0f, oxygenLevel));
        }

        String atmosphereType = hasAtmosphere ?
            (pressure > 1.5f ? "THICK" : pressure < 0.1f ? "THIN" : "NORMAL") : "NONE";

        return new AtmosphereData(hasAtmosphere, breathable, pressure, oxygenLevel, atmosphereType);
    }

    /**
     * Generate rendering data for celestial bodies and visual effects
     */
    private RenderingData generateRenderingData(PlanetRandomizer rand, DimensionEffectsType type, PlanetProperties properties) {
        // Sky colors based on atmosphere and type
        int skyColor = generateSkyColor(rand, type);
        int sunriseColor = generateSunriseColor(rand, skyColor);
        int fogColor = generateFogColor(rand, skyColor);

        // Star visibility
        boolean hasStars = type != DimensionEffectsType.ALTERED_OVERWORLD || rand.randomBoolean(0.3f);
        int starCount = hasStars ? rand.randomInt(1000, 30000) : 0;
        float starBrightness = rand.randomFloat(0.3f, 3.0f);

        // Celestial bodies
        SunData sun = generateSunData(rand, properties.orbitDistance());
        List<MoonData> moons = generateMoons(rand, properties.moonCount());
        List<PlanetData> visiblePlanets = generateVisiblePlanets(rand);

        // Weather and particles
        boolean hasClouds = type == DimensionEffectsType.ALTERED_OVERWORLD || rand.randomBoolean(0.3f);
        boolean hasWeather = type == DimensionEffectsType.ALTERED_OVERWORLD ||
                           (type == DimensionEffectsType.ICE_WORLD && rand.randomBoolean(0.7f)) ||
                           rand.randomBoolean(0.2f);

        return new RenderingData(skyColor, sunriseColor, fogColor, hasStars, starCount, starBrightness,
                                sun, moons, visiblePlanets, hasClouds, hasWeather);
    }

    /**
     * Generate sky color based on planet type and atmosphere
     */
    private int generateSkyColor(PlanetRandomizer rand, DimensionEffectsType type) {
        return switch (type) {
            case ALTERED_OVERWORLD -> 0x78A7FF; // Earth-like blue
            case ROCKY -> rand.randomInt(0x8B4513, 0xDAA520); // Brown to golden
            case ICE_WORLD -> rand.randomInt(0xB0E0E6, 0xE0FFFF); // Light blue to cyan
            case VOLCANIC -> rand.randomInt(0x8B0000, 0xFF4500); // Dark red to orange
            case GAS_GIANT -> rand.randomInt(0x4169E1, 0x9370DB); // Royal blue to purple
            case MOON_LIKE, ASTEROID_LIKE -> 0x000000; // Black space
        };
    }

    private int generateSunriseColor(PlanetRandomizer rand, int skyColor) {
        // Generate complementary or related color
        return rand.randomInt(0xFF6347, 0xFFD700); // Tomato to gold range
    }

    private int generateFogColor(PlanetRandomizer rand, int skyColor) {
        // Slightly darker or different hue of sky color
        return skyColor & 0xCCCCCC; // Darken by masking
    }

    private SunData generateSunData(PlanetRandomizer rand, int orbitDistance) {
        // Scale sun size based on distance (closer = bigger)
        float scale = Math.max(0.1f, Math.min(2.0f, 500.0f / orbitDistance));
        scale *= rand.randomFloat(0.7f, 1.3f); // Add variation

        int color = rand.randomInt(0xFFFFE0, 0xFFFFFF); // Light yellow to white

        return new SunData("minecraft:textures/environment/sun.png", scale, color, true);
    }

    private List<MoonData> generateMoons(PlanetRandomizer rand, int moonCount) {
        List<MoonData> moons = new ArrayList<>();

        for (int i = 0; i < moonCount; i++) {
            String texture = "adastramekanized:textures/celestial/moon_" + (i % 3 + 1) + ".png";
            float scale = rand.randomFloat(0.1f, 0.5f);
            int color = rand.randomInt(0x808080, 0xC0C0C0); // Gray range
            float horizontalPos = rand.randomFloat(-1.0f, 1.0f);
            float verticalPos = rand.randomFloat(-0.5f, 1.5f);

            moons.add(new MoonData(texture, scale, color, horizontalPos, verticalPos, true, true));
        }

        return moons;
    }

    private List<PlanetData> generateVisiblePlanets(PlanetRandomizer rand) {
        List<PlanetData> planets = new ArrayList<>();

        // 30% chance of seeing other planets
        if (rand.randomBoolean(0.3f)) {
            int count = rand.randomInt(1, 3);
            for (int i = 0; i < count; i++) {
                String texture = "adastramekanized:textures/celestial/planet_" + (i % 4 + 1) + ".png";
                float scale = rand.randomFloat(0.2f, 0.8f);
                int color = rand.randomInt(0x4169E1, 0xFF6347);
                float horizontalPos = rand.randomFloat(-1.0f, 1.0f);
                float verticalPos = rand.randomFloat(0.2f, 1.2f);

                planets.add(new PlanetData(texture, scale, color, horizontalPos, verticalPos, true, true));
            }
        }

        return planets;
    }

    // File creation methods would go here...
    // I'll implement the JSON creation methods next

    /**
     * Convert planet name to valid ResourceLocation path
     */
    private String nameToId(String name) {
        return name.toLowerCase()
                  .replaceAll("[^a-z0-9]", "_")
                  .replaceAll("_+", "_")
                  .replaceAll("^_|_$", "");
    }

    /**
     * Find the resources root directory - prioritize server datapack directory
     */
    private Path getResourcesRoot() {
        try {
            Path currentDir = Path.of("").toAbsolutePath();
            AdAstraMekanized.LOGGER.info("Looking for resources directory. Current working directory: {}", currentDir);

            // For server runtime: try datapack directory first
            Path datapackDir = currentDir.resolve("world/datapacks/adastramekanized");
            AdAstraMekanized.LOGGER.debug("Trying datapack directory: {}", datapackDir);
            if (Files.exists(datapackDir) || tryCreateDatapackDir(datapackDir)) {
                AdAstraMekanized.LOGGER.info("Using datapack directory: {}", datapackDir);
                return datapackDir;
            }

            // Try alternative server and development paths
            Path[] serverPaths = {
                // Development environment paths (runClient)
                currentDir.resolve("runs/client/world/datapacks/adastramekanized"),
                currentDir.resolve("runs/server/world/datapacks/adastramekanized"),
                currentDir.resolve("runs/data/world/datapacks/adastramekanized"),
                // Production server paths
                currentDir.resolve("datapacks/adastramekanized"),
                currentDir.resolve("data/adastramekanized"),
                currentDir.resolve("world/datapacks/adastramekanized"),
                currentDir.resolve("saves/world/datapacks/adastramekanized")
            };

            for (Path path : serverPaths) {
                AdAstraMekanized.LOGGER.debug("Trying server path: {}", path);
                if (Files.exists(path) || tryCreateDatapackDir(path)) {
                    AdAstraMekanized.LOGGER.info("Using server path: {}", path);
                    return path;
                }
            }

            // Development environment fallbacks
            Path resourcesDir = currentDir.resolve("src/main/resources");
            if (Files.exists(resourcesDir)) {
                return resourcesDir;
            }

            Path buildResourcesDir = currentDir.resolve("build/resources/main");
            if (Files.exists(buildResourcesDir)) {
                return buildResourcesDir;
            }

            // Last resort: create in current directory
            Path fallbackDir = currentDir.resolve("generated_datapack");
            AdAstraMekanized.LOGGER.info("Using fallback directory: {}", fallbackDir);
            if (tryCreateDatapackDir(fallbackDir)) {
                AdAstraMekanized.LOGGER.info("Successfully created fallback directory: {}", fallbackDir);
                return fallbackDir;
            }

            AdAstraMekanized.LOGGER.error("Could not find or create resources directory. Tried paths: {}",
                String.join(", ", "world/datapacks/adastramekanized", "src/main/resources", "build/resources/main", "generated_datapack"));
            return null;
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error finding resources directory", e);
            return null;
        }
    }

    /**
     * Try to create datapack directory structure
     */
    private boolean tryCreateDatapackDir(Path datapackRoot) {
        try {
            // Create the basic datapack structure
            Files.createDirectories(datapackRoot);
            Files.createDirectories(datapackRoot.resolve("data/adastramekanized/planets"));
            Files.createDirectories(datapackRoot.resolve("data/adastramekanized/dimension_type"));
            Files.createDirectories(datapackRoot.resolve("data/adastramekanized/worldgen/biome"));
            Files.createDirectories(datapackRoot.resolve("data/adastramekanized/worldgen/noise_settings"));

            // Create pack.mcmeta file for datapack
            Path packMcmeta = datapackRoot.resolve("pack.mcmeta");
            if (!Files.exists(packMcmeta)) {
                String packMcmetaContent = """
                    {
                        "pack": {
                            "pack_format": 26,
                            "description": "Generated planets for Ad Astra Mekanized"
                        }
                    }
                    """;
                Files.writeString(packMcmeta, packMcmetaContent);
            }

            AdAstraMekanized.LOGGER.info("Created datapack directory structure at: {}", datapackRoot);
            return true;
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Failed to create datapack directory at {}: {}", datapackRoot, e.getMessage());
            return false;
        }
    }

    // Record classes for data structures
    public record GenerationResult(boolean success, String message, int planetsGenerated) {}

    public record GeneratedPlanetData(String name, String id, DimensionEffectsType type) {}

    public record PlanetProperties(float gravity, float temperature, float dayLength,
                                  int orbitDistance, boolean hasRings, int moonCount) {}

    public record AtmosphereData(boolean hasAtmosphere, boolean breathable, float pressure,
                                float oxygenLevel, String type) {}

    public record RenderingData(int skyColor, int sunriseColor, int fogColor, boolean hasStars,
                               int starCount, float starBrightness, SunData sun, List<MoonData> moons,
                               List<PlanetData> visiblePlanets, boolean hasClouds, boolean hasWeather) {}

    public record SunData(String texture, float scale, int color, boolean visible) {}

    public record MoonData(String texture, float scale, int color, float horizontalPosition,
                          float verticalPosition, boolean movesWithTime, boolean visible) {}

    public record PlanetData(String texture, float scale, int color, float horizontalPosition,
                            float verticalPosition, boolean movesWithTime, boolean visible) {}

    /**
     * Create the main planet JSON file
     */
    private void createPlanetJson(Path resourcesRoot, String planetId, String planetName,
                                 PlanetProperties properties, AtmosphereData atmosphere,
                                 RenderingData rendering) throws IOException {
        JsonObject planet = new JsonObject();

        // Basic planet info
        planet.addProperty("id", "adastramekanized:" + planetId);
        planet.addProperty("display_name", planetName);

        // Properties
        JsonObject props = new JsonObject();
        props.addProperty("gravity", properties.gravity());
        props.addProperty("temperature", properties.temperature());
        props.addProperty("day_length", properties.dayLength());
        props.addProperty("orbit_distance", properties.orbitDistance());
        props.addProperty("has_rings", properties.hasRings());
        props.addProperty("moon_count", properties.moonCount());
        planet.add("properties", props);

        // Atmosphere
        JsonObject atm = new JsonObject();
        atm.addProperty("has_atmosphere", atmosphere.hasAtmosphere());
        atm.addProperty("breathable", atmosphere.breathable());
        atm.addProperty("pressure", atmosphere.pressure());
        atm.addProperty("oxygen_level", atmosphere.oxygenLevel());
        atm.addProperty("type", atmosphere.type());
        planet.add("atmosphere", atm);

        // Dimension
        JsonObject dim = new JsonObject();
        dim.addProperty("dimension_type", "adastramekanized:" + planetId);
        dim.addProperty("biome_source", "adastramekanized:" + planetId + "_biome_source");
        dim.addProperty("chunk_generator", "adastramekanized:" + planetId + "_generator");
        dim.addProperty("is_orbital", false);
        dim.addProperty("sky_color", rendering.skyColor());
        dim.addProperty("fog_color", rendering.fogColor());
        dim.addProperty("ambient_light", Math.max(0.1f, Math.min(1.0f, properties.temperature() / 100f + 0.5f)));
        planet.add("dimension", dim);

        // Rendering
        JsonObject render = new JsonObject();

        // Sky
        JsonObject sky = new JsonObject();
        sky.addProperty("sky_color", rendering.skyColor());
        sky.addProperty("sunrise_color", rendering.sunriseColor());
        sky.addProperty("custom_sky", true);
        sky.addProperty("has_stars", rendering.hasStars());
        sky.addProperty("star_count", rendering.starCount());
        sky.addProperty("star_brightness", rendering.starBrightness());
        sky.addProperty("star_visibility", rendering.hasStars() ? "constant" : "none");
        render.add("sky", sky);

        // Fog
        JsonObject fog = new JsonObject();
        fog.addProperty("fog_color", rendering.fogColor());
        fog.addProperty("has_fog", true);
        fog.addProperty("fog_density", Math.max(0.01f, Math.min(1.0f, atmosphere.pressure())));
        fog.addProperty("near_plane", 16.0f);
        fog.addProperty("far_plane", atmosphere.hasAtmosphere() ? 256.0f : 128.0f);
        render.add("fog", fog);

        // Celestial bodies
        JsonObject celestial = new JsonObject();

        // Sun
        JsonObject sun = new JsonObject();
        sun.addProperty("texture", rendering.sun().texture());
        sun.addProperty("scale", rendering.sun().scale());
        sun.addProperty("color", rendering.sun().color());
        sun.addProperty("visible", rendering.sun().visible());
        celestial.add("sun", sun);

        // Moons
        JsonArray moonsArray = new JsonArray();
        for (MoonData moon : rendering.moons()) {
            JsonObject moonObj = new JsonObject();
            moonObj.addProperty("texture", moon.texture());
            moonObj.addProperty("scale", moon.scale());
            moonObj.addProperty("color", moon.color());
            moonObj.addProperty("horizontal_position", moon.horizontalPosition());
            moonObj.addProperty("vertical_position", moon.verticalPosition());
            moonObj.addProperty("moves_with_time", moon.movesWithTime());
            moonObj.addProperty("visible", moon.visible());
            moonsArray.add(moonObj);
        }
        celestial.add("moons", moonsArray);

        // Visible planets
        JsonArray planetsArray = new JsonArray();
        for (PlanetData planetData : rendering.visiblePlanets()) {
            JsonObject planetObj = new JsonObject();
            planetObj.addProperty("texture", planetData.texture());
            planetObj.addProperty("scale", planetData.scale());
            planetObj.addProperty("color", planetData.color());
            planetObj.addProperty("horizontal_position", planetData.horizontalPosition());
            planetObj.addProperty("vertical_position", planetData.verticalPosition());
            planetObj.addProperty("moves_with_time", planetData.movesWithTime());
            planetObj.addProperty("visible", planetData.visible());
            planetsArray.add(planetObj);
        }
        celestial.add("visible_planets", planetsArray);

        render.add("celestial_bodies", celestial);

        // Weather
        JsonObject weather = new JsonObject();
        weather.addProperty("has_clouds", rendering.hasClouds());
        weather.addProperty("has_rain", rendering.hasWeather() && atmosphere.hasAtmosphere());
        weather.addProperty("has_snow", rendering.hasWeather() && properties.temperature() < 0);
        weather.addProperty("has_storms", rendering.hasWeather() && atmosphere.pressure() > 0.5f);
        weather.addProperty("rain_acidity", atmosphere.hasAtmosphere() ? Math.min(10.0f, Math.abs(properties.temperature()) / 50f) : 0.0f);
        render.add("weather", weather);

        // Particles
        JsonObject particles = new JsonObject();
        particles.addProperty("has_dust", !atmosphere.hasAtmosphere() || atmosphere.pressure() < 0.1f);
        particles.addProperty("has_ash", properties.temperature() > 200f);
        particles.addProperty("has_spores", atmosphere.breathable() && properties.temperature() > 10f);
        particles.addProperty("has_snowfall", properties.temperature() < -50f);
        particles.addProperty("particle_density", atmosphere.hasAtmosphere() ? atmosphere.pressure() : 0.0f);
        particles.addProperty("particle_color", rendering.fogColor());
        render.add("particles", particles);

        planet.add("rendering", render);

        // Write to file
        Path planetDir = resourcesRoot.resolve("data/adastramekanized/planets");
        Files.createDirectories(planetDir);
        Path planetFile = planetDir.resolve(planetId + ".json");

        Files.writeString(planetFile, gson.toJson(planet), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        AdAstraMekanized.LOGGER.debug("Created planet JSON: {}", planetFile);
    }

    /**
     * Create dimension type JSON file
     */
    private void createDimensionTypeJson(Path resourcesRoot, String planetId) throws IOException {
        JsonObject dimensionType = new JsonObject();

        dimensionType.addProperty("ultrawarm", false);
        dimensionType.addProperty("natural", false);
        dimensionType.addProperty("coordinate_scale", 1.0);
        dimensionType.addProperty("has_skylight", true);
        dimensionType.addProperty("has_ceiling", false);
        dimensionType.addProperty("ambient_light", 0.1);
        dimensionType.add("fixed_time", null);
        dimensionType.addProperty("monster_spawn_light_level", 0);
        dimensionType.addProperty("monster_spawn_block_light_limit", 0);
        dimensionType.addProperty("min_y", -64);
        dimensionType.addProperty("height", 384);
        dimensionType.addProperty("logical_height", 384);
        dimensionType.addProperty("infiniburn", "#minecraft:infiniburn_overworld");
        dimensionType.addProperty("effects", "adastramekanized:" + planetId);
        dimensionType.addProperty("respawn_anchor_works", false);
        dimensionType.addProperty("has_raids", false);
        dimensionType.addProperty("bed_works", false);
        dimensionType.addProperty("piglin_safe", false);

        // Write to file
        Path dimTypeDir = resourcesRoot.resolve("data/adastramekanized/dimension_type");
        Files.createDirectories(dimTypeDir);
        Path dimTypeFile = dimTypeDir.resolve(planetId + ".json");

        Files.writeString(dimTypeFile, gson.toJson(dimensionType), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        AdAstraMekanized.LOGGER.debug("Created dimension type JSON: {}", dimTypeFile);
    }

    /**
     * Create biome JSON file
     */
    private void createBiomeJson(Path resourcesRoot, String planetId, DimensionEffectsType type,
                                PlanetProperties properties) throws IOException {
        JsonObject biome = new JsonObject();

        biome.addProperty("has_precipitation", properties.temperature() > -10f && properties.temperature() < 50f);
        biome.addProperty("temperature", Math.max(-2.0f, Math.min(2.0f, properties.temperature() / 100f)));
        biome.addProperty("temperature_modifier", "none");
        biome.addProperty("downfall", properties.temperature() > 0f ? 0.4f : 0.0f);

        // Effects
        JsonObject effects = new JsonObject();
        effects.addProperty("fog_color", generateBiomeFogColor(type));
        effects.addProperty("water_color", generateWaterColor(type, properties.temperature()));
        effects.addProperty("water_fog_color", 329011);
        effects.addProperty("sky_color", generateBiomeSkyColor(type));
        effects.addProperty("grass_color", generateGrassColor(type, properties.temperature()));
        effects.addProperty("foliage_color", generateFoliageColor(type, properties.temperature()));

        // Mood sound
        JsonObject moodSound = new JsonObject();
        moodSound.addProperty("sound", "minecraft:ambient.cave");
        moodSound.addProperty("tick_delay", 6000);
        moodSound.addProperty("block_search_extent", 8);
        moodSound.addProperty("offset", 2.0);
        effects.add("mood_sound", moodSound);

        biome.add("effects", effects);

        // Empty spawners and features for hostile worlds
        JsonObject spawners = new JsonObject();
        spawners.add("monster", new JsonArray());
        spawners.add("creature", new JsonArray());
        spawners.add("ambient", new JsonArray());
        spawners.add("axolotls", new JsonArray());
        spawners.add("underground_water_creature", new JsonArray());
        spawners.add("water_creature", new JsonArray());
        spawners.add("water_ambient", new JsonArray());
        spawners.add("misc", new JsonArray());
        biome.add("spawners", spawners);

        biome.add("spawn_costs", new JsonObject());

        JsonObject carvers = new JsonObject();
        carvers.add("air", new JsonArray());
        biome.add("carvers", carvers);

        // Empty features for simplicity
        JsonArray features = new JsonArray();
        for (int i = 0; i < 10; i++) {
            features.add(new JsonArray());
        }
        biome.add("features", features);

        // Write to file
        Path biomeDir = resourcesRoot.resolve("data/adastramekanized/worldgen/biome");
        Files.createDirectories(biomeDir);
        Path biomeFile = biomeDir.resolve(planetId + "_plains.json");

        Files.writeString(biomeFile, gson.toJson(biome), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        AdAstraMekanized.LOGGER.debug("Created biome JSON: {}", biomeFile);
    }

    private int generateBiomeFogColor(DimensionEffectsType type) {
        return switch (type) {
            case ROCKY -> 0xD2B48C;
            case ICE_WORLD -> 0xB0E0E6;
            case VOLCANIC -> 0x8B0000;
            case GAS_GIANT -> 0x4169E1;
            case ALTERED_OVERWORLD -> 0xC0D8FF;
            default -> 0x808080;
        };
    }

    private int generateWaterColor(DimensionEffectsType type, float temperature) {
        if (temperature < -50f) return 0x4682B4; // Steel blue (frozen)
        if (temperature > 100f) return 0xFF4500; // Orange red (hot)
        return switch (type) {
            case ICE_WORLD -> 0x87CEEB; // Sky blue
            case VOLCANIC -> 0x8B0000; // Dark red
            case ALTERED_OVERWORLD -> 0x006994; // Water blue
            default -> 0x4159204; // Default water
        };
    }

    private int generateBiomeSkyColor(DimensionEffectsType type) {
        return switch (type) {
            case ALTERED_OVERWORLD -> 0x78A7FF;
            case ICE_WORLD -> 0xB0E0E6;
            case VOLCANIC -> 0x8B0000;
            case GAS_GIANT -> 0x4169E1;
            default -> 0x000000;
        };
    }

    private int generateGrassColor(DimensionEffectsType type, float temperature) {
        if (temperature < -30f) return 0x606060; // Gray (dead)
        if (temperature > 60f) return 0x8B4513; // Brown (burnt)
        return switch (type) {
            case ALTERED_OVERWORLD -> 0x91BD59; // Green
            case ICE_WORLD -> 0x708090; // Slate gray
            case VOLCANIC -> 0x8B0000; // Dark red
            default -> 0x7F7F7F; // Gray
        };
    }

    private int generateFoliageColor(DimensionEffectsType type, float temperature) {
        return generateGrassColor(type, temperature); // Same as grass for simplicity
    }

    /**
     * Create noise settings JSON file based on planet type
     */
    private void createNoiseSettingsJson(Path resourcesRoot, String planetId,
                                        DimensionEffectsType type) throws IOException {
        // For simplicity, we'll base this on the moon template but can be expanded
        JsonObject noiseSettings = new JsonObject();

        noiseSettings.addProperty("sea_level", 63);
        noiseSettings.addProperty("disable_mob_generation", type != DimensionEffectsType.ALTERED_OVERWORLD);
        noiseSettings.addProperty("aquifers_enabled", type == DimensionEffectsType.ALTERED_OVERWORLD);
        noiseSettings.addProperty("ore_veins_enabled", true);
        noiseSettings.addProperty("legacy_random_source", false);

        // Default blocks based on type
        JsonObject defaultBlock = new JsonObject();
        String blockName = switch (type) {
            case ICE_WORLD -> "minecraft:ice";
            case VOLCANIC -> "minecraft:basalt";
            case ALTERED_OVERWORLD -> "minecraft:stone";
            default -> "adastramekanized:moon_stone"; // Fallback
        };
        defaultBlock.addProperty("Name", blockName);
        noiseSettings.add("default_block", defaultBlock);

        JsonObject defaultFluid = new JsonObject();
        defaultFluid.addProperty("Name", type == DimensionEffectsType.ALTERED_OVERWORLD ? "minecraft:water" : "minecraft:air");
        noiseSettings.add("default_fluid", defaultFluid);

        // Noise configuration (simplified)
        JsonObject noise = new JsonObject();
        noise.addProperty("min_y", -64);
        noise.addProperty("height", 384);
        noise.addProperty("size_horizontal", type == DimensionEffectsType.GAS_GIANT ? 2 : 1);
        noise.addProperty("size_vertical", type == DimensionEffectsType.ASTEROID_LIKE ? 1 : 2);
        noiseSettings.add("noise", noise);

        // Simplified noise router (using basic overworld patterns)
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

        noiseSettings.add("spawn_target", new JsonArray());

        // Surface rule
        JsonObject surfaceRule = new JsonObject();
        surfaceRule.addProperty("type", "minecraft:sequence");
        JsonArray sequence = new JsonArray();
        JsonObject conditionRule = new JsonObject();
        conditionRule.addProperty("type", "minecraft:condition");
        JsonObject ifTrue = new JsonObject();
        ifTrue.addProperty("type", "minecraft:above_preliminary_surface");
        conditionRule.add("if_true", ifTrue);
        JsonObject thenRun = new JsonObject();
        thenRun.addProperty("type", "minecraft:block");
        JsonObject resultState = new JsonObject();
        resultState.addProperty("Name", blockName);
        thenRun.add("result_state", resultState);
        conditionRule.add("then_run", thenRun);
        sequence.add(conditionRule);
        surfaceRule.add("sequence", sequence);
        noiseSettings.add("surface_rule", surfaceRule);

        // Write to file
        Path noiseDir = resourcesRoot.resolve("data/adastramekanized/worldgen/noise_settings");
        Files.createDirectories(noiseDir);
        Path noiseFile = noiseDir.resolve(planetId + ".json");

        Files.writeString(noiseFile, gson.toJson(noiseSettings), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        AdAstraMekanized.LOGGER.debug("Created noise settings JSON: {}", noiseFile);
    }

    /**
     * Create Java dimension effects class
     */
    private void createDimensionEffectsClass(String planetId, DimensionEffectsType type) throws IOException {
        String className = toPascalCase(planetId) + "DimensionEffects";
        String templateType = type.getId();

        String javaCode = String.format("""
            package com.hecookin.adastramekanized.client.dimension;

            import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
            import net.minecraft.world.phys.Vec3;
            import org.jetbrains.annotations.NotNull;

            /**
             * Generated dimension effects for %s
             * Based on %s template
             */
            public class %s extends TemplateDimensionEffects {

                public %s() {
                    super(DimensionEffectsType.%s, 192.0f, true, SkyType.NORMAL, false, false);
                }

                @Override
                public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
                    return applyBrightness(colorIntToVec3(0x%06X), brightness);
                }

                @Override
                public boolean isFoggyAt(int x, int z) {
                    return %s;
                }
            }
            """,
            planetId,
            templateType,
            className,
            className,
            type.name(),
            0x808080, // Default fog color
            type == DimensionEffectsType.GAS_GIANT || type == DimensionEffectsType.ICE_WORLD
        );

        // Write to file
        Path javaDir = Path.of("src/main/java/com/hecookin/adastramekanized/client/dimension");
        Files.createDirectories(javaDir);
        Path javaFile = javaDir.resolve(className + ".java");

        Files.writeString(javaFile, javaCode, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        AdAstraMekanized.LOGGER.debug("Created dimension effects class: {}", javaFile);
    }

    private String toPascalCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}