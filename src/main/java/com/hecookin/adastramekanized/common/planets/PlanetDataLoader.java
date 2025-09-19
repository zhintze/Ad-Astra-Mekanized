package com.hecookin.adastramekanized.common.planets;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings;
import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * Handles loading planet data from JSON files in data packs.
 *
 * Loads planet definitions from data/[namespace]/planets/[planet_name].json
 * and registers them with the planet registry.
 */
public class PlanetDataLoader {

    private static final String PLANETS_DIRECTORY = "planets";

    private final MinecraftServer server;
    private final PlanetRegistry registry;
    private final ResourceManager resourceManager;

    public PlanetDataLoader(MinecraftServer server) {
        this.server = server;
        this.registry = PlanetRegistry.getInstance();
        this.resourceManager = server.getResourceManager();
    }

    /**
     * Load all planet definitions from data packs
     */
    public void loadAllPlanets() {
        AdAstraMekanized.LOGGER.info("Loading planet definitions from data packs...");

        int loadedCount = 0;
        int errorCount = 0;

        // Find all planet JSON files in data packs
        Map<ResourceLocation, Resource> planetResources = resourceManager.listResources(
                PLANETS_DIRECTORY, location -> location.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : planetResources.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            Resource resource = entry.getValue();

            try {
                // Extract planet ID from resource location
                // data/namespace/planets/planet_name.json -> namespace:planet_name
                ResourceLocation planetId = extractPlanetId(resourceLocation);

                if (planetId == null) {
                    AdAstraMekanized.LOGGER.warn("Could not extract planet ID from resource: {}", resourceLocation);
                    errorCount++;
                    continue;
                }

                // Load and parse the planet data
                Planet planet = loadPlanetFromResource(planetId, resource);

                if (planet != null) {
                    if (registry.registerPlanet(planet)) {
                        loadedCount++;
                        AdAstraMekanized.LOGGER.debug("Loaded planet: {} from {}", planetId, resourceLocation);
                    } else {
                        AdAstraMekanized.LOGGER.warn("Failed to register planet: {} (validation failed or duplicate)", planetId);
                        errorCount++;
                    }
                } else {
                    errorCount++;
                }

            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Error loading planet from {}: {}", resourceLocation, e.getMessage(), e);
                errorCount++;
            }
        }

        AdAstraMekanized.LOGGER.info("Planet loading complete: {} loaded, {} errors", loadedCount, errorCount);

        // Load default planets if no planets were loaded from data packs
        if (loadedCount == 0) {
            AdAstraMekanized.LOGGER.info("No planets loaded from data packs, creating default planets");
            createDefaultPlanets();
        }
    }

