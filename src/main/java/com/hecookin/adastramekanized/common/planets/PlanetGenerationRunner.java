package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;

/**
 * Runner to generate planet files using PlanetMaker system
 */
public class PlanetGenerationRunner {
    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");

        try {
            AdAstraMekanized.LOGGER.info("Starting planet generation with PlanetMaker...");

            // Configure planets using builder pattern
            configurePlanets();

            // Generate all configured planets
            PlanetMaker.generateAllPlanets();

            AdAstraMekanized.LOGGER.info("Planet generation completed successfully!");
        } catch (Exception e) {
            System.err.println("Failed to generate planets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure planets using PlanetMaker builder pattern
     */
    private static void configurePlanets() {
        // Moon planet with advanced terrain controls - craterous lunar landscape
        PlanetMaker.planet("moon")
            .continentalScale(2.5f)
            .erosionScale(8f)
            .ridgeScale(3f)
            .heightVariation(4f, 2.5f, 1.0f, 0.6f)
            // Advanced noise parameters for crater-like terrain
            .temperatureScale(0.5f)
            .humidityScale(0.2f)
            .weirdnessScale(1.5f)
            .densityFactor(1.2f)
            .densityOffset(-0.1f)
            // Custom vertical gradient for sharper craters
            .verticalGradient(-64, 320, 2.0f, -2.0f)
            .gradientMultiplier(0.8f)
            // Enhanced terrain shaping for lunar features
            .initialDensityOffset(-0.3f)
            .terrainShapingFactor(0.2f)
            // Hill/mountain generation for crater rims and lunar highlands
            .jaggednessScale(0.3f)          // Moderate jaggedness for crater rims
            .jaggednessNoiseScale(800.0f)   // Medium-scale for crater features
            .depthFactor(1.2f)              // Slightly enhanced depth variation
            .depthOffset(-0.1f)             // Lower baseline for crater floors
            .terrainFactor(1.1f)            // Slightly enhanced terrain
            // Surface configuration
            .surfaceBlock("adastramekanized:moon_sand")
            .subsurfaceBlock("adastramekanized:moon_stone")
            .deepBlock("adastramekanized:moon_deepslate")
            .defaultBlock("adastramekanized:moon_deepslate")
            // World structure
            .worldDimensions(-64, 384)
            .noiseSize(2, 1)
            .seaLevel(63)
            .disableMobGeneration(true)
            .aquifersEnabled(false)
            // Visual properties
            .skyColor(0x0A0A0A)
            .fogColor(0x0A0A0A)
            .hasAtmosphere(false)
            .ambientLight(0.1f)
            .generate();

        // Mars planet with advanced atmospheric controls and varied terrain
        PlanetMaker.planet("mars")
            .continentalScale(1.5f)
            .erosionScale(15f)
            .ridgeScale(2f)
            .heightVariation(3f, 2f, 0.8f, 0.5f)
            // Advanced atmospheric noise for dust storms and weather
            .temperatureNoise(0.3f)
            .vegetationNoise(0.1f)
            .barrierNoise(0.2f)
            // Fluid dynamics for dust/atmosphere interaction
            .fluidLevelFloodedness(0.1f)
            .fluidLevelSpread(0.05f)
            // Enhanced ore generation for Mars minerals
            .veinToggle(0.8f)
            .veinRidged(0.6f)
            .veinGap(0.4f)
            // Dramatic mountain generation for Martian landscape (based on Ad Astra research)
            .jaggednessScale(0.7f)          // High jaggedness for sharp mountain peaks
            .jaggednessNoiseScale(1200.0f)  // High-scale dramatic terrain
            .depthFactor(2.0f)              // Double depth variation for canyons/mountains
            .depthOffset(0.2f)              // Elevated baseline for Martian highlands
            .terrainFactor(1.8f)            // Intense terrain features
            .base3DNoiseScale(0.15f, 0.1f)  // Tighter noise for sharper features
            .base3DNoiseFactor(120.0f, 110.0f) // Higher amplitude for dramatic terrain
            // Surface and depth configuration
            .surfaceBlock("adastramekanized:mars_sand")
            .subsurfaceBlock("adastramekanized:mars_stone")
            .deepBlock("minecraft:stone")
            .defaultBlock("minecraft:stone")
            .seaLevel(48)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // Enhanced atmospheric rendering
            .skyColor(0xD2691E)
            .fogColor(0xCD853F)
            .hasAtmosphere(true)
            .ambientLight(0.2f)
            .generate();


        // HEMPHY PLANET - ABSOLUTE STRESS TEST OF ALL GENERATION LIMITS
        // WARNING: This planet pushes EVERY parameter to extreme values!
        // Use this as a reference for the maximum safe bounds of each setting.
        PlanetMaker.planet("hemphy")
                // ========== EXTREME TERRAIN SHAPING ==========
                .continentalScale(50.0f)        // MAX: Massive continental variations
                .erosionScale(100.0f)           // MAX: Extreme erosion patterns
                .ridgeScale(25.0f)              // MAX: Towering ridge formations
                .heightVariation(50f, 25f, 15f, 10f)  // MAX: All height variations pushed to limits

                // ========== EXTREME NOISE PARAMETERS ==========
                .temperatureScale(10.0f)        // MAX: Wild temperature variations
                .humidityScale(8.0f)            // MAX: Extreme humidity gradients
                .weirdnessScale(15.0f)          // MAX: Maximum terrain weirdness
                .densityFactor(5.0f)            // MAX: Extreme density multiplication
                .densityOffset(2.0f)            // MAX: Maximum density offset

                // ========== MAXED NOISE ROUTING ==========
                .barrierNoise(1.0f)             // MAX: Full barrier effects
                .fluidLevelFloodedness(1.0f)    // MAX: Complete floodedness
                .fluidLevelSpread(1.0f)         // MAX: Maximum fluid spread
                .lavaNoise(1.0f)                // MAX: Lava everywhere
                .temperatureNoise(1.0f)         // MAX: Extreme temperature noise
                .vegetationNoise(1.0f)          // MAX: Wild vegetation patterns

                // ========== EXTREME VERTICAL GRADIENTS ==========
                .verticalGradient(-128, 512, 10.0f, -10.0f)  // MAX: Extreme height range & gradient
                .gradientMultiplier(3.0f)       // MAX: Triple gradient strength

                // ========== EXTREME TERRAIN SHAPING ==========
                .initialDensityOffset(-1.0f)    // MAX: Negative density offset
                .terrainShapingFactor(1.0f)     // MAX: Maximum terrain complexity
                .legacyRandomSource(true)       // MAX: Legacy mode enabled

                // ========== EXTREME HILL/MOUNTAIN GENERATION ==========
                .jaggednessScale(1.0f)          // MAX: Maximum mountain jaggedness
                .jaggednessNoiseScale(2000.0f)  // MAX: Ultra-high scale terrain (limit test)
                .depthFactor(5.0f)              // MAX: Extreme depth variation
                .depthOffset(1.0f)              // MAX: Maximum elevation offset
                .terrainFactor(3.0f)            // MAX: Triple terrain intensity
                .base3DNoiseScale(0.1f, 0.05f)  // MAX: Tightest noise for sharpest features
                .base3DNoiseFactor(200.0f, 180.0f) // MAX: Extreme amplitude for alien terrain
                .smearScaleMultiplier(20.0f)    // MAX: Maximum terrain smoothing factor

                // ========== EXOTIC SURFACE CONFIGURATION ==========
                .surfaceBlock("minecraft:magma_block")      // Dangerous surface
                .subsurfaceBlock("minecraft:netherrack")    // Hellish subsurface
                .deepBlock("minecraft:blackstone")          // Dark depths
                .defaultBlock("minecraft:crying_obsidian")  // Alien default material
                .defaultFluid("minecraft:lava")             // Lava world
                .underwaterBlock("minecraft:obsidian")      // Underwater areas
                .bedrockBlock("minecraft:crying_obsidian")  // Custom bedrock
                // Prevent unwanted default blocks from appearing
                .preventGrassGeneration(true)               // MAX: No grass on alien world
                .preventGravelGeneration(true)              // MAX: No gravel generation
                .preventSandGeneration(true)                // MAX: No sand generation
                .disableDefaultSurfaceGeneration(true)      // MAX: Full surface control

                // ========== EXTREME WORLD DIMENSIONS ==========
                .worldDimensions(-128, 512)     // MAX: Extended world height (640 total)
                .noiseSize(4, 4)                // MAX: Highest resolution noise (4,4 is maximum)
                .seaLevel(0)                    // MIN: No sea level (all lava)

                // ========== EXTREME GENERATION CONTROLS ==========
                .disableMobGeneration(true)     // MAX: No mobs (too dangerous)
                .aquifersEnabled(true)          // MAX: Full aquifer generation
                .oreVeinsEnabled(true)          // MAX: Maximum ore generation
                .abovePreliminaryRule(false)    // MAX: Disable surface smoothing
                .waterRule(false)               // MAX: No water rules (lava only)
                .surfaceDepthMultiplier(10)     // MAX: Deep surface layers
                .addStoneDepth(true)            // MAX: Extra stone depth

                // ========== MAXED VEIN GENERATION ==========
                .veinToggle(1.0f)               // MAX: Full vein activation
                .veinRidged(1.0f)               // MAX: Maximum ridged veins
                .veinGap(0.0f)                  // MIN: No gaps (solid veins)
                // Enhanced ore vein configuration
                .oreVeinDensity(3.0f)           // MAX: Triple ore density
                .oreVeinSize(2.0f)              // MAX: Double vein size
                .maxOreVeinCount(50)            // MAX: 50 different ore types
                .enableRareOres(true)           // MAX: All rare ores
                .enableCommonOres(true)         // MAX: All common ores
                .enableDeepslateOres(true)      // MAX: All deepslate variants
                .addCustomOreVein("minecraft:diamond_ore")
                .addCustomOreVein("minecraft:emerald_ore")
                .addCustomOreVein("minecraft:ancient_debris")
                .addCustomOreVein("minecraft:gold_ore")
                .addCustomOreVein("minecraft:iron_ore")
                .addCustomOreVein("minecraft:copper_ore")
                .addCustomOreVein("minecraft:redstone_ore")
                .addCustomOreVein("minecraft:lapis_ore")

                // ========== EXTREME BIOME DISTRIBUTION ==========
                .biomeDistribution(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f)  // MAX: All parameters at maximum

                // ========== ALIEN VISUAL EXTREMES ==========
                .skyColor(0xFF00FF)             // MAX: Magenta alien sky
                .fogColor(0x00FFFF)             // MAX: Cyan alien fog
                .hasAtmosphere(true)            // MAX: Thick alien atmosphere
                .ambientLight(1.0f)             // MAX: Always bright (alien sun)

                // ========== DYNAMIC BIOME SYSTEM - VOLCANIC HELLSCAPE ==========
                .clearBiomes()                   // Clear any default biomes
                .addBiome("minecraft:basalt_deltas", 0.35f)     // Volcanic basalt regions
                .addBiome("minecraft:crimson_forest", 0.25f)    // Alien crimson growth
                .addBiome("minecraft:warped_forest", 0.20f)     // Twisted alien forest
                .addBiome("minecraft:soul_sand_valley", 0.15f)  // Deep soul valleys
                .addBiome("minecraft:nether_wastes", 0.05f)     // Desolate wastelands

                // ========== LIQUID SYSTEM - LAVA OCEANS & LAKES ==========
                .oceanConfig("minecraft:lava", 32, 0.6f)        // Massive lava oceans at Y=32
                .lakeConfig("minecraft:lava", 0.3f)             // Frequent lava lakes
                .lavaLakes(-20, 0.4f)                           // Deep lava pools
                .undergroundLiquids("minecraft:lava", true)     // Lava aquifers
                .generate();

        // ========== ORE TEST PLANET - OVERWORLD-LIKE SETTINGS ==========
        // Adjusted for realistic Overworld-style terrain with proper ore generation
        PlanetMaker.planet("oretest")
                // ========== REALISTIC TERRAIN SHAPING ==========
                .continentalScale(8.0f)         // Smaller scale for varied landmasses
                .erosionScale(12.0f)            // Moderate erosion for natural valleys
                .ridgeScale(5.0f)               // Gentler ridge formations
                .heightVariation(0.8f, 0.5f, 0.3f, 0.2f) // Much lower values for realistic hills

                // ========== STANDARD NOISE ROUTING ==========
                .barrierNoise(0.0f)             // No barriers for natural flow
                .fluidLevelFloodedness(0.3f)    // Some water areas
                .fluidLevelSpread(0.2f)         // Normal fluid spread
                .lavaNoise(0.0f)                // No surface lava
                .temperatureNoise(0.2f)         // Mild temperature variation
                .vegetationNoise(0.3f)          // More vegetation

                // ========== OVERWORLD-LIKE GRADIENTS ==========
                .verticalGradient(-64, 320, 1.5f, -1.5f) // Standard gradient
                .gradientMultiplier(0.64f)      // Vanilla-like multiplier
                .initialDensityOffset(-0.15f)   // Slight negative offset for caves
                .terrainShapingFactor(0.25f)    // Reduced for more variation

                // ========== NATURAL HILLS AND MOUNTAINS ==========
                .jaggednessScale(0.15f)         // Gentle mountain jaggedness
                .jaggednessNoiseScale(600.0f)   // Smaller scale for rolling hills
                .depthFactor(1.5f)              // Enhanced depth for valleys
                .depthOffset(0.0f)              // No offset
                .terrainFactor(0.8f)            // Slightly reduced terrain intensity

                // ========== EARTH-LIKE SURFACE BLOCKS ==========
                .surfaceBlock("minecraft:grass_block")      // AVERAGE: Natural grass
                .subsurfaceBlock("minecraft:dirt")          // AVERAGE: Dirt subsurface
                .deepBlock("minecraft:stone")               // AVERAGE: Stone deep
                .underwaterBlock("minecraft:cobblestone")        // AVERAGE: Gravel underwater
                .shallowUnderwaterBlock("minecraft:sand")   // AVERAGE: Sand shallow
                .deepUnderwaterBlock("minecraft:clay")      // AVERAGE: Clay deep
                .bedrockBlock("minecraft:bedrock")          // AVERAGE: Standard bedrock

                // ========== ALLOW NATURAL BLOCKS ==========
                .preventGrassGeneration(false)  // AVERAGE: Allow natural grass
                .preventGravelGeneration(false) // AVERAGE: Allow natural gravel
                .preventSandGeneration(false)   // AVERAGE: Allow natural sand
                .disableDefaultSurfaceGeneration(false) // AVERAGE: Use natural surface

                // ========== STANDARD WORLD ==========
                .worldDimensions(-64, 384)      // AVERAGE: Standard world height
                .noiseSize(2, 1)                // AVERAGE: Standard noise resolution
                .seaLevel(64)                   // AVERAGE: Normal sea level

                // ========== BALANCED GENERATION ==========
                .disableMobGeneration(false)    // AVERAGE: Allow mobs
                .aquifersEnabled(true)          // AVERAGE: Normal aquifers
                .oreVeinsEnabled(true)          // AVERAGE: Standard ore veins
                .abovePreliminaryRule(true)     // AVERAGE: Use surface rules
                .waterRule(true)                // AVERAGE: Normal water
                .surfaceDepthMultiplier(1)      // AVERAGE: Standard surface depth
                .addStoneDepth(false)           // AVERAGE: No extra stone

                // ========== VANILLA-STYLE ORE GENERATION ==========
                .veinToggle(0.0f)               // Disabled to prevent surface ore rules
                .veinRidged(0.0f)               // No custom ridged veins
                .veinGap(0.0f)                  // No custom gaps

                // ========== VANILLA ORE CONFIGURATION ==========
                .oreVeinDensity(1.0f)           // Normal ore density
                .oreVeinSize(1.0f)              // Standard vein size
                .maxOreVeinCount(0)             // No custom ore veins (use vanilla)
                .enableRareOres(false)          // Let vanilla handle ore distribution
                .enableCommonOres(false)        // Let vanilla handle ore distribution
                .enableDeepslateOres(true)      // Enable deepslate variants at depth
                // No custom ore veins - vanilla generation will handle underground ores

                // ========== BALANCED BIOME DISTRIBUTION ==========
                .biomeDistribution(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f) // AVERAGE: All moderate

                // ========== EARTH-LIKE VISUALS ==========
                .skyColor(0x87CEEB)             // AVERAGE: Sky blue
                .fogColor(0xC0C0C0)             // AVERAGE: Light gray fog
                .hasAtmosphere(true)            // AVERAGE: Normal atmosphere
                .ambientLight(0.8f)             // AVERAGE: Slightly dimmer than Earth

                // ========== DYNAMIC BIOME SYSTEM - EARTH-LIKE VARIETY ==========
                .clearBiomes()                   // Clear default biomes
                .addBiome("minecraft:plains", 0.30f)             // Common plains
                .addBiome("minecraft:forest", 0.25f)             // Temperate forests
                .addBiome("minecraft:taiga", 0.15f)              // Cooler taiga regions
                .addBiome("minecraft:savanna", 0.10f)            // Dry savanna areas
                .addBiome("minecraft:snowy_plains", 0.08f)       // Cold regions
                .addBiome("minecraft:swamp", 0.07f)              // Wet swamplands
                .addBiome("minecraft:desert", 0.05f)             // Small desert areas

                // ========== LIQUID SYSTEM - EARTH-LIKE WATER FEATURES ==========
                .oceanConfig("minecraft:water", 64, 0.4f)        // Normal water oceans at Y=64
                .lakeConfig("minecraft:water", 0.15f)            // Occasional lakes
                .lavaLakes(-55, 0.02f)                           // Rare deep lava pools
                .undergroundLiquids("minecraft:water", true)     // Water aquifers
                .generate();

    }
}