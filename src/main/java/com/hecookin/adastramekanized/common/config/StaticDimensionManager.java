package com.hecookin.adastramekanized.common.config;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.biomes.TerraBlenderIntegration;
import com.hecookin.adastramekanized.common.registry.ModChunkGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

/**
 * Central manager for static dimension configuration and generation strategy selection
 * Coordinates between TerraBlender integration and custom chunk generation approaches
 */
public class StaticDimensionManager {

    public enum GenerationStrategy {
        TERRABLENDER("terrablender", "Uses TerraBlender for biome-rich generation"),
        CUSTOM_CHUNK_GENERATOR("custom", "Uses custom PlanetChunkGenerator for simple generation"),
        HYBRID("hybrid", "Uses TerraBlender with custom surface rules");

        private final String id;
        private final String description;

        GenerationStrategy(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getId() { return id; }
        public String getDescription() { return description; }
    }

    private static final Map<String, GenerationStrategy> DIMENSION_STRATEGIES = new HashMap<>();
    private static boolean initialized = false;

    static {
        setupDefaultStrategies();
    }

    /**
     * Setup default generation strategies for built-in dimensions
     */
    private static void setupDefaultStrategies() {
        // Moon: TerraBlender integration with custom surface rules
        DIMENSION_STRATEGIES.put("adastramekanized:moon", GenerationStrategy.TERRABLENDER);

        // Venus: Custom chunk generator (for now, will migrate to TerraBlender later)
        DIMENSION_STRATEGIES.put("adastramekanized:venus", GenerationStrategy.CUSTOM_CHUNK_GENERATOR);

        // Mars: Full TerraBlender integration (working)
        DIMENSION_STRATEGIES.put("adastramekanized:mars", GenerationStrategy.TERRABLENDER);
    }

    /**
     * Get the generation strategy for a specific dimension
     */
    public static GenerationStrategy getGenerationStrategy(ResourceLocation dimensionId) {
        return DIMENSION_STRATEGIES.getOrDefault(dimensionId.toString(), GenerationStrategy.CUSTOM_CHUNK_GENERATOR);
    }

    /**
     * Set the generation strategy for a dimension
     */
    public static void setGenerationStrategy(ResourceLocation dimensionId, GenerationStrategy strategy) {
        DIMENSION_STRATEGIES.put(dimensionId.toString(), strategy);
        AdAstraMekanized.LOGGER.info("Set generation strategy for {}: {}", dimensionId, strategy.getId());
    }

    /**
     * Check if a dimension should use TerraBlender
     */
    public static boolean shouldUseTerraBlender(ResourceLocation dimensionId) {
        GenerationStrategy strategy = getGenerationStrategy(dimensionId);
        return strategy == GenerationStrategy.TERRABLENDER || strategy == GenerationStrategy.HYBRID;
    }

    /**
     * Check if a dimension should use custom chunk generator
     */
    public static boolean shouldUseCustomChunkGenerator(ResourceLocation dimensionId) {
        GenerationStrategy strategy = getGenerationStrategy(dimensionId);
        return strategy == GenerationStrategy.CUSTOM_CHUNK_GENERATOR;
    }

    /**
     * Get the appropriate chunk generator type for a dimension
     */
    public static String getChunkGeneratorType(ResourceLocation dimensionId) {
        if (shouldUseCustomChunkGenerator(dimensionId)) {
            return "adastramekanized:planet";
        } else {
            return "minecraft:noise";
        }
    }

    /**
     * Get the appropriate biome source configuration for a dimension
     */
    public static Map<String, Object> getBiomeSourceConfig(ResourceLocation dimensionId) {
        StaticPlanetConfig.PlanetDefinition config = StaticPlanetConfig.getPlanetConfig(dimensionId);
        Map<String, Object> biomeSource = new HashMap<>();

        if (config != null && shouldUseTerraBlender(dimensionId)) {
            // Use multi_noise for TerraBlender integration
            biomeSource.put("type", "minecraft:multi_noise");
            biomeSource.put("preset", "minecraft:overworld");
        } else if (config != null) {
            // Use fixed biome source for custom generation
            biomeSource.put("type", "minecraft:fixed");
            biomeSource.put("biome", config.getBiomes().getPrimaryBiome());
        } else {
            // Fallback to fixed biome
            biomeSource.put("type", "minecraft:fixed");
            biomeSource.put("biome", "minecraft:plains");
        }

        return biomeSource;
    }

