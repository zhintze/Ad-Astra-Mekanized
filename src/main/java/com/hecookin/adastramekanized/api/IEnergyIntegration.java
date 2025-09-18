package com.hecookin.adastramekanized.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Interface for energy system integration (primarily with Mekanism)
 *
 * This allows our mod to work with or without Mekanism's energy system,
 * providing graceful fallbacks when the integration is not available.
 */
public interface IEnergyIntegration {

    /**
     * Check if the energy integration system is available and loaded
     * @return true if energy APIs are available
     */
    boolean isEnergySystemAvailable();

    /**
     * Transfer energy between containers
     * @param fromPos Source position (energy storage, cable, etc.)
     * @param toPos Target position
     * @param level The world level
     * @param amount Amount of energy to transfer (in mod energy units)
     * @return Amount actually transferred, or 0 if integration not available
     */
    long transferEnergy(BlockPos fromPos, BlockPos toPos, Level level, long amount);

    /**
     * Check if a block entity can store energy
     * @param level The world level
     * @param pos Position to check
     * @return true if the block can store energy
     */
    boolean canStoreEnergy(Level level, BlockPos pos);

    /**
     * Get the current energy amount stored at a position
     * @param level The world level
     * @param pos Position to check
     * @return Amount of energy stored, or 0 if none or not supported
     */
    long getStoredEnergy(Level level, BlockPos pos);

    /**
     * Get the maximum energy capacity at a position
     * @param level The world level
     * @param pos Position to check
     * @return Maximum energy capacity, or 0 if not supported
     */
    long getEnergyCapacity(Level level, BlockPos pos);

    /**
     * Insert energy into an energy storage at the given position
     * @param level The world level
     * @param pos Position of the storage
     * @param amount Amount to insert
     * @param simulate If true, only simulate the insertion
     * @return Amount actually inserted (or would be inserted if simulating)
     */
    long insertEnergy(Level level, BlockPos pos, long amount, boolean simulate);

    /**
     * Extract energy from an energy storage at the given position
     * @param level The world level
     * @param pos Position of the storage
     * @param amount Maximum amount to extract
     * @param simulate If true, only simulate the extraction
     * @return Amount actually extracted (or would be extracted if simulating)
     */
    long extractEnergy(Level level, BlockPos pos, long amount, boolean simulate);
}