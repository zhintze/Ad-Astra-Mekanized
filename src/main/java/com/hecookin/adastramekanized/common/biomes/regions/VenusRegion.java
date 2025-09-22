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
 * TerraBlender region for Venus biome injection
 * Phase 3.1 - Basic TerraBlender Integration
 */
public class VenusRegion extends Region {

    public VenusRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                         Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        AdAstraMekanized.LOGGER.debug("Adding Venus biomes to TerraBlender region");

        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();

        // Simple parameter point for Venus surface
        Climate.ParameterPoint surfacePoint = Climate.parameters(
            Climate.Parameter.point(0.8f),  // Hot temperature
            Climate.Parameter.point(0.7f),  // Humid atmosphere
            Climate.Parameter.point(0.2f),  // Coastal continentalness
            Climate.Parameter.point(0.2f),  // Medium erosion
            Climate.Parameter.point(0.0f),  // Surface depth
            Climate.Parameter.point(0.0f),  // Normal weirdness
            0.0f // Offset
        );
        builder.add(surfacePoint, PlanetBiomes.VENUS_SURFACE);

        // Simple parameter point for Venus volcanic regions
        Climate.ParameterPoint volcanicPoint = Climate.parameters(
            Climate.Parameter.point(0.9f),  // Very hot temperature
            Climate.Parameter.point(0.8f),  // Very humid (steam/gases)
            Climate.Parameter.point(0.5f),  // Inland continentalness
            Climate.Parameter.point(-0.2f), // Low erosion (fresh volcanic)
            Climate.Parameter.point(0.0f),  // Surface depth
            Climate.Parameter.point(0.4f),  // High weirdness for volcanic activity
            0.0f // Offset
        );
        builder.add(volcanicPoint, PlanetBiomes.VENUS_VOLCANIC);

        builder.build().forEach(mapper);

        AdAstraMekanized.LOGGER.debug("Venus region biomes added: surface, volcanic");
    }
}