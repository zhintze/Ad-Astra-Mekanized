package com.hecookin.adastramekanized.common.planets;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
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
                        planet.dimension()
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
        // Create a basic Moon planet for testing
        createDefaultMoon();

        AdAstraMekanized.LOGGER.info("Created {} default planets", 1);
    }

    /**
     * Create the default Moon planet for testing
     */
    private void createDefaultMoon() {
        ResourceLocation moonId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "moon");

        Planet.PlanetProperties properties = new Planet.PlanetProperties(
                0.16f,              // Low gravity (16% of Earth)
                -170.0f,            // Cold temperature
                708.0f,             // Long day (about 29.5 Earth days)
                384,                // Distance from Earth in thousands of km
                false,              // No rings
                0                   // No moons of its own
        );

        Planet.AtmosphereData atmosphere = new Planet.AtmosphereData(
                false,                          // No atmosphere
                false,                          // Not breathable
                0.0f,                          // No pressure
                0.0f,                          // No oxygen
                Planet.AtmosphereType.NONE     // Vacuum
        );

        Planet.DimensionSettings dimension = new Planet.DimensionSettings(
                ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),      // Use overworld type for now
                ResourceLocation.fromNamespaceAndPath("minecraft", "fixed"),          // Fixed biome source
                ResourceLocation.fromNamespaceAndPath("minecraft", "flat"),           // Flat chunk generator
                false,              // Not orbital
                0x000000,          // Black sky
                0x1a1a1a,          // Dark gray fog
                0.1f               // Low ambient light
        );

        Planet moon = new Planet(moonId, "Moon", properties, atmosphere, dimension);

        if (registry.registerPlanet(moon)) {
            AdAstraMekanized.LOGGER.info("Created default Moon planet");
        } else {
            AdAstraMekanized.LOGGER.error("Failed to register default Moon planet");
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