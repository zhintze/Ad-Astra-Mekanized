package com.hecookin.adastramekanized.common.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Configuration system for static planet definitions using JSON-based templates
 * This provides a simple framework for defining planets without complex generation settings
 */
public class StaticPlanetConfig {

    public enum PlanetType {
        LUNAR("lunar", "Moon-like airless rocky body"),
        VOLCANIC("volcanic", "High-temperature volcanic world"),
        ROCKY("rocky", "Temperate rocky planet"),
        ICY("icy", "Cold ice-covered world"),
        DESERT("desert", "Hot dry desert world"),
        ASTEROID("asteroid", "Small rocky asteroid");

        private final String id;
        private final String description;

        PlanetType(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getId() { return id; }
        public String getDescription() { return description; }
    }

    public static class TerrainConfig {
        private final int baseHeight;
        private final int heightVariation;
        private final String surfaceMaterial;
        private final String subsurfaceMaterial;
        private final String deepMaterial;
        private final boolean generateCraters;
        private final double roughness;

        public TerrainConfig(int baseHeight, int heightVariation, String surfaceMaterial,
                           String subsurfaceMaterial, String deepMaterial, boolean generateCraters, double roughness) {
            this.baseHeight = baseHeight;
            this.heightVariation = heightVariation;
            this.surfaceMaterial = surfaceMaterial;
            this.subsurfaceMaterial = subsurfaceMaterial;
            this.deepMaterial = deepMaterial;
            this.generateCraters = generateCraters;
            this.roughness = roughness;
        }

        public int getBaseHeight() { return baseHeight; }
        public int getHeightVariation() { return heightVariation; }
        public String getSurfaceMaterial() { return surfaceMaterial; }
        public String getSubsurfaceMaterial() { return subsurfaceMaterial; }
        public String getDeepMaterial() { return deepMaterial; }
        public boolean shouldGenerateCraters() { return generateCraters; }
        public double getRoughness() { return roughness; }
    }

    public static class BiomeConfig {
        private final List<String> biomes;
        private final String primaryBiome;
        private final boolean useTerraBlender;

        public BiomeConfig(List<String> biomes, String primaryBiome, boolean useTerraBlender) {
            this.biomes = biomes;
            this.primaryBiome = primaryBiome;
            this.useTerraBlender = useTerraBlender;
        }

        public List<String> getBiomes() { return biomes; }
        public String getPrimaryBiome() { return primaryBiome; }
        public boolean shouldUseTerraBlender() { return useTerraBlender; }
    }

    public static class PlanetDefinition {
        private final String planetId;
        private final PlanetType planetType;
        private final TerrainConfig terrain;
        private final BiomeConfig biomes;
        private final List<String> features;

        public PlanetDefinition(String planetId, PlanetType planetType, TerrainConfig terrain,
                              BiomeConfig biomes, List<String> features) {
            this.planetId = planetId;
            this.planetType = planetType;
            this.terrain = terrain;
            this.biomes = biomes;
            this.features = features;
        }

        public String getPlanetId() { return planetId; }
        public PlanetType getPlanetType() { return planetType; }
        public TerrainConfig getTerrain() { return terrain; }
        public BiomeConfig getBiomes() { return biomes; }
        public List<String> getFeatures() { return features; }
    }

    private static final Map<String, PlanetDefinition> PLANET_CONFIGS = new HashMap<>();
    private static final Map<PlanetType, TerrainConfig> DEFAULT_TERRAIN_CONFIGS = new HashMap<>();

    static {
        initializeDefaultTerrainConfigs();
        loadBuiltInPlanetConfigs();
    }

