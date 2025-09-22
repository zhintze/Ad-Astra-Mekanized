package com.hecookin.adastramekanized.common.planets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Enumeration of celestial body configuration templates for dynamic planets.
 * Each type defines what celestial objects (sun, moons, planets) are visible from the planet's surface.
 */
public enum CelestialType {
    NO_CELESTIALS("no_celestials", "Empty space with only distant stars"),
    SINGLE_SUN("single_sun", "Basic solar system with just the sun visible"),
    SUN_AND_EARTH("sun_and_earth", "Earth-like system with sun and home planet visible"),
    TWO_MOONS("two_moons", "System with sun and two small moons"),
    BINARY_STAR("binary_star", "Twin star system with two suns"),
    RING_SYSTEM("ring_system", "Planet with ring system and multiple moons");

    private final String id;
    private final String description;

    CelestialType(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Get the string ID for this celestial type
     */
    public String getId() {
        return id;
    }

    /**
     * Get the human-readable description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get celestial type by string ID
     */
    public static CelestialType fromId(String id) {
        for (CelestialType type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown celestial type: " + id);
    }

    /**
     * Get a random celestial type for procedural generation
     */
    public static CelestialType getRandomType() {
        CelestialType[] types = values();
        return types[(int) (Math.random() * types.length)];
    }

    /**
     * Generate celestial bodies configuration JSON for this type
     */
    public JsonObject generateCelestialBodies() {
        JsonObject celestialBodies = new JsonObject();

        switch (this) {
            case NO_CELESTIALS:
                celestialBodies.add("sun", createInvisibleSun());
                celestialBodies.add("moons", new JsonArray());
                celestialBodies.add("visible_planets", new JsonArray());
                break;

            case SINGLE_SUN:
                celestialBodies.add("sun", createBasicSun());
                celestialBodies.add("moons", new JsonArray());
                celestialBodies.add("visible_planets", new JsonArray());
                break;

            case SUN_AND_EARTH:
                celestialBodies.add("sun", createSmallSun());
                celestialBodies.add("moons", new JsonArray());
                celestialBodies.add("visible_planets", createEarthView());
                break;

            case TWO_MOONS:
                celestialBodies.add("sun", createBasicSun());
                celestialBodies.add("moons", createTwoMoons());
                celestialBodies.add("visible_planets", new JsonArray());
                break;

            case BINARY_STAR:
                celestialBodies.add("sun", createBinarySun());
                celestialBodies.add("moons", new JsonArray());
                celestialBodies.add("visible_planets", createBinaryCompanion());
                break;

            case RING_SYSTEM:
                celestialBodies.add("sun", createBasicSun());
                celestialBodies.add("moons", createRingMoons());
                celestialBodies.add("visible_planets", new JsonArray());
                break;
        }

        return celestialBodies;
    }

    /**
     * Generate sky configuration for this celestial type
     */
    public JsonObject generateSkyConfig(DimensionEffectsType effectsType) {
        JsonObject sky = new JsonObject();

        // Base sky configuration varies by dimension type
        int skyColor = switch (effectsType) {
            case MOON_LIKE -> 0; // Black space
            case ROCKY -> 15510660; // Rust-colored like Mars
            case GAS_GIANT -> 8421631; // Purple-blue atmospheric
            case ICE_WORLD -> 11657279; // Pale blue-white
            case VOLCANIC -> 6619136; // Dark red-orange
            case ASTEROID_LIKE -> 0; // Black space
            case ALTERED_OVERWORLD -> 7842047; // Sky blue
        };

        int sunriseColor = switch (effectsType) {
            case MOON_LIKE -> 0; // No sunrise in space
            case ROCKY -> 14349555; // Mars-like blue sunrise
            case GAS_GIANT -> 16744703; // Purple sunrise
            case ICE_WORLD -> 12632319; // Pale sunrise
            case VOLCANIC -> 16711680; // Red sunrise
            case ASTEROID_LIKE -> 0; // No sunrise in space
            case ALTERED_OVERWORLD -> 16777087; // Golden sunrise
        };

        sky.addProperty("sky_color", skyColor);
        sky.addProperty("sunrise_color", sunriseColor);
        sky.addProperty("custom_sky", true);
        sky.addProperty("has_stars", true);

        // Star configuration based on celestial complexity
        int starCount = switch (this) {
            case NO_CELESTIALS -> 50000; // Dense starfield
            case SINGLE_SUN, SUN_AND_EARTH -> 15000; // Moderate stars
            case TWO_MOONS -> 8000; // Mars-like star count
            case BINARY_STAR -> 25000; // Rich starfield for binary system
            case RING_SYSTEM -> 12000; // Moderate with ring interference
        };

        float starBrightness = switch (effectsType) {
            case MOON_LIKE -> 2.5f; // Very bright in airless space
            case ROCKY -> 1.2f; // Dimmed by thin atmosphere
            case GAS_GIANT -> 0.5f; // Heavily dimmed by thick atmosphere
            case ICE_WORLD -> 1.8f; // Clear but cold
            case VOLCANIC -> 0.8f; // Dimmed by ash and heat haze
            case ASTEROID_LIKE -> 2.8f; // Very bright in airless space
            case ALTERED_OVERWORLD -> 1.0f; // Normal atmospheric conditions
        };

        String starVisibility = (effectsType == DimensionEffectsType.MOON_LIKE) ? "constant" : "night_only";

        sky.addProperty("star_count", starCount);
        sky.addProperty("star_brightness", starBrightness);
        sky.addProperty("star_visibility", starVisibility);

        return sky;
    }

    // Private helper methods for creating celestial body configurations

    private JsonObject createInvisibleSun() {
        JsonObject sun = new JsonObject();
        sun.addProperty("texture", "minecraft:textures/environment/sun.png");
        sun.addProperty("scale", 0.0);
        sun.addProperty("color", 16777215);
        sun.addProperty("visible", false);
        return sun;
    }

    private JsonObject createBasicSun() {
        JsonObject sun = new JsonObject();
        sun.addProperty("texture", "minecraft:textures/environment/sun.png");
        sun.addProperty("scale", 1.0);
        sun.addProperty("color", 16777215);
        sun.addProperty("visible", true);
        return sun;
    }

    private JsonObject createSmallSun() {
        JsonObject sun = new JsonObject();
        sun.addProperty("texture", "minecraft:textures/environment/sun.png");
        sun.addProperty("scale", 0.3);
        sun.addProperty("color", 16777215);
        sun.addProperty("visible", true);
        return sun;
    }

    private JsonObject createBinarySun() {
        JsonObject sun = new JsonObject();
        sun.addProperty("texture", "adastramekanized:textures/celestial/binary_star.png");
        sun.addProperty("scale", 1.2);
        sun.addProperty("color", 16755200); // Orange tint
        sun.addProperty("visible", true);
        return sun;
    }

    private JsonArray createEarthView() {
        JsonArray planets = new JsonArray();
        JsonObject earth = new JsonObject();
        earth.addProperty("texture", "adastramekanized:textures/celestial/earth.png");
        earth.addProperty("scale", 0.5);
        earth.addProperty("color", 6737151);
        earth.addProperty("horizontal_position", 0.3);
        earth.addProperty("vertical_position", 0.8);
        earth.addProperty("moves_with_time", true);
        earth.addProperty("visible", true);
        planets.add(earth);
        return planets;
    }

    private JsonArray createTwoMoons() {
        JsonArray moons = new JsonArray();

        // First moon (Phobos-like)
        JsonObject moon1 = new JsonObject();
        moon1.addProperty("texture", "adastramekanized:textures/celestial/phobos.png");
        moon1.addProperty("scale", 0.3);
        moon1.addProperty("color", 11184810);
        moon1.addProperty("horizontal_position", 0.4);
        moon1.addProperty("vertical_position", 0.15);
        moon1.addProperty("moves_with_time", true);
        moon1.addProperty("visible", true);
        moons.add(moon1);

        // Second moon (Deimos-like)
        JsonObject moon2 = new JsonObject();
        moon2.addProperty("texture", "adastramekanized:textures/celestial/deimos.png");
        moon2.addProperty("scale", 0.2);
        moon2.addProperty("color", 9474192);
        moon2.addProperty("horizontal_position", -0.3);
        moon2.addProperty("vertical_position", 1.30);
        moon2.addProperty("moves_with_time", true);
        moon2.addProperty("visible", true);
        moons.add(moon2);

        return moons;
    }

    private JsonArray createBinaryCompanion() {
        JsonArray planets = new JsonArray();
        JsonObject companion = new JsonObject();
        companion.addProperty("texture", "adastramekanized:textures/celestial/binary_companion.png");
        companion.addProperty("scale", 0.4);
        companion.addProperty("color", 16744448); // Yellow companion star
        companion.addProperty("horizontal_position", -0.7);
        companion.addProperty("vertical_position", 0.3);
        companion.addProperty("moves_with_time", false); // Binary companion is relatively fixed
        companion.addProperty("visible", true);
        planets.add(companion);
        return planets;
    }

    private JsonArray createRingMoons() {
        JsonArray moons = new JsonArray();

        // Create 3 small moons for ring system
        for (int i = 0; i < 3; i++) {
            JsonObject moon = new JsonObject();
            moon.addProperty("texture", "adastramekanized:textures/celestial/ring_moon.png");
            moon.addProperty("scale", 0.15 + (i * 0.05)); // Varying sizes
            moon.addProperty("color", 12632256); // Grayish
            moon.addProperty("horizontal_position", -0.5 + (i * 0.5));
            moon.addProperty("vertical_position", 0.2 + (i * 0.3));
            moon.addProperty("moves_with_time", true);
            moon.addProperty("visible", true);
            moons.add(moon);
        }

        return moons;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", id, description);
    }
}