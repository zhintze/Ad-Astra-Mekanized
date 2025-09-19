package com.hecookin.adastramekanized.client.sky;

/**
 * Defines how celestial objects move in the sky.
 * Based on Ad Astra's MovementType enum.
 */
public enum MovementType {
    /**
     * Object doesn't move, stays in fixed position
     */
    STATIC,

    /**
     * Object rotates with the time of day (like the sun)
     */
    TIME_OF_DAY,

    /**
     * Object rotates opposite to the time of day (like some moons)
     */
    TIME_OF_DAY_REVERSED
}