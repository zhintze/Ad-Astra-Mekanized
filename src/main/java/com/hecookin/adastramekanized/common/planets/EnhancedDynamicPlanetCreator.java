package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.performance.PerformanceMonitor;
import com.hecookin.adastramekanized.common.dimensions.DimensionJsonGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Enhanced planet creation system that generates complete planets with templates,
 * celestial configurations, and all associated data files dynamically during gameplay.
 */
public class EnhancedDynamicPlanetCreator {

    private static final EnhancedDynamicPlanetCreator INSTANCE = new EnhancedDynamicPlanetCreator();
    private MinecraftServer server;
    private boolean initialized = false;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Planet name generators for procedural creation
    private static final String[] NAME_PREFIXES = {
        "Kepler", "Gliese", "HD", "TOI", "K2", "TRAPPIST", "Proxima", "Wolf", "Ross", "LHS",
        "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta", "Nova", "Stellar"
    };

    private static final String[] NAME_SUFFIXES = {
        "b", "c", "d", "e", "f", "g", "h", "Prime", "Alpha", "Beta", "Major", "Minor"
    };

    private EnhancedDynamicPlanetCreator() {
        // Private constructor for singleton
    }

    public static EnhancedDynamicPlanetCreator getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the planet creator with server instance
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        this.initialized = true;
        AdAstraMekanized.LOGGER.info("EnhancedDynamicPlanetCreator initialized");
    }

    /**
     * Create a new planet with specified templates and configurations
     */
    public DynamicPlanetData createPlanet(String planetName, DimensionEffectsType effectsType, CelestialType celestialType) {
        if (!initialized) {
            throw new IllegalStateException("EnhancedDynamicPlanetCreator not initialized");
        }

        try (var timer = PerformanceMonitor.getInstance().startOperation("planet_creation")) {
            AdAstraMekanized.LOGGER.info("Creating dynamic planet: {} (Type: {}, Celestial: {})",
                planetName, effectsType, celestialType);

            // Check registry limits
            DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
            if (!registry.canCreateMorePlanets()) {
                throw new RuntimeException("Cannot create more planets: limit of " +
                    DynamicPlanetRegistry.MAX_PLANETS + " reached");
            }

            // Generate unique planet ID and data
            DynamicPlanetData planetData = generatePlanetData(planetName, effectsType, celestialType);

            // Generate and write planet JSON to world datapack
            Path planetJsonPath = generatePlanetJson(planetData);
            planetData.setPlanetJsonFile(planetJsonPath);
            planetData.setJsonFileExists(true);

            // CRITICAL FIX: Generate all required dimension infrastructure files
            AdAstraMekanized.LOGGER.info("Generating dimension infrastructure files for planet: {}", planetName);
            DimensionJsonGenerator dimensionGenerator = new DimensionJsonGenerator(server);
            boolean dimensionFilesGenerated = dimensionGenerator.generateDimensionFiles(planetData);

            if (!dimensionFilesGenerated) {
                throw new RuntimeException("Failed to generate dimension infrastructure files for planet: " + planetName);
            }

            AdAstraMekanized.LOGGER.info("Successfully generated dimension infrastructure for planet: {}", planetName);

            // Register with the dynamic planet registry
            boolean registered = registry.registerPlanet(planetData);
            if (!registered) {
                throw new RuntimeException("Failed to register planet with DynamicPlanetRegistry");
            }

            AdAstraMekanized.LOGGER.info("Successfully created planet: {} at {}", planetName, planetJsonPath);
            return planetData;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to create planet: {}", planetName, e);
            throw new RuntimeException("Planet creation failed", e);
        }
    }

    /**
     * Create a planet with random templates (for procedural generation)
     */
    public DynamicPlanetData createRandomPlanet() {
        String randomName = generateRandomPlanetName();
        DimensionEffectsType randomEffects = DimensionEffectsType.getRandomType();
        CelestialType randomCelestial = CelestialType.getRandomType();

        return createPlanet(randomName, randomEffects, randomCelestial);
    }

    /**
     * Create multiple planets as a system
     */
    public List<DynamicPlanetData> createPlanetSystem(String systemName, int planetCount) {
        List<DynamicPlanetData> system = new ArrayList<>();

        for (int i = 0; i < planetCount; i++) {
            String planetName = systemName + " " + NAME_SUFFIXES[i % NAME_SUFFIXES.length];
            DimensionEffectsType effectsType = DimensionEffectsType.getRandomType();
            CelestialType celestialType = CelestialType.getRandomType();

            try {
                DynamicPlanetData planet = createPlanet(planetName, effectsType, celestialType);
                system.add(planet);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to create planet {} in system {}", planetName, systemName, e);
            }
        }

        AdAstraMekanized.LOGGER.info("Created planet system '{}' with {} planets", systemName, system.size());
        return system;
    }

    /**
     * Generate complete planet data from templates
     */
    private DynamicPlanetData generatePlanetData(String planetName, DimensionEffectsType effectsType, CelestialType celestialType) {
        // Generate unique planet ID
        DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
        ResourceLocation planetId = registry.generateNextPlanetId();

        // Get template properties
        DimensionEffectsType.PhysicalProperties physics = effectsType.getDefaultPhysics();
        DimensionEffectsType.AtmosphericProperties atmosphere = effectsType.getDefaultAtmosphere();

        // Add some variation to template properties
        float gravityVariation = 1.0f + (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.4f; // ±20% variation
        float tempVariation = 1.0f + (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.6f; // ±30% variation
        float dayLengthVariation = 1.0f + (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.8f; // ±40% variation

        float gravity = physics.gravity() * gravityVariation;
        float temperature = physics.temperature() * tempVariation;
        float dayLength = physics.dayLength() * dayLengthVariation;
        int orbitDistance = generateOrbitDistance();

        return new DynamicPlanetData(
            planetId,
            planetName,
            effectsType,
            celestialType,
            gravity,
            temperature,
            dayLength,
            orbitDistance,
            atmosphere.hasAtmosphere(),
            atmosphere.breathable(),
            atmosphere.pressure()
        );
    }

    /**
     * Generate planet JSON file with complete celestial configuration
     */
    private Path generatePlanetJson(DynamicPlanetData planetData) throws IOException {
        // Get world datapack directory with organized structure
        Path worldDatapackPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR);

        // Create organized directory structure (groups of 20 planets)
        int planetNumber = extractPlanetNumber(planetData.getPlanetId());
        int groupNumber = (planetNumber / 20) + 1;

        Path planetDataPath = worldDatapackPath
            .resolve("data")
            .resolve(AdAstraMekanized.MOD_ID)
            .resolve("planets")
            .resolve(String.format("group_%02d", groupNumber));

        // Ensure directory exists
        Files.createDirectories(planetDataPath);

        // Generate JSON file path
        String fileName = planetData.getPlanetId().getPath() + ".json";
        Path planetJsonPath = planetDataPath.resolve(fileName);

        // Generate complete planet JSON
        JsonObject planetJson = createCompletePlanetJson(planetData);

        // Write JSON file with pretty formatting
        String jsonString = gson.toJson(planetJson);
        Files.writeString(planetJsonPath, jsonString);

        AdAstraMekanized.LOGGER.debug("Generated planet JSON: {}", planetJsonPath);
        return planetJsonPath;
    }

    /**
     * Create complete planet JSON using templates
     */
    private JsonObject createCompletePlanetJson(DynamicPlanetData planetData) {
        JsonObject planetJson = new JsonObject();

        // Basic planet information
        planetJson.addProperty("id", planetData.getPlanetId().toString());
        planetJson.addProperty("display_name", planetData.getDisplayName());

        // Planet properties
        JsonObject properties = new JsonObject();
        properties.addProperty("gravity", planetData.getGravity());
        properties.addProperty("temperature", planetData.getTemperature());
        properties.addProperty("day_length", planetData.getDayLength());
        properties.addProperty("orbit_distance", planetData.getOrbitDistance());
        properties.addProperty("has_rings", false); // Could be randomized later
        properties.addProperty("moon_count", calculateMoonCount(planetData.getCelestialType()));
        planetJson.add("properties", properties);

        // Atmosphere configuration from template
        JsonObject atmosphere = new JsonObject();
        atmosphere.addProperty("has_atmosphere", planetData.hasAtmosphere());
        atmosphere.addProperty("breathable", planetData.isBreathable());
        atmosphere.addProperty("pressure", planetData.getAtmospherePressure());
        atmosphere.addProperty("oxygen_level", planetData.isBreathable() ? 0.21f : 0.0f);
        atmosphere.addProperty("type", planetData.hasAtmosphere() ? "THIN" : "NONE");
        planetJson.add("atmosphere", atmosphere);

        // Dimension configuration using template
        JsonObject dimension = new JsonObject();
        dimension.addProperty("dimension_type", planetData.getEffectsType().getResourceLocation().toString());
        dimension.addProperty("biome_source", planetData.getEffectsType().getId() + "_biome_source");
        dimension.addProperty("chunk_generator", planetData.getEffectsType().getId() + "_generator");
        dimension.addProperty("is_orbital", false);
        dimension.addProperty("sky_color", getTemplateSkyColor(planetData.getEffectsType()));
        dimension.addProperty("fog_color", getTemplateFogColor(planetData.getEffectsType()));
        dimension.addProperty("ambient_light", getTemplateAmbientLight(planetData.getEffectsType()));
        planetJson.add("dimension", dimension);

        // Rendering configuration using both templates
        JsonObject rendering = createTemplateRenderingConfiguration(planetData);
        planetJson.add("rendering", rendering);

        return planetJson;
    }

    /**
     * Create rendering configuration using celestial and dimension templates
     */
    private JsonObject createTemplateRenderingConfiguration(DynamicPlanetData planetData) {
        JsonObject rendering = new JsonObject();

        // Sky configuration from celestial template
        JsonObject sky = planetData.getCelestialType().generateSkyConfig(planetData.getEffectsType());
        rendering.add("sky", sky);

        // Fog configuration from dimension template
        JsonObject fog = new JsonObject();
        fog.addProperty("fog_color", getTemplateFogColor(planetData.getEffectsType()));
        fog.addProperty("has_fog", shouldHaveFog(planetData.getEffectsType()));
        fog.addProperty("fog_density", getTemplateFogDensity(planetData.getEffectsType()));
        fog.addProperty("near_plane", 16.0);
        fog.addProperty("far_plane", getTemplateFarPlane(planetData.getEffectsType()));
        rendering.add("fog", fog);

        // Celestial bodies from celestial template
        JsonObject celestialBodies = planetData.getCelestialType().generateCelestialBodies();
        rendering.add("celestial_bodies", celestialBodies);

        // Weather configuration from dimension template
        JsonObject weather = createTemplateWeatherConfig(planetData.getEffectsType());
        rendering.add("weather", weather);

        // Particles configuration from dimension template
        JsonObject particles = createTemplateParticleConfig(planetData.getEffectsType());
        rendering.add("particles", particles);

        return rendering;
    }

    // Template property helper methods

    private int getTemplateSkyColor(DimensionEffectsType type) {
        return switch (type) {
            case MOON_LIKE -> 0; // Black space
            case ASTEROID_LIKE -> 0; // Black space like moon
            case ROCKY -> 15510660; // Rust-colored
            case GAS_GIANT -> 8421631; // Purple-blue
            case ICE_WORLD -> 11657279; // Pale blue-white
            case VOLCANIC -> 6619136; // Dark red-orange
            case ALTERED_OVERWORLD -> 7907327; // Earth-like sky blue
        };
    }

    private int getTemplateFogColor(DimensionEffectsType type) {
        return switch (type) {
            case MOON_LIKE -> 1447444; // Space gray
            case ASTEROID_LIKE -> 2631720; // Darker space gray
            case ROCKY -> 13791774; // Rust fog
            case GAS_GIANT -> 6684774; // Dense atmospheric
            case ICE_WORLD -> 12632319; // Ice crystal fog
            case VOLCANIC -> 3932160; // Sulfurous ash
            case ALTERED_OVERWORLD -> 12638463; // Earth-like atmospheric fog
        };
    }

    private float getTemplateAmbientLight(DimensionEffectsType type) {
        return switch (type) {
            case MOON_LIKE -> 0.1f; // Very dark space
            case ASTEROID_LIKE -> 0.0f; // Completely dark space
            case ROCKY -> 0.8f; // Mars-like
            case GAS_GIANT -> 0.3f; // Thick atmosphere blocks light
            case ICE_WORLD -> 0.9f; // Ice reflects light well
            case VOLCANIC -> 0.7f; // Volcanic glow
            case ALTERED_OVERWORLD -> 1.0f; // Earth-like normal lighting
        };
    }

    private boolean shouldHaveFog(DimensionEffectsType type) {
        return type != DimensionEffectsType.MOON_LIKE; // Only airless worlds have no fog
    }

    private float getTemplateFogDensity(DimensionEffectsType type) {
        return switch (type) {
            case MOON_LIKE -> 0.0f; // No atmosphere = no fog
            case ASTEROID_LIKE -> 0.0f; // No atmosphere = no fog
            case ROCKY -> 0.4f; // Dust storms
            case GAS_GIANT -> 0.8f; // Very thick atmosphere
            case ICE_WORLD -> 0.3f; // Ice crystal haze
            case VOLCANIC -> 0.7f; // Ash and sulfur
            case ALTERED_OVERWORLD -> 0.1f; // Earth-like minimal atmospheric haze
        };
    }

    private float getTemplateFarPlane(DimensionEffectsType type) {
        return switch (type) {
            case MOON_LIKE -> 256.0f; // Clear space view
            case ASTEROID_LIKE -> 256.0f; // Clear space view like moon
            case ROCKY -> 192.0f; // Moderate visibility
            case GAS_GIANT -> 96.0f; // Limited by thick atmosphere
            case ICE_WORLD -> 200.0f; // Clear but cold
            case VOLCANIC -> 128.0f; // Limited by ash
            case ALTERED_OVERWORLD -> 256.0f; // Earth-like clear visibility
        };
    }

    private JsonObject createTemplateWeatherConfig(DimensionEffectsType type) {
        JsonObject weather = new JsonObject();
        weather.addProperty("has_clouds", type == DimensionEffectsType.GAS_GIANT);
        weather.addProperty("has_rain", false); // No liquid water rain on alien worlds
        weather.addProperty("has_snow", type == DimensionEffectsType.ICE_WORLD);
        weather.addProperty("has_storms", type != DimensionEffectsType.MOON_LIKE);
        weather.addProperty("rain_acidity", type == DimensionEffectsType.VOLCANIC ? 1.0f : 0.0f);
        return weather;
    }

    private JsonObject createTemplateParticleConfig(DimensionEffectsType type) {
        JsonObject particles = new JsonObject();
        particles.addProperty("has_dust", type == DimensionEffectsType.ROCKY || type == DimensionEffectsType.MOON_LIKE);
        particles.addProperty("has_ash", type == DimensionEffectsType.VOLCANIC);
        particles.addProperty("has_spores", false); // No organic particles on these worlds
        particles.addProperty("has_snowfall", type == DimensionEffectsType.ICE_WORLD);

        float density = switch (type) {
            case MOON_LIKE -> 0.0f; // No atmosphere
            case ASTEROID_LIKE -> 0.0f; // No atmosphere like moon
            case ROCKY -> 0.6f; // Dust
            case GAS_GIANT -> 0.4f; // Atmospheric particles
            case ICE_WORLD -> 0.3f; // Ice crystals
            case VOLCANIC -> 0.8f; // Heavy ash
            case ALTERED_OVERWORLD -> 0.1f; // Minimal Earth-like particles
        };
        particles.addProperty("particle_density", density);
        particles.addProperty("particle_color", getTemplateFogColor(type)); // Match fog color
        return particles;
    }

    // Helper methods

    private int calculateMoonCount(CelestialType celestialType) {
        return switch (celestialType) {
            case TWO_MOONS -> 2;
            case RING_SYSTEM -> 3;
            default -> 0;
        };
    }

    private int generateOrbitDistance() {
        // Generate realistic orbit distances (in millions of km)
        return ThreadLocalRandom.current().nextInt(50, 1000);
    }

    private int extractPlanetNumber(ResourceLocation planetId) {
        String path = planetId.getPath();
        try {
            // Extract number from planet_XXX format
            String numberPart = path.substring(path.lastIndexOf('_') + 1);
            return Integer.parseInt(numberPart);
        } catch (Exception e) {
            return 1; // Default to group 1 if parsing fails
        }
    }

    private String generateRandomPlanetName() {
        String prefix = NAME_PREFIXES[ThreadLocalRandom.current().nextInt(NAME_PREFIXES.length)];
        String number = String.valueOf(ThreadLocalRandom.current().nextInt(100, 9999));
        String suffix = NAME_SUFFIXES[ThreadLocalRandom.current().nextInt(NAME_SUFFIXES.length)];

        return prefix + "-" + number + suffix;
    }

    /**
     * Create planet with specific template combination for testing
     */
    public DynamicPlanetData createTemplatePlanet(String name, DimensionEffectsType effects, CelestialType celestial) {
        return createPlanet(name, effects, celestial);
    }

    /**
     * Create Earth-like planet
     */
    public DynamicPlanetData createEarthLikePlanet(String name) {
        return createPlanet(name, DimensionEffectsType.ROCKY, CelestialType.SINGLE_SUN);
    }

    /**
     * Create Moon-like planet
     */
    public DynamicPlanetData createMoonLikePlanet(String name) {
        return createPlanet(name, DimensionEffectsType.MOON_LIKE, CelestialType.SUN_AND_EARTH);
    }

    /**
     * Create Mars-like planet
     */
    public DynamicPlanetData createMarsLikePlanet(String name) {
        return createPlanet(name, DimensionEffectsType.ROCKY, CelestialType.TWO_MOONS);
    }

    /**
     * Check if creator is ready to create planets
     */
    public boolean isReady() {
        return initialized && server != null;
    }

    /**
     * Get creation statistics
     */
    public CreatorStats getStats() {
        if (!initialized) {
            return new CreatorStats(0, 0, 0, false);
        }

        DynamicPlanetRegistry registry = DynamicPlanetRegistry.get(server);
        DynamicPlanetRegistry.RegistryStats registryStats = registry.getStats();

        return new CreatorStats(
            registryStats.totalPlanets(),
            registryStats.activePlanets(),
            registryStats.loadedDimensions(),
            isReady()
        );
    }

    /**
     * Creator statistics record
     */
    public record CreatorStats(
        int totalPlanetsCreated,
        int activePlanets,
        int loadedDimensions,
        boolean ready
    ) {}
}