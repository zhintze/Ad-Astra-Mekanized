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
        // Moon planet with moderate terrain parameters (reduced from extreme values)
        PlanetMaker.planet("moon")
            .continentalScale(2.5f)
            .erosionScale(8f)      // Reduced from 55 to prevent chunk generation issues
            .ridgeScale(3f)        // Reduced from 5 to prevent extreme terrain
            .heightVariation(4f, 2.5f, 1.0f, 0.6f)  // Reduced from extreme values
            .surfaceBlock("adastramekanized:moon_sand")
            .subsurfaceBlock("adastramekanized:moon_stone")
            .deepBlock("adastramekanized:moon_deepslate")
            .seaLevel(63)
            .disableMobGeneration(true)
            .aquifersEnabled(false)
            .skyColor(0x0A0A0A)  // Dark sky like current Moon
            .fogColor(0x0A0A0A)  // Dark fog like current Moon
            .hasAtmosphere(false)
            .ambientLight(0.1f)
            .generate();

        // Mars planet using Venus assets
        PlanetMaker.planet("mars")
            .continentalScale(1.5f)
            .erosionScale(15f)
            .ridgeScale(2f)
            .heightVariation(3f, 2f, 0.8f, 0.5f)
            .surfaceBlock("adastramekanized:mars_sand")
            .subsurfaceBlock("adastramekanized:mars_stone")
            .deepBlock("minecraft:stone")
            .seaLevel(48)
            .disableMobGeneration(false)
            .aquifersEnabled(false)
            .skyColor(0xD2691E)  // Mars-like orange sky
            .fogColor(0xCD853F)  // Sandy brown fog
            .hasAtmosphere(true)
            .ambientLight(0.2f)
            .generate();


        // Asteroid planet
        PlanetMaker.planet("asteroid")
                .continentalScale(1.5f)
                .erosionScale(35f)
                .ridgeScale(5f)
                .heightVariation(10f, 2f, 0.8f, 0.5f)
                .surfaceBlock("minecraft:stone")
                .subsurfaceBlock("minecraft:stone")
                .deepBlock("minecraft:stone")
                .seaLevel(48)
                .disableMobGeneration(false)
                .aquifersEnabled(false)
                .skyColor(0x00FF00)  // Mars-like orange sky
                .fogColor(0x0000FF)  // Sandy brown fog
                .hasAtmosphere(false)
                .ambientLight(0.1f)
                .generate();

    }
}