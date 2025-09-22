package com.hecookin.adastramekanized.common.biomes;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.config.StaticDimensionManager;
import com.hecookin.adastramekanized.common.config.StaticPlanetConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.HashMap;
import java.util.Map;

/**
 * Template-based surface rule system for automatic planet terrain generation
 * Generates surface rules dynamically based on planet type and configuration
 */
public class TemplateSurfaceRules {

    /**
     * Generate surface rules for a planet based on its configuration
     */
    public static SurfaceRules.RuleSource generatePlanetSurfaceRules(ResourceLocation planetId) {
        StaticPlanetConfig.PlanetDefinition config = StaticPlanetConfig.getPlanetConfig(planetId);

        if (config == null) {
            AdAstraMekanized.LOGGER.warn("No configuration found for planet {}, using default surface rules", planetId);
            return createDefaultSurfaceRules();
        }

        return switch (config.getPlanetType()) {
            case LUNAR -> createLunarSurfaceRules(config);
            case VOLCANIC -> createVolcanicSurfaceRules(config);
            case ROCKY -> createRockySurfaceRules(config);
            case ICY -> createIcySurfaceRules(config);
            case DESERT -> createDesertSurfaceRules(config);
            case ASTEROID -> createAsteroidSurfaceRules(config);
        };
    }

