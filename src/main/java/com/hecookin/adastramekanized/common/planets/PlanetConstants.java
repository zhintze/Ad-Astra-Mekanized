package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.resources.ResourceLocation;

/**
 * Constants and default values for the planet system.
 */
public final class PlanetConstants {

    // Default planet properties
    public static final float DEFAULT_GRAVITY = 1.0f;
    public static final float DEFAULT_TEMPERATURE = 20.0f;
    public static final float DEFAULT_DAY_LENGTH = 24.0f;
    public static final int DEFAULT_ORBIT_DISTANCE = 150; // Earth's distance from sun in millions of km

    // Atmosphere constants
    public static final float EARTH_PRESSURE = 1.0f;
    public static final float EARTH_OXYGEN_LEVEL = 0.21f;
    public static final float MINIMUM_BREATHABLE_OXYGEN = 0.16f;
    public static final float MINIMUM_BREATHABLE_PRESSURE = 0.5f;

    // Dimension settings
    public static final ResourceLocation DEFAULT_DIMENSION_TYPE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");
    public static final ResourceLocation DEFAULT_BIOME_SOURCE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "fixed");
    public static final ResourceLocation DEFAULT_CHUNK_GENERATOR =
            ResourceLocation.fromNamespaceAndPath("minecraft", "flat");

    // Planet dimension namespace
    public static final String PLANET_DIMENSION_PREFIX = "planets/";

    // Default planets
    public static final ResourceLocation EARTH_ID =
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "earth");
    public static final ResourceLocation MOON_ID =
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "moon");
    public static final ResourceLocation MARS_ID =
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "mars");
    public static final ResourceLocation VENUS_ID =
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "venus");

    // Sky and rendering colors
    public static final int SPACE_SKY_COLOR = 0x000000;      // Black
    public static final int SPACE_FOG_COLOR = 0x1a1a1a;      // Dark gray
    public static final int MARS_SKY_COLOR = 0x8B4513;       // Reddish brown
    public static final int VENUS_SKY_COLOR = 0xFFA500;      // Orange

    // Physics constants
    public static final float MAX_SAFE_GRAVITY = 3.0f;       // Beyond this, special handling needed
    public static final float MIN_GRAVITY = 0.01f;          // Minimum non-zero gravity
    public static final float ZERO_G_THRESHOLD = 0.05f;     // Below this is considered zero-g

    // Temperature constants (Celsius)
    public static final float ABSOLUTE_ZERO = -273.15f;
    public static final float WATER_FREEZING = 0.0f;
    public static final float WATER_BOILING = 100.0f;
    public static final float HUMAN_COMFORT_MIN = 18.0f;
    public static final float HUMAN_COMFORT_MAX = 25.0f;

    // Orbital mechanics
    public static final int MIN_ORBIT_DISTANCE = 50;         // Minimum safe distance from star
    public static final int MAX_ORBIT_DISTANCE = 5000;      // Maximum reasonable distance
    public static final int HABITABLE_ZONE_MIN = 100;       // Start of habitable zone
    public static final int HABITABLE_ZONE_MAX = 200;       // End of habitable zone

    // Travel calculations
    public static final int BASE_FUEL_COST = 10;            // Base fuel cost for any travel
    public static final float FUEL_DISTANCE_MULTIPLIER = 0.1f; // Fuel cost per distance unit

    private PlanetConstants() {
        // Utility class - no instances
    }

    /**
     * Check if a gravity value is within safe limits
     */
    public static boolean isSafeGravity(float gravity) {
        return gravity >= MIN_GRAVITY && gravity <= MAX_SAFE_GRAVITY;
    }

    /**
     * Check if a temperature is within human comfort range
     */
    public static boolean isComfortableTemperature(float temperature) {
        return temperature >= HUMAN_COMFORT_MIN && temperature <= HUMAN_COMFORT_MAX;
    }

    /**
     * Check if orbit distance is in habitable zone
     */
    public static boolean isInHabitableZone(int orbitDistance) {
        return orbitDistance >= HABITABLE_ZONE_MIN && orbitDistance <= HABITABLE_ZONE_MAX;
    }

    /**
     * Calculate fuel cost between two orbital distances
     */
    public static int calculateFuelCost(int fromDistance, int toDistance) {
        int distance = Math.abs(toDistance - fromDistance);
        return BASE_FUEL_COST + Math.round(distance * FUEL_DISTANCE_MULTIPLIER);
    }

    /**
     * Get sky color for a planet based on its atmosphere
     */
    public static int getSkyColorForPlanet(ResourceLocation planetId) {
        if (MARS_ID.equals(planetId)) {
            return MARS_SKY_COLOR;
        } else if (VENUS_ID.equals(planetId)) {
            return VENUS_SKY_COLOR;
        } else if (MOON_ID.equals(planetId)) {
            return SPACE_SKY_COLOR;
        }
        // Default to space color for unknown planets
        return SPACE_SKY_COLOR;
    }
}