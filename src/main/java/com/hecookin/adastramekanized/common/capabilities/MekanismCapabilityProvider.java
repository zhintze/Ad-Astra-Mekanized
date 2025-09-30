package com.hecookin.adastramekanized.common.capabilities;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.blockentities.machines.WirelessPowerRelayBlockEntity;
import com.hecookin.adastramekanized.common.blocks.machines.OxygenDistributorBlock;
import com.hecookin.adastramekanized.common.items.armor.JetSuitItem;
import com.hecookin.adastramekanized.common.items.armor.NetheriteSpaceSuitItem;
import com.hecookin.adastramekanized.common.items.armor.SpaceSuitItem;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.registry.ModItems;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
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

        // Register item capabilities for space suits
        registerSpaceSuitCapabilities(event);
    }

    private static void registerSpaceSuitCapabilities(RegisterCapabilitiesEvent event) {
        // Try to register Mekanism chemical capabilities for our space suits
        try {
            Class<?> mekCapabilities = Class.forName("mekanism.common.capabilities.Capabilities");
            java.lang.reflect.Field chemicalField = mekCapabilities.getField("CHEMICAL");
            Object chemicalCapObject = chemicalField.get(null);

            // Get the item() method to get ItemCapability
            java.lang.reflect.Method itemMethod = chemicalCapObject.getClass().getMethod("item");
            Object chemicalItemCap = itemMethod.invoke(chemicalCapObject);

            if (chemicalItemCap instanceof ItemCapability) {
                ItemCapability<IChemicalHandler, Void> capability = (ItemCapability<IChemicalHandler, Void>) chemicalItemCap;

                // Register for Space Suit (chest piece)
                event.registerItem(
                    capability,
                    (stack, context) -> {
                        // Always ensure the stack has chemical data
                        ensureChemicalData(stack, "oxygen");
                        return createChemicalHandler(stack, "oxygen");
                    },
                    ModItems.SPACE_SUIT.get()
                );

                // Register for Netherite Space Suit (chest piece)
                event.registerItem(
                    capability,
                    (stack, context) -> {
                        ensureChemicalData(stack, "oxygen");
                        return createChemicalHandler(stack, "oxygen");
                    },
                    ModItems.NETHERITE_SPACE_SUIT.get()
                );

                // Register for Jet Suit (handles both oxygen and hydrogen)
                event.registerItem(
                    capability,
                    (stack, context) -> {
                        ensureChemicalData(stack, "oxygen", "hydrogen");
                        return createChemicalHandler(stack, "oxygen", "hydrogen");
                    },
                    ModItems.JET_SUIT.get()
                );

                AdAstraMekanized.LOGGER.info("Registered Mekanism chemical capabilities for space suits");
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.info("Mekanism not available, skipping chemical capability registration for items: {}", e.getMessage());
        }
    }

    /**
     * Ensures the ItemStack has chemical data in its NBT
     */
    private static void ensureChemicalData(ItemStack stack, String... acceptedChemicals) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains("mekanism")) {
            // Get the actual capacity from the armor item
            long capacity = getArmorCapacity(stack);

            // Initialize chemical data
            CompoundTag mekData = new CompoundTag();
            mekData.putLong("capacity", capacity);
            mekData.putString("chemicalType", String.join(",", acceptedChemicals));

            CompoundTag storedData = new CompoundTag();
            storedData.putLong("amount", 0L);
            storedData.putString("chemical", acceptedChemicals[0]);
            mekData.put("stored", storedData);

            tag.put("mekanism", mekData);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /**
     * Get the actual capacity from the armor item
     */
    private static long getArmorCapacity(ItemStack stack) {
        if (stack.getItem() instanceof com.hecookin.adastramekanized.common.items.armor.base.ItemChemicalArmor armor) {
            // Access the capacity field from the armor
            return armor.getCapacity();
        }
        // Default fallback
        return 1000L;
    }

    /**
     * Creates a chemical handler for an ItemStack using reflection to avoid compile-time dependency
     */
    private static IChemicalHandler createChemicalHandler(ItemStack stack, String... acceptedChemicals) {
        try {
            // Get necessary classes
            Class<?> chemicalClass = Class.forName("mekanism.api.chemical.Chemical");
            Class<?> chemicalStackClass = Class.forName("mekanism.api.chemical.ChemicalStack");
            Class<?> mekanismChemicalsClass = Class.forName("mekanism.common.registries.MekanismChemicals");
            Class<?> holderClass = Class.forName("net.minecraft.core.Holder");
            Class<?> actionClass = Class.forName("mekanism.api.Action");

            // Get references to oxygen and hydrogen
            Object oxygenHolder = mekanismChemicalsClass.getField("OXYGEN").get(null);
            Object hydrogenHolder = mekanismChemicalsClass.getField("HYDROGEN").get(null);
            java.lang.reflect.Method getValue = holderClass.getMethod("value");
            Object oxygen = getValue.invoke(oxygenHolder);
            Object hydrogen = getValue.invoke(hydrogenHolder);

            // Get ChemicalStack.EMPTY and Action enum values
            Object chemicalStackEmpty = chemicalStackClass.getField("EMPTY").get(null);
            Object actionExecute = actionClass.getField("EXECUTE").get(null);
            Object actionSimulate = actionClass.getField("SIMULATE").get(null);

            // The handler needs to always read/write to the actual ItemStack
            // since it gets recreated each time the capability is requested

            final long capacity = getArmorCapacity(stack);

            // Create the handler directly implementing IChemicalHandler
            return new IChemicalHandler() {
                @Override
                public int getChemicalTanks() {
                    return 1; // Single tank
                }

                @Override
                public ChemicalStack getChemicalInTank(int tank) {
                    if (tank != 0) return (ChemicalStack) chemicalStackEmpty;
                    try {
                        // Read current state from NBT
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        if (!tag.isEmpty() && tag.contains("mekanism")) {
                            CompoundTag mekData = tag.getCompound("mekanism");
                            if (mekData.contains("stored")) {
                                CompoundTag storedData = mekData.getCompound("stored");
                                long amount = storedData.getLong("amount");
                                String chemicalType = storedData.getString("chemical");
                                if (amount > 0) {
                                    Object chemical = "hydrogen".equals(chemicalType) ? hydrogen : oxygen;
                                    java.lang.reflect.Method createMethod = chemicalStackClass.getMethod("create", chemicalClass, long.class);
                                    return (ChemicalStack) createMethod.invoke(null, chemical, amount);
                                }
                            }
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to create chemical stack: {}", e.getMessage());
                    }
                    return (ChemicalStack) chemicalStackEmpty;
                }

                @Override
                public void setChemicalInTank(int tank, ChemicalStack stack) {
                    if (tank != 0) return;
                    updateNBT(stack);
                }

                @Override
                public long getChemicalTankCapacity(int tank) {
                    return tank == 0 ? capacity : 0;
                }

                @Override
                public boolean isValid(int tank, ChemicalStack stack) {
                    if (tank != 0 || stack == null) return false;
                    try {
                        java.lang.reflect.Method getChemicalMethod = stack.getClass().getMethod("getChemical");
                        Object chemical = getChemicalMethod.invoke(stack);
                        for (String accepted : acceptedChemicals) {
                            if ("oxygen".equalsIgnoreCase(accepted) && chemical.equals(oxygen)) return true;
                            if ("hydrogen".equalsIgnoreCase(accepted) && chemical.equals(hydrogen)) return true;
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to validate chemical: {}", e.getMessage());
                    }
                    return false;
                }

                @Override
                public ChemicalStack insertChemical(int tank, ChemicalStack chemStack, mekanism.api.Action action) {
                    if (tank != 0 || !isValid(tank, chemStack)) return chemStack;
                    try {
                        // Read current amount from NBT
                        long currentAmount = 0;
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        if (!tag.isEmpty() && tag.contains("mekanism")) {
                            CompoundTag mekData = tag.getCompound("mekanism");
                            if (mekData.contains("stored")) {
                                currentAmount = mekData.getCompound("stored").getLong("amount");
                            }
                        }

                        java.lang.reflect.Method getAmountMethod = chemStack.getClass().getMethod("getAmount");
                        long toInsert = (long) getAmountMethod.invoke(chemStack);

                        // Slow down the filling rate - max 50 mB per operation (2% of a 2500 capacity tank)
                        long maxInsertPerTick = 50L;
                        long canInsert = Math.min(Math.min(toInsert, maxInsertPerTick), capacity - currentAmount);

                        if (canInsert > 0 && action == actionExecute) {
                            // Update NBT directly
                            java.lang.reflect.Method getChemicalMethod = chemStack.getClass().getMethod("getChemical");
                            Object chemical = getChemicalMethod.invoke(chemStack);

                            CompoundTag newTag = customData.copyTag();
                            CompoundTag mekData = newTag.contains("mekanism") ? newTag.getCompound("mekanism") : new CompoundTag();
                            CompoundTag storedData = new CompoundTag();

                            storedData.putLong("amount", currentAmount + canInsert);
                            storedData.putString("chemical", chemical.equals(hydrogen) ? "hydrogen" : "oxygen");

                            mekData.put("stored", storedData);
                            mekData.putLong("capacity", capacity);
                            newTag.put("mekanism", mekData);
                            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));

                            AdAstraMekanized.LOGGER.debug("Inserted {} chemical, new amount: {}", canInsert, currentAmount + canInsert);
                        }

                        if (canInsert == toInsert) {
                            return (ChemicalStack) chemicalStackEmpty;
                        } else {
                            java.lang.reflect.Method copyMethod = chemStack.getClass().getMethod("copyWithAmount", long.class);
                            return (ChemicalStack) copyMethod.invoke(chemStack, toInsert - canInsert);
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.error("Failed to insert chemical: ", e);
                    }
                    return chemStack;
                }

                @Override
                public ChemicalStack extractChemical(int tank, long amount, mekanism.api.Action action) {
                    if (tank != 0) return (ChemicalStack) chemicalStackEmpty;
                    try {
                        // Read current state from NBT
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        if (!tag.isEmpty() && tag.contains("mekanism")) {
                            CompoundTag mekData = tag.getCompound("mekanism");
                            if (mekData.contains("stored")) {
                                CompoundTag storedData = mekData.getCompound("stored");
                                long currentAmount = storedData.getLong("amount");
                                String chemicalType = storedData.getString("chemical");

                                long toExtract = Math.min(amount, currentAmount);
                                if (toExtract > 0) {
                                    Object chemical = "hydrogen".equals(chemicalType) ? hydrogen : oxygen;

                                    if (action == actionExecute) {
                                        // Update NBT with new amount
                                        CompoundTag newTag = customData.copyTag();
                                        CompoundTag newMekData = newTag.getCompound("mekanism");
                                        CompoundTag newStoredData = newMekData.getCompound("stored");
                                        newStoredData.putLong("amount", currentAmount - toExtract);
                                        newMekData.put("stored", newStoredData);
                                        newTag.put("mekanism", newMekData);
                                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));

                                        AdAstraMekanized.LOGGER.debug("Extracted {} chemical, new amount: {}", toExtract, currentAmount - toExtract);
                                    }

                                    java.lang.reflect.Method createMethod = chemicalStackClass.getMethod("create", chemicalClass, long.class);
                                    return (ChemicalStack) createMethod.invoke(null, chemical, toExtract);
                                }
                            }
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to extract chemical: {}", e.getMessage());
                    }
                    return (ChemicalStack) chemicalStackEmpty;
                }

                private void updateNBT(ChemicalStack chemStack) {
                    try {
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        CompoundTag mekData = tag.contains("mekanism") ? tag.getCompound("mekanism") : new CompoundTag();
                        CompoundTag storedData = new CompoundTag();

                        if (chemStack != null && chemStack != chemicalStackEmpty) {
                            java.lang.reflect.Method getAmountMethod = chemStack.getClass().getMethod("getAmount");
                            long amount = (long) getAmountMethod.invoke(chemStack);
                            java.lang.reflect.Method getChemicalMethod = chemStack.getClass().getMethod("getChemical");
                            Object chemical = getChemicalMethod.invoke(chemStack);

                            storedData.putLong("amount", amount);

                            if (chemical.equals(hydrogen)) {
                                storedData.putString("chemical", "hydrogen");
                            } else {
                                storedData.putString("chemical", "oxygen");
                            }
                        } else {
                            storedData.putLong("amount", 0L);
                            storedData.putString("chemical", acceptedChemicals[0]);
                        }

                        mekData.put("stored", storedData);
                        mekData.putLong("capacity", capacity);
                        tag.put("mekanism", mekData);
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to update NBT: {}", e.getMessage());
                    }
                }
            };

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Failed to create chemical handler: {}", e.getMessage());
            return null;
        }
    }

}