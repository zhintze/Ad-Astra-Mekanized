package com.hecookin.adastramekanized.api;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Interface for fuel system integration (primarily with Immersive Engineering)
 *
 * This allows our mod to work with or without IE's fuel system,
 * providing graceful fallbacks when the integration is not available.
 */
public interface IFuelIntegration {

    /**
     * Check if the fuel integration system is available and loaded
     * @return true if fuel APIs are available
     */
    boolean isFuelSystemAvailable();

    /**
     * Check if a fluid can be used as rocket fuel
     * @param fluid The fluid to check
     * @return true if this fluid is valid rocket fuel
     */
    boolean isValidRocketFuel(FluidStack fluid);

    /**
     * Check if an item can be used as rocket fuel
     * @param item The item to check
     * @return true if this item is valid rocket fuel
     */
    boolean isValidRocketFuel(ItemStack item);

    /**
     * Get the energy value of a fuel fluid (for calculating range/consumption)
     * @param fluid The fuel fluid
     * @return Energy value per mB, or 0 if not fuel
     */
    long getFuelEnergyValue(FluidStack fluid);

    /**
     * Get the energy value of a fuel item
     * @param item The fuel item
     * @return Energy value per item, or 0 if not fuel
     */
    long getFuelEnergyValue(ItemStack item);

    /**
     * Get the primary rocket fuel fluid (IE fuel)
     * @return FluidStack representing the primary fuel, or empty if not available
     */
    FluidStack getPrimaryRocketFuel(int amount);

    /**
     * Get the secondary rocket fuel fluid (IE biodiesel)
     * @return FluidStack representing the secondary fuel, or empty if not available
     */
    FluidStack getSecondaryRocketFuel(int amount);

    /**
     * Get all available rocket fuels
     * @return Array of fuel FluidStacks, may be empty if integration unavailable
     */
    FluidStack[] getAvailableRocketFuels();
}