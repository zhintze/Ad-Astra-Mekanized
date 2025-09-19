package com.hecookin.adastramekanized.common.worldgen.ores;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.List;

/**
 * Advanced ore generation system for planetary dimensions.
 *
 * Features:
 * - Configurable ore veins based on planet settings
 * - Realistic ore distribution patterns
 * - Planet-specific ore types and abundances
 * - Depth-based ore generation
 * - Cluster and vein generation patterns
 * - Resource scarcity/abundance based on planet type
 */
public class PlanetOreGenerator {

    private final List<PlanetGenerationSettings.OreConfiguration> oreConfigurations;
    private final float resourceAbundance;
    private final SimplexNoise oreNoise;
    private final SimplexNoise clusterNoise;
    private final SimplexNoise densityNoise;
    private final RandomSource random;

    public PlanetOreGenerator(PlanetGenerationSettings generationSettings, long seed) {
        this.oreConfigurations = generationSettings.resources().ores();
        this.resourceAbundance = generationSettings.resources().resourceAbundance();
        this.random = RandomSource.create(seed);

        // Initialize noise generators for ore distribution
        this.oreNoise = new SimplexNoise(RandomSource.create(seed + 100));
        this.clusterNoise = new SimplexNoise(RandomSource.create(seed + 200));
        this.densityNoise = new SimplexNoise(RandomSource.create(seed + 300));

        AdAstraMekanized.LOGGER.info("Initialized PlanetOreGenerator with {} ore types, abundance: {}x",
            oreConfigurations.size(), resourceAbundance);
    }

    /**
     * Generate ores for a chunk
     */
    public void generateOres(ChunkAccess chunk, int chunkX, int chunkZ) {
        if (oreConfigurations.isEmpty()) {
            return;
        }

        for (var oreConfig : oreConfigurations) {
            generateOreType(chunk, chunkX, chunkZ, oreConfig);
        }
    }

    /**
     * Generate a specific ore type in the chunk
     */
    private void generateOreType(ChunkAccess chunk, int chunkX, int chunkZ, PlanetGenerationSettings.OreConfiguration oreConfig) {
        // Calculate number of veins for this chunk
        int maxVeins = (int) (oreConfig.maxVeinsPerChunk() * resourceAbundance);
        if (maxVeins <= 0) return;

        // Use noise to determine actual vein count
        double veinDensity = densityNoise.getValue(chunkX * 0.1, chunkZ * 0.1);
        int veinCount = (int) (maxVeins * (0.5 + veinDensity * 0.5));

        for (int i = 0; i < veinCount; i++) {
            attemptOreVeinGeneration(chunk, chunkX, chunkZ, oreConfig);
        }
    }

    /**
     * Attempt to generate an ore vein
     */
    private void attemptOreVeinGeneration(ChunkAccess chunk, int chunkX, int chunkZ, PlanetGenerationSettings.OreConfiguration oreConfig) {
        // Random position within chunk
        int localX = random.nextInt(16);
        int localZ = random.nextInt(16);
        int worldX = chunkX * 16 + localX;
        int worldZ = chunkZ * 16 + localZ;

        // Random height within ore range
        int minY = Math.max(oreConfig.minHeight(), chunk.getMinBuildHeight());
        int maxY = Math.min(oreConfig.maxHeight(), chunk.getMaxBuildHeight() - 1);

        if (minY >= maxY) return;

        int y = minY + random.nextInt(maxY - minY + 1);

        // Check rarity using noise
        double rarityNoise = oreNoise.getValue(worldX * 0.05, y * 0.1, worldZ * 0.05);
        if (rarityNoise < (1.0 - oreConfig.rarity())) {
            return;
        }

        // Generate vein at this position
        BlockPos center = new BlockPos(localX, y, localZ);
        generateOreVein(chunk, center, oreConfig);
    }

    /**
     * Generate an ore vein at the specified center position
     */
    private void generateOreVein(ChunkAccess chunk, BlockPos center, PlanetGenerationSettings.OreConfiguration oreConfig) {
        BlockState oreBlock = getBlockState(oreConfig.oreBlock());
        BlockState replaceBlock = getBlockState(oreConfig.replaceBlock());

        if (oreBlock == null || replaceBlock == null) {
            AdAstraMekanized.LOGGER.warn("Failed to get block states for ore: {} or replace: {}",
                oreConfig.oreBlock(), oreConfig.replaceBlock());
            return;
        }

        int veinSize = oreConfig.veinSize();
        VeinShape shape = determineVeinShape(center);

        switch (shape) {
            case SPHERICAL -> generateSphericalVein(chunk, center, oreBlock, replaceBlock, veinSize);
            case ELONGATED -> generateElongatedVein(chunk, center, oreBlock, replaceBlock, veinSize);
            case CLUSTER -> generateClusterVein(chunk, center, oreBlock, replaceBlock, veinSize);
            case LAYERED -> generateLayeredVein(chunk, center, oreBlock, replaceBlock, veinSize);
        }
    }

