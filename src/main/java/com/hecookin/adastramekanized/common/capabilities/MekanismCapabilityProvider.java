package com.hecookin.adastramekanized.common.capabilities;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.blockentities.machines.WirelessPowerRelayBlockEntity;
import com.hecookin.adastramekanized.common.blocks.machines.OxygenDistributorBlock;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Simple capability provider for Mekanism integration.
 * The oxygen distributor block entity handles its own capabilities directly.
 */
public class MekanismCapabilityProvider {

    /**
     * Register capabilities during mod setup
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register capability providers for our block entities
        // This tells the system how to get capabilities from our block entities

        // Register Forge Energy capability for Wireless Power Relay
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntityTypes.WIRELESS_POWER_RELAY.get(),
            (blockEntity, side) -> {
                if (blockEntity instanceof WirelessPowerRelayBlockEntity relay) {
                    return relay.getEnergyStorage();
                }
                return null;
            }
        );

        // Register Forge Energy capability for Oxygen Distributor
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(),
            (blockEntity, side) -> {
                if (blockEntity instanceof ImprovedOxygenDistributor distributor) {
                    return distributor.getEnergyStorage();
                }
                return null;
            }
        );

        // Try to register Mekanism capabilities if available
        try {
            Class<?> mekCapabilities = Class.forName("mekanism.common.capabilities.Capabilities");
            java.lang.reflect.Field strictEnergyField = mekCapabilities.getField("STRICT_ENERGY");
            Object strictEnergyCapObject = strictEnergyField.get(null);
            java.lang.reflect.Method blockMethod = strictEnergyCapObject.getClass().getMethod("block");
            Object strictEnergyBlockCap = blockMethod.invoke(strictEnergyCapObject);

            if (strictEnergyBlockCap instanceof BlockCapability) {
                // Register Mekanism STRICT_ENERGY capability for Wireless Power Relay
                event.registerBlockEntity(
                    (BlockCapability) strictEnergyBlockCap,
                    ModBlockEntityTypes.WIRELESS_POWER_RELAY.get(),
                    (blockEntity, side) -> {
                        if (blockEntity instanceof WirelessPowerRelayBlockEntity relay) {
                            return relay.getEnergyStorage();  // EnergyStorage implements IStrictEnergyHandler
                        }
                        return null;
                    }
                );

                // Register Mekanism STRICT_ENERGY capability for Oxygen Distributor
                event.registerBlockEntity(
                    (BlockCapability) strictEnergyBlockCap,
                    ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(),
                    (blockEntity, side) -> {
                        if (blockEntity instanceof ImprovedOxygenDistributor distributor) {
                            return distributor.getStrictEnergyHandler();
                        }
                        return null;
                    }
                );

                AdAstraMekanized.LOGGER.info("Registered Mekanism STRICT_ENERGY capabilities");
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.info("Mekanism not available, skipping STRICT_ENERGY capability registration");
        }

        AdAstraMekanized.LOGGER.info("Registered block entity capabilities for energy storage");
    }
}