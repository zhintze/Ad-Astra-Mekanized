package com.hecookin.adastramekanized.common.worldgen;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.planets.StaticPlanetDefinitions.PlanetDefinition;
import com.hecookin.adastramekanized.common.planets.StaticPlanetDefinitions.PlanetType;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Integration system for enhanced planet terrain generation using advanced worldgen mods
 * Provides integration points for Tectonic, Terralith, TerraBlender, and Biomes O' Plenty
 */
public class PlanetTerrainIntegration {

    private static final Map<String, BiomeSet> PLANET_BIOME_SETS = new HashMap<>();
    private static final Map<String, TerrainProfile> PLANET_TERRAIN_PROFILES = new HashMap<>();

    public static void initializeIntegration() {
        AdAstraMekanized.LOGGER.info("Initializing planet terrain integration with worldgen mods...");

        setupBiomeSets();
        setupTerrainProfiles();
        registerDefaultProviders();

        AdAstraMekanized.LOGGER.info("Planet terrain integration initialized successfully");
        AdAstraMekanized.LOGGER.info("Provider registry status: {}", ProviderRegistry.getRegistryStatus());
    }

    /**
     * Register default providers based on available mods
     */
    private static void registerDefaultProviders() {
        // Always register vanilla fallback
        ProviderRegistry.registerBiomeProvider(new VanillaBiomeProvider());

        // Register mod-specific providers if mods are available
        if (isModAvailable("terrablender.core.TerraBlenderNeoForge")) {
            ProviderRegistry.registerBiomeProvider(new TerraBlenderBiomeProvider());
            AdAstraMekanized.LOGGER.info("Registered TerraBlender biome provider");
        }

        if (isModAvailable("dev.worldgen.tectonic.Tectonic")) {
            ProviderRegistry.registerTerrainModifier(new TectonicTerrainModifier());
            AdAstraMekanized.LOGGER.info("Registered Tectonic terrain modifier");
        }

        if (isModAvailable("net.minecraft.data.worldgen.biome.OverworldBiomes")) {
            ProviderRegistry.registerBiomeProvider(new TerralithBiomeProvider());
            AdAstraMekanized.LOGGER.info("Registered Terralith biome provider");
        }
    }

    /**
     * Biome sets define which biomes can appear on different planet types
     */
    public static class BiomeSet {
        public final List<String> primaryBiomes;
        public final List<String> secondaryBiomes;
        public final List<String> rareBiomes;
        public final boolean useTerralithBiomes;
        public final boolean useBOPBiomes;

        public BiomeSet(List<String> primary, List<String> secondary, List<String> rare,
                       boolean terralith, boolean bop) {
            this.primaryBiomes = primary;
            this.secondaryBiomes = secondary;
            this.rareBiomes = rare;
            this.useTerralithBiomes = terralith;
            this.useBOPBiomes = bop;
        }
    }

    /**
     * Terrain profiles define the landscape characteristics using Tectonic-style generation
     */
    public static class TerrainProfile {
        public final TerrainType type;
        public final float continentScale;
        public final float mountainIntensity;
        public final float oceanDepth;
        public final boolean hasRivers;
        public final boolean hasCanyons;
        public final boolean hasPlateaus;
        public final Map<String, Double> customConstants;

        public TerrainProfile(TerrainType type, float continentScale, float mountainIntensity,
                            float oceanDepth, boolean rivers, boolean canyons, boolean plateaus) {
            this.type = type;
            this.continentScale = continentScale;
            this.mountainIntensity = mountainIntensity;
            this.oceanDepth = oceanDepth;
            this.hasRivers = rivers;
            this.hasCanyons = canyons;
            this.hasPlateaus = plateaus;
            this.customConstants = new HashMap<>();
        }

        public TerrainProfile withConstant(String key, double value) {
            this.customConstants.put(key, value);
            return this;
        }
    }

