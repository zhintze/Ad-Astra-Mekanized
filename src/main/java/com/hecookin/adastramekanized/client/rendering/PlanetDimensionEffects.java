package com.hecookin.adastramekanized.client.rendering;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Custom dimension special effects for planets.
 *
 * Provides planet-specific atmospheric rendering including:
 * - Sky color based on atmosphere composition
 * - Fog rendering for atmospheric density
 * - Star visibility based on planet properties
 * - Celestial body rendering
 */
public class PlanetDimensionEffects extends DimensionSpecialEffects {

    private final Planet planet;

    public PlanetDimensionEffects(Planet planet) {
        super(
            // Cloud height - check if clouds are enabled in rendering config
            (planet != null && planet.rendering().weather().hasClouds()) ? 192.0f : Float.NaN,
            // Has precipitation - based on planet weather
            planet != null && planet.rendering().weather().hasRain(),
            // Sky type - always normal for planets
            SkyType.NORMAL,
            // Force bright lightmap - false for natural lighting
            false,
            // Force dark water - true for harsh environments
            planet == null || !planet.atmosphere().breathable()
        );
        this.planet = planet;

        // Debug cloud configuration
        if (planet != null) {
            boolean hasClouds = planet.rendering().weather().hasClouds();
            float cloudHeight = hasClouds ? 192.0f : Float.NaN;
            AdAstraMekanized.LOGGER.info("Planet {} clouds: {} (height: {})",
                planet.id(), hasClouds, cloudHeight);
        }
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 skyColor, float celestialAngle) {
        // Handle null planet case or no atmosphere
        if (planet == null || !planet.atmosphere().hasAtmosphere()) {
            // No atmosphere = constant black space, no sunrise/sunset color transitions
            return new Vec3(0.0, 0.0, 0.0);
        }

        // Get fog color from planet rendering data
        int fogColorInt = planet.rendering().fog().fogColor();
        float r = ((fogColorInt >> 16) & 0xFF) / 255.0f;
        float g = ((fogColorInt >> 8) & 0xFF) / 255.0f;
        float b = (fogColorInt & 0xFF) / 255.0f;

        Vec3 fogColor = new Vec3(r, g, b);

        // Apply brightness variation based on atmosphere type
        // Non-breathable atmospheres have minimal color transitions to avoid Earth-like sunrises
        if (!planet.atmosphere().breathable()) {
            // Thin/toxic atmospheres: very minimal variation (90-100% brightness)
            float brightness = 0.9f + 0.1f * (float) Math.cos(celestialAngle * 2.0 * Math.PI);
            return fogColor.scale(brightness);
        } else {
            // Breathable atmospheres: normal day/night variation like Earth
            float brightness = 0.5f + 0.5f * (float) Math.cos(celestialAngle * 2.0 * Math.PI);
            return fogColor.scale(brightness);
        }
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // Handle null planet case
        if (planet == null) {
            return false; // Default to no fog for unknown planets
        }

        // Fog based on planet atmosphere density
        return planet.rendering().fog().hasFog();
    }

    /**
     * Get the planet associated with these effects
     */
    public Planet getPlanet() {
        return planet;
    }

    /**
     * Create effects for a specific planet type
     */
    public static PlanetDimensionEffects createForPlanet(Planet planet) {
        return new PlanetDimensionEffects(planet);
    }
}