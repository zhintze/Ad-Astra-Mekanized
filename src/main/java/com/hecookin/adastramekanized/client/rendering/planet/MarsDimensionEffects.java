package com.hecookin.adastramekanized.client.rendering.planet;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Mars-specific dimension effects based on actual Mars planetary data.
 *
 * Mars Properties (from mars.json):
 * - Gravity: 0.379 (38% of Earth)
 * - Temperature: -65°C (extremely cold)
 * - Atmosphere: Thin CO2 atmosphere (0.006 pressure, not breathable)
 * - Sky: Rust-colored (15510660), sunrise: 14349555
 * - Fog: Dust storms (13791774), density 0.4
 * - Weather: No rain/snow, but dust storms
 * - Particles: Heavy dust (density 0.8)
 */
public class MarsDimensionEffects extends DimensionSpecialEffects {

    // Mars atmospheric colors from JSON
    private static final int SKY_COLOR = 15510660;        // Rust-colored sky
    private static final int FOG_COLOR = 13791774;        // Rust-colored fog
    private static final int SUNRISE_COLOR = 14349555;    // Mars sunrise

    public MarsDimensionEffects() {
        super(
            // Cloud height - Mars has no clouds
            Float.NaN,
            // Has precipitation - Mars has no rain/snow
            false,
            // Sky type - normal sky with custom colors
            SkyType.NORMAL,
            // Force bright lightmap - false for natural lighting
            false,
            // Force dark water - true due to harsh environment
            true
        );
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 skyColor, float celestialAngle) {
        // Mars fog color (rust/orange from dust storms)
        float r = ((FOG_COLOR >> 16) & 0xFF) / 255.0f;
        float g = ((FOG_COLOR >> 8) & 0xFF) / 255.0f;
        float b = (FOG_COLOR & 0xFF) / 255.0f;

        Vec3 fogColor = new Vec3(r, g, b);

        // Mars has very little atmosphere, so fog doesn't change much with time
        // Slight variation based on celestial angle for dust storm effects
        float dustIntensity = 0.7f + 0.3f * (float) Math.sin(celestialAngle * Math.PI);
        return fogColor.scale(dustIntensity);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // Mars has dust storms that create foggy conditions
        // Simulate occasional dust storms based on coordinates
        return (x + z) % 100 < 40; // 40% chance of dusty areas
    }

    /**
     * Get Mars sky color
     */
    public static Vec3 getMarsSkyColor() {
        float r = ((SKY_COLOR >> 16) & 0xFF) / 255.0f;
        float g = ((SKY_COLOR >> 8) & 0xFF) / 255.0f;
        float b = (SKY_COLOR & 0xFF) / 255.0f;
        return new Vec3(r, g, b);
    }

    /**
     * Get Mars sunrise color
     */
    public static Vec3 getMarsSunriseColor() {
        float r = ((SUNRISE_COLOR >> 16) & 0xFF) / 255.0f;
        float g = ((SUNRISE_COLOR >> 8) & 0xFF) / 255.0f;
        float b = (SUNRISE_COLOR & 0xFF) / 255.0f;
        return new Vec3(r, g, b);
    }
}