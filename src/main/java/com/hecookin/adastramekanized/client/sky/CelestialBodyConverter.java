package com.hecookin.adastramekanized.client.sky;

import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts our atmospheric rendering celestial body data into SkyRenderable objects.
 */
public class CelestialBodyConverter {

    // Default textures for celestial objects - guaranteed to be available
    private static final ResourceLocation SUN_TEXTURE = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
    private static final ResourceLocation MOON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final ResourceLocation PLANET_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/stone.png");

    // Additional texture options for variety
    private static final ResourceLocation[] PLANET_TEXTURES = {
        ResourceLocation.withDefaultNamespace("textures/block/stone.png"),
        ResourceLocation.withDefaultNamespace("textures/block/cobblestone.png"),
        ResourceLocation.withDefaultNamespace("textures/block/dirt.png"),
        ResourceLocation.withDefaultNamespace("textures/block/sand.png"),
        ResourceLocation.withDefaultNamespace("textures/block/red_sand.png"),
        ResourceLocation.withDefaultNamespace("textures/block/iron_block.png"),
        ResourceLocation.withDefaultNamespace("textures/block/gold_block.png"),
        ResourceLocation.withDefaultNamespace("textures/block/lapis_block.png")
    };

    /**
     * Converts celestial bodies configuration to list of SkyRenderables
     */
    public static List<SkyRenderable> convertCelestialBodies(AtmosphericRendering.CelestialBodies celestialBodies) {
        List<SkyRenderable> skyRenderables = new ArrayList<>();

        // Convert Sun configuration
        if (celestialBodies.sun() != null && celestialBodies.sun().visible()) {
            skyRenderables.add(convertSun(celestialBodies.sun()));
        }

        // Convert Moon configurations
        for (AtmosphericRendering.MoonConfiguration moon : celestialBodies.moons()) {
            if (moon.visible()) {
                skyRenderables.add(convertMoon(moon));
            }
        }

        // Convert Planet configurations
        for (AtmosphericRendering.PlanetConfiguration planet : celestialBodies.visiblePlanets()) {
            if (planet.visible()) {
                skyRenderables.add(convertPlanet(planet));
            }
        }

        return skyRenderables;
    }

    /**
     * Converts sun configuration to SkyRenderable
     */
    private static SkyRenderable convertSun(AtmosphericRendering.SunConfiguration sun) {
        ResourceLocation texture = sun.texture() != null ? sun.texture().orElse(SUN_TEXTURE) : SUN_TEXTURE;

        // Sun follows time of day
        MovementType movementType = MovementType.TIME_OF_DAY;

        // Convert RGB to ARGB format
        int color = convertRGBToARGB(sun.color());

        return new SkyRenderable(
            texture,
            sun.scale(),
            Vec3.ZERO,              // No global rotation
            Vec3.ZERO,              // No local rotation
            movementType,
            true,                   // Enable blending for sun
            color,
            sun.scale() * 3.0f      // Backlight scale
        );
    }

    /**
     * Converts moon configuration to SkyRenderable
     */
    private static SkyRenderable convertMoon(AtmosphericRendering.MoonConfiguration moon) {
        ResourceLocation texture = moon.texture() != null ? moon.texture() : MOON_TEXTURE;

        // Moons with orbit phase > 0 move opposite to time, others are static
        MovementType movementType = moon.orbitPhase() > 0 ? MovementType.TIME_OF_DAY_REVERSED : MovementType.STATIC;

        // Calculate rotation based on orbit phase
        Vec3 globalRotation = new Vec3(
            moon.orbitPhase() * 360.0,  // X rotation based on orbit phase
            moon.orbitPhase() * 180.0,  // Y rotation for variety
            0.0                         // No Z rotation
        );

        int color = convertRGBToARGB(moon.color());

        return new SkyRenderable(
            texture,
            moon.scale(),
            globalRotation,
            Vec3.ZERO,              // No local rotation
            movementType,
            false,                  // No blending for moons
            color,
            moon.scale() * 2.0f     // Smaller backlight
        );
    }

    /**
     * Converts planet configuration to SkyRenderable
     */
    private static SkyRenderable convertPlanet(AtmosphericRendering.PlanetConfiguration planet) {
        ResourceLocation texture = planet.texture() != null ? planet.texture() : PLANET_TEXTURE;

        // Planets with distance < 1.0 move with time, others are static
        // This allows close planets (like Earth from Moon) to move across the sky
        MovementType movementType = planet.distance() < 1.0 ? MovementType.TIME_OF_DAY : MovementType.STATIC;

        // Position planets based on distance parameter
        Vec3 globalRotation = new Vec3(
            planet.distance() * 30.0,   // Spread planets based on distance
            planet.distance() * 45.0,   // Vary positioning
            0.0
        );

        int color = convertRGBToARGB(planet.color());

        return new SkyRenderable(
            texture,
            planet.scale(),
            globalRotation,
            Vec3.ZERO,              // No local rotation
            movementType,
            false,                  // No blending for planets
            color,
            planet.scale() * 1.5f   // Minimal backlight
        );
    }

    /**
     * Converts RGB integer to ARGB format
     */
    private static int convertRGBToARGB(int rgb) {
        // Add full alpha (0xFF) to RGB color
        return 0xFF000000 | rgb;
    }
}