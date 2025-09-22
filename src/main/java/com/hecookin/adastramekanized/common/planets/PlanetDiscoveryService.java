package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Modular service for discovering and registering all planets created by the mod.
 *
 * This service automatically scans for planet data files in the mod's datapack
 * and provides utilities for finding, filtering, and accessing planets dynamically.
 */
public class PlanetDiscoveryService {

    private static final String PLANET_DATA_PATH = "planets";
    private static final PlanetDiscoveryService INSTANCE = new PlanetDiscoveryService();

    private final Map<ResourceLocation, Planet> discoveredPlanets = new ConcurrentHashMap<>();
    private final Set<ResourceLocation> modPlanets = ConcurrentHashMap.newKeySet();
    private final Set<String> planetCategories = ConcurrentHashMap.newKeySet();
    private volatile boolean discoveryComplete = false;
    private MinecraftServer server;

    private PlanetDiscoveryService() {
        // Private constructor for singleton
    }

    public static PlanetDiscoveryService getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the discovery service with server instance
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        AdAstraMekanized.LOGGER.info("PlanetDiscoveryService initialized");
    }

    /**
     * Discover all planets from datapack resources
     */
    public void discoverAllPlanets() {
        if (server == null) {
            AdAstraMekanized.LOGGER.error("Cannot discover planets: server not initialized");
            return;
        }

        try {
            // Clear previous discoveries
            discoveredPlanets.clear();
            modPlanets.clear();
            planetCategories.clear();
            discoveryComplete = false;

            AdAstraMekanized.LOGGER.info("Starting planet discovery...");

            ResourceManager resourceManager = server.getResourceManager();
            int discoveredCount = 0;

            // Find all planet JSON files in data packs
            Map<ResourceLocation, Resource> planetResources = resourceManager.listResources(
                PLANET_DATA_PATH, resourceLocation -> resourceLocation.getPath().endsWith(".json"));

            AdAstraMekanized.LOGGER.info("Found {} potential planet resource files", planetResources.size());

            // Process each planet resource
            for (Map.Entry<ResourceLocation, Resource> entry : planetResources.entrySet()) {
                ResourceLocation resourceId = entry.getKey();
                Resource resource = entry.getValue();

                try {
                    if (processPlanetResource(resourceId, resource)) {
                        modPlanets.add(resourceId);
                        discoveredCount++;
                        AdAstraMekanized.LOGGER.debug("Discovered planet from: {}", resourceId);
                    }
                } catch (Exception e) {
                    AdAstraMekanized.LOGGER.error("Failed to process planet resource {}: {}", resourceId, e.getMessage());
                }
            }

            // Also discover planets from world datapack directory (for dynamically created planets)
            discoveredCount += discoverFromWorldDatapacks();

            // Register discovered planets with the registry
            registerDiscoveredPlanets();

            discoveryComplete = true;
            AdAstraMekanized.LOGGER.info("Planet discovery complete. Found {} planets in {} categories",
                discoveredCount, planetCategories.size());

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Planet discovery failed", e);
        }
    }

    /**
     * Process a single planet resource file
     */
    private boolean processPlanetResource(ResourceLocation resourceId, Resource resource) {
        // Only process resources from our mod
        if (!resourceId.getNamespace().equals(AdAstraMekanized.MOD_ID)) {
            return false;
        }

        try (BufferedReader reader = resource.openAsReader()) {
            JsonElement jsonElement = GsonHelper.parse(reader);

            if (!jsonElement.isJsonObject()) {
                AdAstraMekanized.LOGGER.warn("Planet resource {} is not a JSON object", resourceId);
                return false;
            }

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Parse the planet using the Planet codec
            var result = Planet.CODEC.parse(JsonOps.INSTANCE, jsonObject);

            if (result.isSuccess()) {
                Planet planet = result.getOrThrow();
                discoveredPlanets.put(resourceId, planet);

                // Extract category information if available
                if (jsonObject.has("category")) {
                    planetCategories.add(jsonObject.get("category").getAsString());
                }

                // Auto-detect categories based on planet properties
                categorizeDiscoveredPlanet(planet);

                return true;
            } else {
                AdAstraMekanized.LOGGER.error("Failed to parse planet {}: {}", resourceId, result.error());
                return false;
            }
        } catch (IOException e) {
            AdAstraMekanized.LOGGER.error("Failed to read planet resource {}: {}", resourceId, e.getMessage());
            return false;
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Exception parsing planet {}: {}", resourceId, e.getMessage());
            return false;
        }
    }

    /**
     * Auto-categorize planets based on their properties
     */
    private void categorizeDiscoveredPlanet(Planet planet) {
        // Add categories based on planet characteristics
        if (planet.isHabitable()) {
            planetCategories.add("habitable");
        } else {
            planetCategories.add("hostile");
        }

        if (!planet.atmosphere().hasAtmosphere()) {
            planetCategories.add("airless");
        } else {
            planetCategories.add("atmospheric");
        }

        if (planet.properties().temperature() < -50) {
            planetCategories.add("ice_world");
        } else if (planet.properties().temperature() > 100) {
            planetCategories.add("hot_world");
        } else {
            planetCategories.add("temperate");
        }

        if (planet.dimension().isOrbital()) {
            planetCategories.add("orbital");
        } else {
            planetCategories.add("planetary");
        }
    }

    /**
     * Register all discovered planets with the main registry
     */
    private void registerDiscoveredPlanets() {
        PlanetRegistry registry = PlanetRegistry.getInstance();

        for (Planet planet : discoveredPlanets.values()) {
            if (!registry.planetExists(planet.id())) {
                registry.registerPlanet(planet);
            }
        }
    }

    /**
     * Get all planets discovered by this mod
     */
    public Collection<Planet> getAllModPlanets() {
        return Collections.unmodifiableCollection(discoveredPlanets.values());
    }

    /**
     * Get planets by category
     */
    public Collection<Planet> getPlanetsByCategory(String category) {
        return discoveredPlanets.values().stream()
            .filter(planet -> matchesCategory(planet, category))
            .collect(Collectors.toList());
    }

    /**
     * Check if a planet matches a specific category
     */
    private boolean matchesCategory(Planet planet, String category) {
        switch (category.toLowerCase()) {
            case "habitable": return planet.isHabitable();
            case "hostile": return !planet.isHabitable();
            case "airless": return !planet.atmosphere().hasAtmosphere();
            case "atmospheric": return planet.atmosphere().hasAtmosphere();
            case "ice_world": return planet.properties().temperature() < -50;
            case "hot_world": return planet.properties().temperature() > 100;
            case "temperate": return planet.properties().temperature() >= -50 && planet.properties().temperature() <= 100;
            case "orbital": return planet.dimension().isOrbital();
            case "planetary": return !planet.dimension().isOrbital();
            default: return false;
        }
    }

    /**
     * Get all available planet categories
     */
    public Set<String> getAvailableCategories() {
        return Collections.unmodifiableSet(planetCategories);
    }

    /**
     * Find planets within a specific distance range from Earth
     */
    public Collection<Planet> getPlanetsInDistanceRange(int minDistance, int maxDistance) {
        return discoveredPlanets.values().stream()
            .filter(planet -> {
                int distance = planet.properties().orbitDistance();
                return distance >= minDistance && distance <= maxDistance;
            })
            .sorted(Comparator.comparingInt(p -> p.properties().orbitDistance()))
            .collect(Collectors.toList());
    }

    /**
     * Find the nearest planets to a given orbital distance
     */
    public List<Planet> getNearestPlanets(int orbitDistance, int limit) {
        return discoveredPlanets.values().stream()
            .sorted(Comparator.comparingInt(p ->
                Math.abs(p.properties().orbitDistance() - orbitDistance)))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get planets sorted by various criteria
     */
    public List<Planet> getPlanetsSorted(SortCriteria criteria) {
        return switch (criteria) {
            case NAME -> discoveredPlanets.values().stream()
                .sorted(Comparator.comparing(Planet::displayName))
                .collect(Collectors.toList());
            case DISTANCE -> discoveredPlanets.values().stream()
                .sorted(Comparator.comparingInt(p -> p.properties().orbitDistance()))
                .collect(Collectors.toList());
            case GRAVITY -> discoveredPlanets.values().stream()
                .sorted(Comparator.comparing(p -> p.properties().gravity()))
                .collect(Collectors.toList());
            case TEMPERATURE -> discoveredPlanets.values().stream()
                .sorted(Comparator.comparing(p -> p.properties().temperature()))
                .collect(Collectors.toList());
            case HABITABILITY -> discoveredPlanets.values().stream()
                .sorted(Comparator.comparing(Planet::isHabitable).reversed())
                .collect(Collectors.toList());
        };
    }

    /**
     * Search planets by display name (fuzzy search)
     */
    public List<Planet> searchPlanets(String query) {
        String lowerQuery = query.toLowerCase();
        return discoveredPlanets.values().stream()
            .filter(planet -> planet.displayName().toLowerCase().contains(lowerQuery) ||
                           planet.id().getPath().toLowerCase().contains(lowerQuery))
            .sorted(Comparator.comparing(Planet::displayName))
            .collect(Collectors.toList());
    }

    /**
     * Get discovery service statistics
     */
    public DiscoveryStats getStats() {
        long habitablePlanets = discoveredPlanets.values().stream()
            .mapToLong(planet -> planet.isHabitable() ? 1 : 0)
            .sum();

        long atmosphericPlanets = discoveredPlanets.values().stream()
            .mapToLong(planet -> planet.atmosphere().hasAtmosphere() ? 1 : 0)
            .sum();

        return new DiscoveryStats(
            discoveredPlanets.size(),
            (int) habitablePlanets,
            (int) atmosphericPlanets,
            planetCategories.size(),
            discoveryComplete
        );
    }

    /**
     * Check if discovery is complete
     */
    public boolean isDiscoveryComplete() {
        return discoveryComplete;
    }

    /**
     * Check if a specific planet was discovered by this mod
     */
    public boolean isModPlanet(ResourceLocation planetId) {
        return modPlanets.contains(planetId);
    }

    /**
     * Manually register a generated planet with the discovery service
     * Used by WorldStartPlanetGenerator to register dynamically created planets
     */
    public void registerGeneratedPlanet(Planet planet) {
        if (planet == null) {
            AdAstraMekanized.LOGGER.warn("Attempted to register null planet");
            return;
        }

        // Create a resource location for the generated planet
        ResourceLocation resourceId = planet.id();

        // Add to our discovered planets map
        discoveredPlanets.put(resourceId, planet);
        modPlanets.add(resourceId);

        // Auto-categorize the planet
        categorizeDiscoveredPlanet(planet);

        // Register with the main planet registry
        PlanetRegistry registry = PlanetRegistry.getInstance();
        if (!registry.planetExists(planet.id())) {
            registry.registerPlanet(planet);
        }

        AdAstraMekanized.LOGGER.info("Registered generated planet: {} ({})", planet.displayName(), planet.id());
    }

    /**
     * Sort criteria enumeration
     */
    public enum SortCriteria {
        NAME,
        DISTANCE,
        GRAVITY,
        TEMPERATURE,
        HABITABILITY
    }

    /**
     * Discover planets from world datapack directory
     */
    private int discoverFromWorldDatapacks() {
        try {
            Path worldDatapackPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR);
            Path modPlanetsPath = worldDatapackPath.resolve("data").resolve(AdAstraMekanized.MOD_ID).resolve("planets");

            if (!Files.exists(modPlanetsPath)) {
                AdAstraMekanized.LOGGER.debug("No world datapack planets directory found at: {}", modPlanetsPath);
                return 0;
            }

            int discoveredCount = 0;
            try (var files = Files.list(modPlanetsPath)) {
                for (Path file : files.toList()) {
                    if (file.toString().endsWith(".json")) {
                        try {
                            if (processWorldDatapackPlanetFile(file)) {
                                discoveredCount++;
                                AdAstraMekanized.LOGGER.info("Discovered planet from world datapack: {}", file.getFileName());
                            }
                        } catch (Exception e) {
                            AdAstraMekanized.LOGGER.error("Failed to process world datapack planet file {}: {}", file, e.getMessage(), e);
                        }
                    }
                }
            }

            return discoveredCount;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to discover planets from world datapacks: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Process a planet file from world datapack directory
     */
    private boolean processWorldDatapackPlanetFile(Path file) throws IOException {
        String content = Files.readString(file);

        try {
            JsonElement jsonElement = GsonHelper.parse(content);
            if (!jsonElement.isJsonObject()) {
                AdAstraMekanized.LOGGER.warn("World datapack planet file {} is not a JSON object", file);
                return false;
            }

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Parse the planet using the Planet codec
            var result = Planet.CODEC.parse(JsonOps.INSTANCE, jsonObject);

            if (result.isSuccess()) {
                Planet planet = result.getOrThrow();

                // Create a pseudo resource location for this world datapack planet
                ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(
                    AdAstraMekanized.MOD_ID,
                    "world_datapack/" + file.getFileName().toString().replace(".json", "")
                );

                discoveredPlanets.put(resourceId, planet);
                modPlanets.add(resourceId);

                // Auto-categorize the planet
                categorizeDiscoveredPlanet(planet);

                AdAstraMekanized.LOGGER.debug("Successfully processed world datapack planet: {}", planet.id());
                return true;
            } else {
                AdAstraMekanized.LOGGER.error("Failed to parse world datapack planet {}: {}", file, result.error());
                return false;
            }

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Exception parsing world datapack planet {}: {}", file, e.getMessage());
            return false;
        }
    }

    /**
     * Discovery statistics record
     */
    public record DiscoveryStats(
        int totalPlanets,
        int habitablePlanets,
        int atmosphericPlanets,
        int categories,
        boolean discoveryComplete
    ) {}
}