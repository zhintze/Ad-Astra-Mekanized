package com.hecookin.adastramekanized.common.items;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Gas Tank that can store oxygen or nitrogen for use with space suits.
 * Compatible with Mekanism's chemical system - can be filled in Chemical Tanks.
 */
public class GasTankItem extends Item {

    protected final long capacity;
    protected final String defaultChemical = "oxygen"; // Default but can store any gas

    public GasTankItem(long capacity, Properties properties) {
        super(properties.stacksTo(1)); // Gas tanks don't stack
        this.capacity = capacity;
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        // Mark this as a chemical container for Mekanism compatibility
        MekanismCompatibleItems.createChemicalArmor(stack, capacity, defaultChemical);
        return stack;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        // Ensure the item has chemical data when crafted
        if (!hasChemicalData(stack)) {
            MekanismCompatibleItems.createChemicalArmor(stack, capacity, defaultChemical);
        }
    }

    private boolean hasChemicalData(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return !tag.isEmpty() && tag.contains("mekanism");
    }

    public long getChemicalAmount(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.contains("mekanism")) {
            CompoundTag mekData = tag.getCompound("mekanism");
            if (mekData.contains("stored")) {
                CompoundTag stored = mekData.getCompound("stored");
                return stored.getLong("amount");
            }
        }
        return 0;
    }

    private String getChemicalType(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.contains("mekanism")) {
            CompoundTag mekData = tag.getCompound("mekanism");
            if (mekData.contains("stored")) {
                CompoundTag storedData = mekData.getCompound("stored");
                return storedData.getString("chemical");
            }
        }
        return defaultChemical; // Default to oxygen
    }

    public void consumeChemical(ItemStack stack, long amount) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.contains("mekanism")) {
            CompoundTag mekData = tag.getCompound("mekanism");
            if (mekData.contains("stored")) {
                CompoundTag stored = mekData.getCompound("stored");
                long current = stored.getLong("amount");
                long newAmount = Math.max(0, current - amount);
                stored.putLong("amount", newAmount);
                mekData.put("stored", stored);
                tag.put("mekanism", mekData);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        long stored = getChemicalAmount(stack);
        String displayName;

        if (stored == 0) {
            displayName = "Empty";
        } else {
            String chemicalType = getChemicalType(stack);
            // Capitalize chemical name for display
            displayName = chemicalType.substring(0, 1).toUpperCase() + chemicalType.substring(1);
        }

        tooltip.add(Component.translatable("tooltip.adastramekanized.gas_tank.stored",
            displayName, stored, capacity).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.adastramekanized.gas_tank.info")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getChemicalAmount(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getChemicalAmount(stack) / capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        String chemicalType = getChemicalType(stack);

        // Match the color to the gas type
        if (chemicalType.equalsIgnoreCase("nitrogen")) {
            return 0xE1B4B8; // Pinkish orange for nitrogen (matches jet suit)
        } else {
            return 0x00FFFF; // Cyan for oxygen (matches space suits)
        }
    }
}
