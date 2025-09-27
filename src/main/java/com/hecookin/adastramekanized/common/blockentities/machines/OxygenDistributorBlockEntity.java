package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
import com.hecookin.adastramekanized.common.blocks.base.SidedMachineBlock;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.utils.OxygenFloodFill;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import java.util.HashSet;
import java.util.Set;

/**
 * Improved oxygen distributor that extends the Mekanism-based one
 * to inherit all pipe connections and GUI, but uses smart flood fill
 */
public class OxygenDistributorBlockEntity extends MekanismBasedOxygenDistributor {

    private static final int INITIAL_SEARCH_RADIUS = 10;
    // 10-block radius sphere contains approximately 4,100 blocks
    private static final int MAX_OXYGEN_BLOCKS = 4100;
    private static final float OXYGEN_PER_BLOCK = 0.05f; // Reduced consumption for larger area

    // Animation fields for renderer
    public float yRot = 0.0f;
    private float lastYRot = 0.0f;

    public OxygenDistributorBlockEntity(BlockPos pos, BlockState blockState) {
        // Call parent constructor which initializes all Mekanism integration
        // But we need to use our own block entity type
        super(ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(), pos, blockState);
    }

    /**
     * Override the parent's simple distribution with our smart flood fill
     */
    @Override
    protected void distributeOxygen() {
        // Get the proper starting position based on block orientation
        BlockPos startPos = getStartPosition(worldPosition);

        // Use the improved flood fill algorithm
        Set<BlockPos> newOxygenatedBlocks = OxygenFloodFill.findOxygenatableArea(
            level,
            startPos,
            INITIAL_SEARCH_RADIUS,
            MAX_OXYGEN_BLOCKS
        );

        // Only update if the oxygenated area has changed
        Set<BlockPos> currentBlocks = getOxygenatedBlocks();
        if (!newOxygenatedBlocks.equals(currentBlocks)) {
            // Clear old oxygen distribution first
            if (!currentBlocks.isEmpty()) {
                OxygenManager.getInstance().setOxygen(level, currentBlocks, false);
            }

            // Update internal tracking
            currentBlocks.clear();
            currentBlocks.addAll(newOxygenatedBlocks);

            // Apply new oxygen distribution
            if (!newOxygenatedBlocks.isEmpty()) {
                OxygenManager.getInstance().setOxygen(level, newOxygenatedBlocks, true);
            }

            AdAstraMekanized.LOGGER.info("Oxygen distributor at {} updated oxygenated area to {} blocks",
                worldPosition, newOxygenatedBlocks.size());
        }

        // Consume oxygen from tank regardless (continuous consumption)
        if (!newOxygenatedBlocks.isEmpty() && getOxygenTank() != null && !getOxygenTank().isEmpty()) {
            // Calculate dynamic oxygen consumption based on blocks oxygenated
            float oxygenToConsume = newOxygenatedBlocks.size() * OXYGEN_PER_BLOCK;

            // Extract oxygen from tank
            int oxygenToExtract = (int) Math.ceil(oxygenToConsume);
            if (oxygenToExtract > 0) {
                getOxygenTank().extract(oxygenToExtract, Action.EXECUTE, AutomationType.INTERNAL);
            }
        }
    }

    /**
     * Gets the proper starting position based on block orientation
     */
    private BlockPos getStartPosition(BlockPos distributorPos) {
        BlockState state = level.getBlockState(distributorPos);

        // Check if block has a face property (floor/wall/ceiling mount)
        if (state.hasProperty(SidedMachineBlock.FACE)) {
            AttachFace face = state.getValue(SidedMachineBlock.FACE);
            return switch (face) {
                case FLOOR -> distributorPos.above();
                case CEILING -> distributorPos.below();
                case WALL -> {
                    if (state.hasProperty(SidedMachineBlock.FACING)) {
                        Direction facing = state.getValue(SidedMachineBlock.FACING);
                        yield distributorPos.relative(facing);
                    }
                    yield distributorPos.above();
                }
            };
        }

        // Default to above the distributor
        return distributorPos.above();
    }

    // Additional getters for status display
    public int getMaxOxygenBlocks() {
        return MAX_OXYGEN_BLOCKS;
    }

    public int getEnergyPerTick() {
        if (!isActive()) return 0;
        // Use parent's ENERGY_PER_DISTRIBUTION value
        return 400; // From parent class
    }

    public float getOxygenPerTick() {
        if (!isActive()) return 0;
        return getOxygenatedBlockCount() * OXYGEN_PER_BLOCK;
    }

    public float getEfficiency() {
        if (MAX_OXYGEN_BLOCKS == 0) return 0;
        return (float) getOxygenatedBlockCount() / MAX_OXYGEN_BLOCKS * 100f;
    }

    // Override tick to handle animation
    @Override
    public void tick() {
        // Call parent tick for all logic
        super.tick();

        // Handle animation on client side
        if (level != null && level.isClientSide && isActive()) {
            lastYRot = yRot;
            yRot = (yRot + 3.6f) % 360.0f;
        }
    }

    // Getters for renderer
    public float yRot() {
        return yRot;
    }

    public float lastYRot() {
        return lastYRot;
    }
}