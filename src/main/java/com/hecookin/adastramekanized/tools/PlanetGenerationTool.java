package com.hecookin.adastramekanized.tools;

import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import com.hecookin.adastramekanized.common.worldgen.NameGenerator;
import com.hecookin.adastramekanized.common.worldgen.PlanetRandomizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.Map;

/**
 * Planet Generation Tool
 *
 * Standalone tool for generating all necessary planet dimension files
 * before server startup, making them work like static moon/mars dimensions.
 *
 * Run with: gradle generatePlanets
 */
public class PlanetGenerationTool {

    // ========================================================================================
    // CONFIGURATION CONSTANTS - Modify these to change generation behavior
    // ========================================================================================

    /** Total number of planets to generate */
    public static final int TOTAL_PLANETS = 10;

    /** Whether to overwrite existing planet files */
    public static final boolean OVERWRITE_EXISTING = true;

    /** Base output path for generated files */
    public static final String OUTPUT_BASE_PATH = "src/main/resources";

    /** Planet type distribution weights (must match DimensionEffectsType.CHANCE_WEIGHT) */
    public static final Map<DimensionEffectsType, Integer> PLANET_TYPE_WEIGHTS = Map.of(
        DimensionEffectsType.MOON_LIKE, 20,
        DimensionEffectsType.ROCKY, 17,
        DimensionEffectsType.ICE_WORLD, 10,
        DimensionEffectsType.VOLCANIC, 8,
        DimensionEffectsType.GAS_GIANT, 8,
        DimensionEffectsType.ASTEROID_LIKE, 10,
        DimensionEffectsType.ALTERED_OVERWORLD, 7
    );

    /** Seed for deterministic generation (0 = random) */
    public static final long GENERATION_SEED = 0L;

    /** Whether to generate Java dimension effects classes */
    public static final boolean GENERATE_JAVA_CLASSES = false;

    /** Whether to generate dimension files (in addition to dimension_type) */
    public static final boolean GENERATE_DIMENSION_FILES = true;

    // ========================================================================================
    // MAIN METHOD
    // ========================================================================================

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Ad Astra Mekanized - Planet Generation Tool");
        System.out.println("=".repeat(80));
        System.out.println();

