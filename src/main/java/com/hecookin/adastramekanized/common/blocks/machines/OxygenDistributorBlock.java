package com.hecookin.adastramekanized.common.blocks.machines;

import com.hecookin.adastramekanized.common.blockentities.machines.OxygenDistributorBlockEntity;
import com.hecookin.adastramekanized.common.blocks.base.SidedMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class OxygenDistributorBlock extends SidedMachineBlock {

    // Collision shapes for different orientations (from Ad Astra)
    public static final VoxelShape BOTTOM_SHAPE = Shapes.join(
        Block.box(1, 0, 1, 15, 5, 15),
        Block.box(4, 5, 4, 12, 15, 12),
        BooleanOp.OR);

    public static final VoxelShape TOP_SHAPE = Shapes.join(
        Block.box(1, 11, 1, 15, 16, 15),
        Block.box(4, 1, 4, 12, 11, 12),
        BooleanOp.OR);

    public static final VoxelShape NORTH_SIDE_SHAPE = Shapes.join(
        Block.box(1, 1, 11, 15, 15, 16),
        Block.box(4, 4, 1, 12, 12, 11),
        BooleanOp.OR);

    public static final VoxelShape EAST_SIDE_SHAPE = Shapes.join(
        Block.box(0, 1, 1, 5, 15, 15),
        Block.box(5, 4, 4, 15, 12, 12),
        BooleanOp.OR);

    public static final VoxelShape SOUTH_SIDE_SHAPE = Shapes.join(
        Block.box(1, 1, 0, 15, 15, 5),
        Block.box(4, 4, 5, 12, 12, 15),
        BooleanOp.OR);

    public static final VoxelShape WEST_SIDE_SHAPE = Shapes.join(
        Block.box(11, 1, 1, 16, 15, 15),
        Block.box(1, 4, 4, 11, 12, 12),
        BooleanOp.OR);

    public OxygenDistributorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> BOTTOM_SHAPE;
            case CEILING -> TOP_SHAPE;
            case WALL -> switch (state.getValue(FACING)) {
                case NORTH -> NORTH_SIDE_SHAPE;
                case EAST -> EAST_SIDE_SHAPE;
                case SOUTH -> SOUTH_SIDE_SHAPE;
                case WEST -> WEST_SIDE_SHAPE;
                default -> Shapes.block();
            };
        };
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof OxygenDistributorBlockEntity entity) {
            return entity.isActive() ? 15 : 0;
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OxygenDistributorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof OxygenDistributorBlockEntity entity) {
                OxygenDistributorBlockEntity.tick(level1, pos, state1, entity);
            }
        };
    }
}