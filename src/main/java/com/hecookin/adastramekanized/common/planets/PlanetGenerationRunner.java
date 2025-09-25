package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;

/**
 * Runner to generate planet files using PlanetMaker system
 */
public class PlanetGenerationRunner {

    /**
     * Regenerate all planet files - call this to update planet configurations
     */
    public static void regeneratePlanets() {
        try {
            AdAstraMekanized.LOGGER.info("Regenerating planet files with PlanetMaker...");

            // Configure planets using builder pattern
            configurePlanets();

            // Generate all configured planets
            PlanetMaker.generateAllPlanets();

            AdAstraMekanized.LOGGER.info("Planet regeneration completed successfully!");
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to regenerate planets: ", e);
        }
    }

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
     * Apply Moon mob preset
     */
    private static void applyMoonMobPreset(PlanetMaker.PlanetBuilder planet) {
        // Increased spawn rates for testing - hostile mobs
        planet.addMobSpawn("monster", "minecraft:enderman", 100, 2, 4)    // More common for testing
              .addMobSpawn("monster", "minecraft:phantom", 50, 1, 3)       // More phantoms
              .addMobSpawn("monster", "minecraft:husk", 200, 3, 5)         // Very common husks
              .addMobSpawn("monster", "minecraft:zombie", 150, 2, 4)       // Add regular zombies for testing
              .addMobSpawn("monster", "minecraft:spider", 120, 2, 3)       // Add spiders for testing
              .addMobSpawn("monster", "minecraft:skeleton", 100, 2, 3);    // Add skeletons for testing
    }

    /**
     * Apply Mars mob preset
     */
    private static void applyMarsMobPreset(PlanetMaker.PlanetBuilder planet) {
        // Hostile mobs for Mars - harsh environment
        planet.addMobSpawn("monster", "minecraft:husk", 30, 2, 4)
              .addMobSpawn("monster", "minecraft:spider", 20, 1, 2)
              .addMobSpawn("monster", "minecraft:phantom", 10, 1, 2)
              .addMobSpawn("monster", "minecraft:enderman", 5, 1, 1);
    }

    /**
     * Apply test planet mob preset
     */
    private static void applyTestPlanetMobPreset(PlanetMaker.PlanetBuilder planet) {
        // Standard overworld mobs for testing
        planet.addMobSpawn("monster", "minecraft:zombie", 25, 2, 4)
              .addMobSpawn("monster", "minecraft:skeleton", 25, 2, 4)
              .addMobSpawn("monster", "minecraft:creeper", 25, 1, 2)
              .addMobSpawn("monster", "minecraft:spider", 25, 1, 3)
              .addMobSpawn("creature", "minecraft:sheep", 10, 2, 3)
              .addMobSpawn("creature", "minecraft:pig", 10, 2, 3);
    }

    /**
     * Configure planets using PlanetMaker builder pattern
     */
    private static void configurePlanets() {
        // Moon planet with advanced terrain controls - craterous lunar landscape
        PlanetMaker.PlanetBuilder moon = PlanetMaker.planet("moon")
            // Gentle, connected terrain for Moon
            .continentalScale(0.3f)  // Very low for fully connected terrain
            .erosionScale(0.5f)      // Minimal erosion for smooth landmasses
            .ridgeScale(0.1f)        // Almost no ridges for gentle hills
            .heightVariation(0.8f, 0.5f, 0.3f, 0.2f)  // Much gentler height variation
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
            // Surface configuration - use vanilla blocks for ore compatibility
            .surfaceBlock("adastramekanized:moon_sand")
            .subsurfaceBlock("adastramekanized:moon_stone")  // Use vanilla stone for ore compatibility
            .deepBlock("minecraft:deepslate")
            .defaultBlock("adastramekanized:moon_stone")  // Use vanilla stone for ore compatibility
            .bedrockBlock("minecraft:bedrock")  // Add bedrock floor for moon
            // World structure
            .worldDimensions(-32, 256)  // Reduced underground space
            .noiseSize(2, 1)
            .seaLevel(63)
            .disableMobGeneration(false)  // Enable mob generation
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)  // Enable ore generation
            // Ore vein configuration for Moon minerals - balanced for survival
            .configureOre("iron", 8)        // Less iron on Moon
            .configureOre("copper", 6)      // Some copper
            .configureOre("gold", 3)        // Rare gold
            .configureOre("diamond", 1)     // Very rare diamonds
            .configureOre("coal", 0)        // No coal on Moon (no organic matter)
            .configureOre("redstone", 4)    // Some redstone
            .veinToggle(0.7f)  // Enable ore veins
            .veinRidged(0.5f)  // Some ridged veins
            .veinGap(0.4f)     // Moderate vein gaps
            // CAVES DISABLED for stable terrain
            .caveConfig(0.0f, 0.0f)  // No caves
            .cheeseCaves(false)      // Disabled
            .spaghettiCaves(false)   // Disabled
            .noodleCaves(false)      // Disabled
            // Custom Moon biomes (will be properly created)
            .clearBiomes()  // Clear default biomes
            .addBiome("adastramekanized:moon_highlands", -0.8f, -0.9f, 0.4f, 0.2f, 0.0f, 0.1f)  // Lunar highlands
            .addBiome("adastramekanized:moon_maria", -0.6f, -0.8f, -0.2f, 0.3f, -0.5f, -0.1f)  // Lunar lowlands
            .addBiome("adastramekanized:moon_craters", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)  // Crater biome
            // Visual properties
            .skyColor(0x0A0A0A)
            .fogColor(0x0A0A0A)
            .hasAtmosphere(false)
            .ambientLight(0.04f)  // No ambient light for proper mob spawning
            // Sun and spawn settings
            .hasSkylight(false)  // No sun damage on the Moon
            .monsterSpawnLightLevel(15);  // Monsters spawn in any light level

