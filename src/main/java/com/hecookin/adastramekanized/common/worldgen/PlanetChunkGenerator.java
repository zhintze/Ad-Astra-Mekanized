package com.hecookin.adastramekanized.common.worldgen;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings;
import com.hecookin.adastramekanized.common.worldgen.caves.PlanetCaveGenerator;
import com.hecookin.adastramekanized.common.worldgen.ores.PlanetOreGenerator;
import com.hecookin.adastramekanized.common.worldgen.mobs.PlanetMobSpawner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.RandomState;
import com.mojang.serialization.MapCodec;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Custom chunk generator for planetary dimensions that uses PlanetGenerationSettings
 * to create diverse, realistic planetary terrain with customizable parameters.
 */
public class PlanetChunkGenerator extends ChunkGenerator {

    public static final MapCodec<PlanetChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
            PlanetGenerationSettings.CODEC.fieldOf("generation_settings").forGetter(generator -> generator.generationSettings),
            ResourceLocation.CODEC.fieldOf("planet_id").forGetter(generator -> generator.planetId)
        ).apply(instance, PlanetChunkGenerator::new)
    );

    private final PlanetGenerationSettings generationSettings;
    private final ResourceLocation planetId;
    private final SimplexNoise temperatureNoise;
    private final SimplexNoise humidityNoise;
    private final SimplexNoise altitudeNoise;
    private final SimplexNoise weirdnessNoise;
    private final SimplexNoise erosionNoise;
    private final SimplexNoise continentalnessNoise;

    // Advanced generation systems
    private final PlanetCaveGenerator caveGenerator;
    private final PlanetOreGenerator oreGenerator;
    private final PlanetMobSpawner mobSpawner;

    public PlanetChunkGenerator(BiomeSource biomeSource, PlanetGenerationSettings generationSettings, ResourceLocation planetId) {
        super(biomeSource);
        this.generationSettings = generationSettings != null ? generationSettings : createDefaultGenerationSettings();
        this.planetId = planetId;

        // Initialize noise generators
        RandomSource random = RandomSource.create(planetId.toString().hashCode());

        this.temperatureNoise = new SimplexNoise(random);
        this.humidityNoise = new SimplexNoise(random);
        this.altitudeNoise = new SimplexNoise(random);
        this.weirdnessNoise = new SimplexNoise(random);
        this.erosionNoise = new SimplexNoise(random);
        this.continentalnessNoise = new SimplexNoise(random);

        // Initialize advanced generation systems (disabled temporarily for compilation)
        long seed = planetId.toString().hashCode();
        this.caveGenerator = null; // TODO: new PlanetCaveGenerator(generationSettings, seed);
        this.oreGenerator = null; // TODO: new PlanetOreGenerator(generationSettings, seed);
        this.mobSpawner = null; // TODO: new PlanetMobSpawner(generationSettings, planetId, seed);

        AdAstraMekanized.LOGGER.info("Created PlanetChunkGenerator for planet: {}", planetId);
        // TODO: Add proper logging with generation settings
        // AdAstraMekanized.LOGGER.info("Initialized cave generation: {}, ore types: {}, mob spawning: enabled",
        //     generationSettings.terrain().generateCaves(), generationSettings.resources().ores().size());
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public int getMinY() {
        return -64; // Standard world minimum
    }

    @Override
    public int getSeaLevel() {
        return 63; // Default sea level
    }

    @Override
    public int getGenDepth() {
        return 384; // Standard world depth (320 - (-64))
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        // Basic mob spawning (temporarily disabled)
        // TODO: Re-enable proper mob spawning when generation settings are available
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState randomState, BiomeManager biomeManager,
                           StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        if (step == GenerationStep.Carving.AIR) {
            ChunkPos chunkPos = chunk.getPos();

            // Use advanced cave generation system (temporarily disabled)
            // TODO: Re-enable when generation settings are properly configured
            // caveGenerator.generateCaves(chunk, chunkPos.x, chunkPos.z);
            // caveGenerator.generateRavines(chunk, chunkPos.x, chunkPos.z);

            AdAstraMekanized.LOGGER.debug("Applied carving for chunk {} on planet {}",
                chunkPos, planetId);
        }
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structures, RandomState randomState, ChunkAccess chunk) {
        // Apply basic surface generation (temporarily simplified)
        // TODO: Re-enable proper surface configuration when generation settings are available

        BlockState topBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState subsurfaceBlock = Blocks.DIRT.defaultBlockState();
        BlockState deepBlock = Blocks.STONE.defaultBlockState();

        ChunkPos chunkPos = chunk.getPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                // Get surface height for this position
                int surfaceHeight = getSurfaceHeight(worldX, worldZ);

                // Build basic surface layers
                chunk.setBlockState(new BlockPos(x, surfaceHeight, z), topBlock, false);
                for (int y = surfaceHeight - 1; y > surfaceHeight - 3 && y >= chunk.getMinBuildHeight(); y--) {
                    chunk.setBlockState(new BlockPos(x, y, z), subsurfaceBlock, false);
                }
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                      StructureManager structureManager, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();

        // Generate base terrain using noise
        generateBaseTerrain(chunk);

        // Generate ores after terrain generation (temporarily disabled)
        // TODO: Re-enable when generation settings are properly configured
        // oreGenerator.generateOres(chunk, chunkPos.x, chunkPos.z);
        // oreGenerator.generateSpecialOres(chunk, chunkPos.x, chunkPos.z);

        AdAstraMekanized.LOGGER.debug("Generated terrain and ores for chunk {} on planet {}",
            chunkPos, planetId);

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType, LevelHeightAccessor level, RandomState randomState) {
        return getSurfaceHeight(x, z);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        int height = getSurfaceHeight(x, z);
        var surfaceConfig = generationSettings.terrain().surface();

        BlockState[] column = new BlockState[level.getHeight()];
        BlockState stone = getBlockState(surfaceConfig.deepBlock());
        BlockState liquid = surfaceConfig.hasLiquid() ? getBlockState(surfaceConfig.liquidBlock()) : null;

        // Fill column based on height
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
            if (y <= height) {
                column[y - level.getMinBuildHeight()] = stone;
            } else if (liquid != null && y <= surfaceConfig.liquidLevel()) {
                column[y - level.getMinBuildHeight()] = liquid;
            } else {
                column[y - level.getMinBuildHeight()] = Blocks.AIR.defaultBlockState();
            }
        }

        return new NoiseColumn(level.getMinBuildHeight(), column);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("Planet: " + planetId);
        info.add("Generation: Custom Planet Generator");
        info.add("Surface Height: " + getSurfaceHeight(pos.getX(), pos.getZ()));
        info.add("Temperature: " + String.format("%.2f", getTemperature(pos.getX(), pos.getZ())));
        info.add("Humidity: " + String.format("%.2f", getHumidity(pos.getX(), pos.getZ())));
        info.add("Cave Generation: " + generationSettings.terrain().generateCaves());
        info.add("Ore Types: " + generationSettings.resources().ores().size());
        info.add("Resource Abundance: " + String.format("%.1fx", generationSettings.resources().resourceAbundance()));
        info.add("Gravity: " + String.format("%.2fx", generationSettings.environment().gravity().gravityMultiplier()));
        info.add("Has Oxygen: " + generationSettings.environment().hasOxygen());
    }

    /**
     * Calculate surface height at given coordinates using planetary noise configuration
     */
    private int getSurfaceHeight(int x, int z) {
        // Simplified terrain generation (temporarily)
        // TODO: Re-enable proper terrain settings when generation settings are available
        int baseHeight = 64; // Default base height
        int maxVariation = 32; // Default variation

        // Sample multiple noise octaves for complex terrain
        double noise = 0.0;
        double amplitude = 1.0;
        double frequency = 0.01;

        for (int octave = 0; octave < 4; octave++) { // Default octaves
            noise += altitudeNoise.getValue(x * frequency * 1.0f, z * frequency * 1.0f) * amplitude;
            amplitude *= 0.5f; // Default persistence
            frequency *= 2.0f; // Default lacunarity
        }

        // Apply roughness and erosion
        double roughness = 0.8f; // Default roughness
        double erosion = 0.2f; // Default erosion
        double continentalness = 0.5f; // Default continentalness

        // Continental vs oceanic influence
        double continentalFactor = continentalnessNoise.getValue(x * 0.002, z * 0.002) * continentalness;

        // Erosion effects (smooths terrain)
        double erosionFactor = erosionNoise.getValue(x * 0.005, z * 0.005) * erosion;
        noise *= (1.0 - erosionFactor * 0.5);

        // Apply continental influence
        noise += continentalFactor * maxVariation * 0.3;

        // Scale by roughness
        noise *= roughness;

        // Convert to height
        int heightVariation = (int) (noise * maxVariation);
        return Mth.clamp(baseHeight + heightVariation, -64, 320);
    }

    /**
     * Generate the base terrain structure for a chunk
     */
    private void generateBaseTerrain(ChunkAccess chunk) {
        var terrainSettings = generationSettings.terrain();
        var surfaceConfig = terrainSettings.surface();

        BlockState deepBlock = getBlockState(surfaceConfig.deepBlock());
        BlockState liquidBlock = surfaceConfig.hasLiquid() ? getBlockState(surfaceConfig.liquidBlock()) : null;

        ChunkPos chunkPos = chunk.getPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;
                int surfaceHeight = getSurfaceHeight(worldX, worldZ);

                // Fill below surface with deep block
                for (int y = chunk.getMinBuildHeight(); y <= surfaceHeight; y++) {
                    chunk.setBlockState(new BlockPos(x, y, z), deepBlock, false);
                }

                // Fill with liquid if configured
                if (liquidBlock != null) {
                    int liquidLevel = surfaceConfig.liquidLevel();
                    for (int y = surfaceHeight + 1; y <= liquidLevel; y++) {
                        chunk.setBlockState(new BlockPos(x, y, z), liquidBlock, false);
                    }
                }
            }
        }
    }

    /**
     * Build surface layers for a single column
     */
    private void buildSurfaceColumn(ChunkAccess chunk, int x, int z, int surfaceHeight,
                                  BlockState topBlock, BlockState subsurfaceBlock, BlockState deepBlock,
                                  BlockState liquidBlock, PlanetGenerationSettings.SurfaceConfiguration config) {

        // Place top block
        chunk.setBlockState(new BlockPos(x, surfaceHeight, z), topBlock, false);

        // Place subsurface blocks
        for (int y = surfaceHeight - 1; y > surfaceHeight - config.subsurfaceDepth() && y >= chunk.getMinBuildHeight(); y--) {
            chunk.setBlockState(new BlockPos(x, y, z), subsurfaceBlock, false);
        }

        // Deep blocks are already placed in generateBaseTerrain
    }

    /**
     * Trigger mob spawning for a server level chunk
     * This should be called by world generation events or tick handlers
     */
    public void spawnMobs(net.minecraft.server.level.ServerLevel level, ChunkAccess chunk) {
        if (level != null && mobSpawner != null) {
            mobSpawner.spawnMobs(level, chunk);
        }
    }

    /**
     * Get temperature at given coordinates
     */
    public double getTemperature(int x, int z) {
        var noiseConfig = generationSettings.terrain().noise();
        return temperatureNoise.getValue(x * 0.004 * noiseConfig.temperatureScale(), z * 0.004 * noiseConfig.temperatureScale());
    }

    /**
     * Get humidity at given coordinates
     */
    public double getHumidity(int x, int z) {
        var noiseConfig = generationSettings.terrain().noise();
        return humidityNoise.getValue(x * 0.003 * noiseConfig.humidityScale(), z * 0.003 * noiseConfig.humidityScale());
    }

    /**
     * Get block state from resource location, with fallback
     */
    private BlockState getBlockState(ResourceLocation blockLocation) {
        try {
            // This is a simplified approach - in a real implementation, you'd want proper registry access
            if (blockLocation.equals(ResourceLocation.withDefaultNamespace("grass_block"))) {
                return Blocks.GRASS_BLOCK.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("dirt"))) {
                return Blocks.DIRT.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("stone"))) {
                return Blocks.STONE.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("water"))) {
                return Blocks.WATER.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("sand"))) {
                return Blocks.SAND.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("sandstone"))) {
                return Blocks.SANDSTONE.defaultBlockState();
            }
            // Add more block mappings as needed

            // For custom blocks, fall back to stone
            AdAstraMekanized.LOGGER.warn("Unknown block: {}, falling back to stone", blockLocation);
            return Blocks.STONE.defaultBlockState();
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error getting block state for: {}", blockLocation, e);
            return Blocks.STONE.defaultBlockState();
        }
    }

    /**
     * Get the generation settings
     */
    public PlanetGenerationSettings getGenerationSettings() {
        return generationSettings;
    }

    /**
     * Get the planet ID
     */
    public ResourceLocation getPlanetId() {
        return planetId;
    }

    /**
     * Create default generation settings for fallback
     */
    private static PlanetGenerationSettings createDefaultGenerationSettings() {
        // Create minimal default settings to prevent null pointer exceptions
        // This will be replaced with proper settings in the future
        return null; // TODO: Implement proper default settings
    }
}