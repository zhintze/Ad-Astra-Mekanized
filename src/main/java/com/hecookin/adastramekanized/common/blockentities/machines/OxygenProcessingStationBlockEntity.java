package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.common.blocks.base.SidedMachineBlock;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class OxygenProcessingStationBlockEntity extends BlockEntity {

    // Rotation animation properties
    private float yRot = 0f;
    private float lastYRot = 0f;

    // Oxygen distribution properties
    private Set<BlockPos> distributedBlocks = new HashSet<>();
    private int distributionRadius = 16; // blocks
    private int maxDistributedBlocks = 256;
    private boolean isActive = false;
    private int workTimer = 0;
    private static final int WORK_INTERVAL = 20; // ticks (1 second)

    // Energy and fluid simulation (simplified)
    private int energyStored = 0;
    private int maxEnergy = 10000;
    private int energyPerTick = 10;
    private int oxygenStored = 0;
    private int maxOxygen = 1000;
    private int oxygenPerTick = 1;

    public OxygenProcessingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.OXYGEN_PROCESSING_STATION.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OxygenProcessingStationBlockEntity entity) {
        if (level == null) return;

        // Update rotation animation
        entity.lastYRot = entity.yRot;

        // Check if machine should be active
        entity.isActive = entity.canFunction();

        if (entity.isActive) {
            // Rotate when active
            entity.yRot += 10f;
            if (entity.yRot >= 360f) {
                entity.yRot -= 360f;
            }

            // Work timer
            entity.workTimer++;
            if (entity.workTimer >= WORK_INTERVAL) {
                entity.workTimer = 0;
                entity.distributeOxygen(level, pos, state);
            }
        }

        // Update block state LIT property
        if (state.getValue(SidedMachineBlock.LIT) != entity.isActive) {
            level.setBlockAndUpdate(pos, state.setValue(SidedMachineBlock.LIT, entity.isActive));
        }

        // Sync to client for rendering
        if (!level.isClientSide && entity.workTimer % 10 == 0) {
            entity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private boolean canFunction() {
        // Check if we have energy and oxygen to function
        return energyStored >= energyPerTick && oxygenStored >= oxygenPerTick;
    }

    private void distributeOxygen(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // Simple flood fill algorithm for oxygen distribution
        distributedBlocks.clear();
        Set<BlockPos> toCheck = new HashSet<>();
        Set<BlockPos> checked = new HashSet<>();

        // Start from the machine position
        BlockPos startPos = getDistributionStartPos(state, pos);
        toCheck.add(startPos);

        while (!toCheck.isEmpty() && distributedBlocks.size() < maxDistributedBlocks) {
            BlockPos current = toCheck.iterator().next();
            toCheck.remove(current);

            if (checked.contains(current)) continue;
            checked.add(current);

            // Check if position is within range
            if (current.distSqr(pos) > distributionRadius * distributionRadius) continue;

            // Check if this position can hold oxygen (air blocks or similar)
            if (level.getBlockState(current).isAir()) {
                distributedBlocks.add(current);

                // Add neighboring positions to check
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos neighbor = current.offset(dx, dy, dz);
                            if (!checked.contains(neighbor)) {
                                toCheck.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        // Consume resources
        energyStored = Math.max(0, energyStored - energyPerTick);
        oxygenStored = Math.max(0, oxygenStored - oxygenPerTick);

        // For testing, auto-refill resources (remove this for real implementation)
        energyStored = Math.min(maxEnergy, energyStored + energyPerTick * 2);
        oxygenStored = Math.min(maxOxygen, oxygenStored + oxygenPerTick * 2);
    }

    private BlockPos getDistributionStartPos(BlockState state, BlockPos pos) {
        AttachFace face = state.getValue(SidedMachineBlock.FACE);
        return switch (face) {
            case FLOOR -> pos.above();
            case CEILING -> pos.below();
            case WALL -> pos.relative(state.getValue(SidedMachineBlock.FACING).getOpposite());
        };
    }

    // Getters for renderer
    public float yRot() {
        return yRot;
    }

    public float lastYRot() {
        return lastYRot;
    }

    public boolean isLit() {
        return getBlockState().getValue(SidedMachineBlock.LIT);
    }

    public int distributedBlocksCount() {
        return distributedBlocks.size();
    }

    public int distributedBlocksLimit() {
        return maxDistributedBlocks;
    }

    public Set<BlockPos> getDistributedBlocks() {
        return new HashSet<>(distributedBlocks);
    }

    // NBT serialization
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("yRot", yRot);
        tag.putFloat("lastYRot", lastYRot);
        tag.putBoolean("isActive", isActive);
        tag.putInt("workTimer", workTimer);
        tag.putInt("energyStored", energyStored);
        tag.putInt("oxygenStored", oxygenStored);
        tag.putInt("distributionRadius", distributionRadius);
        tag.putInt("maxDistributedBlocks", maxDistributedBlocks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        yRot = tag.getFloat("yRot");
        lastYRot = tag.getFloat("lastYRot");
        isActive = tag.getBoolean("isActive");
        workTimer = tag.getInt("workTimer");
        energyStored = tag.getInt("energyStored");
        oxygenStored = tag.getInt("oxygenStored");
        distributionRadius = tag.getInt("distributionRadius");
        maxDistributedBlocks = tag.getInt("maxDistributedBlocks");
    }

    // Client sync
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}