    public enum TerrainType {
        CONTINENTAL,    // Large landmasses with varied terrain (Earth-like)
        ARCHIPELAGO,    // Many islands with deep oceans
        PANGAEA,        // Single massive continent
        MOUNTAINOUS,    // Dominated by mountain ranges
        FLAT_WORLD,     // Minimal elevation changes
        CRATER_WORLD,   // Moon-like with impact craters
        VOLCANIC,       // Volcanic activity and formations
        CANYON_WORLD    // Deep canyon systems
    }

    private static void setupBiomeSets() {
        // Rocky Worlds - Earth-like biome diversity
        PLANET_BIOME_SETS.put("ROCKY_WORLD", new BiomeSet(
            List.of("minecraft:plains", "minecraft:forest", "minecraft:mountains"),
            List.of("minecraft:taiga", "minecraft:savanna", "minecraft:desert"),
            List.of("minecraft:jungle", "minecraft:badlands"),
            true,  // Use Terralith biomes
            true   // Use Biomes O' Plenty
        ));

        // Ice Worlds - Frozen biomes
        PLANET_BIOME_SETS.put("ICE_WORLD", new BiomeSet(
            List.of("minecraft:snowy_plains", "minecraft:ice_spikes", "minecraft:frozen_peaks"),
            List.of("minecraft:snowy_taiga", "minecraft:frozen_ocean"),
            List.of("minecraft:snowy_slopes"),
            true,  // Terralith has great frozen biomes
            false  // Don't use BOP for ice worlds
        ));

        // Hot Worlds - Desert and volcanic biomes
        PLANET_BIOME_SETS.put("HOT_WORLD", new BiomeSet(
            List.of("minecraft:desert", "minecraft:badlands", "minecraft:nether_wastes"),
            List.of("minecraft:savanna", "minecraft:eroded_badlands"),
            List.of("minecraft:basalt_deltas"),
            false, // Terralith might not fit hot worlds well
            true   // BOP has good hot biomes
        ));

        // Airless Bodies - Minimal biome variety
        PLANET_BIOME_SETS.put("AIRLESS_BODY", new BiomeSet(
            List.of("minecraft:desert"),
            List.of(),
            List.of(),
            false,
            false
        ));

        // Extreme Worlds - Harsh conditions
        PLANET_BIOME_SETS.put("EXTREME_WORLD", new BiomeSet(
            List.of("minecraft:badlands", "minecraft:nether_wastes"),
            List.of("minecraft:soul_sand_valley", "minecraft:basalt_deltas"),
            List.of("minecraft:warped_forest"),
            false,
            true
        ));

        // Gas Giant Moons - Ocean and swamp biomes
        PLANET_BIOME_SETS.put("GAS_GIANT_MOON", new BiomeSet(
            List.of("minecraft:ocean", "minecraft:swamp", "minecraft:mangrove_swamp"),
            List.of("minecraft:river", "minecraft:lukewarm_ocean"),
            List.of("minecraft:warm_ocean"),
            true,  // Terralith ocean biomes
            true   // BOP water biomes
        ));
    }

