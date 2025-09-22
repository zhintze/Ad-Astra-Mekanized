package com.hecookin.adastramekanized.common.planets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.api.planets.Planet.AtmosphereData;
import com.hecookin.adastramekanized.api.planets.Planet.AtmosphereType;
import com.hecookin.adastramekanized.api.planets.Planet.DimensionSettings;
import com.hecookin.adastramekanized.api.planets.Planet.PlanetProperties;
import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering;
import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering.StarVisibility;
import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering.CelestialBodies;
import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering.SunConfiguration;
import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering.MoonConfiguration;
import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering.PlanetConfiguration;
import com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings;
import com.hecookin.adastramekanized.common.dimensions.DimensionFileGenerator;
import com.hecookin.adastramekanized.common.dimensions.RuntimeDimensionRegistry;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Dynamic planet creation system allowing runtime planet generation with celestial bodies.
 *
 * Provides a fluent API for creating planets and their celestial configurations during gameplay,
 * automatically generating JSON files and registering with the planet system.
 */
public class DynamicPlanetCreator {

    private static final DynamicPlanetCreator INSTANCE = new DynamicPlanetCreator();

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private MinecraftServer server;
    private Path dataPackPath;

    private DynamicPlanetCreator() {
        // Private constructor for singleton
    }

