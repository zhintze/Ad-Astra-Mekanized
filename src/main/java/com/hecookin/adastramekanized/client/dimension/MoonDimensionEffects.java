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
 * Custom dimension effects for the Moon.
 *
 * Handles the visual appearance of the Moon dimension including:
 * - Black sky with stars visible
 * - No clouds or weather
 * - Space-like atmosphere
 */
public class MoonDimensionEffects extends DimensionSpecialEffects {

    public MoonDimensionEffects() {
        super(
            Float.NaN, // cloudLevel - disabled
            false,     // hasGround - no horizon fog
            SkyType.NORMAL,  // skyType - normal sky to allow stars
            false,     // forceBrightLightmap
            false      // constantAmbientLight
        );
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 biomeFogColor, float daylight) {
        // Return very dark fog color for space-like atmosphere
        return new Vec3(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // No fog effects on the Moon
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        // No sunrise/sunset colors on the Moon - space has no atmosphere scattering
        return null;
    }


}