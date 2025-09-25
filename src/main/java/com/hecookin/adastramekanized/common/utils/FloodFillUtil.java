package com.hecookin.adastramekanized.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Utility for finding enclosed spaces using flood fill algorithm.
 * Used by oxygen distributors to find sealed areas.
 */
public class FloodFillUtil {

    /**
     * Find an enclosed area starting from a position.
     * Returns empty set if area is not enclosed or exceeds limit.
     */
    public static Set<BlockPos> findEnclosedArea(Level level, BlockPos startPos, int maxBlocks) {
        if (level == null || startPos == null) {
            return Collections.emptySet();
        }

        // Check if starting position is air
        if (!level.getBlockState(startPos).isAir()) {
            return Collections.emptySet();
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        toCheck.add(startPos);
        visited.add(startPos);

        boolean isEnclosed = true;

        while (!toCheck.isEmpty() && visited.size() < maxBlocks) {
            BlockPos current = toCheck.poll();

            // Check all 6 directions
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);

                // Skip if already visited
                if (visited.contains(neighbor)) {
                    continue;
                }

                BlockState state = level.getBlockState(neighbor);

                // If it's air, add to check queue
                if (state.isAir()) {
                    visited.add(neighbor);
                    toCheck.add(neighbor);

                    // Check if we've exceeded the limit (not enclosed)
                    if (visited.size() >= maxBlocks) {
                        isEnclosed = false;
                        break;
                    }
                }
                // If it's not solid, the area is not enclosed
                else if (!state.isSolid()) {
                    isEnclosed = false;
                    break;
                }
            }

            if (!isEnclosed) {
                break;
            }
        }

        // Return empty set if not enclosed or exceeded limit
        if (!isEnclosed || visited.size() >= maxBlocks) {
            return Collections.emptySet();
        }

        return visited;
    }

    /**
     * Check if a block forms a valid seal (solid and not allowing air passage)
     */
    private static boolean isSealing(BlockState state) {
        if (state.isAir()) {
            return false;
        }

        // Check if block is solid and doesn't allow air passage
        return state.isSolid() && state.isCollisionShapeFullBlock(null, null);
    }
}