    /**
     * Create lunar-style surface rules (gray, cratered terrain)
     */
    private static SurfaceRules.RuleSource createLunarSurfaceRules(StaticPlanetConfig.PlanetDefinition config) {
        AdAstraMekanized.LOGGER.debug("Creating lunar surface rules for planet: {}", config.getPlanetId());

        StaticPlanetConfig.TerrainConfig terrain = config.getTerrain();
        SurfaceRules.RuleSource surfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSurfaceMaterial()));
        SurfaceRules.RuleSource subsurfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSubsurfaceMaterial()));
        SurfaceRules.RuleSource deepBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getDeepMaterial()));

        return SurfaceRules.sequence(
            // Main lunar terrain - simple approach that works
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, surfaceBlock),
            SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, subsurfaceBlock),
            // Deep material default
            deepBlock
        );
    }

    /**
     * Create volcanic-style surface rules (lava, magma, volcanic rock)
     */
    private static SurfaceRules.RuleSource createVolcanicSurfaceRules(StaticPlanetConfig.PlanetDefinition config) {
        AdAstraMekanized.LOGGER.debug("Creating volcanic surface rules for planet: {}", config.getPlanetId());

        StaticPlanetConfig.TerrainConfig terrain = config.getTerrain();
        SurfaceRules.RuleSource surfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSurfaceMaterial()));
        SurfaceRules.RuleSource subsurfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSubsurfaceMaterial()));
        SurfaceRules.RuleSource deepBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getDeepMaterial()));

        return SurfaceRules.sequence(
            // Volcanic terrain - simple approach
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, surfaceBlock),
            SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, subsurfaceBlock),
            deepBlock
        );
    }

    /**
     * Create rocky-style surface rules (standard terrestrial planet)
     */
    private static SurfaceRules.RuleSource createRockySurfaceRules(StaticPlanetConfig.PlanetDefinition config) {
        AdAstraMekanized.LOGGER.debug("Creating rocky surface rules for planet: {}", config.getPlanetId());

        StaticPlanetConfig.TerrainConfig terrain = config.getTerrain();
        SurfaceRules.RuleSource surfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSurfaceMaterial()));
        SurfaceRules.RuleSource subsurfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSubsurfaceMaterial()));
        SurfaceRules.RuleSource deepBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getDeepMaterial()));

        return SurfaceRules.sequence(
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, surfaceBlock),
            SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, subsurfaceBlock),
            deepBlock
        );
    }

    /**
     * Create icy-style surface rules (frozen terrain)
     */
    private static SurfaceRules.RuleSource createIcySurfaceRules(StaticPlanetConfig.PlanetDefinition config) {
        AdAstraMekanized.LOGGER.debug("Creating icy surface rules for planet: {}", config.getPlanetId());

        StaticPlanetConfig.TerrainConfig terrain = config.getTerrain();
        SurfaceRules.RuleSource surfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSurfaceMaterial()));
        SurfaceRules.RuleSource subsurfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSubsurfaceMaterial()));
        SurfaceRules.RuleSource deepBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getDeepMaterial()));

        return SurfaceRules.sequence(
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, surfaceBlock),
            SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, subsurfaceBlock),
            deepBlock
        );
    }

    /**
     * Create desert-style surface rules (sandy, arid terrain)
     */
    private static SurfaceRules.RuleSource createDesertSurfaceRules(StaticPlanetConfig.PlanetDefinition config) {
        AdAstraMekanized.LOGGER.debug("Creating desert surface rules for planet: {}", config.getPlanetId());

        StaticPlanetConfig.TerrainConfig terrain = config.getTerrain();
        SurfaceRules.RuleSource surfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSurfaceMaterial()));
        SurfaceRules.RuleSource subsurfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSubsurfaceMaterial()));
        SurfaceRules.RuleSource deepBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getDeepMaterial()));

        return SurfaceRules.sequence(
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, surfaceBlock),
            SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, subsurfaceBlock),
            deepBlock
        );
    }

    /**
     * Create asteroid-style surface rules (rocky, sparse terrain)
     */
    private static SurfaceRules.RuleSource createAsteroidSurfaceRules(StaticPlanetConfig.PlanetDefinition config) {
        AdAstraMekanized.LOGGER.debug("Creating asteroid surface rules for planet: {}", config.getPlanetId());

        StaticPlanetConfig.TerrainConfig terrain = config.getTerrain();
        SurfaceRules.RuleSource surfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSurfaceMaterial()));
        SurfaceRules.RuleSource subsurfaceBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getSubsurfaceMaterial()));
        SurfaceRules.RuleSource deepBlock = makeStateRule(StaticDimensionManager.getBlock(terrain.getDeepMaterial()));

        return SurfaceRules.sequence(
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, surfaceBlock),
            SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, subsurfaceBlock),
            deepBlock
        );
    }

    /**
     * Create default surface rules fallback
     */
    private static SurfaceRules.RuleSource createDefaultSurfaceRules() {
        AdAstraMekanized.LOGGER.debug("Creating default surface rules");

        return SurfaceRules.sequence(
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, makeStateRule(Blocks.STONE)),
            makeStateRule(Blocks.DEEPSLATE)
        );
    }

    /**
     * Helper method to create a state rule from a block
     */
    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    /**
     * Helper method to create biome resource key from string
     */
    private static ResourceKey<Biome> createBiomeKey(String biomeId) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.parse(biomeId));
    }

    /**
     * Generate surface rules for all configured planets
     */
    public static Map<String, SurfaceRules.RuleSource> generateAllPlanetSurfaceRules() {
        Map<String, SurfaceRules.RuleSource> surfaceRules = new HashMap<>();

        for (String planetId : StaticPlanetConfig.getAllPlanetConfigs().keySet()) {
            ResourceLocation resourceLocation = ResourceLocation.parse(planetId);
            SurfaceRules.RuleSource rules = generatePlanetSurfaceRules(resourceLocation);
            surfaceRules.put(planetId, rules);
            AdAstraMekanized.LOGGER.debug("Generated surface rules for planet: {}", planetId);
        }

        return surfaceRules;
    }

    /**
     * Get surface rules for a specific planet
     */
    public static SurfaceRules.RuleSource getPlanetSurfaceRules(ResourceLocation planetId) {
        return generatePlanetSurfaceRules(planetId);
    }

    /**
     * Check if a planet should use template-based surface rules
     */
    public static boolean shouldUseTemplateSurfaceRules(ResourceLocation planetId) {
        return StaticPlanetConfig.getPlanetConfig(planetId) != null;
    }

    /**
     * Initialize the template surface rule system
     */
    public static void initialize() {
        AdAstraMekanized.LOGGER.info("Initialized Template Surface Rule system");

        // Generate surface rules for all configured planets
        Map<String, SurfaceRules.RuleSource> allRules = generateAllPlanetSurfaceRules();
        AdAstraMekanized.LOGGER.info("Generated surface rules for {} planets", allRules.size());

        for (String planetId : allRules.keySet()) {
            AdAstraMekanized.LOGGER.debug("  Surface rules available for: {}", planetId);
        }
    }
}