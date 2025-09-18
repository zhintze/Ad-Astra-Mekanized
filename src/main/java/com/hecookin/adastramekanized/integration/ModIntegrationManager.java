package com.hecookin.adastramekanized.integration;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.IChemicalIntegration;
import com.hecookin.adastramekanized.api.IEnergyIntegration;
import com.hecookin.adastramekanized.api.IFuelIntegration;
import com.hecookin.adastramekanized.integration.create.CreateIntegration;
import com.hecookin.adastramekanized.integration.immersiveengineering.ImmersiveEngineeringIntegration;
import com.hecookin.adastramekanized.integration.mekanism.MekanismIntegration;

/**
 * Central manager for all mod integrations
 *
 * This class provides a single access point for all mod integrations,
 * handles initialization, and provides fallback implementations when
 * integrated mods are not available.
 */
public class ModIntegrationManager {
    private static ModIntegrationManager instance;

    // Integration instances
    private final MekanismIntegration mekanismIntegration;
    private final ImmersiveEngineeringIntegration ieIntegration;
    private final CreateIntegration createIntegration;

    // Fallback implementations
    private final IChemicalIntegration fallbackChemicalIntegration;
    private final IEnergyIntegration fallbackEnergyIntegration;
    private final IFuelIntegration fallbackFuelIntegration;

    private ModIntegrationManager() {
        AdAstraMekanized.LOGGER.info("Initializing mod integrations...");

        // Initialize all integrations
        this.mekanismIntegration = new MekanismIntegration();
        this.ieIntegration = new ImmersiveEngineeringIntegration();
        this.createIntegration = new CreateIntegration();

        // Initialize fallback implementations
        this.fallbackChemicalIntegration = new FallbackChemicalIntegration();
        this.fallbackEnergyIntegration = new FallbackEnergyIntegration();
        this.fallbackFuelIntegration = new FallbackFuelIntegration();

        // Initialize oxygen chemical if Mekanism is available
        if (mekanismIntegration.isChemicalSystemAvailable()) {
            mekanismIntegration.initializeOxygenChemical();
        }

        AdAstraMekanized.LOGGER.info("Mod integrations initialized successfully");
        logIntegrationStatus();
    }

    public static ModIntegrationManager getInstance() {
        if (instance == null) {
            instance = new ModIntegrationManager();
        }
        return instance;
    }

    // === Integration Access Methods ===

    /**
     * Get the chemical integration (Mekanism or fallback)
     * @return Active chemical integration implementation
     */
    public IChemicalIntegration getChemicalIntegration() {
        if (mekanismIntegration.isChemicalSystemAvailable()) {
            return mekanismIntegration;
        }
        return fallbackChemicalIntegration;
    }

    /**
     * Get the energy integration (Mekanism or fallback)
     * @return Active energy integration implementation
     */
    public IEnergyIntegration getEnergyIntegration() {
        if (mekanismIntegration.isEnergySystemAvailable()) {
            return mekanismIntegration;
        }
        return fallbackEnergyIntegration;
    }

    /**
     * Get the fuel integration (IE or fallback)
     * @return Active fuel integration implementation
     */
    public IFuelIntegration getFuelIntegration() {
        if (ieIntegration.isFuelSystemAvailable()) {
            return ieIntegration;
        }
        return fallbackFuelIntegration;
    }

    /**
     * Get the Create integration
     * @return Create integration instance (may not be available)
     */
    public CreateIntegration getCreateIntegration() {
        return createIntegration;
    }

    /**
     * Get the raw Mekanism integration
     * @return Mekanism integration instance
     */
    public MekanismIntegration getMekanismIntegration() {
        return mekanismIntegration;
    }

    /**
     * Get the raw IE integration
     * @return IE integration instance
     */
    public ImmersiveEngineeringIntegration getIEIntegration() {
        return ieIntegration;
    }

    // === Status Methods ===

    /**
     * Check if any energy system is available
     * @return true if energy integration is available
     */
    public boolean hasEnergyIntegration() {
        return mekanismIntegration.isEnergySystemAvailable();
    }