    /**
     * Determine what shape the vein should be
     */
    private VeinShape determineVeinShape(BlockPos center) {
        double shapeNoise = clusterNoise.getValue(center.getX() * 0.02, center.getY() * 0.05, center.getZ() * 0.02);

        if (shapeNoise > 0.6) {
            return VeinShape.LAYERED;
        } else if (shapeNoise > 0.2) {
            return VeinShape.ELONGATED;
        } else if (shapeNoise > -0.2) {
            return VeinShape.CLUSTER;
        } else {
            return VeinShape.SPHERICAL;
        }
    }

    /**
     * Generate a spherical ore vein
     */
    private void generateSphericalVein(ChunkAccess chunk, BlockPos center, BlockState oreBlock, BlockState replaceBlock, int size) {
        int radius = Math.max(1, size / 3);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    // Use noise to make irregular vein edges
                    double noiseOffset = oreNoise.getValue(
                        (center.getX() + dx) * 0.2,
                        (center.getY() + dy) * 0.2,
                        (center.getZ() + dz) * 0.2
                    ) * 0.5;

                    if (distance + noiseOffset <= radius) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        placeOreBlock(chunk, pos, oreBlock, replaceBlock);
                    }
                }
            }
        }
    }

    /**
     * Generate an elongated ore vein (like a tube or line)
     */
    private void generateElongatedVein(ChunkAccess chunk, BlockPos center, BlockState oreBlock, BlockState replaceBlock, int size) {
        // Choose random direction for elongation
        int direction = random.nextInt(3); // 0=X, 1=Y, 2=Z
        int length = size;
        int width = Math.max(1, size / 4);

        for (int i = -length; i <= length; i++) {
            for (int j = -width; j <= width; j++) {
                for (int k = -width; k <= width; k++) {
                    BlockPos pos;
                    double distance;

                    switch (direction) {
                        case 0 -> { // X-direction
                            pos = center.offset(i, j, k);
                            distance = Math.sqrt(j * j + k * k);
                        }
                        case 1 -> { // Y-direction
                            pos = center.offset(j, i, k);
                            distance = Math.sqrt(j * j + k * k);
                        }
                        default -> { // Z-direction
                            pos = center.offset(j, k, i);
                            distance = Math.sqrt(j * j + k * k);
                        }
                    }

                    if (distance <= width) {
                        placeOreBlock(chunk, pos, oreBlock, replaceBlock);
                    }
                }
            }
        }
    }

    /**
     * Generate a cluster-style ore vein (multiple small spheres)
     */
    private void generateClusterVein(ChunkAccess chunk, BlockPos center, BlockState oreBlock, BlockState replaceBlock, int size) {
        int numClusters = 2 + random.nextInt(4);
        int clusterSize = Math.max(1, size / numClusters);

        for (int cluster = 0; cluster < numClusters; cluster++) {
            // Random offset for each cluster
            int offsetX = random.nextInt(size) - size / 2;
            int offsetY = random.nextInt(size / 2) - size / 4;
            int offsetZ = random.nextInt(size) - size / 2;

            BlockPos clusterCenter = center.offset(offsetX, offsetY, offsetZ);
            generateSphericalVein(chunk, clusterCenter, oreBlock, replaceBlock, clusterSize);
        }
    }

    /**
     * Generate a layered ore vein (like sedimentary deposits)
     */
    private void generateLayeredVein(ChunkAccess chunk, BlockPos center, BlockState oreBlock, BlockState replaceBlock, int size) {
        int layerThickness = 1 + random.nextInt(2);
        int width = size;

        for (int dy = -layerThickness; dy <= layerThickness; dy++) {
            for (int dx = -width; dx <= width; dx++) {
                for (int dz = -width; dz <= width; dz++) {
                    double distance = Math.sqrt(dx * dx + dz * dz);

                    // Use noise to make irregular layer edges
                    double noiseOffset = oreNoise.getValue(
                        (center.getX() + dx) * 0.1,
                        center.getY() + dy,
                        (center.getZ() + dz) * 0.1
                    ) * 2.0;

                    if (distance + noiseOffset <= width) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        placeOreBlock(chunk, pos, oreBlock, replaceBlock);
                    }
                }
            }
        }
    }

    /**
     * Place an ore block if conditions are met
     */
    private void placeOreBlock(ChunkAccess chunk, BlockPos pos, BlockState oreBlock, BlockState replaceBlock) {
        // Check bounds
        if (!isValidPosition(chunk, pos)) {
            return;
        }

        // Check if current block can be replaced
        BlockState currentBlock = chunk.getBlockState(pos);
        if (canReplaceBlock(currentBlock, replaceBlock)) {
            chunk.setBlockState(pos, oreBlock, false);
        }
    }

    /**
     * Check if a position is valid for ore placement
     */
    private boolean isValidPosition(ChunkAccess chunk, BlockPos pos) {
        return pos.getX() >= 0 && pos.getX() < 16 &&
               pos.getZ() >= 0 && pos.getZ() < 16 &&
               pos.getY() >= chunk.getMinBuildHeight() &&
               pos.getY() < chunk.getMaxBuildHeight();
    }

    /**
     * Check if a block can be replaced with ore
     */
    private boolean canReplaceBlock(BlockState currentBlock, BlockState targetReplaceBlock) {
        // Don't replace air or liquids
        if (currentBlock.isAir() || currentBlock.getBlock() == Blocks.WATER || currentBlock.getBlock() == Blocks.LAVA) {
            return false;
        }

        // If specific replace block is configured, only replace that
        if (targetReplaceBlock != Blocks.STONE.defaultBlockState()) {
            return currentBlock.getBlock() == targetReplaceBlock.getBlock();
        }

        // Otherwise, replace stone-like blocks
        return currentBlock.getBlock() == Blocks.STONE ||
               currentBlock.getBlock() == Blocks.DEEPSLATE ||
               currentBlock.getBlock() == Blocks.GRANITE ||
               currentBlock.getBlock() == Blocks.DIORITE ||
               currentBlock.getBlock() == Blocks.ANDESITE;
    }

    /**
     * Get block state from resource location
     */
    private BlockState getBlockState(ResourceLocation blockLocation) {
        try {
            // Basic block mappings - expand as needed
            if (blockLocation.equals(ResourceLocation.withDefaultNamespace("iron_ore"))) {
                return Blocks.IRON_ORE.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("gold_ore"))) {
                return Blocks.GOLD_ORE.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("diamond_ore"))) {
                return Blocks.DIAMOND_ORE.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("coal_ore"))) {
                return Blocks.COAL_ORE.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("copper_ore"))) {
                return Blocks.COPPER_ORE.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("redstone_ore"))) {
                return Blocks.REDSTONE_ORE.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("lapis_ore"))) {
                return Blocks.LAPIS_ORE.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("emerald_ore"))) {
                return Blocks.EMERALD_ORE.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("stone"))) {
                return Blocks.STONE.defaultBlockState();
            } else if (blockLocation.equals(ResourceLocation.withDefaultNamespace("deepslate"))) {
                return Blocks.DEEPSLATE.defaultBlockState();
            }

            // For mod-specific blocks, try to map based on namespace
            if (blockLocation.getNamespace().equals("adastramekanized")) {
                String path = blockLocation.getPath();
                if (path.contains("moon")) {
                    // Moon ores - use iron ore as placeholder
                    return Blocks.IRON_ORE.defaultBlockState();
                } else if (path.contains("mars")) {
                    // Mars ores - use copper ore as placeholder
                    return Blocks.COPPER_ORE.defaultBlockState();
                } else if (path.contains("stone")) {
                    return Blocks.STONE.defaultBlockState();
                } else if (path.contains("rock")) {
                    return Blocks.COBBLESTONE.defaultBlockState();
                }
            }

            // Default fallback
            AdAstraMekanized.LOGGER.warn("Unknown ore block: {}, using iron ore as fallback", blockLocation);
            return Blocks.IRON_ORE.defaultBlockState();

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error getting block state for: {}", blockLocation, e);
            return Blocks.STONE.defaultBlockState();
        }
    }

    /**
     * Generate rare earth elements and special ores
     */
    public void generateSpecialOres(ChunkAccess chunk, int chunkX, int chunkZ) {
        // Generate special rare ores at very low frequencies
        for (int attempt = 0; attempt < 3; attempt++) {
            if (random.nextFloat() < 0.01f * resourceAbundance) { // 1% chance per attempt
                generateRareOreVein(chunk, chunkX, chunkZ);
            }
        }
    }

    /**
     * Generate rare ore veins (like diamonds, ancient debris equivalent)
     */
    private void generateRareOreVein(ChunkAccess chunk, int chunkX, int chunkZ) {
        int localX = random.nextInt(16);
        int localZ = random.nextInt(16);
        int y = chunk.getMinBuildHeight() + 5 + random.nextInt(20); // Deep rare ores

        BlockPos center = new BlockPos(localX, y, localZ);

        // Generate small diamond or rare metal vein
        BlockState rareOre = random.nextBoolean() ? Blocks.DIAMOND_ORE.defaultBlockState() : Blocks.GOLD_ORE.defaultBlockState();
        BlockState replaceBlock = Blocks.STONE.defaultBlockState();

        generateSphericalVein(chunk, center, rareOre, replaceBlock, 2 + random.nextInt(2));
    }

    /**
     * Ore vein shapes
     */
    private enum VeinShape {
        SPHERICAL,  // Round veins
        ELONGATED,  // Tube-like veins
        CLUSTER,    // Multiple small veins
        LAYERED     // Flat sedimentary layers
    }
}