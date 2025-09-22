package com.hecookin.adastramekanized.client.dimension;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Dimension effects for ice worlds (frozen, snow storms)
 */
public class IceWorldDimensionEffects extends TemplateDimensionEffects {

    public IceWorldDimensionEffects() {
        super(
            192.0f,    // cloudLevel - snow clouds
            true,      // hasGround - has horizon fog
            SkyType.NORMAL,  // skyType
            false,     // forceBrightLightmap
            false      // constantAmbientLight
        );
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 biomeFogColor, float daylight) {
        // Cold, bluish-white fog
        return new Vec3(0.8 * daylight, 0.9 * daylight, 1.0 * daylight);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // Frequent snow/ice fog
        return Math.random() < 0.3;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        // Cold, pale sunrise colors
        if (timeOfDay > 0.75f || timeOfDay < 0.25f) {
            float intensity = (timeOfDay > 0.75f) ? (1.0f - timeOfDay) * 4.0f : timeOfDay * 4.0f;
            return new float[]{0.8f * intensity, 0.9f * intensity, 1.0f * intensity, intensity};
        }
        return null;
    }

    @Override
    public String getPlanetName() {
        return "Ice World";
    }
}