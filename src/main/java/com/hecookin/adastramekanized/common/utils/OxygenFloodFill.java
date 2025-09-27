package com.hecookin.adastramekanized.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

/**
 * Advanced oxygen flood fill algorithm that respects block boundaries
 * and simulates realistic oxygen flow patterns.
 */
public class OxygenFloodFill {

    private static final Direction[] DIRECTIONS = Direction.values();

    /**
     * Find oxygenatable positions starting from a center point.
     * Uses a two-phase approach: initial radius check, then expansion.
     *
     * @param level The world level
     * @param startPos The starting position (usually above the distributor)
     * @param initialRadius Initial search radius (typically 10)
     * @param maxBlocks Maximum number of blocks to oxygenate (100 for testing)
     * @return Set of positions that should be oxygenated
     */
    public static Set<BlockPos> findOxygenatableArea(Level level, BlockPos startPos, int initialRadius, int maxBlocks) {
        Set<BlockPos> oxygenatedPositions = new LinkedHashSet<>();
        Set<BlockPos> visitedPositions = new HashSet<>();
        Queue<BlockPos> expansionQueue = new ArrayDeque<>();

        com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.info("OxygenFloodFill starting from {} with radius {} max blocks {}",
            startPos, initialRadius, maxBlocks);

        BlockPos distributorPos = startPos.below(); // The actual distributor position

        // First check if there's at least one adjacent air block to start from
        boolean hasAdjacentAir = false;
        for (Direction dir : DIRECTIONS) {
            BlockPos adjacentPos = distributorPos.relative(dir);
            if (needsOxygen(level, adjacentPos)) {
                hasAdjacentAir = true;
                // Add all adjacent air blocks as starting points
                oxygenatedPositions.add(adjacentPos);
                visitedPositions.add(adjacentPos);
                expansionQueue.add(adjacentPos);

                if (oxygenatedPositions.size() >= maxBlocks) {
                    return oxygenatedPositions;
                }
            }
        }

        // Also check diagonal adjacent blocks (corners)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue; // Skip center

                    BlockPos adjacentPos = distributorPos.offset(dx, dy, dz);
                    if (!visitedPositions.contains(adjacentPos) && needsOxygen(level, adjacentPos)) {
                        hasAdjacentAir = true;
                        oxygenatedPositions.add(adjacentPos);
                        visitedPositions.add(adjacentPos);
                        expansionQueue.add(adjacentPos);

                        if (oxygenatedPositions.size() >= maxBlocks) {
                            return oxygenatedPositions;
                        }
                    }
                }
            }
        }

        // If no adjacent air blocks, can't distribute oxygen
        if (!hasAdjacentAir) {
            com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.info("OxygenFloodFill: No adjacent air blocks to distributor, cannot distribute oxygen");
            return oxygenatedPositions; // Empty set
        }

        // Phase 1: Continue with radius search from distributor

        for (int x = -initialRadius; x <= initialRadius; x++) {
            for (int y = -initialRadius; y <= initialRadius; y++) {
                for (int z = -initialRadius; z <= initialRadius; z++) {
                    BlockPos checkPos = distributorPos.offset(x, y, z);

                    // Check if within initial radius
                    if (checkPos.distManhattan(distributorPos) > initialRadius) {
                        continue;
                    }

                    // Skip if already visited
                    if (visitedPositions.contains(checkPos)) {
                        continue;
                    }

                    visitedPositions.add(checkPos);

                    // Check if this position needs oxygen
                    if (needsOxygen(level, checkPos)) {
                        // Verify there's a path from the start position
                        if (hasPathTo(level, startPos, checkPos, initialRadius * 2)) {
                            oxygenatedPositions.add(checkPos);
                            expansionQueue.add(checkPos);

                            if (oxygenatedPositions.size() >= maxBlocks) {
                                return oxygenatedPositions;
                            }
                        }
                    }
                }
            }
        }

        // Phase 2: Expand from existing oxygen blocks
        while (!expansionQueue.isEmpty() && oxygenatedPositions.size() < maxBlocks) {
            BlockPos currentPos = expansionQueue.poll();

            // Check all 6 directions
            for (Direction dir : DIRECTIONS) {
                BlockPos neighborPos = currentPos.relative(dir);

                // Skip if already visited or oxygenated
                if (visitedPositions.contains(neighborPos)) {
                    continue;
                }

                visitedPositions.add(neighborPos);

                // Check if this position needs oxygen
                if (needsOxygen(level, neighborPos)) {
                    oxygenatedPositions.add(neighborPos);
                    expansionQueue.add(neighborPos);

                    if (oxygenatedPositions.size() >= maxBlocks) {
                        return oxygenatedPositions;
                    }
                }
            }
        }

        com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.info("OxygenFloodFill found {} positions to oxygenate. First 5: {}",
            oxygenatedPositions.size(),
            oxygenatedPositions.stream().limit(5).toList());

        return oxygenatedPositions;
    }

    /**
     * Determines if a block position needs oxygen.
     * Solid full blocks don't need oxygen, but partial blocks and air do.
     */
    private static boolean needsOxygen(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        // Air always needs oxygen
        if (state.isAir()) {
            return true;
        }

        // Water and lava don't get oxygenated
        if (state.getFluidState().isSource()) {
            return false;
        }

        // Check special block types that need oxygen despite being "solid"

        // Stairs need oxygen (players can walk on them)
        if (block instanceof StairBlock) {
            return true;
        }

        // Slabs need oxygen unless they're double slabs
        if (block instanceof SlabBlock) {
            return state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
        }

        // Doors need oxygen in their space
        if (block instanceof DoorBlock) {
            return true;
        }

        // Trapdoors need oxygen when open
        if (block instanceof TrapDoorBlock) {
            return state.getValue(TrapDoorBlock.OPEN);
        }

        // Pressure plates, buttons, etc. need oxygen
        if (!state.isCollisionShapeFullBlock(level, pos)) {
            // Check if players can pass through
            VoxelShape collisionShape = state.getCollisionShape(level, pos, CollisionContext.empty());

            // If collision shape is not full (less than 1 block tall), it needs oxygen
            if (collisionShape.isEmpty() || collisionShape.max(Direction.Axis.Y) < 1.0) {
                return true;
            }
        }

        // Full solid blocks don't need oxygen
        if (state.isSolid() && state.isCollisionShapeFullBlock(level, pos)) {
            return false;
        }

        // Default to needing oxygen for safety
        return true;
    }

    /**
     * Check if there's a valid path between two positions.
     * This ensures oxygen doesn't bleed through walls.
     */
    private static boolean hasPathTo(Level level, BlockPos from, BlockPos to, int maxDistance) {
        if (from.equals(to)) {
            return true;
        }

        if (from.distManhattan(to) > maxDistance) {
            return false;
        }

        // Simple A* pathfinding
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Set<BlockPos> closedSet = new HashSet<>();

        openSet.add(new PathNode(from, 0, from.distManhattan(to)));

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();

            if (current.pos.equals(to)) {
                return true;
            }

            if (closedSet.contains(current.pos)) {
                continue;
            }

            closedSet.add(current.pos);

            if (current.cost > maxDistance) {
                continue;
            }

            for (Direction dir : DIRECTIONS) {
                BlockPos neighborPos = current.pos.relative(dir);

                if (closedSet.contains(neighborPos)) {
                    continue;
                }

                // Check if we can pass through this position
                if (!canOxygenPassThrough(level, current.pos, neighborPos, dir)) {
                    continue;
                }

                int newCost = current.cost + 1;
                int heuristic = neighborPos.distManhattan(to);
                openSet.add(new PathNode(neighborPos, newCost, heuristic));
            }
        }

        return false;
    }

    /**
     * Check if oxygen can pass from one position to another.
     * This handles the logic of solid blocks blocking oxygen flow.
     */
    private static boolean canOxygenPassThrough(Level level, BlockPos from, BlockPos to, Direction direction) {
        BlockState fromState = level.getBlockState(from);
        BlockState toState = level.getBlockState(to);

        // Can always pass into air
        if (toState.isAir()) {
            return true;
        }

        // Check if the destination block blocks oxygen
        if (toState.isSolid() && toState.isCollisionShapeFullBlock(level, to)) {
            return false; // Solid blocks block oxygen flow
        }

        // Check collision shapes to see if there's an opening
        VoxelShape fromShape = fromState.getCollisionShape(level, from, CollisionContext.empty());
        VoxelShape toShape = toState.getCollisionShape(level, to, CollisionContext.empty());

        // Check if there's an opening between the blocks
        if (!fromShape.isEmpty() && !toShape.isEmpty()) {
            // Check face connectivity
            VoxelShape fromFace = fromShape.getFaceShape(direction);
            VoxelShape toFace = toShape.getFaceShape(direction.getOpposite());

            // If both faces are solid, oxygen can't pass
            if (!fromFace.isEmpty() && !toFace.isEmpty()) {
                return false;
            }
        }

        // Allow oxygen through partial blocks
        return true;
    }

    /**
     * Simple path node for A* pathfinding
     */
    private static class PathNode implements Comparable<PathNode> {
        final BlockPos pos;
        final int cost;
        final int heuristic;

        PathNode(BlockPos pos, int cost, int heuristic) {
            this.pos = pos;
            this.cost = cost;
            this.heuristic = heuristic;
        }

        @Override
        public int compareTo(PathNode other) {
            return Integer.compare(
                this.cost + this.heuristic,
                other.cost + other.heuristic
            );
        }
    }
}