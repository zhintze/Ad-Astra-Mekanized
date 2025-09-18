package com.hecookin.adastramekanized.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Interface for chemical system integration (primarily with Mekanism)
 *
 * This allows our mod to work with or without Mekanism's chemical system,
 * providing graceful fallbacks when the integration is not available.
 */
public interface IChemicalIntegration {

    /**
     * Check if the chemical integration system is available and loaded
     * @return true if chemical APIs are available
     */
    boolean isChemicalSystemAvailable();

    /**
     * Initialize oxygen chemical registration if possible
     * This should be called during mod setup
     */
    void initializeOxygenChemical();

    /**
     * Transfer oxygen chemical between containers
     * @param fromPos Source position (tank, pipe, etc.)
     * @param toPos Target position
     * @param level The world level
     * @param amount Amount of oxygen to transfer (in mB or mod units)
     * @return Amount actually transferred, or 0 if integration not available
     */
    long transferOxygen(BlockPos fromPos, BlockPos toPos, Level level, long amount);

    /**
     * Check if a block entity can store oxygen chemical
     * @param level The world level
     * @param pos Position to check
     * @return true if the block can store oxygen chemical
     */
    boolean canStoreOxygen(Level level, BlockPos pos);

    /**
     * Get the current oxygen amount stored at a position
     * @param level The world level
     * @param pos Position to check
     * @return Amount of oxygen stored, or 0 if none or not supported
     */
    long getStoredOxygen(Level level, BlockPos pos);

    /**
     * Insert oxygen into a chemical storage at the given position
     * @param level The world level
     * @param pos Position of the storage
     * @param amount Amount to insert
     * @return Amount actually inserted
     */
    long insertOxygen(Level level, BlockPos pos, long amount);

    /**
     * Extract oxygen from a chemical storage at the given position
     * @param level The world level
     * @param pos Position of the storage
     * @param amount Maximum amount to extract
     * @return Amount actually extracted
     */
    long extractOxygen(Level level, BlockPos pos, long amount);
}