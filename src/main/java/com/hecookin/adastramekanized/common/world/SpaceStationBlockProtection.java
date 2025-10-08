package com.hecookin.adastramekanized.common.world;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks protected space station blocks that cannot be broken.
 * Persists block positions in the level's saved data.
 */
public class SpaceStationBlockProtection extends SavedData {

    private static final String DATA_NAME = AdAstraMekanized.MOD_ID + "_space_station_protection";

    private final Set<BlockPos> protectedBlocks = new HashSet<>();

    public SpaceStationBlockProtection() {
        super();
    }

    public static SpaceStationBlockProtection load(CompoundTag tag, HolderLookup.Provider registries) {
        SpaceStationBlockProtection protection = new SpaceStationBlockProtection();

        ListTag blockList = tag.getList("ProtectedBlocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < blockList.size(); i++) {
            CompoundTag blockTag = blockList.getCompound(i);
            int x = blockTag.getInt("x");
            int y = blockTag.getInt("y");
            int z = blockTag.getInt("z");
            protection.protectedBlocks.add(new BlockPos(x, y, z));
        }

        return protection;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag blockList = new ListTag();

        for (BlockPos pos : protectedBlocks) {
            CompoundTag blockTag = new CompoundTag();
            blockTag.putInt("x", pos.getX());
            blockTag.putInt("y", pos.getY());
            blockTag.putInt("z", pos.getZ());
            blockList.add(blockTag);
        }

        tag.put("ProtectedBlocks", blockList);
        return tag;
    }

    public static SpaceStationBlockProtection get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(SpaceStationBlockProtection::new, SpaceStationBlockProtection::load),
            DATA_NAME
        );
    }

    /**
     * Add a block position to the protected set.
     */
    public void protectBlock(BlockPos pos) {
        protectedBlocks.add(pos.immutable());
        setDirty();
    }

    /**
     * Add multiple block positions to the protected set.
     */
    public void protectBlocks(Set<BlockPos> positions) {
        for (BlockPos pos : positions) {
            protectedBlocks.add(pos.immutable());
        }
        setDirty();
    }

    /**
     * Check if a block is protected.
     */
    public boolean isProtected(BlockPos pos) {
        return protectedBlocks.contains(pos);
    }

    /**
     * Remove a block from protection (for admin/debugging).
     */
    public void unprotectBlock(BlockPos pos) {
        protectedBlocks.remove(pos);
        setDirty();
    }

    /**
     * Clear all protected blocks (for admin/debugging).
     */
    public void clearAll() {
        protectedBlocks.clear();
        setDirty();
    }

    /**
     * Get the number of protected blocks.
     */
    public int getProtectedBlockCount() {
        return protectedBlocks.size();
    }
}
