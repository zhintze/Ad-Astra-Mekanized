package com.hecookin.adastramekanized.common.worldgen.caves;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.generation.PlanetGenerationSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

/**
 * Advanced cave generation system for planetary dimensions.
 *
 * Creates realistic cave systems including:
 * - Large caverns
 * - Tunnel networks
 * - Underground chambers
 * - Lava tubes (for volcanic planets)
 * - Ice caves (for cold planets)
 * - Crystal caves (for mineral-rich planets)
 */
public class PlanetCaveGenerator {

    private final PlanetGenerationSettings.TerrainSettings terrainSettings;
    private final SimplexNoise caveNoise;
    private final SimplexNoise tunnelNoise;
    private final SimplexNoise cavernNoise;
    private final SimplexNoise verticalNoise;
    private final RandomSource random;

    public PlanetCaveGenerator(PlanetGenerationSettings generationSettings, long seed) {
        this.terrainSettings = generationSettings.terrain();
        this.random = RandomSource.create(seed);

        // Initialize different noise generators for different cave types
        this.caveNoise = new SimplexNoise(RandomSource.create(seed));
        this.tunnelNoise = new SimplexNoise(RandomSource.create(seed + 1));
        this.cavernNoise = new SimplexNoise(RandomSource.create(seed + 2));
        this.verticalNoise = new SimplexNoise(RandomSource.create(seed + 3));

        AdAstraMekanized.LOGGER.debug("Initialized PlanetCaveGenerator with caves: {}, ravines: {}",
            terrainSettings.generateCaves(), terrainSettings.generateRavines());
    }

