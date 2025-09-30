package com.hecookin.adastramekanized.common.tags;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class ModFluidTags {

    // Fluid tags
    public static final TagKey<Fluid> OXYGEN = tag("oxygen");
    public static final TagKey<Fluid> HYDROGEN = tag("hydrogen");
    public static final TagKey<Fluid> FUEL = tag("fuel");
    public static final TagKey<Fluid> OIL = tag("oil");

    private static TagKey<Fluid> tag(String name) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, name));
    }
}