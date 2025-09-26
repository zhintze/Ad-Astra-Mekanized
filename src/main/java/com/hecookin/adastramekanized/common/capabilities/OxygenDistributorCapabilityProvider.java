package com.hecookin.adastramekanized.common.capabilities;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.OxygenDistributorBlockEntity;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Registers capabilities for the oxygen distributor to work with Mekanism.
 * Allows connection to universal cables and pressurized tubes on all sides.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class OxygenDistributorCapabilityProvider {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register energy capability for all sides (universal cables)
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(),
            (blockEntity, direction) -> {
                if (blockEntity instanceof OxygenDistributorBlockEntity distributor) {
                    return distributor.getEnergyCapability(direction);
                }
                return null;
            }
        );

        // Register Mekanism chemical capability if available
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager != null && manager.hasChemicalIntegration()) {
            registerMekanismCapabilities(event, manager);
        }
    }

    /**
     * Register Mekanism-specific capabilities using reflection
     */
    private static void registerMekanismCapabilities(RegisterCapabilitiesEvent event, ModIntegrationManager manager) {
        try {
            // Use reflection to get Mekanism's chemical capability
            Class<?> capabilitiesClass = Class.forName("mekanism.common.capabilities.Capabilities");

            // Get the CHEMICAL field which is the BlockCapability for chemicals
            java.lang.reflect.Field chemicalField = capabilitiesClass.getField("CHEMICAL");
            Object chemicalCapability = chemicalField.get(null);

            if (chemicalCapability instanceof BlockCapability) {
                @SuppressWarnings("unchecked")
                BlockCapability<Object, Direction> chemicalBlockCap = (BlockCapability<Object, Direction>) chemicalCapability;

                // Register our block entity to provide chemical capability
                event.registerBlockEntity(
                    chemicalBlockCap,
                    ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(),
                    (blockEntity, direction) -> {
                        if (blockEntity instanceof OxygenDistributorBlockEntity distributor) {
                            return distributor.getChemicalCapability(direction);
                        }
                        return null;
                    }
                );

                AdAstraMekanized.LOGGER.info("Successfully registered Mekanism chemical capability for oxygen distributor");
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.warn("Could not register Mekanism chemical capability: {}", e.getMessage());
        }
    }

    /**
     * Helper method to check if a block entity can connect to pipes/cables
     */
    public static boolean canConnect(Level level, BlockPos pos, Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof OxygenDistributorBlockEntity) {
            // Allow connection from all sides
            return true;
        }
        return false;
    }
}