package com.hecookin.adastramekanized.common.blockentities.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Legacy oxygen distributor - now just extends the improved version
 * Kept for backward compatibility
 */
public class OxygenDistributorBlockEntity extends ImprovedOxygenDistributor {

    public OxygenDistributorBlockEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

}