        // Apply Moon mob preset
        applyMoonMobPreset(moon);
        moon.generate();

        // CAVETEST PLANET - Extreme cave generation test
        PlanetMaker.PlanetBuilder cavetest = PlanetMaker.planet("cavetest")
            // Moderate terrain for cave visibility
            .continentalScale(0.5f)
            .erosionScale(1.0f)
            .ridgeScale(0.3f)
            .heightVariation(2f, 1f, 0.5f, 0.3f)
            // Surface configuration
            .surfaceBlock("minecraft:light_blue_terracotta")
            .subsurfaceBlock("minecraft:blue_terracotta")
            .deepBlock("minecraft:black_terracotta")
            .defaultBlock("minecraft:blue_terracotta")
            .underwaterBlock("minecraft:blue_terracotta")  // Set underwater block to match defaultBlock
            .bedrockBlock("minecraft:bedrock")
            // World structure
            .worldDimensions(-64, 320)
            .noiseSize(2, 2)
            .seaLevel(64)  // Much lower sea level to expose caves
            .disableMobGeneration(false)
            .aquifersEnabled(true)  // Enable water in caves
            .oreVeinsEnabled(true)
            // EXTREME CAVE SETTINGS
            .caveConfig(1.0f, 3.0f)  // Maximum frequency and size
            .caveYScale(0.2f)  // Tall caves
            .caveHeightRange(-64, 50)  // Caves throughout world
            .cheeseCaves(false)  // Enable all cave types
            .spaghettiCaves(true)
            .noodleCaves(true)
            .ravineConfig(0.2f, 2.0f)  // Maximum ravines
            // Add cave decorations
            .addCaveDecoration("minecraft:glowstone", 0.1f, -64, 256, true)  // Light sources
            .addCaveDecoration("minecraft:amethyst_block", 0.05f, -64, 128, false)
            // Ore configuration - specify exact vein counts per chunk
            //.configureOre("iron", 20)      // 20 iron veins per chunk
            //.configureOre("copper", 15)    // 15 copper veins per chunk
            //.configureOre("gold", 8)       // 8 gold veins per chunk
            //.configureOre("diamond", 4)    // 4 diamond veins per chunk
            //.configureOre("coal", 25)      // 25 coal veins per chunk
            //.configureOre("redstone", 10)  // 10 redstone veins per chunk
            .configureOre("lapis", 50)      // 50 lapis veins per chunk
            .configureOre("osmium", 30)     // 30 osmium veins per chunk (Mekanism)
            .configureOre("tin", 25)        // 25 tin veins per chunk (Mekanism)
            .configureOre("uranium", 5)      // 5 uranium veins per chunk (Mekanism - rare)
            //.configureOre("emerald", 2)    // 2 emerald veins per chunk
            .veinToggle(0.8f)
            .veinRidged(0.6f)
            .veinGap(0.5f)
            // Add custom biome for ore generation to work
            .clearBiomes()
            .addBiome("adastramekanized:cavetest_caverns", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            // Visual properties
            .skyColor(0x78A7FF)
            .fogColor(0x00FF00)
            .hasAtmosphere(true)
            .ambientLight(0.1f)
            // Sun and spawn settings for testing
            .hasSkylight(false)  // No sun damage for testing
            .monsterSpawnLightLevel(15);  // Spawn everywhere for cave testing

        // Apply aggressive mob spawning for cave testing
        cavetest.addMobSpawn("monster", "minecraft:zombie", 200, 4, 8)
                .addMobSpawn("monster", "minecraft:skeleton", 200, 4, 8)
                .addMobSpawn("monster", "minecraft:spider", 150, 3, 6)
                .addMobSpawn("monster", "minecraft:creeper", 100, 2, 4)
                .addMobSpawn("monster", "minecraft:cave_spider", 150, 4, 8)
                .addMobSpawn("monster", "minecraft:enderman", 50, 1, 3);

        cavetest.generate();

        // Other planets temporarily disabled to focus on Moon stability
        /*
        // Mars planet with advanced atmospheric controls and varied terrain
        PlanetMaker.planet("mars")
            .continentalScale(0.4f)  // Very low for fully connected Mars terrain
            .erosionScale(0.8f)      // Minimal erosion to prevent separation
            .ridgeScale(0.2f)        // Almost no ridges for stability
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
            .bedrockBlock("minecraft:bedrock")  // Add bedrock floor for mars
            .seaLevel(48)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .oreVeinsEnabled(true)
            // CAVES DISABLED for stable terrain
            .caveConfig(0.0f, 0.0f)  // No caves for now
            .cheeseCaves(false)      // Disabled
            .spaghettiCaves(false)   // Disabled
            .noodleCaves(false)      // Disabled
            // Mob spawning - Mars hostile environment
            .addMobSpawn("monster", "minecraft:husk", 50, 2, 4)  // Desert zombies common on Mars
            .addMobSpawn("monster", "minecraft:spider", 30, 1, 2)  // Cave spiders in Martian caves
            .addMobSpawn("monster", "minecraft:phantom", 10, 1, 2)  // Flying threats in thin atmosphere
            .addMobSpawn("monster", "minecraft:enderman", 15, 1, 1)  // Dimensional visitors
            // Add custom biomes for Mars
            .clearBiomes()
            .addBiome("adastramekanized:mars_highlands", -0.2f, -0.7f, 0.6f, -0.1f, 0.5f, 0.0f)  // Mars highlands
            .addBiome("adastramekanized:mars_canyons", 0.1f, -0.6f, 0.2f, 0.4f, -0.3f, -0.1f)  // Mars valleys
            .addBiome("adastramekanized:mars_polar", -0.9f, -0.8f, 0.8f, 0.0f, 0.2f, 0.3f)  // Mars polar
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
                // ========== STABILIZED TERRAIN SHAPING ==========
                .continentalScale(0.5f)         // Very low for connected hellscape
                .erosionScale(1.0f)             // Minimal erosion for stable terrain
                .ridgeScale(0.3f)               // Almost no ridges for stability
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
                .bedrockBlock("minecraft:bedrock")  // Standard bedrock floor
                // Prevent unwanted default blocks from appearing
                .preventGrassGeneration(true)               // MAX: No grass on alien world
                .preventGravelGeneration(true)              // MAX: No gravel generation
                .preventSandGeneration(true)                // MAX: No sand generation
                .disableDefaultSurfaceGeneration(true)      // MAX: Full surface control

                // ========== EXTREME WORLD DIMENSIONS ==========
                .worldDimensions(-64, 384)     // Reduced from extreme to manageable
                .noiseSize(4, 4)                // MAX: Highest resolution noise (4,4 is maximum)
                .seaLevel(0)                    // MIN: No sea level (all lava)

                // ========== CAVES DISABLED for stable terrain ==========
                .caveConfig(0.0f, 0.0f)         // No caves for now
                .cheeseCaves(false)             // Disabled
                .spaghettiCaves(false)          // Disabled
                .noodleCaves(false)             // Disabled

                // ========== EXTREME GENERATION CONTROLS ==========
                .disableMobGeneration(false)    // Enable mobs for hellish world
                .aquifersEnabled(true)          // Enable aquifers for cave generation
                // ========== EXTREME MOB SPAWNING - NETHER-LIKE HELL ==========
                .addMobSpawn("monster", "minecraft:magma_cube", 100, 2, 4)
                .addMobSpawn("monster", "minecraft:blaze", 50, 1, 3)
                .addMobSpawn("monster", "minecraft:ghast", 20, 1, 1)
                .addMobSpawn("monster", "minecraft:wither_skeleton", 30, 2, 4)
                .addMobSpawn("monster", "minecraft:zombified_piglin", 80, 4, 4)
                .addMobSpawn("creature", "minecraft:strider", 60, 1, 2)  // Only passive mob
                .oreVeinsEnabled(true)          // Enable Minecraft's ore vein system
                .abovePreliminaryRule(false)    // MAX: Disable surface smoothing
                .waterRule(false)               // MAX: No water rules (lava only)
                .surfaceDepthMultiplier(10)     // MAX: Deep surface layers
                .addStoneDepth(true)            // MAX: Extra stone depth

                // ========== ORE GENERATION - Use vanilla ore veins ==========
                .veinToggle(0.0f)               // Disable custom vein generation (use vanilla features)
                .veinRidged(0.0f)               // No ridged veins
                .veinGap(0.0f)                  // No vein gaps
                .oreVeinsEnabled(true)          // Enable Minecraft's built-in ore vein system

                // ========== EXTREME BIOME DISTRIBUTION ==========
                .biomeDistribution(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f)  // MAX: All parameters at maximum

                // ========== ALIEN VISUAL EXTREMES ==========
                .skyColor(0xFF00FF)             // MAX: Magenta alien sky
                .fogColor(0x00FFFF)             // MAX: Cyan alien fog
                .hasAtmosphere(true)            // MAX: Thick alien atmosphere
                .ambientLight(1.0f)             // MAX: Always bright (alien sun)

                // ========== CUSTOM BIOME SYSTEM - VOLCANIC HELLSCAPE ==========
                .clearBiomes()                   // Clear any default biomes
                .addBiome("adastramekanized:hemphy_volcanic", -0.5f, -0.7f, 0.3f, 0.1f, 0.2f, 0.0f)  // Volcanic regions
                .addBiome("adastramekanized:hemphy_infernal", 0.2f, -0.3f, -0.1f, 0.4f, -0.2f, -0.1f) // Infernal plains
                .addBiome("adastramekanized:hemphy_ashlands", -0.8f, -0.9f, 0.5f, 0.0f, 0.1f, 0.2f)  // Ash-covered lands

                // ========== LIQUID SYSTEM - LAVA OCEANS & LAKES ==========
                .oceanConfig("minecraft:lava", 32, 0.6f)        // Massive lava oceans at Y=32
                .lakeConfig("minecraft:lava", 0.3f)             // Frequent lava lakes
                .lavaLakes(-20, 0.4f)                           // Deep lava pools
                .undergroundLiquids("minecraft:lava", true)     // Lava aquifers

                // ========== STRUCTURE SYSTEM - NETHER FORTRESS WORLD ==========
                .clearStructures()                               // Clear defaults
                .addStructurePreset("nether")                   // Nether structures (fortress, bastion, fossils)
                .addStructure("minecraft:ruined_portal")        // Ruined portals for escape

                // ========== FEATURE SYSTEM - VOLCANIC HELLSCAPE ==========
                .clearFeatures()                                 // Clear defaults
                .addFeaturePreset("volcanic")                   // Basalt columns, pillars, fire
                .addFeature("minecraft:crimson_fungus", 0.2f)  // Crimson fungus growth
                .addFeature("minecraft:warped_fungus", 0.15f)  // Warped fungus growth
                .addFeature("minecraft:nether_sprouts", 0.3f)  // Nether vegetation
                .addFeature("minecraft:weeping_vines", 0.25f)  // Hanging vines
                .addFeature("minecraft:twisting_vines", 0.2f)  // Twisting vines
                .enableCrystals()                               // Amethyst geodes

                // ========== CAVE SYSTEM - LAVA TUBES & VOLCANIC CAVES ==========
                .addCavePreset("lava_tubes")                    // Massive lava tube cave system
                .ravineConfig(0.3f, 5.0f)                       // Deep volcanic ravines
                .addCaveDecoration("minecraft:blackstone", 0.3f, -128, 128, false)        // Blackstone floor
                .addCaveDecoration("minecraft:basalt", 0.2f, -128, 128, true)             // Basalt ceiling
                .addCaveDecoration("minecraft:ancient_debris", 0.01f, -64, 32, false)     // Rare ancient debris
                .addCaveDecoration("minecraft:gilded_blackstone", 0.02f, -128, 64, false) // Rare gilded blackstone
                .addCaveDecoration("minecraft:nether_gold_ore", 0.05f, -128, 128, false)  // Gold ore pockets
                .generate();

        // ========== ORE TEST PLANET - OVERWORLD-LIKE SETTINGS ==========
        // Adjusted for realistic Overworld-style terrain with proper ore generation
        PlanetMaker.planet("oretest")
                // ========== REALISTIC TERRAIN SHAPING ==========
                .continentalScale(2.0f)         // Lower for connected landmasses
                .erosionScale(3.0f)             // Minimal erosion for stable terrain
                .ridgeScale(1.0f)               // Low ridges for connected surface
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
                .worldDimensions(-32, 256)      // Reduced underground for better gameplay
                .noiseSize(2, 1)                // AVERAGE: Standard noise resolution
                .seaLevel(64)                   // AVERAGE: Normal sea level

                // ========== CAVE GENERATION FOR TEST PLANET ==========
                .caveConfig(0.2f, 0.7f)         // TESTING: Lower for stable terrain
                .cheeseCaves(true)              // TESTING: Some large caverns
                .spaghettiCaves(false)          // TESTING: Disabled to prevent spaghetti
                .noodleCaves(false)             // TESTING: Simplified cave system

                // ========== BALANCED GENERATION ==========
                .disableMobGeneration(false)    // AVERAGE: Allow mobs
                .aquifersEnabled(true)          // AVERAGE: Normal aquifers
                .oreVeinsEnabled(true)          // AVERAGE: Standard ore veins
                // ========== OVERWORLD-LIKE MOB SPAWNING FOR TESTING ==========
                .addHostileMobPreset("overworld")  // Standard hostile mobs
                .addPassiveMobPreset("overworld")  // Standard passive mobs
                // Add some custom test spawns with percentages
                .addMobSpawnPercentage("monster", "minecraft:cave_spider", 25.0, 1, 3)
                .addMobSpawnPercentage("creature", "minecraft:rabbit", 10.0, 2, 3)
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

                // ========== STRUCTURE SYSTEM - OVERWORLD-LIKE CIVILIZATION ==========
                .clearStructures()                                // Clear defaults
                .enableVillages()                                 // Villages with villagers
                .enableStrongholds()                              // Strongholds with end portals
                .enableMineshafts()                               // Abandoned mineshafts
                .enableDungeons()                                 // Monster spawner dungeons
                .addStructure("minecraft:pillager_outpost")      // Pillager outposts
                .addStructure("minecraft:ruined_portal")         // Ruined nether portals

                // ========== FEATURE SYSTEM - EARTH-LIKE ECOSYSTEM ==========
                .clearFeatures()                                  // Clear defaults
                .vegetation(0.6f, 0.8f, 0.3f)                   // Trees, grass, flowers
                .addTrees("oak", 0.3f)                          // Oak trees
                .addTrees("birch", 0.15f)                       // Birch trees
                .addTrees("spruce", 0.1f)                       // Spruce trees
                .addFeature("minecraft:flower_default", 0.2f)  // Various flowers
                .addFeature("minecraft:grass", 0.8f)           // Tall grass
                .addFeature("minecraft:pumpkin", 0.01f)        // Rare pumpkins
                .addFeature("minecraft:sugar_cane", 0.1f)      // Sugar cane near water
                .addRocks(0.05f)                                // Occasional rocks
                .enableGlowLichen()                              // Cave glow lichen

                // ========== CAVE SYSTEM - STANDARD OVERWORLD CAVES ==========
                .addCavePreset("standard")                       // Normal Earth-like caves
                .floodedCaves("minecraft:water", 10f)           // Water-filled caves below Y=10
                .addCaveDecoration("minecraft:stone", 0.5f, -64, 256, false)              // Stone floor patches
                .addCaveDecoration("minecraft:dripstone_block", 0.1f, -64, 64, true)      // Dripstone ceiling
                .addCaveDecoration("minecraft:pointed_dripstone", 0.08f, -64, 64, true)   // Stalactites
                .addCaveDecoration("minecraft:pointed_dripstone", 0.08f, -64, 64, false)  // Stalagmites
                .addCaveDecoration("minecraft:moss_block", 0.05f, -32, 64, false)         // Mossy cave floors
                .addCaveDecoration("minecraft:glow_lichen", 0.15f, -64, 256, true)        // Glowing lichen
                .addCaveDecoration("minecraft:coal_ore", 0.2f, 0, 256, false)             // Coal ore deposits
                .addCaveDecoration("minecraft:iron_ore", 0.15f, -64, 72, false)           // Iron ore deposits
                .addCaveDecoration("minecraft:copper_ore", 0.12f, -16, 112, false)        // Copper ore deposits
                .generate();
        */

    }
}