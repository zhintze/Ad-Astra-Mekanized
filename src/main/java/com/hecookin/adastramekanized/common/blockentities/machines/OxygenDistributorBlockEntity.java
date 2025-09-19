package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.common.blocks.base.SidedMachineBlock;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class OxygenDistributorBlockEntity extends BlockEntity {

    private static final int MAX_OXYGEN_RANGE = 16;
    private static final int OXYGEN_DISTRIBUTION_INTERVAL = 20; // ticks

    public float yRot = 0.0f;
    public float lastYRot = 0.0f;
    private boolean isActive = false;
    private int tickCounter = 0;

    public OxygenDistributorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OxygenDistributorBlockEntity entity) {
        if (level.isClientSide) {
            // Client-side animation
            entity.lastYRot = entity.yRot;
            entity.isActive = entity.canFunction();

            if (entity.isActive) {
                entity.yRot += 2f; // Rotation speed (reduced for smoother animation)
                // Keep rotation within 0-360 range, but handle wraparound properly
                entity.yRot = entity.yRot % 360f;
            }
        } else {
            // Server-side oxygen distribution
            entity.tickCounter++;
            if (entity.tickCounter >= OXYGEN_DISTRIBUTION_INTERVAL) {
                entity.tickCounter = 0;
                if (entity.canFunction()) {
                    entity.distributeOxygen(level, pos);
                }
            }
        }
    }

    private boolean canFunction() {
        return true; // TODO: Add power/energy requirements
    }

    private void distributeOxygen(Level level, BlockPos centerPos) {
        Set<BlockPos> oxygenatedBlocks = new HashSet<>();
        Queue<BlockPos> toProcess = new ArrayDeque<>();

        // Start from the distributor position
        toProcess.add(centerPos);
        oxygenatedBlocks.add(centerPos);

        while (!toProcess.isEmpty() && oxygenatedBlocks.size() < MAX_OXYGEN_RANGE * MAX_OXYGEN_RANGE) {
            BlockPos currentPos = toProcess.poll();

            // Check all 6 directions from current position
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(direction);

                // Skip if already processed or too far from center
                if (oxygenatedBlocks.contains(neighborPos) ||
                    neighborPos.distManhattan(centerPos) > MAX_OXYGEN_RANGE) {
                    continue;
                }

                BlockState neighborState = level.getBlockState(neighborPos);

                // Only spread through air blocks
                if (neighborState.isAir() || neighborState.is(Blocks.CAVE_AIR)) {
                    oxygenatedBlocks.add(neighborPos);
                    toProcess.add(neighborPos);

                    // TODO: Add actual oxygen block/effect here
                    // For now, this is just calculating the distribution area
                }
            }
        }

        // Debug: Log how many blocks would be oxygenated
        if (oxygenatedBlocks.size() > 1) { // More than just the distributor itself
            // System.out.println("Oxygen distributor at " + centerPos + " would oxygenate " + oxygenatedBlocks.size() + " blocks");
        }
    }

    public float yRot() {
        return yRot;
    }

    public float lastYRot() {
        return lastYRot;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("yRot", yRot);
        tag.putBoolean("isActive", isActive);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        yRot = tag.getFloat("yRot");
        lastYRot = yRot;
        isActive = tag.getBoolean("isActive");
    }
}