    private static void setupTerrainProfiles() {
        // Rocky Worlds get continental terrain like Earth
        PLANET_TERRAIN_PROFILES.put("ROCKY_WORLD", new TerrainProfile(
            TerrainType.CONTINENTAL, 1.0f, 0.8f, 1.0f, true, true, true)
            .withConstant("continent_scale", 0.8)
            .withConstant("mountain_height", 1.2)
        );

        // Ice Worlds get mountainous terrain with plateaus
        PLANET_TERRAIN_PROFILES.put("ICE_WORLD", new TerrainProfile(
            TerrainType.MOUNTAINOUS, 0.7f, 1.2f, 0.5f, false, false, true)
            .withConstant("ice_thickness", 2.0)
            .withConstant("glacier_scale", 1.5)
        );

        // Hot Worlds get canyon systems
        PLANET_TERRAIN_PROFILES.put("HOT_WORLD", new TerrainProfile(
            TerrainType.CANYON_WORLD, 0.9f, 0.6f, 0.3f, false, true, true)
            .withConstant("lava_level", 0.8)
            .withConstant("canyon_depth", 1.8)
        );

        // Airless Bodies get crater terrain
        PLANET_TERRAIN_PROFILES.put("AIRLESS_BODY", new TerrainProfile(
            TerrainType.CRATER_WORLD, 0.5f, 0.4f, 0.0f, false, false, false)
            .withConstant("crater_frequency", 1.5)
            .withConstant("impact_roughness", 0.6)
        );

        // Extreme Worlds get volcanic terrain
        PLANET_TERRAIN_PROFILES.put("EXTREME_WORLD", new TerrainProfile(
            TerrainType.VOLCANIC, 0.8f, 1.5f, 0.7f, false, true, false)
            .withConstant("volcanic_activity", 2.0)
            .withConstant("lava_flow", 1.3)
        );

        // Gas Giant Moons get archipelago terrain
        PLANET_TERRAIN_PROFILES.put("GAS_GIANT_MOON", new TerrainProfile(
            TerrainType.ARCHIPELAGO, 0.3f, 0.5f, 2.0f, true, false, false)
            .withConstant("island_density", 0.4)
            .withConstant("ocean_depth", 2.5)
        );
    }

    /**
     * Get the biome set for a planet type
     */
    public static BiomeSet getBiomeSet(PlanetType type) {
        return PLANET_BIOME_SETS.get(type.name());
    }

    /**
     * Get the terrain profile for a planet type
     */
    public static TerrainProfile getTerrainProfile(PlanetType type) {
        return PLANET_TERRAIN_PROFILES.get(type.name());
    }

    /**
     * Generate enhanced noise settings using terrain profiles
     */
    public static Map<String, Object> generateEnhancedNoiseSettings(PlanetDefinition planet) {
        TerrainProfile profile = getTerrainProfile(planet.type);
        Map<String, Object> settings = new HashMap<>();

        if (profile != null) {
            Random random = new Random(planet.seed);

            // Apply terrain-specific modifications
            settings.put("continent_scale", profile.continentScale);
            settings.put("mountain_intensity", profile.mountainIntensity);
            settings.put("ocean_depth_multiplier", profile.oceanDepth);

            // Add custom constants from profile
            settings.putAll(profile.customConstants);

            // Add some planet-specific variation
            settings.put("terrain_seed", planet.seed);
            settings.put("variation_factor", random.nextFloat() * 0.3f + 0.85f);
        }

        return settings;
    }

    /**
     * Get enhanced biome list for a planet
     */
    public static List<String> getEnhancedBiomes(PlanetDefinition planet) {
        BiomeSet biomeSet = getBiomeSet(planet.type);
        if (biomeSet == null) {
            return List.of(planet.name + "_plains"); // Fallback to our generated biome
        }

        Random random = new Random(planet.seed);

        // TODO: Integrate with TerraBlender API to inject these biomes
        // For now, return the primary biomes that could be used
        return biomeSet.primaryBiomes;
    }