    /**
     * Initialize default terrain configurations for each planet type
     */
    private static void initializeDefaultTerrainConfigs() {
        DEFAULT_TERRAIN_CONFIGS.put(PlanetType.LUNAR, new TerrainConfig(
            64, 32, "minecraft:light_gray_concrete", "minecraft:gray_concrete",
            "minecraft:stone", true, 0.6
        ));

        DEFAULT_TERRAIN_CONFIGS.put(PlanetType.VOLCANIC, new TerrainConfig(
            68, 40, "minecraft:magma_block", "minecraft:netherrack",
            "minecraft:blackstone", false, 0.8
        ));

        DEFAULT_TERRAIN_CONFIGS.put(PlanetType.ROCKY, new TerrainConfig(
            64, 35, "minecraft:cobblestone", "minecraft:stone",
            "minecraft:deepslate", false, 0.7
        ));

        DEFAULT_TERRAIN_CONFIGS.put(PlanetType.ICY, new TerrainConfig(
            62, 25, "minecraft:snow_block", "minecraft:packed_ice",
            "minecraft:blue_ice", false, 0.4
        ));

        DEFAULT_TERRAIN_CONFIGS.put(PlanetType.DESERT, new TerrainConfig(
            65, 30, "minecraft:sand", "minecraft:sandstone",
            "minecraft:red_sandstone", false, 0.5
        ));

        DEFAULT_TERRAIN_CONFIGS.put(PlanetType.ASTEROID, new TerrainConfig(
            45, 15, "minecraft:gravel", "minecraft:cobblestone",
            "minecraft:stone", true, 0.9
        ));
    }

    /**
     * Load built-in planet configurations
     */
    private static void loadBuiltInPlanetConfigs() {
        // Moon configuration
        PLANET_CONFIGS.put("adastramekanized:moon", new PlanetDefinition(
            "adastramekanized:moon",
            PlanetType.LUNAR,
            DEFAULT_TERRAIN_CONFIGS.get(PlanetType.LUNAR),
            new BiomeConfig(
                Arrays.asList("adastramekanized:lunar_highlands", "adastramekanized:lunar_maria"),
                "adastramekanized:lunar_highlands",
                true
            ),
            Arrays.asList("craters", "regolith_deposits")
        ));

        // Venus configuration
        PLANET_CONFIGS.put("adastramekanized:venus", new PlanetDefinition(
            "adastramekanized:venus",
            PlanetType.VOLCANIC,
            new TerrainConfig(70, 45, "minecraft:yellow_terracotta", "minecraft:orange_terracotta",
                            "minecraft:magma_block", false, 0.7),
            new BiomeConfig(
                Arrays.asList("adastramekanized:venus_surface", "adastramekanized:venus_volcanic"),
                "adastramekanized:venus_surface",
                false // Use custom generator for now
            ),
            Arrays.asList("volcanic_vents", "sulfur_deposits")
        ));

    }

    /**
     * Get planet configuration by ID
     */
    public static PlanetDefinition getPlanetConfig(String planetId) {
        return PLANET_CONFIGS.get(planetId);
    }

    /**
     * Get planet configuration by ResourceLocation
     */
    public static PlanetDefinition getPlanetConfig(ResourceLocation planetId) {
        return getPlanetConfig(planetId.toString());
    }

    /**
     * Get all configured planets
     */
    public static Map<String, PlanetDefinition> getAllPlanetConfigs() {
        return Collections.unmodifiableMap(PLANET_CONFIGS);
    }

    /**
     * Get default terrain configuration for a planet type
     */
    public static TerrainConfig getDefaultTerrainConfig(PlanetType planetType) {
        return DEFAULT_TERRAIN_CONFIGS.get(planetType);
    }

    /**
     * Register a new planet configuration
     */
    public static void registerPlanetConfig(PlanetDefinition planetDefinition) {
        PLANET_CONFIGS.put(planetDefinition.getPlanetId(), planetDefinition);
        AdAstraMekanized.LOGGER.info("Registered static planet config: {}", planetDefinition.getPlanetId());
    }

