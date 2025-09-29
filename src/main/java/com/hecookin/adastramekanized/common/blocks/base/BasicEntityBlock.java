package com.hecookin.adastramekanized.common.blocks.base;

import com.hecookin.adastramekanized.common.blockentities.base.TickableBlockEntity;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Base class for blocks with block entities, providing automatic block entity type resolution
 * and ticker support for TickableBlockEntity implementations.
 */
public abstract class BasicEntityBlock extends BaseEntityBlock {

    private final boolean shouldTick;
    private final Supplier<BlockEntityType<?>> blockEntityType;

    public BasicEntityBlock(Properties properties, boolean shouldTick, Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties);
        this.shouldTick = shouldTick;
        this.blockEntityType = blockEntityType;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityType.get().create(pos, state);
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !shouldTick ? null : (entityLevel, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof TickableBlockEntity tickable) {
                long time = level.getGameTime() - pos.hashCode();
                tickable.tick(entityLevel, time, blockState, pos);
                if (level.isClientSide()) {
                    tickable.clientTick((ClientLevel) level, time, state, pos);
                } else {
                    tickable.serverTick((ServerLevel) level, time, state, pos);
                    tickable.internalServerTick((ServerLevel) level, time, state, pos);
                }
                if (!tickable.isInitialized()) tickable.firstTick(level, pos, state);
            }
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof TickableBlockEntity tickable) {
                tickable.onRemoved();
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }
}