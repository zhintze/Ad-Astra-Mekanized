package com.hecookin.adastramekanized.client.dimension;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.client.sky.CelestialBodyConverter;
import com.hecookin.adastramekanized.client.sky.CelestialSkyRenderer;
import com.hecookin.adastramekanized.client.sky.SkyRenderable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Custom dimension effects for Mars.
 *
 * Handles the visual appearance of Mars including:
 * - Rusty orange/red sky atmosphere
 * - Appropriate lighting for Mars atmosphere
 * - Custom fog effects for dust storms
 */
public class MarsDimensionEffects extends DimensionSpecialEffects {

    public MarsDimensionEffects() {
        super(
            Float.NaN, // cloudLevel - disabled (Mars has no Earth-like clouds)
            true,      // hasGround - horizon fog
            SkyType.NORMAL,  // skyType - normal sky but custom colors
            false,     // forceBrightLightmap
            false      // constantAmbientLight
        );
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 biomeFogColor, float daylight) {
        // Return reddish-orange fog color typical of Mars
        // During day: orange-red, during night: darker reddish
        float red = 0.8f + (daylight * 0.2f);
        float green = 0.4f + (daylight * 0.3f);
        float blue = 0.2f + (daylight * 0.1f);

        return new Vec3(red, green, blue);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // Mars can have dust storms - moderate fog levels
        return false; // For now, disable to keep it simple
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        // Mars sunrises are blue-ish due to atmospheric scattering
        if (timeOfDay >= 0.75f || timeOfDay <= 0.25f) {
            return new float[]{0.6f, 0.7f, 0.9f, 0.3f}; // Blue sunrise/sunset
        }
        return null;
    }


}