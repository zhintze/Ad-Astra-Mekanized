package com.hecookin.adastramekanized.common.blockentities.base;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Interface for block entities that need to tick every game tick.
 */
public interface TickableBlockEntity {

    /**
     * Called every tick for both client and server.
     */
    default void tick(Level level, long time, BlockState state, BlockPos pos) {}

    /**
     * Called on the client side only.
     */
    default void clientTick(ClientLevel level, long time, BlockState state, BlockPos pos) {}

    /**
     * Called on the server side only.
     */
    default void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos) {}

    /**
     * Internal server tick for special processing.
     */
    default void internalServerTick(ServerLevel level, long time, BlockState state, BlockPos pos) {}

    /**
     * Called when the block entity is first initialized.
     */
    default void firstTick(Level level, BlockPos pos, BlockState state) {}

    /**
     * Returns whether this block entity has been initialized.
     */
    default boolean isInitialized() {
        return true;
    }

    /**
     * Called when the block entity is removed.
     */
    default void onRemoved() {}
}