        try {
            PlanetGenerationTool tool = new PlanetGenerationTool();
            tool.generateAllPlanets();

            System.out.println("\n" + "=".repeat(80));
            System.out.println("Planet generation completed successfully!");
            System.out.println("Generated " + TOTAL_PLANETS + " planets in " + OUTPUT_BASE_PATH);
            System.out.println("=".repeat(80));

        } catch (Exception e) {
            System.err.println("ERROR: Planet generation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // ========================================================================================
    // IMPLEMENTATION
    // ========================================================================================

    private final PlanetRandomizer randomizer;
    private final NameGenerator nameGenerator;
    private final Path outputBase;

    public PlanetGenerationTool() {
        this.randomizer = new PlanetRandomizer(GENERATION_SEED, GENERATION_SEED != 0);
        this.nameGenerator = new NameGenerator(randomizer);
        this.outputBase = Paths.get(OUTPUT_BASE_PATH).toAbsolutePath();

        System.out.println("Configuration:");
        System.out.println("  Total planets: " + TOTAL_PLANETS);
        System.out.println("  Overwrite existing: " + OVERWRITE_EXISTING);
        System.out.println("  Output path: " + outputBase);
        System.out.println("  Generation seed: " + (GENERATION_SEED == 0 ? "random" : GENERATION_SEED));
        System.out.println("  Generate Java classes: " + GENERATE_JAVA_CLASSES);
        System.out.println("  Generate dimension files: " + GENERATE_DIMENSION_FILES);
        System.out.println();
    }

    /**
     * Generate all planets and their associated files
     */
    public void generateAllPlanets() throws IOException {
        // Clean up existing files if overwrite is enabled
        if (OVERWRITE_EXISTING) {
            cleanupExistingFiles();
        }

        // Create base directories
        createDirectories();

        // Generate planet distribution
        Map<DimensionEffectsType, Integer> planetCounts = calculatePlanetDistribution();

        System.out.println("Planet type distribution:");
        int generatedCount = 0;

        for (Map.Entry<DimensionEffectsType, Integer> entry : planetCounts.entrySet()) {
            DimensionEffectsType type = entry.getKey();
            int count = entry.getValue();

            System.out.println("  " + type.name() + ": " + count + " planets");

            for (int i = 0; i < count; i++) {
                generateSinglePlanet(type, generatedCount + 1);
                generatedCount++;
            }
        }

        System.out.println("\nGenerated " + generatedCount + " planets total");
    }

    /**
     * Calculate how many planets of each type to generate based on weights
     */
    private Map<DimensionEffectsType, Integer> calculatePlanetDistribution() {
        Map<DimensionEffectsType, Integer> distribution = new EnumMap<>(DimensionEffectsType.class);

        // Calculate total weight
        int totalWeight = PLANET_TYPE_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();

        int remaining = TOTAL_PLANETS;

        for (Map.Entry<DimensionEffectsType, Integer> entry : PLANET_TYPE_WEIGHTS.entrySet()) {
            DimensionEffectsType type = entry.getKey();
            int weight = entry.getValue();

            // Calculate proportional count
            int count = Math.round((float) TOTAL_PLANETS * weight / totalWeight);

            // Ensure we don't exceed the total
            count = Math.min(count, remaining);

            distribution.put(type, count);
            remaining -= count;
        }

        // Distribute any remaining planets to the most common type
        if (remaining > 0) {
            DimensionEffectsType mostCommon = PLANET_TYPE_WEIGHTS.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DimensionEffectsType.ROCKY);

            distribution.put(mostCommon, distribution.get(mostCommon) + remaining);
        }

        return distribution;
    }

    /**
     * Generate a single planet with all its files
     */
    private void generateSinglePlanet(DimensionEffectsType type, int planetNumber) throws IOException {
        // Create unique randomizer for this planet to ensure diversity
        long planetSeed = GENERATION_SEED == 0 ?
            System.currentTimeMillis() + planetNumber * 1000L :
            GENERATION_SEED + planetNumber * 1000L;
        PlanetRandomizer planetRandomizer = new PlanetRandomizer(planetSeed, GENERATION_SEED != 0);
        NameGenerator planetNameGenerator = new NameGenerator(planetRandomizer);

        // Generate unique planet name
        String planetName = planetNameGenerator.generateUniquePlanetName(planetNumber, type);
        String planetId = nameToId(planetName);

        System.out.println("  Generating planet " + planetNumber + "/" + TOTAL_PLANETS + ": " + planetName + " (" + type.name() + ") [seed: " + planetSeed + "]");

        // Generate all planet data with planet-specific randomizer
        PlanetProperties properties = generatePlanetProperties(type, planetRandomizer);
        AtmosphereData atmosphere = generateAtmosphereData(type, planetRandomizer);
        CelestialData celestialData = generateCelestialData(properties, planetRandomizer);

        // Create all files
        createPlanetJson(planetId, planetName, type, properties, atmosphere, celestialData, planetRandomizer);
        createDimensionTypeJson(planetId, type);

        if (GENERATE_DIMENSION_FILES) {
            createDimensionJson(planetId);
        }

        createBiomeJson(planetId, type, planetRandomizer);
        createNoiseSettingsJson(planetId, type, planetRandomizer);

        if (GENERATE_JAVA_CLASSES) {
            createDimensionEffectsClass(planetId, planetName, type);
        }
    }


    /**
     * Convert planet name to valid ID
     */
    private String nameToId(String name) {
        return name.toLowerCase()
                  .replaceAll("[^a-z0-9]", "_")
                  .replaceAll("_+", "_")
                  .replaceAll("^_|_$", "");
    }

    /**
     * Clean up existing planet files when overwrite is enabled
     */
    private void cleanupExistingFiles() throws IOException {
        System.out.println("Cleaning up existing planet files...");

        String[] directories = {
            "data/adastramekanized/planets",
            "data/adastramekanized/dimension_type",
            "data/adastramekanized/dimension",
            "data/adastramekanized/worldgen/biome",
            "data/adastramekanized/worldgen/noise_settings"
        };

        for (String dir : directories) {
            Path dirPath = outputBase.resolve(dir);
            if (Files.exists(dirPath)) {
                System.out.println("  Removing: " + dirPath);
                // Delete directory and all contents
                Files.walk(dirPath)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                        }
                    });
            }
        }

        if (GENERATE_JAVA_CLASSES) {
            Path javaDir = outputBase.resolve("../java/com/hecookin/adastramekanized/client/dimension");
            if (Files.exists(javaDir)) {
                System.out.println("  Removing Java dimension effects: " + javaDir);
                Files.walk(javaDir)
                    .filter(path -> path.toString().endsWith("DimensionEffects.java"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                        }
                    });
            }
        }

        System.out.println("Cleanup complete.");
    }

    /**
     * Create necessary output directories
     */
    private void createDirectories() throws IOException {
        String[] directories = {
            "data/adastramekanized/planets",
            "data/adastramekanized/dimension_type",
            "data/adastramekanized/dimension",
            "data/adastramekanized/worldgen/biome",
            "data/adastramekanized/worldgen/noise_settings"
        };

        for (String dir : directories) {
            Files.createDirectories(outputBase.resolve(dir));
        }

        if (GENERATE_JAVA_CLASSES) {
            Files.createDirectories(outputBase.resolve("../java/com/hecookin/adastramekanized/client/dimension"));
        }
    }

    // ========================================================================================
    // FILE GENERATION METHODS - These will be implemented to create actual files
    // ========================================================================================

    private PlanetProperties generatePlanetProperties(DimensionEffectsType type, PlanetRandomizer randomizer) {
        DimensionEffectsType.PhysicalProperties defaults = type.getDefaultPhysics();

        // Generate gravity with variation
        float gravity = randomizer.randomFloat(
            Math.max(0.05f, defaults.gravity() - 0.3f),
            Math.min(12.0f, defaults.gravity() + 0.8f)
        );

        // Extreme outliers (5% chance)
        if (randomizer.randomBoolean(0.05f)) {
            gravity = randomizer.randomFloat(0.01f, 15.0f);
        }

        // Temperature: -273 to 2000 Kelvin
        float temperature = randomizer.randomFloat(
            Math.max(-273.0f, defaults.temperature() - 50.0f),
            defaults.temperature() + 100.0f
        );

        // Day length: 0.5 to 2000 hours with variation
        float dayLength = randomizer.randomFloat(
            Math.max(0.5f, defaults.dayLength() * 0.3f),
            defaults.dayLength() * 3.0f
        );

        // Orbit distance: 50 to 5000 million km
        int orbitDistance = randomizer.randomInt(50, 5000);

        // Rings and moons
        boolean hasRings = randomizer.randomBoolean(0.15f); // 15% chance
        int moonCount = randomizer.randomInt(0, Math.min(5, type == DimensionEffectsType.GAS_GIANT ? 8 : 3));

        return new PlanetProperties(gravity, temperature, dayLength, orbitDistance, hasRings, moonCount);
    }

    private AtmosphereData generateAtmosphereData(DimensionEffectsType type, PlanetRandomizer randomizer) {
        DimensionEffectsType.AtmosphericProperties defaults = type.getDefaultAtmosphere();

        boolean hasAtmosphere = defaults.hasAtmosphere();
        boolean breathable = defaults.breathable();

        // Add some variation to breathable atmospheres (rare)
        if (hasAtmosphere && !breathable && randomizer.randomBoolean(0.02f)) {
            breathable = true; // 2% chance of finding breathable atmosphere
        }

        float pressure = defaults.pressure();
        if (hasAtmosphere) {
            // Vary pressure ±50%
            pressure *= randomizer.randomFloat(0.5f, 1.5f);
        }

        float oxygenLevel = defaults.oxygenLevel();
        if (hasAtmosphere) {
            // Vary oxygen ±30%
            oxygenLevel *= randomizer.randomFloat(0.7f, 1.3f);
            oxygenLevel = Math.max(0.0f, Math.min(1.0f, oxygenLevel));
        }

        String atmosphereType = hasAtmosphere ?
            (pressure > 1.5f ? "THICK" : pressure < 0.1f ? "THIN" : "NORMAL") : "NONE";

        return new AtmosphereData(hasAtmosphere, breathable, pressure, oxygenLevel, atmosphereType);
    }

    private CelestialData generateCelestialData(PlanetProperties properties, PlanetRandomizer randomizer) {
        // Generate sun
        float sunScale = randomizer.randomFloat(0.1f, 1.2f);
        sunScale *= randomizer.randomFloat(0.7f, 1.3f); // Add variation
        int sunColor = randomizer.randomInt(0xFFFFE0, 0xFFFFFF); // Light yellow to white

        // Generate moons with valid textures
        java.util.List<MoonData> moons = new java.util.ArrayList<>();
        String[] moonTextures = {
            "minecraft:textures/environment/moon_phases.png",
            "minecraft:textures/environment/sun.png", // Smaller celestial body
            "minecraft:textures/block/gray_concrete.png", // Gray moon-like
            "minecraft:textures/block/light_gray_concrete.png", // Light gray variant
            "minecraft:textures/block/white_concrete.png" // Bright moon
        };

        for (int i = 0; i < properties.moonCount(); i++) {
            String texture = moonTextures[randomizer.randomInt(0, moonTextures.length - 1)];
            float scale = randomizer.randomFloat(0.1f, 0.5f);
            int color = randomizer.randomInt(0x808080, 0xF0F0F0); // Gray to white range
            float horizontalPos = randomizer.randomFloat(-1.0f, 1.0f);
            float verticalPos = randomizer.randomFloat(-0.5f, 1.5f);

            moons.add(new MoonData(texture, scale, color, horizontalPos, verticalPos));
        }

        // Generate visible planets with valid textures (40% chance for more variety)
        java.util.List<PlanetData> visiblePlanets = new java.util.ArrayList<>();
        if (randomizer.randomBoolean(0.4f)) {
            String[] planetTextures = {
                "minecraft:textures/block/blue_terracotta.png", // Blue planet
                "minecraft:textures/block/red_terracotta.png", // Red planet
                "minecraft:textures/block/orange_terracotta.png", // Orange planet
                "minecraft:textures/block/green_terracotta.png", // Green planet
                "minecraft:textures/block/purple_terracotta.png", // Purple planet
                "minecraft:textures/block/brown_terracotta.png", // Brown planet
                "minecraft:textures/block/yellow_terracotta.png", // Yellow planet
                "minecraft:textures/environment/sun.png" // Distant star
            };

            int count = randomizer.randomInt(1, 4); // Up to 4 visible celestial bodies
            for (int i = 0; i < count; i++) {
                String texture = planetTextures[randomizer.randomInt(0, planetTextures.length - 1)];
                float scale = randomizer.randomFloat(0.1f, 0.6f); // Smaller for distance

                // More diverse color ranges based on texture type
                int color = switch (texture) {
                    case "minecraft:textures/block/blue_terracotta.png" -> randomizer.randomInt(0x4169E1, 0x87CEEB);
                    case "minecraft:textures/block/red_terracotta.png" -> randomizer.randomInt(0xFF4500, 0xFF6347);
                    case "minecraft:textures/block/orange_terracotta.png" -> randomizer.randomInt(0xFF8C00, 0xFFA500);
                    case "minecraft:textures/block/green_terracotta.png" -> randomizer.randomInt(0x228B22, 0x32CD32);
                    case "minecraft:textures/block/purple_terracotta.png" -> randomizer.randomInt(0x8B008B, 0xDA70D6);
                    case "minecraft:textures/environment/sun.png" -> randomizer.randomInt(0xFFFFE0, 0xFFFFFF);
                    default -> randomizer.randomInt(0x8B4513, 0xD2691E);
                };

                float horizontalPos = randomizer.randomFloat(-1.2f, 1.2f);
                float verticalPos = randomizer.randomFloat(0.1f, 1.4f);

                visiblePlanets.add(new PlanetData(texture, scale, color, horizontalPos, verticalPos));
            }
        }

        return new CelestialData(sunScale, sunColor, moons, visiblePlanets);
    }

    private void createPlanetJson(String planetId, String planetName, DimensionEffectsType type,
                                 PlanetProperties properties, AtmosphereData atmosphere,
                                 CelestialData celestialData, PlanetRandomizer randomizer) throws IOException {
        System.out.println("    Creating planet JSON: " + planetId + ".json");

        // Generate colors based on type
        int skyColor = generateSkyColor(type, randomizer);
        int fogColor = generateFogColor(type, randomizer);
        int sunriseColor = randomizer.randomInt(0xFF4500, 0xFFFFC1); // Orange to light yellow

        // Create JSON content
        String json = String.format("""
            {
              "id": "adastramekanized:%s",
              "display_name": "%s",
              "properties": {
                "gravity": %f,
                "temperature": %f,
                "day_length": %f,
                "orbit_distance": %d,
                "has_rings": %s,
                "moon_count": %d
              },
              "atmosphere": {
                "has_atmosphere": %s,
                "breathable": %s,
                "pressure": %f,
                "oxygen_level": %f,
                "type": "%s"
              },
              "dimension": {
                "dimension_type": "adastramekanized:%s",
                "biome_source": "adastramekanized:%s_biome_source",
                "chunk_generator": "adastramekanized:%s_generator",
                "is_orbital": false,
                "sky_color": %d,
                "fog_color": %d,
                "ambient_light": 0.1
              },
              "rendering": {
                "sky": {
                  "sky_color": %d,
                  "sunrise_color": %d,
                  "custom_sky": true,
                  "has_stars": true,
                  "star_count": %d,
                  "star_brightness": %f,
                  "star_visibility": "%s"
                },
                "fog": {
                  "fog_color": %d,
                  "has_fog": true,
                  "fog_density": %f,
                  "near_plane": %f,
                  "far_plane": %f
                },
                "celestial_bodies": {
                  "sun": {
                    "texture": "minecraft:textures/environment/sun.png",
                    "scale": %f,
                    "color": %d,
                    "visible": true
                  },
                  "moons": [%s],
                  "visible_planets": [%s]
                },
                "weather": {
                  "has_clouds": %s,
                  "has_rain": %s,
                  "has_snow": %s,
                  "has_storms": %s,
                  "rain_acidity": 0.0
                },
                "particles": {
                  "has_dust": %s,
                  "has_ash": %s,
                  "has_spores": %s,
                  "has_snowfall": %s,
                  "particle_density": %f,
                  "particle_color": %d
                }
              }
            }""",
            planetId, planetName,
            properties.gravity(), properties.temperature(), properties.dayLength(),
            properties.orbitDistance(), properties.hasRings(), properties.moonCount(),
            atmosphere.hasAtmosphere(), atmosphere.breathable(), atmosphere.pressure(),
            atmosphere.oxygenLevel(), atmosphere.atmosphereType(),
            planetId, planetId, planetId,
            skyColor, fogColor,
            skyColor, sunriseColor,
            randomizer.randomInt(5000, 25000),
            randomizer.randomFloat(0.2f, 1.0f),
            atmosphere.hasAtmosphere() ? "night_only" : "constant",
            fogColor,
            randomizer.randomFloat(0.01f, 0.8f),
            randomizer.randomFloat(16.0f, 32.0f),
            randomizer.randomFloat(128.0f, 256.0f),
            celestialData.sunScale(), celestialData.sunColor(),
            generateMoonsJson(celestialData.moons()),
            generatePlanetsJson(celestialData.visiblePlanets()),
            generateHasClouds(type),
            atmosphere.hasAtmosphere() && randomizer.randomBoolean(0.3f),
            properties.temperature() < 0 && randomizer.randomBoolean(0.4f),
            randomizer.randomBoolean(0.2f),
            randomizer.randomBoolean(0.7f),
            type == DimensionEffectsType.VOLCANIC,
            false, // spores are rare
            type == DimensionEffectsType.ICE_WORLD,
            randomizer.randomFloat(0.0f, 1.0f),
            fogColor
        );

        Path planetFile = outputBase.resolve("data/adastramekanized/planets/" + planetId + ".json");
        Files.writeString(planetFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String generateMoonsJson(java.util.List<MoonData> moons) {
        if (moons.isEmpty()) return "";

        return moons.stream()
            .map(moon -> String.format("""
                {
                  "texture": "%s",
                  "scale": %f,
                  "color": %d,
                  "horizontal_position": %f,
                  "vertical_position": %f,
                  "moves_with_time": true,
                  "visible": true
                }""", moon.texture(), moon.scale(), moon.color(), moon.horizontalPos(), moon.verticalPos()))
            .reduce((a, b) -> a + ",\n" + b)
            .orElse("");
    }

    private String generatePlanetsJson(java.util.List<PlanetData> planets) {
        if (planets.isEmpty()) return "";

        return planets.stream()
            .map(planet -> String.format("""
                {
                  "texture": "%s",
                  "scale": %f,
                  "color": %d,
                  "horizontal_position": %f,
                  "vertical_position": %f,
                  "moves_with_time": true,
                  "visible": true
                }""", planet.texture(), planet.scale(), planet.color(), planet.horizontalPos(), planet.verticalPos()))
            .reduce((a, b) -> a + ",\n" + b)
            .orElse("");
    }

    private void createDimensionTypeJson(String planetId, DimensionEffectsType type) throws IOException {
        System.out.println("    Creating dimension type: " + planetId + ".json");

        String json = String.format("""
            {
              "ultrawarm": false,
              "natural": false,
              "coordinate_scale": 1.0,
              "has_skylight": true,
              "has_ceiling": false,
              "ambient_light": 0.1,
              "monster_spawn_light_level": 0,
              "monster_spawn_block_light_limit": 0,
              "min_y": -64,
              "height": 384,
              "logical_height": 384,
              "infiniburn": "#minecraft:infiniburn_overworld",
              "effects": "adastramekanized:%s",
              "respawn_anchor_works": false,
              "has_raids": false,
              "bed_works": false,
              "piglin_safe": false
            }""", planetId);

        Path dimensionTypeFile = outputBase.resolve("data/adastramekanized/dimension_type/" + planetId + ".json");
        Files.writeString(dimensionTypeFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void createDimensionJson(String planetId) throws IOException {
        System.out.println("    Creating dimension: " + planetId + ".json");

        String json = String.format("""
            {
              "type": "adastramekanized:%s",
              "generator": {
                "type": "minecraft:noise",
                "biome_source": {
                  "type": "minecraft:fixed",
                  "biome": "adastramekanized:%s_plains"
                },
                "settings": "adastramekanized:%s"
              }
            }""", planetId, planetId, planetId);

        Path dimensionFile = outputBase.resolve("data/adastramekanized/dimension/" + planetId + ".json");
        Files.writeString(dimensionFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void createBiomeJson(String planetId, DimensionEffectsType type, PlanetRandomizer randomizer) throws IOException {
        System.out.println("    Creating biome: " + planetId + "_plains.json");

        // Use existing biome as template based on type
        String templateBiome = switch (type) {
            case ICE_WORLD -> "minecraft:frozen_ocean";
            case VOLCANIC -> "minecraft:basalt_deltas";
            case GAS_GIANT -> "minecraft:mushroom_fields";
            case MOON_LIKE, ASTEROID_LIKE -> "minecraft:desert";
            default -> "minecraft:plains";
        };

        // Generate more diverse biome properties
        boolean hasPrecipitation = switch (type) {
            case ICE_WORLD -> true; // Snow
            case ALTERED_OVERWORLD -> randomizer.randomBoolean(0.7f);
            case GAS_GIANT -> randomizer.randomBoolean(0.3f); // Rare atmospheric precipitation
            default -> false; // Most planets are dry
        };

        float temperature = switch (type) {
            case ICE_WORLD -> randomizer.randomFloat(-0.5f, 0.2f);
            case VOLCANIC -> randomizer.randomFloat(1.5f, 2.0f);
            case GAS_GIANT -> randomizer.randomFloat(0.3f, 1.2f);
            case ALTERED_OVERWORLD -> randomizer.randomFloat(0.2f, 0.9f);
            default -> randomizer.randomFloat(0.1f, 0.8f);
        };

        float downfall = hasPrecipitation ? randomizer.randomFloat(0.1f, 0.9f) : 0.0f;

        // Generate diverse water colors
        int waterColor = switch (type) {
            case ICE_WORLD -> randomizer.randomInt(3750570, 8429567); // Blue ice range
            case VOLCANIC -> randomizer.randomInt(16711680, 16733440); // Red/orange lava colors
            case GAS_GIANT -> randomizer.randomInt(8388736, 16776960); // Purple/yellow gas colors
            case ALTERED_OVERWORLD -> randomizer.randomInt(4159204, 6591981); // Earth-like blues
            default -> randomizer.randomInt(2829099, 9474192); // Varied alien water colors
        };

        int waterFogColor = switch (type) {
            case ICE_WORLD -> randomizer.randomInt(329011, 2105376);
            case VOLCANIC -> randomizer.randomInt(8388608, 16711680);
            case GAS_GIANT -> randomizer.randomInt(4194304, 12582912);
            default -> waterColor; // Use same as water color for simplicity
        };

        String json = String.format("""
            {
              "has_precipitation": %s,
              "temperature": %f,
              "downfall": %f,
              "effects": {
                "fog_color": %d,
                "water_color": %d,
                "water_fog_color": %d,
                "sky_color": %d,
                "grass_color": %d,
                "foliage_color": %d
              },
              "spawners": {},
              "spawn_costs": {},
              "carvers": {},
              "features": []
            }""",
            hasPrecipitation,
            temperature,
            downfall,
            generateFogColor(type, randomizer),
            waterColor,
            waterFogColor,
            generateSkyColor(type, randomizer),
            generateGrassColor(type, randomizer),
            generateFoliageColor(type, randomizer)
        );

        Path biomeFile = outputBase.resolve("data/adastramekanized/worldgen/biome/" + planetId + "_plains.json");
        Files.writeString(biomeFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void createNoiseSettingsJson(String planetId, DimensionEffectsType type, PlanetRandomizer randomizer) throws IOException {
        System.out.println("    Creating noise settings: " + planetId + ".json");

        // Generate planet-specific noise parameters for unique terrain
        float noiseScaleX = randomizer.randomFloat(0.5f, 2.0f);
        float noiseScaleZ = randomizer.randomFloat(0.5f, 2.0f);
        float noiseScaleY = randomizer.randomFloat(0.8f, 1.5f);
        float terrainAmplitude = randomizer.randomFloat(0.3f, 1.5f);
        float heightVariation = randomizer.randomFloat(-0.8f, 0.8f);
        float densityOffset = randomizer.randomFloat(-0.2f, 0.3f);

        // Planet-specific terrain characteristics
        float terrainRoughness = switch (type) {
            case VOLCANIC -> randomizer.randomFloat(1.2f, 2.5f); // Very rough
            case ASTEROID_LIKE -> randomizer.randomFloat(0.8f, 1.8f); // Moderately rough
            case ROCKY -> randomizer.randomFloat(0.6f, 1.4f); // Varied terrain
            case ICE_WORLD -> randomizer.randomFloat(0.4f, 1.0f); // Smoother
            case GAS_GIANT -> randomizer.randomFloat(0.2f, 0.6f); // Very smooth
            default -> randomizer.randomFloat(0.5f, 1.2f); // Moderate
        };

        String json = String.format("""
            {
              "sea_level": %d,
              "disable_mob_generation": false,
              "aquifers_enabled": %s,
              "ore_veins_enabled": false,
              "legacy_random_source": false,
              "default_block": {
                "Name": "%s"
              },
              "default_fluid": {
                "Name": "%s"
              },
              "noise": {
                "min_y": -64,
                "height": 384,
                "size_horizontal": %d,
                "size_vertical": %d
              },
              "noise_router": {
                "barrier": {
                  "type": "minecraft:noise",
                  "noise": "minecraft:aquifer_barrier",
                  "xz_scale": %f,
                  "y_scale": %f
                },
                "fluid_level_floodedness": {
                  "type": "minecraft:noise",
                  "noise": "minecraft:aquifer_fluid_level_floodedness",
                  "xz_scale": %f,
                  "y_scale": %f
                },
                "fluid_level_spread": {
                  "type": "minecraft:noise",
                  "noise": "minecraft:aquifer_fluid_level_spread",
                  "xz_scale": %f,
                  "y_scale": %f
                },
                "lava": {
                  "type": "minecraft:noise",
                  "noise": "minecraft:aquifer_lava",
                  "xz_scale": %f,
                  "y_scale": %f
                },
                "temperature": {
                  "type": "minecraft:shifted_noise",
                  "noise": "minecraft:temperature",
                  "xz_scale": %f,
                  "y_scale": 0,
                  "shift_x": "minecraft:shift_x",
                  "shift_y": 0,
                  "shift_z": "minecraft:shift_z"
                },
                "vegetation": {
                  "type": "minecraft:shifted_noise",
                  "noise": "minecraft:vegetation",
                  "xz_scale": %f,
                  "y_scale": 0,
                  "shift_x": "minecraft:shift_x",
                  "shift_y": 0,
                  "shift_z": "minecraft:shift_z"
                },
                "continents": {
                  "type": "minecraft:noise",
                  "noise": "minecraft:continentalness",
                  "xz_scale": %f,
                  "y_scale": 0
                },
                "erosion": {
                  "type": "minecraft:noise",
                  "noise": "minecraft:erosion",
                  "xz_scale": %f,
                  "y_scale": 0
                },
                "depth": {
                  "type": "minecraft:add",
                  "argument1": "minecraft:y",
                  "argument2": {
                    "type": "minecraft:constant",
                    "argument": %f
                  }
                },
                "ridges": {
                  "type": "minecraft:noise",
                  "noise": "minecraft:ridge",
                  "xz_scale": %f,
                  "y_scale": 0
                },
                "initial_density_without_jaggedness": {
                  "type": "minecraft:mul",
                  "argument1": %f,
                  "argument2": {
                    "type": "minecraft:quarter_negative",
                    "argument": {
                      "type": "minecraft:mul",
                      "argument1": {
                        "type": "minecraft:add",
                        "argument1": "minecraft:y",
                        "argument2": {
                          "type": "minecraft:constant",
                          "argument": %f
                        }
                      },
                      "argument2": {
                        "type": "minecraft:cache_2d",
                        "argument": {
                          "type": "minecraft:noise",
                          "noise": "minecraft:jagged",
                          "xz_scale": %f,
                          "y_scale": 0
                        }
                      }
                    }
                  }
                },
                "final_density": {
                  "type": "minecraft:interpolated",
                  "argument": {
                    "type": "minecraft:blend_density",
                    "argument": {
                      "type": "minecraft:add",
                      "argument1": {
                        "type": "minecraft:mul",
                        "argument1": {
                          "type": "minecraft:y_clamped_gradient",
                          "from_y": 296,
                          "to_y": 320,
                          "from_value": 1,
                          "to_value": 0
                        },
                        "argument2": {
                          "type": "minecraft:add",
                          "argument1": {
                            "type": "minecraft:noise",
                            "noise": "minecraft:cave_cheese",
                            "xz_scale": %f,
                            "y_scale": %f
                          },
                          "argument2": %f
                        }
                      },
                      "argument2": %f
                    }
                  }
                },
                "vein_toggle": 0,
                "vein_ridged": 0,
                "vein_gap": 0
              },
              "spawn_target": [],
              "surface_rule": {
                "type": "minecraft:sequence",
                "sequence": [
                  {
                    "type": "minecraft:condition",
                    "if_true": {
                      "type": "minecraft:vertical_gradient",
                      "random_name": "minecraft:bedrock_floor",
                      "true_at_and_below": {
                        "above_bottom": 0
                      },
                      "false_at_and_above": {
                        "above_bottom": 5
                      }
                    },
                    "then_run": {
                      "type": "minecraft:block",
                      "result_state": {
                        "Name": "minecraft:bedrock"
                      }
                    }
                  }
                ]
              }
            }""",
            // sea_level - vary by planet type
            switch (type) {
                case GAS_GIANT -> randomizer.randomInt(50, 80);
                case ICE_WORLD -> randomizer.randomInt(55, 70);
                case VOLCANIC -> randomizer.randomInt(60, 75);
                default -> randomizer.randomInt(58, 68);
            },
            // aquifers_enabled
            switch (type) {
                case GAS_GIANT, ALTERED_OVERWORLD -> "true";
                default -> "false";
            },
            // default_block
            switch (type) {
                case ICE_WORLD -> "minecraft:ice";
                case VOLCANIC -> "minecraft:basalt";
                case GAS_GIANT -> "minecraft:orange_terracotta";
                case MOON_LIKE, ASTEROID_LIKE -> "minecraft:gray_concrete";
                default -> "minecraft:stone";
            },
            // default_fluid
            switch (type) {
                case MOON_LIKE, ASTEROID_LIKE -> "minecraft:air";
                case GAS_GIANT -> "minecraft:water";
                default -> "minecraft:air";
            },
            // size_horizontal, size_vertical
            randomizer.randomInt(1, 3), randomizer.randomInt(1, 3),
            // barrier noise scales
            noiseScaleX, noiseScaleY * 0.5f,
            // fluid level scales
            noiseScaleX * 0.8f, noiseScaleY * 0.67f,
            noiseScaleX * 0.9f, noiseScaleY * 0.71f,
            // lava scales
            noiseScaleX * 1.1f, noiseScaleY,
            // temperature scale
            0.25f * noiseScaleX,
            // vegetation scale
            0.25f * noiseScaleX,
            // continents scale
            noiseScaleX * 0.7f,
            // erosion scale
            noiseScaleX * 0.8f,
            // depth offset
            heightVariation,
            // ridges scale
            noiseScaleX * terrainRoughness,
            // terrain amplitude
            4.0f * terrainAmplitude,
            // initial density offset
            heightVariation * 0.8f,
            // jagged noise scale
            noiseScaleX * terrainRoughness * 0.6f,
            // final density cave noise scales
            noiseScaleX * 0.5f, noiseScaleY * 0.67f,
            // final density offsets
            10.0f + densityOffset * 20.0f, -10.0f + densityOffset * 10.0f
        );

        Path noiseFile = outputBase.resolve("data/adastramekanized/worldgen/noise_settings/" + planetId + ".json");
        Files.writeString(noiseFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void createDimensionEffectsClass(String planetId, String planetName, DimensionEffectsType type) throws IOException {
        System.out.println("    Creating dimension effects class: " + capitalizeId(planetId) + "DimensionEffects.java");

        String className = capitalizeId(planetId) + "DimensionEffects";
        String templateClass = getTemplateClassName(type);

        String javaCode = String.format("""
            package com.hecookin.adastramekanized.client.dimension;

            import com.hecookin.adastramekanized.client.dimension.%s;

            /**
             * Dimension effects for %s (%s)
             * Generated automatically by PlanetGenerationTool
             */
            public class %s extends %s {

                public %s() {
                    super();
                }

                @Override
                public String getPlanetName() {
                    return "%s";
                }
            }""",
            templateClass, planetName, planetId,
            className, templateClass,
            className, planetName
        );

        Path javaFile = outputBase.resolve("../java/com/hecookin/adastramekanized/client/dimension/" + className + ".java");
        Files.writeString(javaFile, javaCode, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String getTemplateClassName(DimensionEffectsType type) {
        return switch (type) {
            case MOON_LIKE -> "MoonLikeDimensionEffects";
            case ROCKY -> "RockyDimensionEffects";
            case ICE_WORLD -> "IceWorldDimensionEffects";
            case VOLCANIC -> "VolcanicDimensionEffects";
            case GAS_GIANT -> "GasGiantDimensionEffects";
            case ASTEROID_LIKE -> "MoonLikeDimensionEffects"; // Use moon-like for asteroids
            case ALTERED_OVERWORLD -> "RockyDimensionEffects"; // Use rocky for altered overworld
        };
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String capitalizeId(String id) {
        return java.util.Arrays.stream(id.split("_"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
            .reduce("", String::concat);
    }

    // ========================================================================================
    // COLOR GENERATION METHODS
    // ========================================================================================

    private int generateSkyColor(DimensionEffectsType type, PlanetRandomizer randomizer) {
        return switch (type) {
            case MOON_LIKE, ASTEROID_LIKE -> 0x000000; // Black space
            case ROCKY -> randomizer.randomInt(0xEC9A69, 0xFFC477); // Mars-like orange/tan
            case ICE_WORLD -> randomizer.randomInt(0xB0E0E6, 0xE0F6FF); // Light blue/white
            case VOLCANIC -> randomizer.randomInt(0x8B0000, 0xFF4500); // Dark red to orange red
            case GAS_GIANT -> randomizer.randomInt(0x4169E1, 0x9370DB); // Blue to purple
            case ALTERED_OVERWORLD -> randomizer.randomInt(0x78A7FF, 0x87CEEB); // Sky blue
        };
    }

    private int generateFogColor(DimensionEffectsType type, PlanetRandomizer randomizer) {
        return switch (type) {
            case MOON_LIKE, ASTEROID_LIKE -> 0x000000; // No atmosphere, black
            case ROCKY -> randomizer.randomInt(0xD2691E, 0xDEB887); // Sandy brown
            case ICE_WORLD -> randomizer.randomInt(0xF0F8FF, 0xFFFFFF); // Light blue to white
            case VOLCANIC -> randomizer.randomInt(0x696969, 0xA52A2A); // Gray to brown (ash)
            case GAS_GIANT -> randomizer.randomInt(0x8B008B, 0xDA70D6); // Purple/magenta
            case ALTERED_OVERWORLD -> randomizer.randomInt(0xF0F8FF, 0xFFFFFF); // Light colors
        };
    }

    private boolean generateHasClouds(DimensionEffectsType type) {
        return switch (type) {
            case MOON_LIKE, ASTEROID_LIKE -> false; // No atmosphere
            case ROCKY -> false; // Thin atmosphere like Mars
            case ICE_WORLD -> true; // Can have ice clouds
            case VOLCANIC -> true; // Ash clouds
            case GAS_GIANT -> true; // Dense atmosphere
            case ALTERED_OVERWORLD -> true; // Earth-like
        };
    }

    private int generateGrassColor(DimensionEffectsType type, PlanetRandomizer randomizer) {
        return switch (type) {
            case ICE_WORLD -> randomizer.randomInt(0xB0E0E6, 0xFFFFFF); // Light blue to white
            case VOLCANIC -> randomizer.randomInt(0x8B0000, 0xFF4500); // Dark red to orange red
            case GAS_GIANT -> randomizer.randomInt(0x9370DB, 0xDDA0DD); // Medium orchid to plum
            case ALTERED_OVERWORLD -> randomizer.randomInt(0x228B22, 0x90EE90); // Forest green to light green
            case ROCKY -> randomizer.randomInt(0xD2691E, 0xF4A460); // Chocolate to sandy brown
            default -> randomizer.randomInt(0x808080, 0xC0C0C0); // Gray to silver
        };
    }

    private int generateFoliageColor(DimensionEffectsType type, PlanetRandomizer randomizer) {
        return switch (type) {
            case ICE_WORLD -> randomizer.randomInt(0xF0F8FF, 0xFFFFFF); // Alice blue to white
            case VOLCANIC -> randomizer.randomInt(0x696969, 0xFF6347); // Dim gray to tomato
            case GAS_GIANT -> randomizer.randomInt(0x8B008B, 0xDDA0DD); // Dark magenta to plum
            case ALTERED_OVERWORLD -> randomizer.randomInt(0x228B22, 0x90EE90); // Forest green to light green
            case ROCKY -> randomizer.randomInt(0xBC8F8F, 0xF4A460); // Rosy brown to sandy brown
            default -> randomizer.randomInt(0x696969, 0x909090); // Dim gray to gray
        };
    }

    // ========================================================================================
    // DATA CLASSES
    // ========================================================================================

    public record PlanetProperties(float gravity, float temperature, float dayLength,
                                  int orbitDistance, boolean hasRings, int moonCount) {}

    public record AtmosphereData(boolean hasAtmosphere, boolean breathable, float pressure,
                                float oxygenLevel, String atmosphereType) {}

    public record CelestialData(float sunScale, int sunColor, java.util.List<MoonData> moons, java.util.List<PlanetData> visiblePlanets) {}

    public record MoonData(String texture, float scale, int color, float horizontalPos, float verticalPos) {}

    public record PlanetData(String texture, float scale, int color, float horizontalPos, float verticalPos) {}
}