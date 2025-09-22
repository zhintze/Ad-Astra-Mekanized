package com.hecookin.adastramekanized.client.dimension;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Dimension effects for gas giant worlds (thick atmosphere, floating islands)
 */
public class GasGiantDimensionEffects extends TemplateDimensionEffects {

    public GasGiantDimensionEffects() {
        super(
            128.0f,    // cloudLevel - thick clouds
            false,     // hasGround - floating in atmosphere
            SkyType.NORMAL,  // skyType
            false,     // forceBrightLightmap
            false      // constantAmbientLight
        );
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 biomeFogColor, float daylight) {
        // Dense atmospheric fog with colorful gases
        return new Vec3(0.6 * daylight, 0.3 * daylight, 0.8 * daylight);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // Very dense atmospheric fog
        return true;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        // Vibrant atmospheric scattering
        if (timeOfDay > 0.7f || timeOfDay < 0.3f) {
            float intensity = (timeOfDay > 0.7f) ? (1.0f - timeOfDay) * 3.33f : timeOfDay * 3.33f;
            return new float[]{0.9f * intensity, 0.5f * intensity, 0.7f * intensity, intensity};
        }
        return null;
    }

    @Override
    public String getPlanetName() {
        return "Gas Giant";
    }
}