    /**
     * Check if worldgen mod integration is available
     */
    public static boolean isIntegrationAvailable() {
        try {
            // Check if key worldgen mods are present
            Class.forName("terrablender.api.TerraBlenderApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Initialize integration with detected worldgen mods
     */
    public static void detectAndConfigureIntegrations() {
        boolean terraBlenderAvailable = isModAvailable("terrablender.core.TerraBlenderNeoForge");
        boolean tectonicAvailable = isModAvailable("dev.worldgen.tectonic.Tectonic");
        boolean terralithAvailable = isModAvailable("net.minecraft.data.worldgen.biome.OverworldBiomes");

        AdAstraMekanized.LOGGER.info("WorldGen Mod Detection:");
        AdAstraMekanized.LOGGER.info("  TerraBlender: {}", terraBlenderAvailable ? "Available" : "Not Found");
        AdAstraMekanized.LOGGER.info("  Tectonic: {}", tectonicAvailable ? "Available" : "Not Found");
        AdAstraMekanized.LOGGER.info("  Terralith: {}", terralithAvailable ? "Available" : "Not Found");

        if (terraBlenderAvailable) {
            AdAstraMekanized.LOGGER.info("Configuring TerraBlender integration for enhanced biome diversity");
            configureTerraBlenderIntegration();
        }

        if (tectonicAvailable) {
            AdAstraMekanized.LOGGER.info("Configuring Tectonic integration for advanced terrain generation");
            configureTectonicIntegration();
        }
    }

    private static boolean isModAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void configureTerraBlenderIntegration() {
        // Phase 3.1 - Implement TerraBlender biome injection
        try {
            Class<?> integrationClass = Class.forName("com.hecookin.adastramekanized.common.biomes.TerraBlenderIntegration");
            java.lang.reflect.Method initMethod = integrationClass.getMethod("initialize");
            initMethod.invoke(null);
            AdAstraMekanized.LOGGER.info("TerraBlender biome injection initialized successfully");
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.warn("Failed to initialize TerraBlender biome injection: {}", e.getMessage());
            AdAstraMekanized.LOGGER.debug("TerraBlender integration configured (fallback)");
        }
    }

    private static void configureTectonicIntegration() {
        // TODO: Phase 3 - Implement Tectonic terrain enhancement
        AdAstraMekanized.LOGGER.debug("Tectonic integration configured (placeholder)");
    }

    /**
     * Phase 2.2 - API Interfaces and Provider Registry
     */

    /**
     * Provider interface for biome selection based on planet characteristics
     */
    public interface IPlanetBiomeProvider {
        /**
         * Get appropriate biomes for a specific planet
         * @param planet The planet definition to generate biomes for
         * @return List of biome resource location strings
         */
        List<String> getBiomesForPlanet(PlanetDefinition planet);

        /**
         * Check if this provider can handle the given planet type
         * @param planetType The type of planet
         * @return true if this provider can generate biomes for this planet type
         */
        boolean canProvideBiomes(PlanetType planetType);

        /**
         * Get the priority of this provider (higher = more preferred)
         * @return Priority value (0-100, where 100 is highest priority)
         */
        int getPriority();

        /**
         * Get the mod ID this provider represents
         * @return The mod ID string
         */
        String getModId();
    }

    /**
     * Provider interface for terrain modification based on planet characteristics
     */
    public interface IPlanetTerrainModifier {
        /**
         * Modify terrain generation settings for a planet
         * @param planet The planet definition
         * @param baseSettings Base terrain settings to modify
         * @return Modified terrain settings
         */
        Map<String, Object> modifyTerrainSettings(PlanetDefinition planet, Map<String, Object> baseSettings);

        /**
         * Check if this modifier can handle the given planet type
         * @param planetType The type of planet
         * @return true if this modifier can enhance terrain for this planet type
         */
        boolean canModifyTerrain(PlanetType planetType);

        /**
         * Get the priority of this modifier (higher = applied later)
         * @return Priority value (0-100)
         */
        int getPriority();

        /**
         * Get the mod ID this modifier represents
         * @return The mod ID string
         */
        String getModId();
    }

    /**
     * Provider interface for worldgen configuration
     */
    public interface IPlanetWorldgenConfig {
        /**
         * Apply worldgen configuration for a planet
         * @param planet The planet definition to configure
         */
        void applyConfiguration(PlanetDefinition planet);

        /**
         * Check if configuration is supported for the given mod
         * @param modId The mod ID to check
         * @return true if configuration is supported
         */
        boolean isConfigurationSupported(String modId);

        /**
         * Get the configuration priority (higher = applied later)
         * @return Priority value (0-100)
         */
        int getPriority();
    }

    /**
     * Registry for managing worldgen mod providers
     */
    public static class ProviderRegistry {
        private static final List<IPlanetBiomeProvider> biomeProviders = new ArrayList<>();
        private static final List<IPlanetTerrainModifier> terrainModifiers = new ArrayList<>();
        private static final List<IPlanetWorldgenConfig> configProviders = new ArrayList<>();

        /**
         * Register a biome provider
         */
        public static void registerBiomeProvider(IPlanetBiomeProvider provider) {
            if (!biomeProviders.contains(provider)) {
                biomeProviders.add(provider);
                // Sort by priority (highest first)
                biomeProviders.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
                AdAstraMekanized.LOGGER.info("Registered biome provider: {} (priority: {})",
                    provider.getModId(), provider.getPriority());
            }
        }

        /**
         * Register a terrain modifier
         */
        public static void registerTerrainModifier(IPlanetTerrainModifier modifier) {
            if (!terrainModifiers.contains(modifier)) {
                terrainModifiers.add(modifier);
                // Sort by priority (lowest first, so they apply in order)
                terrainModifiers.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
                AdAstraMekanized.LOGGER.info("Registered terrain modifier: {} (priority: {})",
                    modifier.getModId(), modifier.getPriority());
            }
        }

        /**
         * Register a config provider
         */
        public static void registerConfigProvider(IPlanetWorldgenConfig provider) {
            if (!configProviders.contains(provider)) {
                configProviders.add(provider);
                configProviders.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
                AdAstraMekanized.LOGGER.info("Registered config provider with priority: {}",
                    provider.getPriority());
            }
        }

        /**
         * Get the best biome provider for a planet type
         */
        public static IPlanetBiomeProvider getBiomeProvider(PlanetType planetType) {
            return biomeProviders.stream()
                .filter(provider -> provider.canProvideBiomes(planetType))
                .findFirst()
                .orElse(null);
        }

        /**
         * Get all terrain modifiers that can handle a planet type
         */
        public static List<IPlanetTerrainModifier> getTerrainModifiers(PlanetType planetType) {
            return terrainModifiers.stream()
                .filter(modifier -> modifier.canModifyTerrain(planetType))
                .collect(Collectors.toList());
        }

        /**
         * Get all registered config providers
         */
        public static List<IPlanetWorldgenConfig> getConfigProviders() {
            return new ArrayList<>(configProviders);
        }

        /**
         * Clear all registered providers (for testing)
         */
        public static void clearProviders() {
            biomeProviders.clear();
            terrainModifiers.clear();
            configProviders.clear();
        }

        /**
         * Get registry status for debugging
         */
        public static String getRegistryStatus() {
            return String.format("Biome Providers: %d, Terrain Modifiers: %d, Config Providers: %d",
                biomeProviders.size(), terrainModifiers.size(), configProviders.size());
        }
    }

    /**
     * Phase 2.2 - Concrete Provider Implementations
     */

    /**
     * TerraBlender biome provider for enhanced biome diversity
     */
    public static class TerraBlenderBiomeProvider implements IPlanetBiomeProvider {
        @Override
        public List<String> getBiomesForPlanet(PlanetDefinition planet) {
            // Phase 3.1 - Use TerraBlender-injected planet-specific biomes
            List<String> biomes = new ArrayList<>();

            // Add planet-specific TerraBlender biomes based on planet name
            switch (planet.name.toLowerCase()) {
                case "mars":
                    biomes.add("adastramekanized:mars_highlands");
                    biomes.add("adastramekanized:mars_valleys");
                    biomes.add("adastramekanized:mars_polar");
                    break;
                case "moon":
                    biomes.add("adastramekanized:lunar_highlands");
                    biomes.add("adastramekanized:lunar_maria");
                    break;
                case "venus":
                    biomes.add("adastramekanized:venus_surface");
                    biomes.add("adastramekanized:venus_volcanic");
                    break;
                default:
                    // For other planets, use enhanced biome set selection
                    BiomeSet biomeSet = getBiomeSet(planet.type);
                    if (biomeSet != null && (biomeSet.useTerralithBiomes || biomeSet.useBOPBiomes)) {
                        Random random = new Random(planet.seed);

                        // Add primary biomes (always present)
                        biomes.addAll(biomeSet.primaryBiomes);

                        // Add secondary biomes (70% chance each)
                        for (String biome : biomeSet.secondaryBiomes) {
                            if (random.nextFloat() < 0.7f) {
                                biomes.add(biome);
                            }
                        }

                        // Add rare biomes (30% chance each)
                        for (String biome : biomeSet.rareBiomes) {
                            if (random.nextFloat() < 0.3f) {
                                biomes.add(biome);
                            }
                        }
                    } else {
                        biomes.add(planet.name + "_plains");
                    }
                    break;
            }

            return biomes;
        }

        @Override
        public boolean canProvideBiomes(PlanetType planetType) {
            // Can provide biomes for all planet types now with TerraBlender integration
            return true;
        }

        @Override
        public int getPriority() {
            return 80; // High priority for enhanced biome diversity
        }

        @Override
        public String getModId() {
            return "terrablender";
        }
    }

    /**
     * Tectonic terrain modifier for advanced terrain generation
     */
    public static class TectonicTerrainModifier implements IPlanetTerrainModifier {
        @Override
        public Map<String, Object> modifyTerrainSettings(PlanetDefinition planet, Map<String, Object> baseSettings) {
            Map<String, Object> enhanced = new HashMap<>(baseSettings);
            TerrainProfile profile = getTerrainProfile(planet.type);

            if (profile != null) {
                Random random = new Random(planet.seed);

                // Apply Tectonic-style enhancements
                enhanced.put("tectonic_continent_scale", profile.continentScale * (0.8f + random.nextFloat() * 0.4f));
                enhanced.put("tectonic_mountain_intensity", profile.mountainIntensity * (0.9f + random.nextFloat() * 0.2f));
                enhanced.put("tectonic_ocean_depth", profile.oceanDepth * (0.7f + random.nextFloat() * 0.6f));

                // Add terrain type specific parameters
                switch (profile.type) {
                    case CONTINENTAL:
                        enhanced.put("tectonic_continental_drift", 0.8 + random.nextDouble() * 0.4);
                        enhanced.put("tectonic_plate_boundaries", true);
                        break;
                    case MOUNTAINOUS:
                        enhanced.put("tectonic_ridge_intensity", 1.2 + random.nextDouble() * 0.8);
                        enhanced.put("tectonic_valley_depth", 0.6 + random.nextDouble() * 0.8);
                        break;
                    case VOLCANIC:
                        enhanced.put("tectonic_volcanic_activity", 1.5 + random.nextDouble() * 1.0);
                        enhanced.put("tectonic_magma_chambers", true);
                        break;
                    case CRATER_WORLD:
                        enhanced.put("tectonic_impact_erosion", 0.3 + random.nextDouble() * 0.4);
                        enhanced.put("tectonic_rim_stability", false);
                        break;
                }

                // Apply custom constants from profile
                enhanced.putAll(profile.customConstants);
            }

            return enhanced;
        }

        @Override
        public boolean canModifyTerrain(PlanetType planetType) {
            // Tectonic can enhance most planet types except flat worlds
            return planetType != null && getTerrainProfile(planetType) != null &&
                   getTerrainProfile(planetType).type != TerrainType.FLAT_WORLD;
        }

        @Override
        public int getPriority() {
            return 50; // Medium priority - applied after basic setup
        }

        @Override
        public String getModId() {
            return "tectonic";
        }
    }

    /**
     * Terralith biome provider for datapack-based biome enhancement
     */
    public static class TerralithBiomeProvider implements IPlanetBiomeProvider {
        @Override
        public List<String> getBiomesForPlanet(PlanetDefinition planet) {
            BiomeSet biomeSet = getBiomeSet(planet.type);
            if (biomeSet == null || !biomeSet.useTerralithBiomes) {
                return List.of(planet.name + "_plains");
            }

            // Terralith focuses on vanilla-compatible biomes with enhanced variety
            List<String> biomes = new ArrayList<>(biomeSet.primaryBiomes);
            Random random = new Random(planet.seed);

            // Add Terralith-specific biomes based on planet type
            switch (planet.type) {
                case ROCKY_WORLD:
                    if (random.nextFloat() < 0.8f) biomes.add("terralith:ancient_sands");
                    if (random.nextFloat() < 0.6f) biomes.add("terralith:rocky_mountains");
                    break;
                case ICE_WORLD:
                    if (random.nextFloat() < 0.9f) biomes.add("terralith:frozen_cliffs");
                    if (random.nextFloat() < 0.7f) biomes.add("terralith:glacial_chasm");
                    break;
                case HOT_WORLD:
                    if (random.nextFloat() < 0.8f) biomes.add("terralith:red_oasis");
                    if (random.nextFloat() < 0.6f) biomes.add("terralith:volcanic_crater");
                    break;
                case GAS_GIANT_MOON:
                    if (random.nextFloat() < 0.7f) biomes.add("terralith:emerald_peaks");
                    if (random.nextFloat() < 0.5f) biomes.add("terralith:moonlight_grove");
                    break;
            }

            return biomes;
        }

        @Override
        public boolean canProvideBiomes(PlanetType planetType) {
            BiomeSet biomeSet = getBiomeSet(planetType);
            return biomeSet != null && biomeSet.useTerralithBiomes;
        }

        @Override
        public int getPriority() {
            return 70; // Lower priority than TerraBlender but still preferred over vanilla
        }

        @Override
        public String getModId() {
            return "terralith";
        }
    }

    /**
     * Vanilla fallback biome provider
     */
    public static class VanillaBiomeProvider implements IPlanetBiomeProvider {
        @Override
        public List<String> getBiomesForPlanet(PlanetDefinition planet) {
            BiomeSet biomeSet = getBiomeSet(planet.type);
            if (biomeSet != null) {
                return biomeSet.primaryBiomes;
            }
            // Absolute fallback
            return List.of(planet.name + "_plains");
        }

        @Override
        public boolean canProvideBiomes(PlanetType planetType) {
            return true; // Always available as fallback
        }

        @Override
        public int getPriority() {
            return 10; // Lowest priority - fallback only
        }

        @Override
        public String getModId() {
            return "minecraft";
        }
    }

    /**
     * Phase 2.2 - Enhanced Planet Generation API
     */

    /**
     * Generate enhanced biome list for a planet using registered providers
     */
    public static List<String> getEnhancedBiomesWithProviders(PlanetDefinition planet) {
        IPlanetBiomeProvider provider = ProviderRegistry.getBiomeProvider(planet.type);
        if (provider != null) {
            List<String> biomes = provider.getBiomesForPlanet(planet);
            AdAstraMekanized.LOGGER.debug("Using {} biome provider for {}: {} biomes",
                provider.getModId(), planet.name, biomes.size());
            return biomes;
        }

        // Fallback to original method if no provider available
        return getEnhancedBiomes(planet);
    }

    /**
     * Generate enhanced noise settings using registered terrain modifiers
     */
    public static Map<String, Object> getEnhancedNoiseSettingsWithModifiers(PlanetDefinition planet) {
        Map<String, Object> settings = generateEnhancedNoiseSettings(planet);

        // Apply all applicable terrain modifiers in priority order
        List<IPlanetTerrainModifier> modifiers = ProviderRegistry.getTerrainModifiers(planet.type);
        for (IPlanetTerrainModifier modifier : modifiers) {
            settings = modifier.modifyTerrainSettings(planet, settings);
            AdAstraMekanized.LOGGER.debug("Applied {} terrain modifier to {}: {} settings",
                modifier.getModId(), planet.name, settings.size());
        }

        return settings;
    }

    /**
     * Apply worldgen configuration using registered config providers
     */
    public static void applyWorldgenConfiguration(PlanetDefinition planet) {
        List<IPlanetWorldgenConfig> providers = ProviderRegistry.getConfigProviders();
        for (IPlanetWorldgenConfig provider : providers) {
            try {
                provider.applyConfiguration(planet);
                AdAstraMekanized.LOGGER.debug("Applied worldgen configuration for {}", planet.name);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.warn("Failed to apply worldgen configuration for {}: {}",
                    planet.name, e.getMessage());
            }
        }
    }
}