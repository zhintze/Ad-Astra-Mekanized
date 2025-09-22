package com.hecookin.adastramekanized.client.dimension;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Dimension effects for moon-like worlds (airless, stark landscapes)
 */
public class MoonLikeDimensionEffects extends TemplateDimensionEffects {

    public MoonLikeDimensionEffects() {
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
        // Very dark fog color for space-like atmosphere
        return new Vec3(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // No fog effects in vacuum
        return false;
    }

    @Override
    public String getPlanetName() {
        return "Moon-like World";
    }
}