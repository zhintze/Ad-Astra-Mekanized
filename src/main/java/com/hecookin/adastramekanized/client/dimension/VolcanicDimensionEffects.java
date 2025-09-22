package com.hecookin.adastramekanized.client.dimension;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Dimension effects for volcanic worlds (lava, ash storms, extreme heat)
 */
public class VolcanicDimensionEffects extends TemplateDimensionEffects {

    public VolcanicDimensionEffects() {
        super(
            96.0f,     // cloudLevel - ash clouds
            true,      // hasGround - has horizon fog
            SkyType.NORMAL,  // skyType
            false,     // forceBrightLightmap
            false      // constantAmbientLight
        );
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 biomeFogColor, float daylight) {
        // Dark reddish fog from ash and lava
        return new Vec3(0.6 * daylight, 0.2 * daylight, 0.1 * daylight);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // Frequent ash and volcanic fog
        return Math.random() < 0.6;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        // Intense red/orange colors from volcanic atmosphere
        if (timeOfDay > 0.7f || timeOfDay < 0.3f) {
            float intensity = (timeOfDay > 0.7f) ? (1.0f - timeOfDay) * 3.33f : timeOfDay * 3.33f;
            return new float[]{1.0f * intensity, 0.3f * intensity, 0.1f * intensity, intensity};
        }
        return null;
    }

    @Override
    public String getPlanetName() {
        return "Volcanic World";
    }
}