    /**
     * Load a single planet from a resource
     */
    private Planet loadPlanetFromResource(ResourceLocation planetId, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            // Parse JSON
            JsonElement jsonElement = GsonHelper.parse(reader);

            // Use Codec to deserialize
            var result = Planet.CODEC.parse(JsonOps.INSTANCE, jsonElement);

            if (result.error().isPresent()) {
                AdAstraMekanized.LOGGER.error("Failed to parse planet {}: {}",
                        planetId, result.error().get().message());
                return null;
            }

            Planet planet = result.result().orElse(null);
            if (planet == null) {
                AdAstraMekanized.LOGGER.error("Planet codec returned null for: {}", planetId);
                return null;
            }

            // Ensure the planet ID matches the file location
            if (!planet.id().equals(planetId)) {
                AdAstraMekanized.LOGGER.warn("Planet ID mismatch: file says {}, data says {}. Using file location.",
                        planetId, planet.id());

                // Create new planet with correct ID
                planet = new Planet(
                        planetId,
                        planet.displayName(),
                        planet.properties(),
                        planet.atmosphere(),
                        planet.dimension(),
                        planet.generation(),
                        planet.rendering()
                );
            }

            // Validate the planet
            if (!planet.isValid()) {
                AdAstraMekanized.LOGGER.error("Planet {} failed validation", planetId);
                return null;
            }

            return planet;

        } catch (IOException e) {
            AdAstraMekanized.LOGGER.error("Failed to read planet file for {}: {}", planetId, e.getMessage());
            return null;
        } catch (JsonParseException e) {
            AdAstraMekanized.LOGGER.error("Failed to parse JSON for planet {}: {}", planetId, e.getMessage());
            return null;
        }
    }

    /**
     * Extract planet ID from resource location
     * data/namespace/planets/planet_name.json -> namespace:planet_name
     */
    private ResourceLocation extractPlanetId(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();

        // Remove "planets/" prefix and ".json" suffix
        if (!path.startsWith(PLANETS_DIRECTORY + "/") || !path.endsWith(".json")) {
            return null;
        }

        String planetName = path.substring(PLANETS_DIRECTORY.length() + 1, path.length() - 5);

        // Validate planet name
        if (planetName.isEmpty() || !ResourceLocation.isValidPath(planetName)) {
            return null;
        }

        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), planetName);
    }

    /**
     * Create default planets when no data pack planets are found
     */
    private void createDefaultPlanets() {
        // Create all default planets with accurate Ad Astra settings
        createDefaultMoon();
        createDefaultMars();
        createDefaultVenus();
        createDefaultMercury();
        createDefaultGlacio();

        AdAstraMekanized.LOGGER.info("Created {} default planets", 5);
    }

    /**
     * Create the default Moon planet with accurate Ad Astra settings
     */
    private void createDefaultMoon() {
        ResourceLocation moonId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "moon");

        // Ad Astra Moon: Temperature -173°C, Gravity 1.622 m/s², No oxygen, Solar power 24, Tier 1
        Planet.PlanetProperties properties = new Planet.PlanetProperties(
                1.622f / 9.81f,    // Gravity: 1.622 m/s² (16.5% of Earth gravity)
                -173.0f,           // Temperature: -173°C (Ad Astra value)
                708.0f,            // Long day (about 29.5 Earth days)
                384,               // Distance from Earth in thousands of km
                false,             // No rings
                0                  // No moons of its own
        );

        Planet.AtmosphereData atmosphere = new Planet.AtmosphereData(
                false,                          // No atmosphere
                false,                          // Not breathable
                0.0f,                          // No pressure
                0.0f,                          // No oxygen
                Planet.AtmosphereType.NONE     // Vacuum
        );

        // Ad Astra Moon: Sky color 0x000000 (black), Fog color 0x161614 (very dark gray)
        Planet.DimensionSettings dimension = new Planet.DimensionSettings(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "moon_type"),
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "moon_biome_source"),
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "moon_generator"),
                false,              // Not orbital
                0x000000,          // Black sky (Ad Astra value)
                0x161614,          // Very dark gray fog (Ad Astra value)
                0.1f               // Low ambient light
        );

        // Create atmospheric rendering based on Ad Astra Moon settings
        AtmosphericRendering rendering = AtmosphericRendering.createAirless(0x000000, 0x161614);

        Planet moon = new Planet(moonId, "Moon", properties, atmosphere, dimension,
            null, rendering); // TODO: Add proper PlanetGenerationSettings

        if (registry.registerPlanet(moon)) {
            AdAstraMekanized.LOGGER.info("Created default Moon planet with Ad Astra settings");
        } else {
            AdAstraMekanized.LOGGER.error("Failed to register default Moon planet");
        }
    }

    /**
     * Create the default Mars planet with accurate Ad Astra settings
     */
    private void createDefaultMars() {
        ResourceLocation marsId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "mars");

        // Ad Astra Mars: Temperature -65°C, Gravity 3.72076 m/s², No oxygen, Solar power 12, Tier 2
        Planet.PlanetProperties properties = new Planet.PlanetProperties(
                3.72076f / 9.81f,  // Gravity: 3.72076 m/s² (38% of Earth gravity)
                -65.0f,            // Temperature: -65°C (Ad Astra value)
                24.6f,             // Mars day (24.6 hours)
                225,               // Distance from Earth in millions of km
                false,             // No rings
                2                  // Two moons: Phobos and Deimos
        );

        Planet.AtmosphereData atmosphere = new Planet.AtmosphereData(
                false,                          // No breathable atmosphere
                false,                          // Not breathable
                0.01f,                         // Very thin atmosphere
                0.0f,                          // No oxygen
                Planet.AtmosphereType.THIN     // Thin CO2 atmosphere
        );

        // Ad Astra Mars: Sky color 0xe6ac84 (reddish-tan), Fog color 0xe6ac84
        Planet.DimensionSettings dimension = new Planet.DimensionSettings(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "mars_type"),
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "mars_biome_source"),
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "mars_generator"),
                false,              // Not orbital
                0xe6ac84,          // Reddish-tan sky (Ad Astra value)
                0xe6ac84,          // Reddish-tan fog (Ad Astra value)
                0.6f               // Moderate ambient light
        );

        // Create atmospheric rendering based on Ad Astra Mars settings
        AtmosphericRendering rendering = AtmosphericRendering.createMarsLike(0xe6ac84, 0xd2691e);

        Planet mars = new Planet(marsId, "Mars", properties, atmosphere, dimension,
            null, rendering); // TODO: Add proper PlanetGenerationSettings

        if (registry.registerPlanet(mars)) {
            AdAstraMekanized.LOGGER.info("Created default Mars planet with Ad Astra settings");
        } else {
            AdAstraMekanized.LOGGER.error("Failed to register default Mars planet");
        }
    }

    /**
     * Create the default Venus planet with accurate Ad Astra settings
     */
    private void createDefaultVenus() {
        ResourceLocation venusId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "venus");

        // Ad Astra Venus: Temperature 464°C, Gravity 8.87 m/s², No oxygen, Solar power 8, Tier 3
        Planet.PlanetProperties properties = new Planet.PlanetProperties(
                8.87f / 9.81f,     // Gravity: 8.87 m/s² (90% of Earth gravity)
                464.0f,            // Temperature: 464°C (Ad Astra value) - extremely hot
                243.0f,            // Venus day (243 Earth days, retrograde)
                41,                // Distance from Earth in millions of km (closest approach)
                false,             // No rings
                0                  // No moons
        );

        Planet.AtmosphereData atmosphere = new Planet.AtmosphereData(
                false,                          // Toxic atmosphere
                false,                          // Not breathable
                90.0f,                         // Extremely dense atmosphere (90x Earth pressure)
                0.0f,                          // No oxygen (CO2 and sulfuric acid)
                Planet.AtmosphereType.THICK    // Very thick atmosphere
        );

        // Ad Astra Venus: Sky color 0xd18b52 (warm orange-brown), Fog color 0xd18b52
        Planet.DimensionSettings dimension = new Planet.DimensionSettings(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "venus_type"),
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "venus_biome_source"),
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "venus_generator"),
                false,              // Not orbital
                0xd18b52,          // Warm orange-brown sky (Ad Astra value)
                0xd18b52,          // Warm orange-brown fog (Ad Astra value)
                0.3f               // Dim light due to thick atmosphere
        );

        // TODO: Integrate Ad Astra Venus atmospheric rendering:
        // - Sunrise color: 0xf9c21a (bright yellow-orange)
        // - Custom sky: true, Has thick fog: true, Has fog: true
        // - Sunrise angle: 180° (sun rises in west - retrograde rotation)
        // - Stars: 1500
        // - Sun texture: red_sun.png with scale 14
        // - Acid rain in Venus biomes (3 damage per tick)
        // - Temperature: 1.6f (very hot), Downfall: 1 (high humidity)
        // - Has precipitation: true (acid rain particles)

        // Create atmospheric rendering based on Ad Astra Venus settings
        AtmosphericRendering rendering = AtmosphericRendering.createVenusLike(0xd18b52, 0xcc7722);

        Planet venus = new Planet(venusId, "Venus", properties, atmosphere, dimension,
            null, rendering); // TODO: Add proper PlanetGenerationSettings

        if (registry.registerPlanet(venus)) {
            AdAstraMekanized.LOGGER.info("Created default Venus planet with Ad Astra settings");
        } else {
            AdAstraMekanized.LOGGER.error("Failed to register default Venus planet");
        }
    }

    /**
     * Create the default Mercury planet with accurate Ad Astra settings
     */
    private void createDefaultMercury() {
        ResourceLocation mercuryId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "mercury");

        // Ad Astra Mercury: Temperature 167°C, Gravity 3.7 m/s², No oxygen, Solar power 64, Tier 3
        Planet.PlanetProperties properties = new Planet.PlanetProperties(
                3.7f / 9.81f,      // Gravity: 3.7 m/s² (38% of Earth gravity)
                167.0f,            // Temperature: 167°C (Ad Astra value) - very hot
                58.6f,             // Mercury day (58.6 Earth days)
                77,                // Distance from Earth in millions of km (closest approach)
                false,             // No rings
                0                  // No moons
        );

        Planet.AtmosphereData atmosphere = new Planet.AtmosphereData(
                false,                          // No atmosphere
                false,                          // Not breathable
                0.0f,                          // No pressure (exosphere only)
                0.0f,                          // No oxygen
                Planet.AtmosphereType.NONE     // Vacuum
        );

        // Ad Astra Mercury: Sky color 0x8b0000 (dark red), Fog color 0x000000 (black)
        Planet.DimensionSettings dimension = new Planet.DimensionSettings(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "mercury_type"),
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "mercury_biome_source"),
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "mercury_generator"),
                false,              // Not orbital
                0x8b0000,          // Dark red sky (Ad Astra value)
                0x000000,          // Black fog (Ad Astra value)
                0.9f               // Very bright due to proximity to sun
        );

        // TODO: Integrate Ad Astra Mercury atmospheric rendering:
        // - Sunrise color: 0xd63a0b (deep red-orange)
        // - Custom clouds: true, Custom sky: true, Has fog: true
        // - Stars: 9000 with brightness 0.6
        // - Sun texture: red_sun.png with scale 22 (very large sun)
        // - Temperature: 1.6f (very hot), Downfall: 0
        // - Extremely high solar power (64) due to proximity to sun

        // Create atmospheric rendering based on Ad Astra Mercury settings
        AtmosphericRendering rendering = AtmosphericRendering.createAirless(0x8b0000, 0x654321);

        Planet mercury = new Planet(mercuryId, "Mercury", properties, atmosphere, dimension,
            null, rendering); // TODO: Add proper PlanetGenerationSettings

        if (registry.registerPlanet(mercury)) {
            AdAstraMekanized.LOGGER.info("Created default Mercury planet with Ad Astra settings");
        } else {
            AdAstraMekanized.LOGGER.error("Failed to register default Mercury planet");
        }
    }

    /**
     * Create the default Glacio planet with accurate Ad Astra settings
     */
    private void createDefaultGlacio() {
        ResourceLocation glacioId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "glacio");

        // Ad Astra Glacio: Temperature -20°C, Gravity 3.721 m/s², Has oxygen (true), Solar power 14, Tier 4
        Planet.PlanetProperties properties = new Planet.PlanetProperties(
                3.721f / 9.81f,    // Gravity: 3.721 m/s² (38% of Earth gravity)
                -20.0f,            // Temperature: -20°C (Ad Astra value) - cold but habitable
                24.0f,             // Earth-like day
                1000,              // Distance in light-years (Proxima Centauri system)
                false,             // No rings
                0                  // No moons
        );

        Planet.AtmosphereData atmosphere = new Planet.AtmosphereData(
                true,                           // Has oxygen atmosphere
                true,                           // Breathable
                1.0f,                          // Earth-like pressure
                21.0f,                         // Earth-like oxygen content (21%)
                Planet.AtmosphereType.NORMAL // Breathable atmosphere
        );

        // Ad Astra Glacio: Sky color 0xc0d8ff (light blue), Fog color 0xc0d8ff
        Planet.DimensionSettings dimension = new Planet.DimensionSettings(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "glacio_type"),
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "glacio_biome_source"),
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "glacio_generator"),
                false,              // Not orbital
                0xc0d8ff,          // Light blue sky (Ad Astra value)
                0xc0d8ff,          // Light blue fog (Ad Astra value)
                0.8f               // Normal light levels
        );

        // TODO: Integrate Ad Astra Glacio atmospheric rendering:
        // - Sunrise color: 0xd85f33 (default orange-red)
        // - Custom sky: true, Has fog: true
        // - Stars: 1500
        // - Sun texture: sun.png with scale 9
        // - Vicinus star visible with scale 50 (distant star system)
        // - Temperature: -0.7f (very cold), Downfall: 1 (snowy)
        // - Has precipitation: true (snow/ice)
        // - Located in Proxima Centauri solar system

        // Create atmospheric rendering based on Ad Astra Glacio settings
        AtmosphericRendering rendering = AtmosphericRendering.createIcePlanet(0xc0d8ff, 0xa0c0e0);

        Planet glacio = new Planet(glacioId, "Glacio", properties, atmosphere, dimension,
            null, rendering); // TODO: Add proper PlanetGenerationSettings

        if (registry.registerPlanet(glacio)) {
            AdAstraMekanized.LOGGER.info("Created default Glacio planet with Ad Astra settings");
        } else {
            AdAstraMekanized.LOGGER.error("Failed to register default Glacio planet");
        }
    }

    /**
     * Reload planet data from resources
     */
    public void reload() {
        AdAstraMekanized.LOGGER.info("Reloading planet data...");
        registry.clearAll();
        loadAllPlanets();
    }

    /**
     * Check if a specific planet file exists in resources
     */
    public boolean planetFileExists(ResourceLocation planetId) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(
                planetId.getNamespace(),
                PLANETS_DIRECTORY + "/" + planetId.getPath() + ".json"
        );

        return resourceManager.getResource(resourceLocation).isPresent();
    }

    /**
     * Get the resource location for a planet's data file
     */
    public ResourceLocation getPlanetResourceLocation(ResourceLocation planetId) {
        return ResourceLocation.fromNamespaceAndPath(
                planetId.getNamespace(),
                PLANETS_DIRECTORY + "/" + planetId.getPath() + ".json"
        );
    }
}