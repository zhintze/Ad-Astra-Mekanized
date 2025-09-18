package com.hecookin.adastramekanized.api.planets;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * Public API for planet system access.
 *
 * This interface provides external mods and systems with access to planet data
 * and management functions without exposing internal implementation details.
 */
public interface PlanetAPI {

    /**
     * Get a planet by its resource location identifier
     *
     * @param planetId The planet's resource location
     * @return The planet data, or null if not found
     */
    @Nullable
    Planet getPlanet(ResourceLocation planetId);

    /**
     * Get a planet by its resource location identifier, wrapped in Optional
     *
     * @param planetId The planet's resource location
     * @return Optional containing the planet data
     */
    default Optional<Planet> getPlanetOptional(ResourceLocation planetId) {
        return Optional.ofNullable(getPlanet(planetId));
    }

    /**
     * Get all registered planets
     *
     * @return Collection of all registered planets
     */
    Collection<Planet> getAllPlanets();

    /**
     * Check if a planet exists in the registry
     *
     * @param planetId The planet's resource location
     * @return True if the planet exists
     */
    boolean planetExists(ResourceLocation planetId);

    /**
     * Get the number of registered planets
     *
     * @return Total count of registered planets
     */
    int getPlanetCount();

    /**
     * Find planets that match specific criteria
     *
     * @param habitable Only include habitable planets
     * @param hasAtmosphere Only include planets with atmosphere
     * @return Collection of matching planets
     */
    Collection<Planet> findPlanets(boolean habitable, boolean hasAtmosphere);

    /**
     * Calculate travel distance between two planets
     *
     * @param from Source planet
     * @param to Destination planet
     * @return Distance in abstract units, or -1 if calculation fails
     */
    int calculateDistance(ResourceLocation from, ResourceLocation to);

    /**
     * Get the default starting planet (typically Earth)
     *
     * @return The default planet, or null if not configured
     */
    @Nullable
    Planet getDefaultPlanet();

    /**
     * Check if planet data has been synchronized to the client
     *
     * @return True if planet data is available on client
     */
    boolean isPlanetDataSynced();
}