    /**
     * Load planet configuration from JSON resource
     */
    public static PlanetDefinition loadPlanetConfigFromJson(String resourcePath) {
        try (InputStream inputStream = StaticPlanetConfig.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                AdAstraMekanized.LOGGER.warn("Could not find planet config resource: {}", resourcePath);
                return null;
            }

            JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
            return parsePlanetDefinition(json);
        } catch (IOException e) {
            AdAstraMekanized.LOGGER.error("Failed to load planet config from {}: {}", resourcePath, e.getMessage());
            return null;
        }
    }

    /**
     * Parse planet definition from JSON
     */
    private static PlanetDefinition parsePlanetDefinition(JsonObject json) {
        String planetId = json.get("planet_id").getAsString();
        PlanetType planetType = PlanetType.valueOf(json.get("planet_type").getAsString().toUpperCase());

        // Parse terrain configuration
        JsonObject terrainJson = json.getAsJsonObject("terrain");
        TerrainConfig terrain = new TerrainConfig(
            terrainJson.get("base_height").getAsInt(),
            terrainJson.get("height_variation").getAsInt(),
            terrainJson.get("surface_material").getAsString(),
            terrainJson.get("subsurface_material").getAsString(),
            terrainJson.get("deep_material").getAsString(),
            terrainJson.has("generate_craters") ? terrainJson.get("generate_craters").getAsBoolean() : false,
            terrainJson.has("roughness") ? terrainJson.get("roughness").getAsDouble() : 0.7
        );

        // Parse biome configuration
        JsonObject biomesJson = json.getAsJsonObject("biomes");
        List<String> biomeList = new ArrayList<>();
        JsonArray biomesArray = biomesJson.getAsJsonArray("biomes");
        for (int i = 0; i < biomesArray.size(); i++) {
            biomeList.add(biomesArray.get(i).getAsString());
        }
        BiomeConfig biomes = new BiomeConfig(
            biomeList,
            biomesJson.get("primary_biome").getAsString(),
            biomesJson.has("use_terrablender") ? biomesJson.get("use_terrablender").getAsBoolean() : true
        );

        // Parse features
        List<String> features = new ArrayList<>();
        if (json.has("features")) {
            JsonArray featuresArray = json.getAsJsonArray("features");
            for (int i = 0; i < featuresArray.size(); i++) {
                features.add(featuresArray.get(i).getAsString());
            }
        }

        return new PlanetDefinition(planetId, planetType, terrain, biomes, features);
    }

    /**
     * Check if a planet should use TerraBlender integration
     */
    public static boolean shouldUseTerraBlender(ResourceLocation planetId) {
        PlanetDefinition config = getPlanetConfig(planetId);
        return config != null && config.getBiomes().shouldUseTerraBlender();
    }

    /**
     * Get simplified terrain materials for a planet
     */
    public static Map<String, String> getTerrainMaterials(ResourceLocation planetId) {
        PlanetDefinition config = getPlanetConfig(planetId);
        if (config == null) {
            return getDefaultTerrainMaterials();
        }

        Map<String, String> materials = new HashMap<>();
        TerrainConfig terrain = config.getTerrain();
        materials.put("surface", terrain.getSurfaceMaterial());
        materials.put("subsurface", terrain.getSubsurfaceMaterial());
        materials.put("deep", terrain.getDeepMaterial());
        return materials;
    }

    /**
     * Get default terrain materials fallback
     */
    private static Map<String, String> getDefaultTerrainMaterials() {
        Map<String, String> materials = new HashMap<>();
        materials.put("surface", "minecraft:stone");
        materials.put("subsurface", "minecraft:cobblestone");
        materials.put("deep", "minecraft:deepslate");
        return materials;
    }

    /**
     * Initialize the static planet configuration system
     */
    public static void initialize() {
        AdAstraMekanized.LOGGER.info("Initialized Static Planet Configuration system");
        AdAstraMekanized.LOGGER.info("Registered {} planet configurations", PLANET_CONFIGS.size());
        AdAstraMekanized.LOGGER.debug("Available planet types: {}",
            Arrays.stream(PlanetType.values()).map(PlanetType::getId).toList());
    }
}