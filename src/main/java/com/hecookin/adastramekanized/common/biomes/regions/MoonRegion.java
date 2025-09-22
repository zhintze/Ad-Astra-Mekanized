package com.hecookin.adastramekanized.common.biomes.regions;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.biomes.PlanetBiomes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;
import java.util.function.Consumer;

/**
 * TerraBlender region for Moon biome injection
 * Phase 3.1 - Basic TerraBlender Integration
 */
public class MoonRegion extends Region {

    public MoonRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                         Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        AdAstraMekanized.LOGGER.debug("Adding Moon biomes to TerraBlender region");

        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();

        // Simple parameter point for lunar highlands
        Climate.ParameterPoint highlandsPoint = Climate.parameters(
            Climate.Parameter.point(-0.8f), // Icy temperature
            Climate.Parameter.point(-0.9f), // Very dry humidity
            Climate.Parameter.point(0.4f),  // Inland continentalness
            Climate.Parameter.point(-0.4f), // Low erosion
            Climate.Parameter.point(0.0f),  // Surface depth
            Climate.Parameter.point(0.0f),  // Normal weirdness
            0.0f // Offset
        );
        builder.add(highlandsPoint, PlanetBiomes.LUNAR_HIGHLANDS);

        // Simple parameter point for lunar maria
        Climate.ParameterPoint mariaPoint = Climate.parameters(
            Climate.Parameter.point(-0.3f), // Cool temperature
            Climate.Parameter.point(-0.8f), // Dry humidity
            Climate.Parameter.point(0.1f),  // Near inland continentalness
            Climate.Parameter.point(0.2f),  // Medium erosion
            Climate.Parameter.point(1.0f),  // Floor depth
            Climate.Parameter.point(-0.2f), // Slight negative weirdness
            0.0f // Offset
        );
        builder.add(mariaPoint, PlanetBiomes.LUNAR_MARIA);

        builder.build().forEach(mapper);

        AdAstraMekanized.LOGGER.debug("Moon region biomes added: highlands, maria");
    }
}