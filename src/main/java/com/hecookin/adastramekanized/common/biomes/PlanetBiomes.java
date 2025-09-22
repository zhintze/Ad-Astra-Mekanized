package com.hecookin.adastramekanized.common.biomes;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/**
 * Registry for custom planet biomes used with TerraBlender integration
 * Phase 3.1 - Basic TerraBlender Integration
 */
public class PlanetBiomes {

    // Mars biomes - Test Planet 1
    public static final ResourceKey<Biome> MARS_HIGHLANDS = register("mars_highlands");
    public static final ResourceKey<Biome> MARS_VALLEYS = register("mars_valleys");
    public static final ResourceKey<Biome> MARS_POLAR = register("mars_polar");

    // Moon biomes - Test Planet 2
    public static final ResourceKey<Biome> LUNAR_HIGHLANDS = register("lunar_highlands");
    public static final ResourceKey<Biome> LUNAR_MARIA = register("lunar_maria");

    // Venus biomes - Test Planet 3
    public static final ResourceKey<Biome> VENUS_SURFACE = register("venus_surface");
    public static final ResourceKey<Biome> VENUS_VOLCANIC = register("venus_volcanic");

    /**
     * Register a biome resource key
     */
    private static ResourceKey<Biome> register(String name) {
        return ResourceKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, name));
    }

    /**
     * Initialize biome registration (called during mod setup)
     */
    public static void initialize() {
        AdAstraMekanized.LOGGER.info("Initialized planet biomes for TerraBlender integration");
        AdAstraMekanized.LOGGER.debug("Registered biomes: Mars (3), Moon (2), Venus (2)");
    }
}