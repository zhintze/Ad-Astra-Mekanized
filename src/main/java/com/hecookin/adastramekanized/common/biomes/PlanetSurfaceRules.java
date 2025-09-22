package com.hecookin.adastramekanized.common.biomes;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;

/**
 * Surface rules for planet-specific terrain generation
 * Phase 3.1 - Basic TerraBlender Integration
 */
public class PlanetSurfaceRules {

    /**
     * Create surface rules for Mars terrain
     */
    public static SurfaceRules.RuleSource makeMarsSurfaceRules() {
        AdAstraMekanized.LOGGER.debug("Creating Mars surface rules");

        // Mars-specific surface materials
        SurfaceRules.RuleSource marsHighlandStone = makeStateRule(Blocks.RED_TERRACOTTA);
        SurfaceRules.RuleSource marsValleyDust = makeStateRule(Blocks.RED_SAND);
        SurfaceRules.RuleSource marsPolarIce = makeStateRule(Blocks.PACKED_ICE);
        SurfaceRules.RuleSource marsSubsurface = makeStateRule(Blocks.TERRACOTTA);

        return SurfaceRules.sequence(
            // Polar regions get ice caps
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(PlanetBiomes.MARS_POLAR),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, marsPolarIce),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, marsSubsurface)
                )
            ),
            // Highlands get rocky terrain
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(PlanetBiomes.MARS_HIGHLANDS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, marsHighlandStone),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, marsSubsurface)
                )
            ),
            // Valleys get dust and sand
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(PlanetBiomes.MARS_VALLEYS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, marsValleyDust),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, marsSubsurface)
                )
            ),
            // Default Mars surface
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, marsValleyDust)
        );
    }

    /**
     * Create surface rules for Moon terrain
     */
    public static SurfaceRules.RuleSource makeMoonSurfaceRules() {
        AdAstraMekanized.LOGGER.debug("Creating Moon surface rules");

        // Moon-specific surface materials
        SurfaceRules.RuleSource lunarHighlandRock = makeStateRule(Blocks.LIGHT_GRAY_CONCRETE);
        SurfaceRules.RuleSource lunarMariaBasalt = makeStateRule(Blocks.GRAY_CONCRETE);
        SurfaceRules.RuleSource lunarRegolith = makeStateRule(Blocks.LIGHT_GRAY_CONCRETE_POWDER);
        SurfaceRules.RuleSource lunarSubsurface = makeStateRule(Blocks.STONE);

        return SurfaceRules.sequence(
            // Lunar highlands - bright, rocky terrain
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(PlanetBiomes.LUNAR_HIGHLANDS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, lunarHighlandRock),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, lunarSubsurface)
                )
            ),
            // Lunar maria - dark basaltic plains
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(PlanetBiomes.LUNAR_MARIA),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, lunarMariaBasalt),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, lunarSubsurface)
                )
            ),
            // Default lunar surface with regolith
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, lunarRegolith)
        );
    }

    /**
     * Create surface rules for Venus terrain
     */
    public static SurfaceRules.RuleSource makeVenusSurfaceRules() {
        AdAstraMekanized.LOGGER.debug("Creating Venus surface rules");

        // Venus-specific surface materials
        SurfaceRules.RuleSource venusSurfaceRock = makeStateRule(Blocks.YELLOW_TERRACOTTA);
        SurfaceRules.RuleSource venusVolcanicRock = makeStateRule(Blocks.MAGMA_BLOCK);
        SurfaceRules.RuleSource venusLava = makeStateRule(Blocks.LAVA);
        SurfaceRules.RuleSource venusSubsurface = makeStateRule(Blocks.ORANGE_TERRACOTTA);

        return SurfaceRules.sequence(
            // Volcanic regions - active lava and magma
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(PlanetBiomes.VENUS_VOLCANIC),
                SurfaceRules.sequence(
                    // Some areas have exposed lava (simplified condition)
                    SurfaceRules.ifTrue(
                        SurfaceRules.ON_CEILING,
                        venusLava
                    ),
                    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, venusVolcanicRock),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, venusSubsurface)
                )
            ),
            // Venus surface - hot, rocky terrain
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(PlanetBiomes.VENUS_SURFACE),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, venusSurfaceRock),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, venusSubsurface)
                )
            ),
            // Default Venus surface
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, venusSurfaceRock)
        );
    }

    /**
     * Helper method to create a state rule from a block
     */
    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    /**
     * Initialize all planet surface rules
     */
    public static void initialize() {
        AdAstraMekanized.LOGGER.info("Initialized planet surface rules for TerraBlender integration");
        AdAstraMekanized.LOGGER.debug("Surface rules created for: Mars, Moon, Venus");
    }
}