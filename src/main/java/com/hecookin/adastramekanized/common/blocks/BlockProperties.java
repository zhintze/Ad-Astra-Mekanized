package com.hecookin.adastramekanized.common.blocks;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Centralized block properties configuration for Ad Astra Mekanized.
 *
 * This class provides configurable block properties that can be easily adjusted
 * for testing and planet-specific variations. All hardness and resistance values
 * are collected here for easy modification.
 */
public class BlockProperties {

    // ========== CONFIGURABLE VALUES ==========

    // Metal Block Properties
    public static final float METAL_HARDNESS = 5.0f;
    public static final float METAL_RESISTANCE = 6.0f;
    public static final boolean METAL_REQUIRES_TOOL = true;

    // Raw Metal Block Properties
    public static final float RAW_METAL_HARDNESS = 5.0f;
    public static final float RAW_METAL_RESISTANCE = 6.0f;
    public static final boolean RAW_METAL_REQUIRES_TOOL = true;

    // Stone Properties (Planet Stones)
    public static final float PLANET_STONE_HARDNESS = 1.5f;
    public static final float PLANET_STONE_RESISTANCE = 6.0f;
    public static final boolean PLANET_STONE_REQUIRES_TOOL = true;

    // Industrial Block Properties
    public static final float INDUSTRIAL_HARDNESS = 3.5f;
    public static final float INDUSTRIAL_RESISTANCE = 8.0f;
    public static final boolean INDUSTRIAL_REQUIRES_TOOL = true;

    // Special Block Properties
    public static final float CHEESE_HARDNESS = 0.5f;
    public static final float CHEESE_RESISTANCE = 0.5f;
    public static final boolean CHEESE_REQUIRES_TOOL = false;

    public static final float SKY_STONE_HARDNESS = 2.0f;
    public static final float SKY_STONE_RESISTANCE = 6.0f;
    public static final boolean SKY_STONE_REQUIRES_TOOL = true;

    // Wood Properties (Alien Wood)
    public static final float ALIEN_WOOD_HARDNESS = 2.0f;
    public static final float ALIEN_WOOD_RESISTANCE = 3.0f;
    public static final boolean ALIEN_WOOD_REQUIRES_TOOL = false;

    // ========== PROPERTY BUILDERS ==========

    /**
     * Standard metal block properties (ingot blocks)
     */
    public static final BlockBehaviour.Properties METAL_BLOCK = BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(METAL_HARDNESS, METAL_RESISTANCE)
            .sound(SoundType.METAL);

    /**
     * Raw metal block properties (raw ore blocks)
     */
    public static final BlockBehaviour.Properties RAW_METAL_BLOCK = BlockBehaviour.Properties.of()
            .mapColor(MapColor.RAW_IRON)
            .requiresCorrectToolForDrops()
            .strength(RAW_METAL_HARDNESS, RAW_METAL_RESISTANCE)
            .sound(SoundType.STONE);

    /**
     * Planet stone properties (base template)
     */
    public static final BlockBehaviour.Properties PLANET_STONE = BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .requiresCorrectToolForDrops()
            .strength(PLANET_STONE_HARDNESS, PLANET_STONE_RESISTANCE)
            .sound(SoundType.STONE);

    /**
     * Industrial block properties (factory blocks, plating, etc.)
     */
    public static final BlockBehaviour.Properties INDUSTRIAL_BLOCK = BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(INDUSTRIAL_HARDNESS, INDUSTRIAL_RESISTANCE)
            .sound(SoundType.METAL);

    /**
     * Cheese block properties
     */
    public static final BlockBehaviour.Properties CHEESE_BLOCK = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_YELLOW)
            .strength(CHEESE_HARDNESS, CHEESE_RESISTANCE)
            .sound(SoundType.SLIME_BLOCK);

    /**
     * Sky stone properties
     */
    public static final BlockBehaviour.Properties SKY_STONE = BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .requiresCorrectToolForDrops()
            .strength(SKY_STONE_HARDNESS, SKY_STONE_RESISTANCE)
            .sound(SoundType.STONE);

    /**
     * Alien wood properties (mushroom wood)
     */
    public static final BlockBehaviour.Properties ALIEN_WOOD = BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(ALIEN_WOOD_HARDNESS, ALIEN_WOOD_RESISTANCE)
            .sound(SoundType.WOOD)
            .ignitedByLava();

    // ========== PLANET-SPECIFIC VARIANTS ==========

    /**
     * Get planet-specific stone properties with adjusted hardness
     * @param planetHardnessMultiplier Multiplier for planet-specific hardness
     * @param planetMapColor Map color for the planet stone
     */
    public static BlockBehaviour.Properties getPlanetStoneProperties(float planetHardnessMultiplier, MapColor planetMapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(planetMapColor)
                .requiresCorrectToolForDrops()
                .strength(PLANET_STONE_HARDNESS * planetHardnessMultiplier, PLANET_STONE_RESISTANCE)
                .sound(SoundType.STONE);
    }

    /**
     * Planet-specific property constants for easy adjustment
     */
    public static class PlanetMultipliers {
        public static final float MOON_HARDNESS_MULTIPLIER = 0.8f;      // Softer due to low gravity
        public static final float MARS_HARDNESS_MULTIPLIER = 1.0f;      // Standard hardness
        public static final float VENUS_HARDNESS_MULTIPLIER = 1.2f;     // Harder due to extreme conditions
        public static final float MERCURY_HARDNESS_MULTIPLIER = 1.5f;   // Very hard due to extreme heat
        public static final float GLACIO_HARDNESS_MULTIPLIER = 0.9f;    // Slightly softer due to ice
    }

    /**
     * Planet-specific map colors
     */
    public static class PlanetColors {
        public static final MapColor MOON_COLOR = MapColor.QUARTZ;
        public static final MapColor MARS_COLOR = MapColor.TERRACOTTA_RED;
        public static final MapColor VENUS_COLOR = MapColor.TERRACOTTA_ORANGE;
        public static final MapColor MERCURY_COLOR = MapColor.COLOR_GRAY;
        public static final MapColor GLACIO_COLOR = MapColor.ICE;
    }

    // ========== CONVENIENCE METHODS ==========

    /**
     * Create Moon stone properties
     */
    public static BlockBehaviour.Properties moonStone() {
        return getPlanetStoneProperties(PlanetMultipliers.MOON_HARDNESS_MULTIPLIER, PlanetColors.MOON_COLOR);
    }

    /**
     * Create Mars stone properties
     */
    public static BlockBehaviour.Properties marsStone() {
        return getPlanetStoneProperties(PlanetMultipliers.MARS_HARDNESS_MULTIPLIER, PlanetColors.MARS_COLOR);
    }

    /**
     * Create Venus stone properties
     */
    public static BlockBehaviour.Properties venusStone() {
        return getPlanetStoneProperties(PlanetMultipliers.VENUS_HARDNESS_MULTIPLIER, PlanetColors.VENUS_COLOR);
    }

    /**
     * Create Mercury stone properties
     */
    public static BlockBehaviour.Properties mercuryStone() {
        return getPlanetStoneProperties(PlanetMultipliers.MERCURY_HARDNESS_MULTIPLIER, PlanetColors.MERCURY_COLOR);
    }

    /**
     * Create Glacio stone properties
     */
    public static BlockBehaviour.Properties glacioStone() {
        return getPlanetStoneProperties(PlanetMultipliers.GLACIO_HARDNESS_MULTIPLIER, PlanetColors.GLACIO_COLOR);
    }
}