    /**
     * Get block from resource location string with fallback
     */
    public static Block getBlock(String blockName) {
        return switch (blockName) {
            case "minecraft:light_gray_concrete" -> Blocks.LIGHT_GRAY_CONCRETE;
            case "minecraft:gray_concrete" -> Blocks.GRAY_CONCRETE;
            case "minecraft:stone" -> Blocks.STONE;
            case "minecraft:magma_block" -> Blocks.MAGMA_BLOCK;
            case "minecraft:netherrack" -> Blocks.NETHERRACK;
            case "minecraft:yellow_terracotta" -> Blocks.YELLOW_TERRACOTTA;
            case "minecraft:orange_terracotta" -> Blocks.ORANGE_TERRACOTTA;
            case "minecraft:red_terracotta" -> Blocks.RED_TERRACOTTA;
            case "minecraft:terracotta" -> Blocks.TERRACOTTA;
            case "minecraft:cobblestone" -> Blocks.COBBLESTONE;
            case "minecraft:deepslate" -> Blocks.DEEPSLATE;
            case "minecraft:sand" -> Blocks.SAND;
            case "minecraft:sandstone" -> Blocks.SANDSTONE;
            case "minecraft:red_sandstone" -> Blocks.RED_SANDSTONE;
            case "minecraft:snow_block" -> Blocks.SNOW_BLOCK;
            case "minecraft:packed_ice" -> Blocks.PACKED_ICE;
            case "minecraft:blue_ice" -> Blocks.BLUE_ICE;
            case "minecraft:gravel" -> Blocks.GRAVEL;
            case "minecraft:blackstone" -> Blocks.BLACKSTONE;
            default -> {
                AdAstraMekanized.LOGGER.warn("Unknown block: {}, falling back to stone", blockName);
                yield Blocks.STONE;
            }
        };
    }

    /**
     * Create dimension JSON configuration for a planet
     */
    public static Map<String, Object> createDimensionConfig(ResourceLocation dimensionId) {
        StaticPlanetConfig.PlanetDefinition config = StaticPlanetConfig.getPlanetConfig(dimensionId);
        Map<String, Object> dimensionConfig = new HashMap<>();

        // Set dimension type
        dimensionConfig.put("type", dimensionId.toString());

        // Create generator configuration
        Map<String, Object> generator = new HashMap<>();
        generator.put("type", getChunkGeneratorType(dimensionId));
        generator.put("biome_source", getBiomeSourceConfig(dimensionId));

        // Add planet-specific settings for custom chunk generator
        if (shouldUseCustomChunkGenerator(dimensionId)) {
            generator.put("generation_settings", null); // Use defaults for now
            generator.put("planet_id", dimensionId.toString());
        } else {
            // Use noise settings for TerraBlender integration
            generator.put("settings", dimensionId.getNamespace() + ":" + dimensionId.getPath());
        }

        dimensionConfig.put("generator", generator);
        return dimensionConfig;
    }

    /**
     * Convert Moon from TerraBlender to custom chunk generator approach
     * This addresses the lava generation issue
     */
    public static void convertMoonToCustomGenerator() {
        AdAstraMekanized.LOGGER.info("Converting Moon dimension to use custom chunk generator to fix lava generation issue");
        setGenerationStrategy(ResourceLocation.fromNamespaceAndPath("adastramekanized", "moon"),
                            GenerationStrategy.CUSTOM_CHUNK_GENERATOR);
    }

    /**
     * Convert Venus back to TerraBlender integration
     */
    public static void convertVenusToTerraBlender() {
        AdAstraMekanized.LOGGER.info("Converting Venus dimension to use TerraBlender integration");
        setGenerationStrategy(ResourceLocation.fromNamespaceAndPath("adastramekanized", "venus"),
                            GenerationStrategy.TERRABLENDER);
    }

    /**
     * Get generation status for debugging
     */
    public static Map<String, String> getGenerationStatus() {
        Map<String, String> status = new HashMap<>();
        for (Map.Entry<String, GenerationStrategy> entry : DIMENSION_STRATEGIES.entrySet()) {
            status.put(entry.getKey(), entry.getValue().getId());
        }
        return status;
    }

    /**
     * Generate dimension configuration files
     */
    public static void generateDimensionConfigs() {
        AdAstraMekanized.LOGGER.info("Generating dimension configurations based on static planet configs...");

        for (String planetId : StaticPlanetConfig.getAllPlanetConfigs().keySet()) {
            ResourceLocation dimensionId = ResourceLocation.parse(planetId);
            Map<String, Object> config = createDimensionConfig(dimensionId);

            AdAstraMekanized.LOGGER.debug("Generated config for {}: strategy={}, generator={}",
                planetId,
                getGenerationStrategy(dimensionId).getId(),
                config.get("generator"));
        }
    }

    /**
     * Initialize the static dimension management system
     */
    public static void initialize() {
        if (initialized) {
            AdAstraMekanized.LOGGER.warn("StaticDimensionManager already initialized, skipping");
            return;
        }

        AdAstraMekanized.LOGGER.info("Initializing Static Dimension Manager...");

        // Initialize planet configurations first
        StaticPlanetConfig.initialize();

        // Setup generation strategies
        setupDefaultStrategies();

        // Log current configuration
        AdAstraMekanized.LOGGER.info("Dimension generation strategies:");
        for (Map.Entry<String, GenerationStrategy> entry : DIMENSION_STRATEGIES.entrySet()) {
            AdAstraMekanized.LOGGER.info("  {}: {}", entry.getKey(), entry.getValue().getDescription());
        }

        initialized = true;
        AdAstraMekanized.LOGGER.info("Static Dimension Manager initialized successfully");
    }

    /**
     * Check if the system has been initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Get all managed dimensions
     */
    public static Set<String> getManagedDimensions() {
        return Collections.unmodifiableSet(DIMENSION_STRATEGIES.keySet());
    }
}