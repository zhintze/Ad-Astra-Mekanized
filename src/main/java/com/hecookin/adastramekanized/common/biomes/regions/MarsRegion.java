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
 * TerraBlender region for Mars biome injection
 * Phase 3.1 - Basic TerraBlender Integration
 */
public class MarsRegion extends Region {

    public MarsRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                         Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        AdAstraMekanized.LOGGER.debug("Adding Mars biomes to TerraBlender region");

        // For now, use a simple approach without complex parameter building
        // This will be enhanced in later phases
        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();

        // Simple parameter point for Mars highlands
        Climate.ParameterPoint highlandsPoint = Climate.parameters(
            Climate.Parameter.point(-0.5f), // Cold temperature
            Climate.Parameter.point(-0.8f), // Dry humidity
            Climate.Parameter.point(0.3f),  // Inland continentalness
            Climate.Parameter.point(-0.3f), // Low erosion
            Climate.Parameter.point(0.0f),  // Surface depth
            Climate.Parameter.point(0.1f),  // Slight weirdness
            0.0f // Offset
        );
        builder.add(highlandsPoint, PlanetBiomes.MARS_HIGHLANDS);

        // Simple parameter point for Mars valleys
        Climate.ParameterPoint valleysPoint = Climate.parameters(
            Climate.Parameter.point(-0.2f), // Cool temperature
            Climate.Parameter.point(-0.6f), // Dry humidity
            Climate.Parameter.point(0.0f),  // Coastal continentalness
            Climate.Parameter.point(0.3f),  // Higher erosion
            Climate.Parameter.point(1.0f),  // Floor depth
            Climate.Parameter.point(-0.1f), // Negative weirdness
            0.0f // Offset
        );
        builder.add(valleysPoint, PlanetBiomes.MARS_VALLEYS);

        // Simple parameter point for Mars polar
        Climate.ParameterPoint polarPoint = Climate.parameters(
            Climate.Parameter.point(-0.8f), // Icy temperature
            Climate.Parameter.point(-0.4f), // Less dry humidity
            Climate.Parameter.point(0.5f),  // Far inland
            Climate.Parameter.point(-0.5f), // Very low erosion
            Climate.Parameter.point(0.0f),  // Surface depth
            Climate.Parameter.point(0.3f),  // Higher weirdness
            0.0f // Offset
        );
        builder.add(polarPoint, PlanetBiomes.MARS_POLAR);

        builder.build().forEach(mapper);

        AdAstraMekanized.LOGGER.debug("Mars region biomes added: highlands, valleys, polar");
    }
}