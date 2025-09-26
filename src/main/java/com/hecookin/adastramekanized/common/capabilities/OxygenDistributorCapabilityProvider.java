package com.hecookin.adastramekanized.common.capabilities;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.MekanismBasedOxygenDistributor;
import com.hecookin.adastramekanized.common.blocks.machines.OxygenDistributorBlock;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Registers block capabilities for the oxygen distributor
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class OxygenDistributorCapabilityProvider {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        AdAstraMekanized.LOGGER.info("Registering oxygen distributor block capabilities");

        // Register energy capability for the oxygen distributor block
        event.registerBlock(
            Capabilities.EnergyStorage.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof MekanismBasedOxygenDistributor distributor) {
                    return distributor.getEnergyStorage();
                }
                return null;
            },
            ModBlocks.OXYGEN_DISTRIBUTOR.get()
        );

        // Register Mekanism capabilities if available
        try {
            Class<?> mekCapabilities = Class.forName("mekanism.common.capabilities.Capabilities");

            // Register CHEMICAL capability
            java.lang.reflect.Field chemicalField = mekCapabilities.getField("CHEMICAL");
            Object chemicalCapObject = chemicalField.get(null);
            java.lang.reflect.Method blockMethod = chemicalCapObject.getClass().getMethod("block");
            Object chemicalBlockCap = blockMethod.invoke(chemicalCapObject);

            if (chemicalBlockCap instanceof net.neoforged.neoforge.capabilities.BlockCapability) {
                @SuppressWarnings("unchecked")
                var blockCap = (net.neoforged.neoforge.capabilities.BlockCapability<Object, Direction>) chemicalBlockCap;

                event.registerBlock(
                    blockCap,
                    (level, pos, state, be, side) -> {
                        if (be instanceof MekanismBasedOxygenDistributor distributor) {
                            return distributor.getChemicalHandler();
                        }
                        return null;
                    },
                    ModBlocks.OXYGEN_DISTRIBUTOR.get()
                );

                AdAstraMekanized.LOGGER.info("Registered Mekanism chemical capability for oxygen distributor");
            }

            // Register ENERGY capability for Mekanism/JADE compatibility
            java.lang.reflect.Field energyField = mekCapabilities.getField("ENERGY");
            Object energyCapObject = energyField.get(null);
            java.lang.reflect.Method energyBlockMethod = energyCapObject.getClass().getMethod("block");
            Object energyBlockCap = energyBlockMethod.invoke(energyCapObject);

            if (energyBlockCap instanceof net.neoforged.neoforge.capabilities.BlockCapability) {
                @SuppressWarnings("unchecked")
                var blockCap = (net.neoforged.neoforge.capabilities.BlockCapability<Object, Direction>) energyBlockCap;

                event.registerBlock(
                    blockCap,
                    (level, pos, state, be, side) -> {
                        if (be instanceof MekanismBasedOxygenDistributor distributor) {
                            // Return the strict energy handler for JADE compatibility
                            var handler = distributor.getStrictEnergyHandler();
                            AdAstraMekanized.LOGGER.debug("Returning IStrictEnergyHandler for JADE: {}, energy: {}",
                                handler, handler != null ? handler.getEnergy(0) : 0);
                            return handler;
                        }
                        return null;
                    },
                    ModBlocks.OXYGEN_DISTRIBUTOR.get()
                );

                AdAstraMekanized.LOGGER.info("Registered Mekanism energy capability for oxygen distributor");
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.warn("Could not register Mekanism capabilities: {}", e.getMessage());
        }
    }
}