    /**
     * Check if any chemical system is available
     * @return true if chemical integration is available
     */
    public boolean hasChemicalIntegration() {
        return mekanismIntegration.isChemicalSystemAvailable();
    }

    /**
     * Check if any fuel system is available
     * @return true if fuel integration is available
     */
    public boolean hasFuelIntegration() {
        return ieIntegration.isFuelSystemAvailable();
    }

    /**
     * Check if Create integration is available
     * @return true if Create integration is available
     */
    public boolean hasCreateIntegration() {
        return createIntegration.isCreateSystemAvailable();
    }

    /**
     * Get integration status summary
     * @return String describing current integration status
     */
    public String getIntegrationStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Mod Integrations: ");
        status.append("Mekanism=").append(hasChemicalIntegration() || hasEnergyIntegration());
        status.append(", IE=").append(hasFuelIntegration());
        status.append(", Create=").append(hasCreateIntegration());
        return status.toString();
    }

    private void logIntegrationStatus() {
        AdAstraMekanized.LOGGER.info("=== Integration Status ===");
        AdAstraMekanized.LOGGER.info("Mekanism Energy: {}", hasEnergyIntegration());
        AdAstraMekanized.LOGGER.info("Mekanism Chemical: {}", hasChemicalIntegration());
        AdAstraMekanized.LOGGER.info("Immersive Engineering Fuel: {}", hasFuelIntegration());
        AdAstraMekanized.LOGGER.info("Create: {}", hasCreateIntegration());
        AdAstraMekanized.LOGGER.info("========================");
    }

    // === Fallback Implementations ===

    private static class FallbackChemicalIntegration implements IChemicalIntegration {
        @Override
        public boolean isChemicalSystemAvailable() {
            return false;
        }

        @Override
        public void initializeOxygenChemical() {
            // No-op
        }

        @Override
        public long transferOxygen(net.minecraft.core.BlockPos fromPos, net.minecraft.core.BlockPos toPos, net.minecraft.world.level.Level level, long amount) {
            return 0;
        }

        @Override
        public boolean canStoreOxygen(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
            return false;
        }

        @Override
        public long getStoredOxygen(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
            return 0;
        }

        @Override
        public long insertOxygen(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, long amount) {
            return 0;
        }

        @Override
        public long extractOxygen(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, long amount) {
            return 0;
        }
    }

    private static class FallbackEnergyIntegration implements IEnergyIntegration {
        @Override
        public boolean isEnergySystemAvailable() {
            return false;
        }

        @Override
        public long transferEnergy(net.minecraft.core.BlockPos fromPos, net.minecraft.core.BlockPos toPos, net.minecraft.world.level.Level level, long amount) {
            return 0;
        }

        @Override
        public boolean canStoreEnergy(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
            return false;
        }

        @Override
        public long getStoredEnergy(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
            return 0;
        }

        @Override
        public long getEnergyCapacity(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
            return 0;
        }

        @Override
        public long insertEnergy(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, long amount, boolean simulate) {
            return 0;
        }

        @Override
        public long extractEnergy(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, long amount, boolean simulate) {
            return 0;
        }
    }

    private static class FallbackFuelIntegration implements IFuelIntegration {
        @Override
        public boolean isFuelSystemAvailable() {
            return false;
        }

        @Override
        public boolean isValidRocketFuel(net.neoforged.neoforge.fluids.FluidStack fluid) {
            return false;
        }

        @Override
        public boolean isValidRocketFuel(net.minecraft.world.item.ItemStack item) {
            return false;
        }

        @Override
        public long getFuelEnergyValue(net.neoforged.neoforge.fluids.FluidStack fluid) {
            return 0;
        }

        @Override
        public long getFuelEnergyValue(net.minecraft.world.item.ItemStack item) {
            return 0;
        }

        @Override
        public net.neoforged.neoforge.fluids.FluidStack getPrimaryRocketFuel(int amount) {
            return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        }

        @Override
        public net.neoforged.neoforge.fluids.FluidStack getSecondaryRocketFuel(int amount) {
            return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        }

        @Override
        public net.neoforged.neoforge.fluids.FluidStack[] getAvailableRocketFuels() {
            return new net.neoforged.neoforge.fluids.FluidStack[0];
        }
    }
}