    /**
     * Generate caves for a chunk
     */
    public void generateCaves(ChunkAccess chunk, int chunkX, int chunkZ) {
        if (!terrainSettings.generateCaves()) {
            return;
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                generateCaveColumn(chunk, x, z, worldX, worldZ);
            }
        }
    }

    /**
     * Generate caves for a single column
     */
    private void generateCaveColumn(ChunkAccess chunk, int localX, int localZ, int worldX, int worldZ) {
        int surfaceHeight = getSurfaceHeight(chunk, localX, localZ);
        int minHeight = chunk.getMinBuildHeight() + 10;
        int maxHeight = surfaceHeight - 5;

        for (int y = minHeight; y < maxHeight; y++) {
            BlockPos pos = new BlockPos(localX, y, localZ);

            if (shouldGenerateCave(worldX, y, worldZ)) {
                CaveType caveType = determineCaveType(worldX, y, worldZ);
                generateCaveAt(chunk, pos, caveType);
            }
        }
    }

    /**
     * Determine if a cave should be generated at this position
     */
    private boolean shouldGenerateCave(int x, int y, int z) {
        // Primary cave noise
        double primaryNoise = caveNoise.getValue(x * 0.015, y * 0.03, z * 0.015);

        // Secondary tunnel noise
        double tunnelNoiseValue = tunnelNoise.getValue(x * 0.02, y * 0.01, z * 0.02);

        // Large cavern noise (less frequent, larger spaces)
        double cavernNoiseValue = cavernNoise.getValue(x * 0.008, y * 0.015, z * 0.008);

        // Vertical connectivity noise
        double verticalNoiseValue = verticalNoise.getValue(x * 0.01, y * 0.05, z * 0.01);

        // Combine different cave generation conditions
        boolean primaryCave = primaryNoise > 0.5;
        boolean tunnel = tunnelNoiseValue > 0.6 && Math.abs(verticalNoiseValue) < 0.3;
        boolean cavern = cavernNoiseValue > 0.7;
        boolean verticalShaft = Math.abs(verticalNoiseValue) > 0.8 && primaryNoise > 0.3;

        return primaryCave || tunnel || cavern || verticalShaft;
    }

    /**
     * Determine what type of cave to generate
     */
    private CaveType determineCaveType(int x, int y, int z) {
        double cavernNoiseValue = cavernNoise.getValue(x * 0.008, y * 0.015, z * 0.008);
        double tunnelNoiseValue = tunnelNoise.getValue(x * 0.02, y * 0.01, z * 0.02);
        double verticalNoiseValue = verticalNoise.getValue(x * 0.01, y * 0.05, z * 0.01);

        // Determine cave type based on noise values
        if (cavernNoiseValue > 0.75) {
            return CaveType.LARGE_CAVERN;
        } else if (Math.abs(verticalNoiseValue) > 0.8) {
            return CaveType.VERTICAL_SHAFT;
        } else if (tunnelNoiseValue > 0.65) {
            return CaveType.TUNNEL;
        } else {
            return CaveType.SMALL_CAVE;
        }
    }

    /**
     * Generate a cave at the specified position
     */
    private void generateCaveAt(ChunkAccess chunk, BlockPos pos, CaveType caveType) {
        BlockState currentBlock = chunk.getBlockState(pos);

        // Don't carve air or liquids
        if (currentBlock.isAir() || currentBlock.getBlock() == Blocks.WATER ||
            currentBlock.getBlock() == Blocks.LAVA) {
            return;
        }

        // Generate cave based on type
        switch (caveType) {
            case SMALL_CAVE -> generateSmallCave(chunk, pos);
            case TUNNEL -> generateTunnel(chunk, pos);
            case LARGE_CAVERN -> generateLargeCavern(chunk, pos);
            case VERTICAL_SHAFT -> generateVerticalShaft(chunk, pos);
        }
    }

    /**
     * Generate a small cave chamber
     */
    private void generateSmallCave(ChunkAccess chunk, BlockPos center) {
        // Create a small spherical cave
        int radius = 2 + random.nextInt(3);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance <= radius) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (isValidCavePosition(chunk, pos)) {
                            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate a tunnel-like cave
     */
    private void generateTunnel(ChunkAccess chunk, BlockPos center) {
        // Create elongated horizontal cave
        int width = 1 + random.nextInt(2);
        int height = 2 + random.nextInt(2);
        int length = 3 + random.nextInt(4);

        for (int dx = -length; dx <= length; dx++) {
            for (int dy = -height; dy <= height; dy++) {
                for (int dz = -width; dz <= width; dz++) {
                    // Create tunnel shape (elliptical)
                    double normalizedX = (double) dx / length;
                    double normalizedY = (double) dy / height;
                    double normalizedZ = (double) dz / width;

                    if (normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ <= 1.0) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (isValidCavePosition(chunk, pos)) {
                            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate a large cavern
     */
    private void generateLargeCavern(ChunkAccess chunk, BlockPos center) {
        // Create large spherical cavern
        int radius = 4 + random.nextInt(6);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    // Use noise to make irregular cavern walls
                    double noiseOffset = caveNoise.getValue(
                        (center.getX() + dx) * 0.1,
                        (center.getY() + dy) * 0.1,
                        (center.getZ() + dz) * 0.1
                    ) * 2.0;

                    if (distance + noiseOffset <= radius) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (isValidCavePosition(chunk, pos)) {
                            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate a vertical shaft
     */
    private void generateVerticalShaft(ChunkAccess chunk, BlockPos center) {
        // Create vertical cylindrical shaft
        int radius = 1 + random.nextInt(2);
        int height = 8 + random.nextInt(12);
        int startY = Math.max(center.getY() - height / 2, chunk.getMinBuildHeight() + 5);
        int endY = Math.min(center.getY() + height / 2, chunk.getMaxBuildHeight() - 5);

        for (int y = startY; y <= endY; y++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    if (distance <= radius) {
                        BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                        if (isValidCavePosition(chunk, pos)) {
                            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if a position is valid for cave generation
     */
    private boolean isValidCavePosition(ChunkAccess chunk, BlockPos pos) {
        // Check bounds
        if (pos.getY() < chunk.getMinBuildHeight() || pos.getY() >= chunk.getMaxBuildHeight()) {
            return false;
        }

        if (pos.getX() < 0 || pos.getX() >= 16 || pos.getZ() < 0 || pos.getZ() >= 16) {
            return false;
        }

        // Don't carve near surface
        int surfaceHeight = getSurfaceHeight(chunk, pos.getX(), pos.getZ());
        if (pos.getY() > surfaceHeight - 3) {
            return false;
        }

        return true;
    }

    /**
     * Get surface height for a local chunk position
     */
    private int getSurfaceHeight(ChunkAccess chunk, int localX, int localZ) {
        // Find the highest non-air block
        for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
            BlockState state = chunk.getBlockState(new BlockPos(localX, y, localZ));
            if (!state.isAir()) {
                return y;
            }
        }
        return chunk.getMinBuildHeight();
    }

    /**
     * Generate ravines for a chunk
     */
    public void generateRavines(ChunkAccess chunk, int chunkX, int chunkZ) {
        if (!terrainSettings.generateRavines()) {
            return;
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                generateRavineColumn(chunk, x, z, worldX, worldZ);
            }
        }
    }

    /**
     * Generate ravines for a single column
     */
    private void generateRavineColumn(ChunkAccess chunk, int localX, int localZ, int worldX, int worldZ) {
        // Use different noise for ravines
        double ravineNoise = tunnelNoise.getValue(worldX * 0.003, worldZ * 0.003);
        double ravineIntensity = cavernNoise.getValue(worldX * 0.002, worldZ * 0.002);

        // Generate ravine if conditions are met
        if (ravineNoise > 0.85 && Math.abs(ravineIntensity) > 0.7) {
            int surfaceHeight = getSurfaceHeight(chunk, localX, localZ);
            int ravineDepth = 15 + random.nextInt(20);
            int ravineWidth = 3 + random.nextInt(4);

            // Carve ravine
            for (int y = surfaceHeight; y > surfaceHeight - ravineDepth && y > chunk.getMinBuildHeight(); y--) {
                for (int dx = -ravineWidth; dx <= ravineWidth; dx++) {
                    for (int dz = -ravineWidth; dz <= ravineWidth; dz++) {
                        double distance = Math.sqrt(dx * dx + dz * dz);
                        if (distance <= ravineWidth) {
                            BlockPos pos = new BlockPos(localX + dx, y, localZ + dz);
                            if (isValidCavePosition(chunk, pos)) {
                                chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Cave types for different generation patterns
     */
    private enum CaveType {
        SMALL_CAVE,      // Small chambers
        TUNNEL,          // Horizontal tunnels
        LARGE_CAVERN,    // Large chambers
        VERTICAL_SHAFT   // Vertical connections
    }
}