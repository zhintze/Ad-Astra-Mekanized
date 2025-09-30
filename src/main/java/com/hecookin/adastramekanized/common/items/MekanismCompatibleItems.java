package com.hecookin.adastramekanized.common.items;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Helper class to make our items compatible with Mekanism's systems
 */
public class MekanismCompatibleItems {

    /**
     * Mark an item stack as a chemical container for Mekanism compatibility
     */
    public static ItemStack createChemicalArmor(ItemStack stack, long capacity, String chemicalType) {
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();

        // Add NBT data that Mekanism can recognize
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        // Add chemical storage information
        CompoundTag mekData = new CompoundTag();
        mekData.putLong("capacity", capacity);
        mekData.putString("chemicalType", chemicalType);

        // Store filled amount (starts empty)
        CompoundTag chemicalTank = new CompoundTag();
        chemicalTank.putLong("amount", 0L);
        chemicalTank.putString("chemical", chemicalType);
        mekData.put("stored", chemicalTank);

        tag.put("mekanism", mekData);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return stack;
    }

    /**
     * Fill an armor piece with chemical
     */
    public static void fillWithChemical(ItemStack stack, String chemicalType, long amount) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.contains("mekanism")) {
            CompoundTag mekData = tag.getCompound("mekanism");
            CompoundTag chemicalTank = mekData.getCompound("stored");

            long capacity = mekData.getLong("capacity");
            long currentAmount = chemicalTank.getLong("amount");
            long newAmount = Math.min(capacity, currentAmount + amount);

            chemicalTank.putLong("amount", newAmount);
            chemicalTank.putString("chemical", chemicalType);
            mekData.put("stored", chemicalTank);

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /**
     * Get the amount of chemical stored
     */
    public static long getChemicalAmount(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (!tag.isEmpty() && tag.contains("mekanism")) {
            CompoundTag mekData = tag.getCompound("mekanism");
            CompoundTag chemicalTank = mekData.getCompound("stored");
            return chemicalTank.getLong("amount");
        }
        return 0;
    }

    /**
     * Use chemical from the armor
     */
    public static long useChemical(ItemStack stack, long amount) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (!tag.isEmpty() && tag.contains("mekanism")) {
            CompoundTag mekData = tag.getCompound("mekanism");
            CompoundTag chemicalTank = mekData.getCompound("stored");

            long currentAmount = chemicalTank.getLong("amount");
            long used = Math.min(currentAmount, amount);

            chemicalTank.putLong("amount", currentAmount - used);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            return used;
        }
        return 0;
    }
}