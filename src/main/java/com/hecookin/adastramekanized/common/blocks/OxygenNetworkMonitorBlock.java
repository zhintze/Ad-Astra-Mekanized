package com.hecookin.adastramekanized.common.blocks;

// TODO INCOMPLETE - This feature is not complete and has been removed from creative tab/JEI
// The oxygen network monitor was intended to be a wall-mounted display showing distributor status
// but needs more work on rendering, GUI, and network synchronization

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.OxygenNetworkMonitorBlockEntity;
import com.hecookin.adastramekanized.common.items.OxygenNetworkController;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Wall-mounted oxygen network monitor block
 */
public class OxygenNetworkMonitorBlock extends BaseEntityBlock {
    public static final MapCodec<OxygenNetworkMonitorBlock> CODEC = simpleCodec(OxygenNetworkMonitorBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Ultra-thin shapes for wall-mounted appearance (1 pixel thick like RFTools)
    protected static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_SHAPE = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public OxygenNetworkMonitorBlock(Properties properties) {
        super(properties
            .strength(1.0F)
            .sound(SoundType.METAL)
            .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Get the face that was clicked
        Direction clickedFace = context.getClickedFace();

        // Only allow placement on vertical surfaces (walls)
        if (clickedFace.getAxis().isHorizontal()) {
            // The monitor faces outward from the wall (same as clicked face)
            return this.defaultBlockState().setValue(FACING, clickedFace);
        }

        // If clicked on floor/ceiling, try to find a nearby wall
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos wallPos = context.getClickedPos().relative(dir);
            if (context.getLevel().getBlockState(wallPos).isFaceSturdy(context.getLevel(), wallPos, dir.getOpposite())) {
                return this.defaultBlockState().setValue(FACING, dir.getOpposite());
            }
        }

        // Default to north if no wall found (should still be placeable)
        return this.defaultBlockState().setValue(FACING, Direction.NORTH);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        // Like RFTools, use opposite shape because the monitor is ON the wall
        switch (facing) {
            case NORTH:
                return SOUTH_SHAPE;  // Monitor faces north, attached to south side of block space
            case SOUTH:
                return NORTH_SHAPE;  // Monitor faces south, attached to north side of block space
            case WEST:
                return EAST_SHAPE;   // Monitor faces west, attached to east side of block space
            case EAST:
            default:
                return WEST_SHAPE;   // Monitor faces east, attached to west side of block space
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OxygenNetworkMonitorBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        // Check if player is holding a controller and sneaking to pair
        if (player.isShiftKeyDown() && stack.getItem() instanceof OxygenNetworkController) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof OxygenNetworkMonitorBlockEntity monitor) {
                AdAstraMekanized.LOGGER.info("Pairing monitor with controller via useItemOn");
                monitor.pairWithController(stack, player);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof OxygenNetworkMonitorBlockEntity monitor)) {
            AdAstraMekanized.LOGGER.warn("No block entity found at monitor position {}", pos);
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getMainHandItem();
        AdAstraMekanized.LOGGER.info("Monitor interaction - Sneaking: {}, Item: {}",
            player.isShiftKeyDown(), heldItem.getItem().getClass().getSimpleName());

        // Check if player is holding a controller and sneaking to pair
        if (player.isShiftKeyDown() && heldItem.getItem() instanceof OxygenNetworkController) {
            AdAstraMekanized.LOGGER.info("Attempting to pair monitor with controller");
            monitor.pairWithController(heldItem, player);
            return InteractionResult.SUCCESS;
        }

        // Otherwise, try to open the monitor GUI
        if (monitor.isPaired()) {
            if (player instanceof ServerPlayer serverPlayer) {
                monitor.openMenu(serverPlayer);
            }
        } else {
            player.displayClientMessage(
                Component.literal("Monitor not paired with controller")
                    .withStyle(ChatFormatting.RED),
                true
            );
            player.displayClientMessage(
                Component.literal("Sneak + Right-click with controller to pair")
                    .withStyle(ChatFormatting.GRAY),
                false
            );
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof OxygenNetworkMonitorBlockEntity monitor) {
                monitor.onRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}