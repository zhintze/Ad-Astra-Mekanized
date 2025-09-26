package com.hecookin.adastramekanized.common.capabilities;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.SimpleMekanismOxygenDistributor;
import com.hecookin.adastramekanized.common.blocks.machines.OxygenDistributorBlock;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Registers capabilities for Mekanism integration.
 * This allows Mekanism cables and tubes to connect to our oxygen distributor.
 */
public class MekanismCapabilityProvider {

    /**
     * Register capabilities during mod setup
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        AdAstraMekanized.LOGGER.info("Registering Mekanism capability providers...");

        try {
            // Get Mekanism's capability tokens using reflection
            Class<?> capabilitiesClass = Class.forName("mekanism.common.capabilities.Capabilities");

            // Register chemical handler capability
            try {
                java.lang.reflect.Field chemicalField = capabilitiesClass.getField("CHEMICAL");
                @SuppressWarnings("unchecked")
                BlockCapability<IChemicalHandler, Direction> chemicalCap =
                    (BlockCapability<IChemicalHandler, Direction>) chemicalField.get(null);

                // Register for oxygen distributor block
                event.registerBlockEntity(
                    chemicalCap,
                    com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(),
                    (blockEntity, direction) -> {
                        if (blockEntity instanceof SimpleMekanismOxygenDistributor distributor) {
                            // Return the distributor itself as it implements IChemicalHandler
                            return distributor;
                        }
                        return null;
                    }
                );

                AdAstraMekanized.LOGGER.info("Registered chemical handler capability for oxygen distributor");
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to register chemical capability: {}", e.getMessage());
            }

            // Register strict energy handler capability
            try {
                java.lang.reflect.Field energyField = capabilitiesClass.getField("STRICT_ENERGY");
                @SuppressWarnings("unchecked")
                BlockCapability<IStrictEnergyHandler, Direction> energyCap =
                    (BlockCapability<IStrictEnergyHandler, Direction>) energyField.get(null);

                // Register for oxygen distributor block
                event.registerBlockEntity(
                    energyCap,
                    com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(),
                    (blockEntity, direction) -> {
                        if (blockEntity instanceof SimpleMekanismOxygenDistributor distributor) {
                            // Return the distributor itself as it implements IStrictEnergyHandler
                            return distributor;
                        }
                        return null;
                    }
                );

                AdAstraMekanized.LOGGER.info("Registered strict energy handler capability for oxygen distributor");
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to register energy capability: {}", e.getMessage());
            }

            // Note: The legacy ENERGY capability uses IEnergyHandler interface which may not be in the API
            // The STRICT_ENERGY capability should be sufficient for Mekanism cable connections

        } catch (ClassNotFoundException e) {
            AdAstraMekanized.LOGGER.error("Mekanism capabilities class not found - is Mekanism installed?");
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to register Mekanism capabilities: ", e);
        }
    }
}