    public static DynamicPlanetCreator getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the creator with server instance
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        this.dataPackPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR);

        try {
            Path planetsPath = getModDataPath().resolve("planets");
            Files.createDirectories(planetsPath);
            AdAstraMekanized.LOGGER.info("DynamicPlanetCreator initialized at: {}", planetsPath);
        } catch (IOException e) {
            AdAstraMekanized.LOGGER.error("Failed to initialize planet creator directories", e);
        }
    }

    /**
     * Start creating a new planet with the fluent builder API
     */
    public PlanetBuilder createPlanet(String planetId) {
        return new PlanetBuilder(ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, planetId));
    }

    /**
     * Fluent builder for creating planets dynamically
     */
    public class PlanetBuilder {
        private final ResourceLocation id;
        private String displayName;
        private PlanetProperties properties;
        private AtmosphereData atmosphere;
        private DimensionSettings dimension;
        private PlanetGenerationSettings generation;
        private AtmosphericRendering rendering;
        private List<MoonConfiguration> moons;
        private List<PlanetConfiguration> visiblePlanets;

        private PlanetBuilder(ResourceLocation id) {
            this.id = id;
            this.displayName = id.getPath();
            this.moons = new ArrayList<>();
            this.visiblePlanets = new ArrayList<>();

            // Set defaults
            this.properties = new PlanetProperties(1.0f, 15.0f, 24.0f, 150, false, 0);
            this.atmosphere = new AtmosphereData(true, true, 1.0f, 0.21f, AtmosphereType.NORMAL);
            this.dimension = new DimensionSettings(
                id, // Use planet ID as dimension type
                ResourceLocation.withDefaultNamespace("fixed"),
                ResourceLocation.withDefaultNamespace("noise"),
                false,
                0x78A7FF, // Default sky color
                0xC0D8FF, // Default fog color
                0.8f
            );
            this.generation = PlanetGenerationSettings.createEarthlike();
        }

        /**
         * Set planet display name
         */
        public PlanetBuilder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Set planet physical properties
         */
        public PlanetBuilder withProperties(float gravity, float temperature, float dayLength, boolean habitable) {
            this.properties = new PlanetProperties(gravity, temperature, dayLength, (int) (temperature + 100), false, 0);
            return this;
        }

        /**
         * Set atmospheric properties
         */
        public PlanetBuilder withAtmosphere(boolean hasAtmosphere, boolean breathable, boolean oxygenated) {
            AtmosphereType atmosphereType = hasAtmosphere ?
                (breathable ? AtmosphereType.NORMAL : AtmosphereType.TOXIC) :
                AtmosphereType.NONE;

            this.atmosphere = new AtmosphereData(
                hasAtmosphere,
                breathable,
                hasAtmosphere ? 1.0f : 0.0f,
                oxygenated ? 0.21f : 0.0f,
                atmosphereType
            );
            return this;
        }

        /**
         * Set dimension configuration
         */
        public PlanetBuilder withDimension(float ambientLight) {
            this.dimension = new DimensionSettings(
                id, // Use planet ID as dimension type
                ResourceLocation.withDefaultNamespace("fixed"),
                ResourceLocation.withDefaultNamespace("noise"),
                false,
                this.dimension.skyColor(),
                this.dimension.fogColor(),
                ambientLight
            );
            return this;
        }

        /**
         * Add a sun configuration
         */
        public PlanetBuilder addSun(float size, String texture) {
            // Will be handled in atmospheric rendering
            return this;
        }

        /**
         * Add Earth as a celestial body (for planets like Moon)
         */
        public PlanetBuilder addEarth(float size, float orbitRadius, float orbitSpeed) {
            PlanetConfiguration earth = new PlanetConfiguration(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "textures/gui/celestials/earth.png"),
                size,
                0xFFFFFF,
                0.0f, // horizontal position
                0.3f, // vertical position (above horizon)
                true, // moves with time
                true  // visible
            );
            this.visiblePlanets.add(earth);
            return this;
        }

        /**
         * Add a moon as a celestial body
         */
        public PlanetBuilder addMoon(String name, String texture, float size, float orbitRadius, float orbitSpeed) {
            MoonConfiguration moon = new MoonConfiguration(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, texture),
                size,
                0xFFFFFF,
                0.0f, // horizontal position
                0.2f, // vertical position
                true, // moves with time
                true  // visible
            );
            this.moons.add(moon);
            return this;
        }

        /**
         * Add standard Earth system celestial bodies (Sun + Earth + Moon)
         */
        public PlanetBuilder addEarthSystemBodies() {
            return addEarth(8.0f, 100.0f, 0.5f)
                  .addMoon("Moon", "textures/gui/celestials/moon.png", 4.0f, 50.0f, 2.0f);
        }

        /**
         * Add standard Mars system celestial bodies (Sun + Mars moons)
         */
        public PlanetBuilder addMarsSystemBodies() {
            return addMoon("Phobos", "textures/gui/celestials/phobos.png", 2.0f, 30.0f, 3.0f)
                  .addMoon("Deimos", "textures/gui/celestials/deimos.png", 1.5f, 50.0f, 1.5f);
        }

        /**
         * Build and register the planet
         */
        public CompletableFuture<Planet> build() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Create atmospheric rendering based on atmosphere type
                    AtmosphericRendering atmosphericRendering;

                    if (!atmosphere.hasAtmosphere()) {
                        atmosphericRendering = AtmosphericRendering.createAirless(0x000000, 0x000000);
                    } else if (atmosphere.type() == AtmosphereType.TOXIC) {
                        atmosphericRendering = AtmosphericRendering.createVenusLike(0x996633, 0x664422);
                    } else if (properties.temperature() < -50) {
                        atmosphericRendering = AtmosphericRendering.createIcePlanet(0xE6F3FF, 0xB0E0E6);
                    } else if (properties.temperature() > 100) {
                        atmosphericRendering = AtmosphericRendering.createMarsLike(0xD2691E, 0xD2691E);
                    } else {
                        atmosphericRendering = AtmosphericRendering.createDefault();
                    }

                    // Update celestial bodies
                    SunConfiguration sun = new SunConfiguration(Optional.empty(), 1.0f, 0xFFFFFF, true);
                    CelestialBodies celestialBodies = new CelestialBodies(sun, moons, visiblePlanets);

                    // Replace celestial bodies in rendering
                    atmosphericRendering = new AtmosphericRendering(
                        atmosphericRendering.sky(),
                        atmosphericRendering.fog(),
                        celestialBodies,
                        atmosphericRendering.weather(),
                        atmosphericRendering.particles()
                    );

                    // Create the planet object
                    Planet planet = new Planet(
                        id,
                        displayName,
                        properties,
                        atmosphere,
                        dimension,
                        generation,
                        atmosphericRendering
                    );

                    // Generate JSON file
                    boolean jsonCreated = createPlanetJsonFile(planet);
                    if (!jsonCreated) {
                        throw new RuntimeException("Failed to create planet JSON file");
                    }

                    // Generate dimension files
                    boolean dimensionCreated = DimensionFileGenerator.getInstance()
                                                .generateDimensionFiles(planet)
                                                .get();
                    if (!dimensionCreated) {
                        throw new RuntimeException("Failed to create dimension files");
                    }

                    // Register planet
                    PlanetRegistry registry = PlanetRegistry.getInstance();
                    registry.registerPlanet(planet);

                    // Immediately create the runtime dimension for hot-loading
                    if (server != null) {
                        RuntimeDimensionRegistry runtimeRegistry = RuntimeDimensionRegistry.getInstance();
                        runtimeRegistry.initialize(server);

                        ResourceKey<Level> dimensionKey = ResourceKey.create(
                            net.minecraft.core.registries.Registries.DIMENSION,
                            planet.id()
                        );

                        ServerLevel serverLevel = runtimeRegistry.registerAndCreateDimension(dimensionKey, planet);
                        if (serverLevel != null) {
                            AdAstraMekanized.LOGGER.info("Successfully created runtime dimension for new planet: {} at {}",
                                planet.displayName(), dimensionKey.location());
                        } else {
                            AdAstraMekanized.LOGGER.warn("Failed to create runtime dimension for new planet: {}", planet.displayName());
                        }
                    }

                    AdAstraMekanized.LOGGER.info("Successfully created and registered planet: {} with {} moons and {} visible planets",
                                               displayName, moons.size(), visiblePlanets.size());

                    return planet;

                } catch (Exception e) {
                    AdAstraMekanized.LOGGER.error("Failed to build planet: {}", displayName, e);
                    throw new RuntimeException("Failed to build planet: " + displayName, e);
                }
            });
        }
    }

    /**
     * Create planet JSON file from planet data
     */
    private boolean createPlanetJsonFile(Planet planet) {
        try {
            JsonObject planetJson = createPlanetJson(planet);
            Path filePath = getModDataPath()
                    .resolve("planets")
                    .resolve(planet.id().getPath() + ".json");

            writeJsonFile(filePath, planetJson);
            AdAstraMekanized.LOGGER.debug("Created planet JSON file: {}", filePath);
            return true;

        } catch (IOException e) {
            AdAstraMekanized.LOGGER.error("Failed to create planet JSON file for {}", planet.displayName(), e);
            return false;
        }
    }

    /**
     * Create JSON representation of planet (simplified for now)
     */
    private JsonObject createPlanetJson(Planet planet) {
        JsonObject json = new JsonObject();

        // Basic properties
        json.addProperty("displayName", planet.displayName());

        // Properties section
        JsonObject properties = new JsonObject();
        properties.addProperty("gravity", planet.properties().gravity());
        properties.addProperty("temperature", planet.properties().temperature());
        properties.addProperty("dayLength", planet.properties().dayLength());
        properties.addProperty("orbitDistance", planet.properties().orbitDistance());
        properties.addProperty("hasRings", planet.properties().hasRings());
        properties.addProperty("moonCount", planet.properties().moonCount());
        json.add("properties", properties);

        // Atmosphere section
        JsonObject atmosphere = new JsonObject();
        atmosphere.addProperty("hasAtmosphere", planet.atmosphere().hasAtmosphere());
        atmosphere.addProperty("breathable", planet.atmosphere().breathable());
        atmosphere.addProperty("pressure", planet.atmosphere().pressure());
        atmosphere.addProperty("oxygenLevel", planet.atmosphere().oxygenLevel());
        atmosphere.addProperty("type", planet.atmosphere().type().name().toLowerCase());
        json.add("atmosphere", atmosphere);

        // Dimension section
        JsonObject dimension = new JsonObject();
        dimension.addProperty("dimensionType", planet.dimension().dimensionType().toString());
        dimension.addProperty("biomeSource", planet.dimension().biomeSource().toString());
        dimension.addProperty("chunkGenerator", planet.dimension().chunkGenerator().toString());
        dimension.addProperty("isOrbital", planet.dimension().isOrbital());
        dimension.addProperty("skyColor", planet.dimension().skyColor());
        dimension.addProperty("fogColor", planet.dimension().fogColor());
        dimension.addProperty("ambientLight", planet.dimension().ambientLight());
        json.add("dimension", dimension);

        // Full rendering serialization with celestial bodies
        JsonObject rendering = new JsonObject();

        // Sky configuration
        JsonObject sky = new JsonObject();
        sky.addProperty("sky_color", planet.rendering().sky().skyColor());
        sky.addProperty("sunrise_color", planet.rendering().sky().sunriseColor());
        sky.addProperty("custom_sky", planet.rendering().sky().customSky());
        sky.addProperty("has_stars", planet.rendering().sky().hasStars());
        sky.addProperty("star_count", planet.rendering().sky().starCount());
        sky.addProperty("star_brightness", planet.rendering().sky().starBrightness());
        sky.addProperty("star_visibility", planet.rendering().sky().starVisibility().name().toLowerCase());
        rendering.add("sky", sky);

        // Fog configuration
        JsonObject fog = new JsonObject();
        fog.addProperty("fog_color", planet.rendering().fog().fogColor());
        fog.addProperty("has_fog", planet.rendering().fog().hasFog());
        fog.addProperty("fog_density", planet.rendering().fog().fogDensity());
        fog.addProperty("near_plane", planet.rendering().fog().nearPlane());
        fog.addProperty("far_plane", planet.rendering().fog().farPlane());
        rendering.add("fog", fog);

        // Celestial bodies configuration
        JsonObject celestialBodies = new JsonObject();

        // Sun configuration
        JsonObject sun = new JsonObject();
        var sunConfig = planet.rendering().celestialBodies().sun();
        sun.addProperty("texture", sunConfig.texture().map(ResourceLocation::toString).orElse("minecraft:textures/environment/sun.png"));
        sun.addProperty("scale", sunConfig.scale());
        sun.addProperty("color", sunConfig.color());
        sun.addProperty("visible", sunConfig.visible());
        celestialBodies.add("sun", sun);

        // Moons array
        JsonArray moonsArray = new JsonArray();
        for (var moon : planet.rendering().celestialBodies().moons()) {
            JsonObject moonObj = new JsonObject();
            moonObj.addProperty("texture", moon.texture().toString());
            moonObj.addProperty("scale", moon.scale());
            moonObj.addProperty("color", moon.color());
            moonObj.addProperty("horizontal_position", moon.horizontalPosition());
            moonObj.addProperty("vertical_position", moon.verticalPosition());
            moonObj.addProperty("moves_with_time", moon.movesWithTime());
            moonObj.addProperty("visible", moon.visible());
            moonsArray.add(moonObj);
        }
        celestialBodies.add("moons", moonsArray);

        // Visible planets array
        JsonArray planetsArray = new JsonArray();
        for (var visiblePlanet : planet.rendering().celestialBodies().visiblePlanets()) {
            JsonObject planetObj = new JsonObject();
            planetObj.addProperty("texture", visiblePlanet.texture().toString());
            planetObj.addProperty("scale", visiblePlanet.scale());
            planetObj.addProperty("color", visiblePlanet.color());
            planetObj.addProperty("horizontal_position", visiblePlanet.horizontalPosition());
            planetObj.addProperty("vertical_position", visiblePlanet.verticalPosition());
            planetObj.addProperty("moves_with_time", visiblePlanet.movesWithTime());
            planetObj.addProperty("visible", visiblePlanet.visible());
            planetsArray.add(planetObj);
        }
        celestialBodies.add("visible_planets", planetsArray);

        rendering.add("celestial_bodies", celestialBodies);

        // Weather configuration
        JsonObject weather = new JsonObject();
        weather.addProperty("has_clouds", planet.rendering().weather().hasClouds());
        weather.addProperty("has_rain", planet.rendering().weather().hasRain());
        weather.addProperty("has_snow", planet.rendering().weather().hasSnow());
        weather.addProperty("has_storms", planet.rendering().weather().hasStorms());
        weather.addProperty("rain_acidity", planet.rendering().weather().rainAcidity());
        rendering.add("weather", weather);

        // Particles configuration
        JsonObject particles = new JsonObject();
        particles.addProperty("has_dust", planet.rendering().particles().hasDust());
        particles.addProperty("has_ash", planet.rendering().particles().hasAsh());
        particles.addProperty("has_spores", planet.rendering().particles().hasSpores());
        particles.addProperty("has_snowfall", planet.rendering().particles().hasSnowfall());
        particles.addProperty("particle_density", planet.rendering().particles().particleDensity());
        particles.addProperty("particle_color", planet.rendering().particles().particleColor());
        rendering.add("particles", particles);

        json.add("rendering", rendering);

        return json;
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
     * Create a quick planet with minimal configuration
     */
    public CompletableFuture<Planet> createQuickPlanet(String planetId, String displayName,
                                                      float gravity, float temperature,
                                                      boolean hasAtmosphere) {
        PlanetBuilder builder = createPlanet(planetId)
                .withDisplayName(displayName)
                .withProperties(gravity, temperature, 24.0f, hasAtmosphere && temperature > -10 && temperature < 50)
                .withAtmosphere(hasAtmosphere, hasAtmosphere && temperature > 0, hasAtmosphere);

        // Add Earth if this is a moon-like body
        if (gravity < 0.3f) {
            builder.addEarth(12.0f, 80.0f, 0.3f);
        }

        return builder.build();
    }

    /**
     * Create multiple planets from a configuration list
     */
    public CompletableFuture<List<Planet>> createPlanetsFromConfig(List<PlanetConfig> configs) {
        return CompletableFuture.supplyAsync(() -> {
            List<Planet> createdPlanets = new ArrayList<>();

            for (PlanetConfig config : configs) {
                try {
                    Planet planet = createQuickPlanet(
                        config.id,
                        config.displayName,
                        config.gravity,
                        config.temperature,
                        config.hasAtmosphere
                    ).get();

                    createdPlanets.add(planet);
                } catch (Exception e) {
                    AdAstraMekanized.LOGGER.error("Failed to create planet from config: {}", config.id, e);
                }
            }

            AdAstraMekanized.LOGGER.info("Created {} planets from configuration", createdPlanets.size());
            return createdPlanets;
        });
    }

    /**
     * Configuration record for batch planet creation
     */
    public record PlanetConfig(
            String id,
            String displayName,
            float gravity,
            float temperature,
            boolean hasAtmosphere
    ) {}
}