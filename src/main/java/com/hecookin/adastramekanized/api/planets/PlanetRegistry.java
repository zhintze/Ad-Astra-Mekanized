package com.hecookin.adastramekanized.api.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Central registry for planet data management.
 *
 * Handles planet registration, lookup, validation, and caching.
 * Thread-safe for server environments.
 */
public class PlanetRegistry implements PlanetAPI {

    private static final PlanetRegistry INSTANCE = new PlanetRegistry();

    private final Map<ResourceLocation, Planet> planets = new ConcurrentHashMap<>();
    private final Set<ResourceLocation> loadedPlanets = ConcurrentHashMap.newKeySet();
    private volatile boolean dataLoaded = false;
    private volatile boolean clientSynced = false;

    @Nullable
    private ResourceLocation defaultPlanetId = null;

    private PlanetRegistry() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance of the planet registry
     */
    public static PlanetRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register a new planet in the registry
     *
     * @param planet The planet to register
     * @return True if registration succeeded, false if validation failed or planet already exists
     */
    public boolean registerPlanet(Planet planet) {
        if (planet == null || !planet.isValid()) {
            AdAstraMekanized.LOGGER.warn("Attempted to register invalid planet: {}",
                planet != null ? planet.id() : "null");
            return false;
        }

        if (planets.containsKey(planet.id())) {
            AdAstraMekanized.LOGGER.warn("Planet {} is already registered, skipping registration", planet.id());
            return false;
        }

        planets.put(planet.id(), planet);
        loadedPlanets.add(planet.id());

        AdAstraMekanized.LOGGER.info("Registered planet: {} ({})", planet.displayName(), planet.id());
        return true;
    }

    /**
     * Unregister a planet from the registry
     *
     * @param planetId The planet ID to remove
     * @return The removed planet, or null if not found
     */
    @Nullable
    public Planet unregisterPlanet(ResourceLocation planetId) {
        Planet removed = planets.remove(planetId);
        loadedPlanets.remove(planetId);

        if (removed != null) {
            AdAstraMekanized.LOGGER.info("Unregistered planet: {} ({})", removed.displayName(), planetId);

            // Clear default planet if it was removed
            if (planetId.equals(defaultPlanetId)) {
                defaultPlanetId = null;
            }
        }

        return removed;
    }

    /**
     * Clear all registered planets (used for reloading)
     */
    public void clearAll() {
        int count = planets.size();
        planets.clear();
        loadedPlanets.clear();
        defaultPlanetId = null;
        dataLoaded = false;
        clientSynced = false;

        AdAstraMekanized.LOGGER.info("Cleared {} planets from registry", count);
    }

    /**
     * Set the default starting planet
     *
     * @param planetId The planet to set as default
     * @return True if the planet exists and was set as default
     */
    public boolean setDefaultPlanet(ResourceLocation planetId) {
        if (planets.containsKey(planetId)) {
            defaultPlanetId = planetId;
            AdAstraMekanized.LOGGER.info("Set default planet to: {}", planetId);
            return true;
        }

        AdAstraMekanized.LOGGER.warn("Cannot set default planet to non-existent planet: {}", planetId);
        return false;
    }

    /**
     * Mark planet data as loaded and validated
     */
    public void markDataLoaded() {
        dataLoaded = true;
        AdAstraMekanized.LOGGER.info("Planet data loading complete. {} planets registered", planets.size());
    }

    /**
     * Mark planet data as synchronized to client
     */
    public void markClientSynced() {
        clientSynced = true;
        AdAstraMekanized.LOGGER.debug("Planet data synchronized to client");
    }

    /**
     * Check if planet data loading is complete
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    // PlanetAPI Implementation

    @Override
    @Nullable
    public Planet getPlanet(ResourceLocation planetId) {
        return planets.get(planetId);
    }

    @Override
    public Collection<Planet> getAllPlanets() {
        return Collections.unmodifiableCollection(planets.values());
    }

    @Override
    public boolean planetExists(ResourceLocation planetId) {
        return planets.containsKey(planetId);
    }

    @Override
    public int getPlanetCount() {
        return planets.size();
    }

    @Override
    public Collection<Planet> findPlanets(boolean habitable, boolean hasAtmosphere) {
        Predicate<Planet> filter = planet -> {
            if (habitable && !planet.isHabitable()) return false;
            if (hasAtmosphere && !planet.atmosphere().hasAtmosphere()) return false;
            return true;
        };

        return planets.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    @Override
    public int calculateDistance(ResourceLocation from, ResourceLocation to) {
        Planet fromPlanet = planets.get(from);
        Planet toPlanet = planets.get(to);

        if (fromPlanet == null || toPlanet == null) {
            return -1;
        }

        return Math.abs(fromPlanet.properties().orbitDistance() - toPlanet.properties().orbitDistance());
    }

    @Override
    @Nullable
    public Planet getDefaultPlanet() {
        return defaultPlanetId != null ? planets.get(defaultPlanetId) : null;
    }

    @Override
    public boolean isPlanetDataSynced() {
        return clientSynced;
    }

    // Additional utility methods

    /**
     * Get planets sorted by orbit distance
     */
    public List<Planet> getPlanetsByOrbitDistance() {
        return planets.values().stream()
                .sorted(Comparator.comparingInt(p -> p.properties().orbitDistance()))
                .collect(Collectors.toList());
    }

    /**
     * Find the nearest planet to a given orbit distance
     */
    @Nullable
    public Planet findNearestPlanet(int orbitDistance) {
        return planets.values().stream()
                .min(Comparator.comparingInt(p ->
                    Math.abs(p.properties().orbitDistance() - orbitDistance)))
                .orElse(null);
    }

    /**
     * Get planets within a specific orbit distance range
     */
    public Collection<Planet> getPlanetsInRange(int minDistance, int maxDistance) {
        return planets.values().stream()
                .filter(planet -> {
                    int distance = planet.properties().orbitDistance();
                    return distance >= minDistance && distance <= maxDistance;
                })
                .collect(Collectors.toList());
    }

    /**
     * Validate all registered planets
     *
     * @return List of invalid planet IDs
     */
    public List<ResourceLocation> validateAllPlanets() {
        return planets.entrySet().stream()
                .filter(entry -> !entry.getValue().isValid())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get registry statistics for debugging
     */
    public RegistryStats getStats() {
        long habitablePlanets = planets.values().stream()
                .mapToLong(planet -> planet.isHabitable() ? 1 : 0)
                .sum();

        long atmosphericPlanets = planets.values().stream()
                .mapToLong(planet -> planet.atmosphere().hasAtmosphere() ? 1 : 0)
                .sum();

        return new RegistryStats(
                planets.size(),
                (int) habitablePlanets,
                (int) atmosphericPlanets,
                dataLoaded,
                clientSynced
        );
    }

    /**
     * Registry statistics record
     */
    public record RegistryStats(
            int totalPlanets,
            int habitablePlanets,
            int atmosphericPlanets,
            boolean dataLoaded,
            boolean clientSynced
    ) {}
}