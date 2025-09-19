package com.hecookin.adastramekanized.common.blocks.machines;

import com.hecookin.adastramekanized.common.blocks.base.MachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class OxygenLoaderBlock extends MachineBlock {

    public OxygenLoaderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        // For now, just return based on powered state
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Simple block without block entity for now
        return null;
    }
}