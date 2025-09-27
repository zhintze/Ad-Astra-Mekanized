package com.hecookin.adastramekanized.common.atmosphere;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

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
     * Uses atomic batch claiming to prevent race conditions
     */
    public synchronized Set<BlockPos> claimOxygenBlocks(ResourceKey<Level> dimension, BlockPos distributorPos, Set<BlockPos> requestedBlocks) {
        AdAstraMekanized.LOGGER.debug("CLAIM REQUEST: Distributor at {} requesting {} blocks",
            distributorPos, requestedBlocks.size());

        Set<BlockPos> claimedBlocks = ConcurrentHashMap.newKeySet();

        // Get or create dimension-specific maps
        Map<BlockPos, BlockPos> blockOwnership = dimensionBlockOwnership.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());
        Set<BlockPos> occupiedBlocks = dimensionOccupiedBlocks.computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet());

        // First pass: check which blocks we can claim
        Set<BlockPos> availableBlocks = new HashSet<>();
        int alreadyOwned = 0;
        int ownedByOthers = 0;

        for (BlockPos pos : requestedBlocks) {
            BlockPos currentOwner = blockOwnership.get(pos);
            if (currentOwner == null) {
                // Block is unclaimed
                availableBlocks.add(pos);
            } else if (currentOwner.equals(distributorPos)) {
                // We already own this block
                claimedBlocks.add(pos);
                alreadyOwned++;
            } else {
                // Another distributor owns it - skip
                ownedByOthers++;
                AdAstraMekanized.LOGGER.debug("  Block {} already owned by {}", pos, currentOwner);
            }
        }

        // Second pass: claim all available blocks atomically
        // This prevents partial claims and race conditions
        int newlyClaimed = 0;
        for (BlockPos pos : availableBlocks) {
            // Double-check in case of concurrent modification
            if (!occupiedBlocks.contains(pos)) {
                occupiedBlocks.add(pos);
                blockOwnership.put(pos, distributorPos);
                claimedBlocks.add(pos);
                newlyClaimed++;
            }
        }

        AdAstraMekanized.LOGGER.debug("CLAIM COMPLETE: Distributor {} claimed {}/{} blocks (new={}, alreadyOwned={}, blocked={})",
            distributorPos, claimedBlocks.size(), requestedBlocks.size(), newlyClaimed, alreadyOwned, ownedByOthers);

        return claimedBlocks;
    }

    /**
     * Release oxygen blocks owned by a distributor in a specific dimension
     */
    public synchronized void releaseOxygenBlocks(ResourceKey<Level> dimension, BlockPos distributorPos, Set<BlockPos> blocks) {
        if (blocks.isEmpty()) {
            AdAstraMekanized.LOGGER.debug("releaseOxygenBlocks called with EMPTY blocks for distributor at {}", distributorPos);
            return;
        }

        AdAstraMekanized.LOGGER.debug("RELEASE REQUEST: Distributor at {} requesting release of {} blocks",
            distributorPos, blocks.size());

        Map<BlockPos, BlockPos> blockOwnership = dimensionBlockOwnership.get(dimension);
        Set<BlockPos> occupiedBlocks = dimensionOccupiedBlocks.get(dimension);

        if (blockOwnership == null || occupiedBlocks == null) {
            AdAstraMekanized.LOGGER.warn("RELEASE FAILED: No ownership maps for dimension {}", dimension.location());
            return;
        }

        int released = 0;
        int notOwned = 0;
        int ownedByOthers = 0;

        for (BlockPos pos : blocks) {
            BlockPos owner = blockOwnership.get(pos);
            if (owner == null) {
                notOwned++;
                AdAstraMekanized.LOGGER.debug("  Block {} was not owned by anyone", pos);
            } else if (distributorPos.equals(owner)) {
                blockOwnership.remove(pos);
                occupiedBlocks.remove(pos);
                released++;
                AdAstraMekanized.LOGGER.debug("  Released block {} from distributor {}", pos, distributorPos);
            } else {
                ownedByOthers++;
                AdAstraMekanized.LOGGER.debug("  Block {} owned by different distributor: {}", pos, owner);
            }
        }

        AdAstraMekanized.LOGGER.debug("RELEASE COMPLETE: Distributor {} released {}/{} blocks (notOwned={}, ownedByOthers={})",
            distributorPos, released, blocks.size(), notOwned, ownedByOthers);

        // Log current state
        AdAstraMekanized.LOGGER.debug("Remaining occupied blocks in dimension: {}", occupiedBlocks.size());
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

    /**
     * Get the owner of a specific block in a dimension
     * @return The distributor position that owns this block, or null if unclaimed
     */
    public BlockPos getBlockOwner(ResourceKey<Level> dimension, BlockPos pos) {
        Map<BlockPos, BlockPos> blockOwnership = dimensionBlockOwnership.get(dimension);
        return blockOwnership != null ? blockOwnership.get(pos) : null;
    }
}