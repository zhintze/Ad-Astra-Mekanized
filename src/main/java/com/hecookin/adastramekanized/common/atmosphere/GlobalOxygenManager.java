package com.hecookin.adastramekanized.common.atmosphere;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global manager for oxygen blocks to prevent overlap between distributors
 * and allow them to work together efficiently
 */
public class GlobalOxygenManager {
    private static final GlobalOxygenManager INSTANCE = new GlobalOxygenManager();

    // Track per-dimension to prevent cross-dimensional conflicts
    // Dimension -> BlockPos -> DistributorPos
    private final Map<ResourceKey<Level>, Map<BlockPos, BlockPos>> dimensionBlockOwnership = new ConcurrentHashMap<>();
    // Dimension -> Set of occupied blocks
    private final Map<ResourceKey<Level>, Set<BlockPos>> dimensionOccupiedBlocks = new ConcurrentHashMap<>();

    private GlobalOxygenManager() {}

    public static GlobalOxygenManager getInstance() {
        return INSTANCE;
    }

    /**
     * Check if a block position is available for oxygen in a specific dimension
     */
    public boolean isBlockAvailable(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> occupied = dimensionOccupiedBlocks.get(dimension);
        return occupied == null || !occupied.contains(pos);
    }

    /**
     * Try to claim oxygen blocks for a distributor in a specific dimension
     * Returns the set of blocks that were successfully claimed
     */
    public synchronized Set<BlockPos> claimOxygenBlocks(ResourceKey<Level> dimension, BlockPos distributorPos, Set<BlockPos> requestedBlocks) {
        Set<BlockPos> claimedBlocks = ConcurrentHashMap.newKeySet();

        // Get or create dimension-specific maps
        Map<BlockPos, BlockPos> blockOwnership = dimensionBlockOwnership.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());
        Set<BlockPos> occupiedBlocks = dimensionOccupiedBlocks.computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet());

        for (BlockPos pos : requestedBlocks) {
            // Try to claim this block if it's not already occupied
            if (occupiedBlocks.add(pos)) {
                blockOwnership.put(pos, distributorPos);
                claimedBlocks.add(pos);
            } else {
                // Check if we already own it
                BlockPos currentOwner = blockOwnership.get(pos);
                if (distributorPos.equals(currentOwner)) {
                    claimedBlocks.add(pos); // We already own it
                }
                // Otherwise skip it - another distributor owns it
            }
        }

        if (!claimedBlocks.isEmpty()) {
            AdAstraMekanized.LOGGER.debug("Distributor at {} in {} claimed {} of {} requested blocks",
                distributorPos, dimension.location(), claimedBlocks.size(), requestedBlocks.size());
        }

        return claimedBlocks;
    }

    /**
     * Release oxygen blocks owned by a distributor in a specific dimension
     */
    public synchronized void releaseOxygenBlocks(ResourceKey<Level> dimension, BlockPos distributorPos, Set<BlockPos> blocks) {
        if (blocks.isEmpty()) return;

        Map<BlockPos, BlockPos> blockOwnership = dimensionBlockOwnership.get(dimension);
        Set<BlockPos> occupiedBlocks = dimensionOccupiedBlocks.get(dimension);

        if (blockOwnership == null || occupiedBlocks == null) {
            return;
        }

        int released = 0;
        for (BlockPos pos : blocks) {
            BlockPos owner = blockOwnership.get(pos);
            if (distributorPos.equals(owner)) {
                blockOwnership.remove(pos);
                occupiedBlocks.remove(pos);
                released++;
            }
        }

        if (released > 0) {
            AdAstraMekanized.LOGGER.debug("Distributor at {} in {} released {} blocks",
                distributorPos, dimension.location(), released);
        }
    }

    /**
     * Clear all oxygen blocks for a specific dimension
     */
    public void clearDimension(ResourceKey<Level> dimension) {
        Map<BlockPos, BlockPos> blockOwnership = dimensionBlockOwnership.remove(dimension);
        Set<BlockPos> occupiedBlocks = dimensionOccupiedBlocks.remove(dimension);

        if (blockOwnership != null) {
            blockOwnership.clear();
        }
        if (occupiedBlocks != null) {
            occupiedBlocks.clear();
        }
    }

    /**
     * Get total number of occupied oxygen blocks in a dimension
     */
    public int getTotalOxygenBlocks(ResourceKey<Level> dimension) {
        Set<BlockPos> occupied = dimensionOccupiedBlocks.get(dimension);
        return occupied != null ? occupied.size() : 0;
    }

    /**
     * Check if any distributor has claimed a specific block in a dimension
     */
    public boolean isBlockOccupied(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> occupied = dimensionOccupiedBlocks.get(dimension);
        return occupied != null && occupied.contains(pos);
    }
}