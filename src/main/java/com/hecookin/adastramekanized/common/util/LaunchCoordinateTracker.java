package com.hecookin.adastramekanized.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Tracks launch coordinates per player per dimension.
 * When a player launches from a planet, their X/Z coordinates are saved.
 * When they return to that planet, they land at those same coordinates.
 */
public class LaunchCoordinateTracker {

    private static final String NBT_KEY = "LaunchCoordinates";

    /**
     * Save the launch coordinates for a player in their current dimension.
     *
     * @param player The player launching
     */
    public static void saveLaunchCoordinates(ServerPlayer player) {
        if (player == null) return;

        ResourceKey<Level> dimension = player.level().dimension();
        BlockPos launchPos = player.blockPosition();

        CompoundTag playerData = player.getPersistentData();
        CompoundTag launchData = playerData.getCompound(NBT_KEY);

        String dimensionKey = dimension.location().toString();
        launchData.putInt(dimensionKey + "_x", launchPos.getX());
        launchData.putInt(dimensionKey + "_z", launchPos.getZ());

        playerData.put(NBT_KEY, launchData);
    }

    /**
     * Get the saved launch coordinates for a player in a specific dimension.
     * Returns null if no coordinates are saved for that dimension.
     *
     * @param player The player landing
     * @param dimension The dimension they're landing in
     * @return BlockPos with saved X/Z coordinates (Y=0), or null if none saved
     */
    public static BlockPos getLaunchCoordinates(ServerPlayer player, ResourceKey<Level> dimension) {
        if (player == null) return null;

        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains(NBT_KEY)) return null;

        CompoundTag launchData = playerData.getCompound(NBT_KEY);
        String dimensionKey = dimension.location().toString();

        if (!launchData.contains(dimensionKey + "_x")) return null;

        int x = launchData.getInt(dimensionKey + "_x");
        int z = launchData.getInt(dimensionKey + "_z");

        return new BlockPos(x, 0, z);
    }

    /**
     * Check if the player has saved launch coordinates for a dimension.
     *
     * @param player The player
     * @param dimension The dimension to check
     * @return True if coordinates are saved
     */
    public static boolean hasLaunchCoordinates(ServerPlayer player, ResourceKey<Level> dimension) {
        return getLaunchCoordinates(player, dimension) != null;
    }

    /**
     * Clear launch coordinates for a specific dimension.
     *
     * @param player The player
     * @param dimension The dimension to clear
     */
    public static void clearLaunchCoordinates(ServerPlayer player, ResourceKey<Level> dimension) {
        if (player == null) return;

        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains(NBT_KEY)) return;

        CompoundTag launchData = playerData.getCompound(NBT_KEY);
        String dimensionKey = dimension.location().toString();

        launchData.remove(dimensionKey + "_x");
        launchData.remove(dimensionKey + "_z");

        playerData.put(NBT_KEY, launchData);
    }
}
