package com.hecookin.adastramekanized.common.biomes;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.biomes.regions.MoonRegion;
import com.hecookin.adastramekanized.common.biomes.regions.VenusRegion;
import net.minecraft.resources.ResourceLocation;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

/**
 * TerraBlender integration manager for planet biome injection
 * Phase 3.1 - Basic TerraBlender Integration
 */
public class TerraBlenderIntegration {

    private static boolean initialized = false;

    /**
     * Initialize TerraBlender integration for planet biomes
     * Called during mod common setup
     */
    public static void initialize() {
        if (initialized) {
            AdAstraMekanized.LOGGER.warn("TerraBlender integration already initialized, skipping");
            return;
        }

        try {
            AdAstraMekanized.LOGGER.info("Initializing TerraBlender integration for planet biomes...");

            // Initialize biome registrations
            PlanetBiomes.initialize();
            PlanetSurfaceRules.initialize();
            TemplateSurfaceRules.initialize();

            // Register planet-specific regions with low weights to avoid interfering with overworld
            registerPlanetRegions();

            // Add surface rules for each planet
            registerSurfaceRules();

            initialized = true;
            AdAstraMekanized.LOGGER.info("TerraBlender integration initialized successfully");
            AdAstraMekanized.LOGGER.info("Registered 1 planet region: Moon (Venus disabled, Mars removed)");

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to initialize TerraBlender integration: {}", e.getMessage(), e);
        }
    }

    /**
     * Register planet-specific biome regions
     */
    private static void registerPlanetRegions() {
        AdAstraMekanized.LOGGER.debug("Registering planet regions with TerraBlender...");

        // Moon region - weight 1 (very low as it's mostly uniform)
        Regions.register(new MoonRegion(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "moon_region"), 1));
        AdAstraMekanized.LOGGER.debug("Registered Moon region with weight 1");

        // Venus region - DISABLED due to biome array conflicts
        // Regions.register(new VenusRegion(
        //     ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "venus_region"), 2));
        // AdAstraMekanized.LOGGER.debug("Registered Venus region with weight 2");
    }

    /**
     * Register surface rules for planet terrain
     */
    private static void registerSurfaceRules() {
        AdAstraMekanized.LOGGER.debug("Registering planet surface rules with TerraBlender...");

        // Add Moon surface rules (template-based)
        SurfaceRuleManager.addSurfaceRules(
            SurfaceRuleManager.RuleCategory.OVERWORLD,
            AdAstraMekanized.MOD_ID + "_moon",
            TemplateSurfaceRules.getPlanetSurfaceRules(
                ResourceLocation.fromNamespaceAndPath("adastramekanized", "moon"))
        );
        AdAstraMekanized.LOGGER.debug("Registered Moon template surface rules");

        // Venus surface rules - DISABLED (Venus uses fixed biome source)
        // SurfaceRuleManager.addSurfaceRules(
        //     SurfaceRuleManager.RuleCategory.OVERWORLD,
        //     AdAstraMekanized.MOD_ID + "_venus",
        //     PlanetSurfaceRules.makeVenusSurfaceRules()
        // );
        // AdAstraMekanized.LOGGER.debug("Registered Venus surface rules");
    }

    /**
     * Check if TerraBlender integration is available
     */
    public static boolean isAvailable() {
        try {
            Class.forName("terrablender.api.Regions");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if integration has been initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Get integration status for debugging
     */
    public static String getStatus() {
        if (!isAvailable()) {
            return "TerraBlender not available";
        }
        return initialized ? "Initialized (3 regions, 3 surface rule sets)" : "Not initialized";
    }
}