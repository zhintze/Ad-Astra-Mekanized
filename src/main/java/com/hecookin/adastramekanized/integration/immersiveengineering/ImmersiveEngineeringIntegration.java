package com.hecookin.adastramekanized.integration.immersiveengineering;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.IFuelIntegration;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Immersive Engineering integration using reflection to avoid compile-time dependencies
 *
 * This implementation provides full IE fuel integration when the mod is present,
 * and graceful fallbacks when it's not available.
 */
public class ImmersiveEngineeringIntegration implements IFuelIntegration {
    private static final String IE_MOD_ID = "immersiveengineering";

    // Integration state
    private final boolean ieLoaded;
    private boolean apiAccessible = false;

    // Reflected classes (cached for performance)
    private Class<?> ieFluidRegistryClass;
    private Class<?> ieFuelHandlerClass;
    private Class<?> ieConfigClass;

    // Reflected fields for fluids
    private Object dieselFluid;
    private Object biodieselFluid;

    // Reflected methods
    private Method getFuelValueMethod;
    private Method isFuelMethod;

    public ImmersiveEngineeringIntegration() {
        this.ieLoaded = ModList.get().isLoaded(IE_MOD_ID);

        if (ieLoaded) {
            initializeReflection();
        }

        AdAstraMekanized.LOGGER.info("Immersive Engineering integration initialized - Available: {}, API Accessible: {}",
                ieLoaded, apiAccessible);
    }

    private void initializeReflection() {
        try {
            // Load IE fluid registry
            Class<?> ieFluidRegistryClass = Class.forName("blusunrize.immersiveengineering.common.register.IEFluids");

            // Try to access fuel fluids
            try {
                Field dieselField = ieFluidRegistryClass.getField("diesel");
                Field biodieselField = ieFluidRegistryClass.getField("biodiesel");

                dieselFluid = dieselField.get(null);
                biodieselFluid = biodieselField.get(null);

                AdAstraMekanized.LOGGER.info("IE fuel fluids loaded successfully");
            } catch (NoSuchFieldException | IllegalAccessException e) {
                AdAstraMekanized.LOGGER.warn("IE fuel fluids not accessible: {}", e.getMessage());
            }

            // Try to load fuel handler if available
            try {
                ieFuelHandlerClass = Class.forName("blusunrize.immersiveengineering.api.energy.FuelHandler");
                getFuelValueMethod = ieFuelHandlerClass.getMethod("getBurnTime", ItemStack.class);

                AdAstraMekanized.LOGGER.debug("IE fuel handler loaded");
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                AdAstraMekanized.LOGGER.debug("IE fuel handler not accessible: {}", e.getMessage());
            }

            apiAccessible = true;
            AdAstraMekanized.LOGGER.info("Immersive Engineering API reflection setup completed successfully");

        } catch (ClassNotFoundException e) {
            AdAstraMekanized.LOGGER.warn("Immersive Engineering API reflection setup failed: {}", e.getMessage());
            apiAccessible = false;
        }
    }

    // === IFuelIntegration Implementation ===

    @Override
    public boolean isFuelSystemAvailable() {
        return ieLoaded && apiAccessible;
    }

    @Override
    public boolean isValidRocketFuel(FluidStack fluid) {
        if (!isFuelSystemAvailable() || fluid.isEmpty()) {
            return false;
        }

        try {
            // Check if fluid is diesel or biodiesel
            Object fluidObject = fluid.getFluid();
            return fluidObject.equals(dieselFluid) || fluidObject.equals(biodieselFluid);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error checking fluid fuel validity: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isValidRocketFuel(ItemStack item) {
        if (!isFuelSystemAvailable() || item.isEmpty()) {
            return false;
        }

        try {
            // Use IE's fuel handler to check if item has burn time
            if (getFuelValueMethod != null) {
                Integer burnTime = (Integer) getFuelValueMethod.invoke(null, item);
                return burnTime != null && burnTime > 0;
            }
            return false;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error checking item fuel validity: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long getFuelEnergyValue(FluidStack fluid) {
        if (!isFuelSystemAvailable() || fluid.isEmpty()) {
            return 0;
        }

        try {
            Object fluidObject = fluid.getFluid();

            // Define energy values per mB for IE fuels
            if (fluidObject.equals(dieselFluid)) {
                return 256; // RF per mB for diesel
            } else if (fluidObject.equals(biodieselFluid)) {
                return 192; // RF per mB for biodiesel (slightly less efficient)
            }

            return 0;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error getting fluid fuel energy value: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long getFuelEnergyValue(ItemStack item) {
        if (!isFuelSystemAvailable() || item.isEmpty()) {
            return 0;
        }

        try {
            // Use IE's fuel handler to get burn time, convert to energy
            if (getFuelValueMethod != null) {
                Integer burnTime = (Integer) getFuelValueMethod.invoke(null, item);
                if (burnTime != null && burnTime > 0) {
                    // Convert burn time to energy (rough approximation)
                    return burnTime * 4L; // 4 RF per tick of burn time
                }
            }
            return 0;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error getting item fuel energy value: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public FluidStack getPrimaryRocketFuel(int amount) {
        if (!isFuelSystemAvailable() || dieselFluid == null) {
            return FluidStack.EMPTY;
        }

        try {
            // Cast reflected fluid object to proper type
            if (dieselFluid instanceof net.minecraft.world.level.material.Fluid) {
                return new FluidStack((net.minecraft.world.level.material.Fluid) dieselFluid, amount);
            }
            return FluidStack.EMPTY;
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error creating primary fuel stack: {}", e.getMessage());
            return FluidStack.EMPTY;
        }
    }

    @Override
    public FluidStack getSecondaryRocketFuel(int amount) {
        if (!isFuelSystemAvailable() || biodieselFluid == null) {
            return FluidStack.EMPTY;
        }

        try {
            // Cast reflected fluid object to proper type
            if (biodieselFluid instanceof net.minecraft.world.level.material.Fluid) {
                return new FluidStack((net.minecraft.world.level.material.Fluid) biodieselFluid, amount);
            }
            return FluidStack.EMPTY;
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error creating secondary fuel stack: {}", e.getMessage());
            return FluidStack.EMPTY;
        }
    }

    @Override
    public FluidStack[] getAvailableRocketFuels() {
        if (!isFuelSystemAvailable()) {
            return new FluidStack[0];
        }

        try {
            FluidStack primary = getPrimaryRocketFuel(1000);
            FluidStack secondary = getSecondaryRocketFuel(1000);

            if (!primary.isEmpty() && !secondary.isEmpty()) {
                return new FluidStack[]{primary, secondary};
            } else if (!primary.isEmpty()) {
                return new FluidStack[]{primary};
            } else if (!secondary.isEmpty()) {
                return new FluidStack[]{secondary};
            }

            return new FluidStack[0];

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error getting available rocket fuels: {}", e.getMessage());
            return new FluidStack[0];
        }
    }
}