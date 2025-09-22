package com.hecookin.adastramekanized.client.dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Base template dimension effects class for planet generation system
 */
public abstract class TemplateDimensionEffects extends DimensionSpecialEffects {

    protected TemplateDimensionEffects(float cloudLevel, boolean hasGround, SkyType skyType,
                                     boolean forceBrightLightmap, boolean constantAmbientLight) {
        super(cloudLevel, hasGround, skyType, forceBrightLightmap, constantAmbientLight);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 biomeFogColor, float daylight) {
        return biomeFogColor;
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }

    /**
     * Get the planet name for this dimension effects
     */
    public abstract String getPlanetName();
}