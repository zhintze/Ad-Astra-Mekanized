package com.hecookin.adastramekanized.common.planets;

import com.hecookin.adastramekanized.AdAstraMekanized;

/**
 * Simple runner to generate all static planet files
 */
public class PlanetGenerationRunner {
    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");

        try {
            AdAstraMekanized.LOGGER.info("Starting static planet generation...");
            StaticPlanetGenerator.generateAllPlanets();
            AdAstraMekanized.LOGGER.info("Static planet generation completed successfully!");
        } catch (Exception e) {
            System.err.println("Failed to generate planets: " + e.getMessage());
            e.printStackTrace();
        }
    }
}