package com.hecookin.adastramekanized.client.dimension;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Dimension effects for rocky worlds (Mars-like with thin atmosphere)
 */
public class RockyDimensionEffects extends TemplateDimensionEffects {

    public RockyDimensionEffects() {
        super(
            Float.NaN, // cloudLevel - minimal clouds
            true,      // hasGround - has horizon fog
            SkyType.NORMAL,  // skyType
            false,     // forceBrightLightmap
            false      // constantAmbientLight
        );
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 biomeFogColor, float daylight) {
        // Mars-like reddish-orange fog
        return new Vec3(0.8 * daylight, 0.4 * daylight, 0.2 * daylight);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // Light atmospheric fog
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        // Blue sunrise/sunset colors (opposite of Earth due to thin atmosphere)
        if (timeOfDay > 0.75f || timeOfDay < 0.25f) {
            float intensity = (timeOfDay > 0.75f) ? (1.0f - timeOfDay) * 4.0f : timeOfDay * 4.0f;
            return new float[]{0.2f * intensity, 0.4f * intensity, 0.8f * intensity, intensity};
        }
        return null;
    }

    @Override
    public String getPlanetName() {
        return "Rocky World";
    }
}