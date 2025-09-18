package com.hecookin.adastramekanized.config;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration system for Ad Astra Mekanized
 *
 * Handles all configuration options for space exploration features,
 * planet generation, and mod integration settings.
 */
public class AdAstraMekanizedConfig {
    private static final Map<ModConfigSpec, IAdAstraMekanizedConfig> KNOWN_CONFIGS = new HashMap<>();

    public static final CommonConfig COMMON = new CommonConfig();

    public static void registerConfigs(ModContainer modContainer) {
        registerConfig(KNOWN_CONFIGS, modContainer, COMMON);
    }

    private static void registerConfig(Map<ModConfigSpec, IAdAstraMekanizedConfig> knownConfigs,
                                       ModContainer modContainer, IAdAstraMekanizedConfig config) {
        modContainer.registerConfig(config.getConfigType(), config.getConfigSpec(), config.getFileName() + ".toml");
        knownConfigs.put(config.getConfigSpec(), config);
    }

    public static void onConfigLoad(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getModId().equals(AdAstraMekanized.MOD_ID)) {
            IAdAstraMekanizedConfig myConfig = KNOWN_CONFIGS.get(config.getSpec());
            if (myConfig != null) {
                myConfig.clearCache(event instanceof ModConfigEvent.Unloading);
            }
        }
    }

    // Interface for config implementations
    public interface IAdAstraMekanizedConfig {
        String getFileName();
        ModConfigSpec getConfigSpec();
        ModConfig.Type getConfigType();
        void clearCache(boolean unloading);
    }

    /**
     * Common configuration for server-side settings
     */
    public static class CommonConfig implements IAdAstraMekanizedConfig {
        private final ModConfigSpec configSpec;

        // Space Exploration Settings
        public final ModConfigSpec.BooleanValue enableSpaceExploration;
        public final ModConfigSpec.BooleanValue enablePlanetGeneration;
        public final ModConfigSpec.IntValue maxPlanetsPerSolarSystem;
        public final ModConfigSpec.IntValue rocketFuelConsumptionRate;

        // Oxygen System Settings
        public final ModConfigSpec.BooleanValue enableOxygenSystem;
        public final ModConfigSpec.IntValue oxygenDistributorRange;
        public final ModConfigSpec.DoubleValue oxygenConsumptionRate;

        // Planet Generation Settings
        public final ModConfigSpec.BooleanValue enableProceduralPlanets;
        public final ModConfigSpec.IntValue minPlanetDistance;
        public final ModConfigSpec.IntValue maxPlanetDistance;

        // Integration Settings
        public final ModConfigSpec.BooleanValue enableMekanismIntegration;
        public final ModConfigSpec.BooleanValue enableImmersiveEngineeringIntegration;
        public final ModConfigSpec.BooleanValue enableCreateIntegration;

        CommonConfig() {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            // Space Exploration Section
            builder.comment("Space Exploration Settings",
                           "Configure core space exploration functionality")
                   .push("space_exploration");

            enableSpaceExploration = builder
                    .comment("Enable Space Exploration",
                            "Master toggle for all space exploration features")
                    .translation("adastramekanized.config.enableSpaceExploration")
                    .define("enableSpaceExploration", true);

            enablePlanetGeneration = builder
                    .comment("Enable Planet Generation",
                            "Allow generation of new planets and solar systems")
                    .translation("adastramekanized.config.enablePlanetGeneration")
                    .define("enablePlanetGeneration", true);

            maxPlanetsPerSolarSystem = builder
                    .comment("Max Planets Per Solar System",
                            "Maximum number of planets that can exist in a single solar system")
                    .translation("adastramekanized.config.maxPlanetsPerSolarSystem")
                    .defineInRange("maxPlanetsPerSolarSystem", 8, 1, 20);

            rocketFuelConsumptionRate = builder
                    .comment("Rocket Fuel Consumption Rate",
                            "Rate at which rockets consume fuel (lower = more efficient)")
                    .translation("adastramekanized.config.rocketFuelConsumptionRate")
                    .defineInRange("rocketFuelConsumptionRate", 100, 1, 1000);

            builder.pop();

            // Oxygen System Section
            builder.comment("Oxygen System Settings",
                           "Configure oxygen distribution and consumption")
                   .push("oxygen_system");

            enableOxygenSystem = builder
                    .comment("Enable Oxygen System",
                            "Enable oxygen requirements and distribution systems")
                    .translation("adastramekanized.config.enableOxygenSystem")
                    .define("enableOxygenSystem", true);

            oxygenDistributorRange = builder
                    .comment("Oxygen Distributor Range",
                            "Range in blocks for oxygen distributors")
                    .translation("adastramekanized.config.oxygenDistributorRange")
                    .defineInRange("oxygenDistributorRange", 16, 1, 64);

            oxygenConsumptionRate = builder
                    .comment("Oxygen Consumption Rate",
                            "Rate at which entities consume oxygen (lower = less consumption)")
                    .translation("adastramekanized.config.oxygenConsumptionRate")
                    .defineInRange("oxygenConsumptionRate", 1.0, 0.1, 10.0);

            builder.pop();

            // Planet Generation Section
            builder.comment("Planet Generation Settings",
                           "Configure procedural planet generation")
                   .push("planet_generation");

            enableProceduralPlanets = builder
                    .comment("Enable Procedural Planets",
                            "Allow generation of procedural planets with random characteristics")
                    .translation("adastramekanized.config.enableProceduralPlanets")
                    .define("enableProceduralPlanets", true);

            minPlanetDistance = builder
                    .comment("Minimum Planet Distance",
                            "Minimum distance between planets in a solar system")
                    .translation("adastramekanized.config.minPlanetDistance")
                    .defineInRange("minPlanetDistance", 1000, 100, 10000);

            maxPlanetDistance = builder
                    .comment("Maximum Planet Distance",
                            "Maximum distance between planets in a solar system")
                    .translation("adastramekanized.config.maxPlanetDistance")
                    .defineInRange("maxPlanetDistance", 5000, 1000, 50000);

            builder.pop();

            // Integration Section
            builder.comment("Mod Integration Settings",
                           "Configure integration with other mods")
                   .push("mod_integration");

            enableMekanismIntegration = builder
                    .comment("Enable Mekanism Integration",
                            "Enable integration with Mekanism's chemical and energy systems")
                    .translation("adastramekanized.config.enableMekanismIntegration")
                    .define("enableMekanismIntegration", true);

            enableImmersiveEngineeringIntegration = builder
                    .comment("Enable Immersive Engineering Integration",
                            "Enable integration with Immersive Engineering's fuel systems")
                    .translation("adastramekanized.config.enableImmersiveEngineeringIntegration")
                    .define("enableImmersiveEngineeringIntegration", true);

            enableCreateIntegration = builder
                    .comment("Enable Create Integration",
                            "Enable integration with Create's mechanical systems")
                    .translation("adastramekanized.config.enableCreateIntegration")
                    .define("enableCreateIntegration", true);

            builder.pop();
            configSpec = builder.build();
        }

        @Override
        public String getFileName() {
            return "adastramekanized-common";
        }

        @Override
        public ModConfigSpec getConfigSpec() {
            return configSpec;
        }

        @Override
        public ModConfig.Type getConfigType() {
            return ModConfig.Type.COMMON;
        }

        @Override
        public void clearCache(boolean unloading) {
            // Clear any cached values if needed
        }
    }

    // Accessor methods for easy config access
    public static boolean isSpaceExplorationEnabled() {
        return COMMON.enableSpaceExploration.get();
    }

    public static boolean isPlanetGenerationEnabled() {
        return COMMON.enablePlanetGeneration.get();
    }

    public static int getMaxPlanetsPerSolarSystem() {
        return COMMON.maxPlanetsPerSolarSystem.get();
    }

    public static int getRocketFuelConsumptionRate() {
        return COMMON.rocketFuelConsumptionRate.get();
    }

    public static boolean isOxygenSystemEnabled() {
        return COMMON.enableOxygenSystem.get();
    }

    public static int getOxygenDistributorRange() {
        return COMMON.oxygenDistributorRange.get();
    }

    public static double getOxygenConsumptionRate() {
        return COMMON.oxygenConsumptionRate.get();
    }

    public static boolean isProceduralPlanetsEnabled() {
        return COMMON.enableProceduralPlanets.get();
    }

    public static int getMinPlanetDistance() {
        return COMMON.minPlanetDistance.get();
    }

    public static int getMaxPlanetDistance() {
        return COMMON.maxPlanetDistance.get();
    }

    public static boolean isMekanismIntegrationEnabled() {
        return COMMON.enableMekanismIntegration.get();
    }

    public static boolean isImmersiveEngineeringIntegrationEnabled() {
        return COMMON.enableImmersiveEngineeringIntegration.get();
    }

    public static boolean isCreateIntegrationEnabled() {
        return COMMON.enableCreateIntegration.get();
    }
}