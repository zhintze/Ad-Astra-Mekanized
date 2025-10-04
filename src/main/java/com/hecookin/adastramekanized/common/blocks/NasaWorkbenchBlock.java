package com.hecookin.adastramekanized.common.blocks;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.NasaWorkbenchBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NasaWorkbenchBlock extends BaseEntityBlock {

    public static final MapCodec<NasaWorkbenchBlock> CODEC = simpleCodec(NasaWorkbenchBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public NasaWorkbenchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            AdAstraMekanized.LOGGER.info("NASA Workbench clicked (with item) - BlockEntity: {}", blockEntity);
            if (blockEntity instanceof MenuProvider menuProvider && player instanceof ServerPlayer serverPlayer) {
                AdAstraMekanized.LOGGER.info("Opening menu for player: {}", player.getName().getString());
                serverPlayer.openMenu(menuProvider, pos);
                return ItemInteractionResult.SUCCESS;
            } else {
                AdAstraMekanized.LOGGER.warn("Failed to open menu - BlockEntity type: {}, Player type: {}",
                    blockEntity != null ? blockEntity.getClass().getName() : "null",
                    player.getClass().getName());
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            AdAstraMekanized.LOGGER.info("NASA Workbench clicked - BlockEntity: {}", blockEntity);
            if (blockEntity instanceof MenuProvider menuProvider && player instanceof ServerPlayer serverPlayer) {
                AdAstraMekanized.LOGGER.info("Opening menu for player: {}", player.getName().getString());
                serverPlayer.openMenu(menuProvider, pos);
            } else {
                AdAstraMekanized.LOGGER.warn("Failed to open menu - BlockEntity type: {}, Player type: {}",
                    blockEntity != null ? blockEntity.getClass().getName() : "null",
                    player.getClass().getName());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        AdAstraMekanized.LOGGER.info("Creating NASA Workbench BlockEntity at {}", pos);
        NasaWorkbenchBlockEntity entity = new NasaWorkbenchBlockEntity(pos, state);
        AdAstraMekanized.LOGGER.info("Created BlockEntity: {}", entity);
        return entity;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide() ? null : (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof NasaWorkbenchBlockEntity entity) {
                NasaWorkbenchBlockEntity.tick(level1, pos, state1, entity);
            }
        };
    }
}
