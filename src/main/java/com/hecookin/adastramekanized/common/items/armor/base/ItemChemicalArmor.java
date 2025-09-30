package com.hecookin.adastramekanized.common.items.armor.base;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.items.interfaces.IChemicalItem;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Base class for armor that stores chemicals (oxygen, hydrogen, etc.)
 * Works with Mekanism when available, falls back to fluid tanks otherwise.
 */
public abstract class ItemChemicalArmor extends CustomDyeableArmorItem implements IChemicalItem {

    protected final long capacity;
    protected final String chemicalName;

    protected ItemChemicalArmor(Holder<ArmorMaterial> material, ArmorItem.Type armorType, long capacity, String chemicalName, Properties properties) {
        super(material, armorType, properties.rarity(Rarity.RARE).stacksTo(1));
        this.capacity = capacity;
        this.chemicalName = chemicalName;
    }

    public long getCapacity() {
        return capacity;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();

        if (manager.isMekanismAvailable()) {
            try {
                // Let Mekanism handle the chemical display
                manager.getMekanismIntegration().addChemicalTooltip(stack, tooltip, chemicalName, capacity);
            } catch (Exception e) {
                // Fall back to basic display
                addBasicChemicalTooltip(stack, tooltip);
            }
        } else {
            addBasicChemicalTooltip(stack, tooltip);
        }
    }

    protected void addBasicChemicalTooltip(ItemStack stack, List<Component> tooltip) {
        long amount = getChemicalAmount(stack);
        String chemicalDisplay = Character.toUpperCase(chemicalName.charAt(0)) + chemicalName.substring(1);
        int percentage = capacity > 0 ? (int)((amount * 100L) / capacity) : 0;
        tooltip.add(Component.literal(chemicalDisplay + ": ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(percentage + "%").withStyle(ChatFormatting.AQUA)));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        // Always show the bar for chemical armor
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        // Calculate bar width based on chemical amount
        long amount = getChemicalAmount(stack);
        if (capacity == 0) return 0; // Full bar when no capacity
        // The bar width calculation: 0 = full, 13 = empty
        // We return how much is filled
        float fillPercent = (float)amount / (float)capacity;
        return Math.round(13.0F * fillPercent);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        // Use cyan color for oxygen (matches the theme)
        return 0x00FFFF; // Cyan for oxygen
    }

    public boolean hasChemical(ItemStack stack) {
        // Check NBT directly for chemical presence
        long amount = getChemicalAmount(stack);
        return amount > 0;
    }

    public void useChemical(ItemStack stack, long amount) {
        // Update NBT directly for chemical usage
        try {
            net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
            net.minecraft.nbt.CompoundTag tag = customData.copyTag();

            if (!tag.isEmpty() && tag.contains("mekanism")) {
                net.minecraft.nbt.CompoundTag mekData = tag.getCompound("mekanism");
                if (mekData.contains("stored")) {
                    net.minecraft.nbt.CompoundTag storedData = mekData.getCompound("stored");
                    long currentAmount = storedData.getLong("amount");
                    long newAmount = Math.max(0, currentAmount - amount);
                    storedData.putLong("amount", newAmount);
                    mekData.put("stored", storedData);
                    tag.put("mekanism", mekData);
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to use chemical from NBT: ", e);
        }

        // Also try through Mekanism integration if available
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager.isMekanismAvailable()) {
            try {
                manager.getMekanismIntegration().useChemical(stack, chemicalName, amount);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.debug("Failed to use chemical via integration: ", e);
            }
        }
    }

    public long getChemicalAmount(ItemStack stack) {
        // Try to read directly from NBT first (most reliable)
        try {
            net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
            net.minecraft.nbt.CompoundTag tag = customData.copyTag();
            if (!tag.isEmpty() && tag.contains("mekanism")) {
                net.minecraft.nbt.CompoundTag mekData = tag.getCompound("mekanism");
                if (mekData.contains("stored")) {
                    return mekData.getCompound("stored").getLong("amount");
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Failed to read chemical amount from NBT: {}", e.getMessage());
        }

        // Fall back to Mekanism integration if available
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager.isMekanismAvailable()) {
            try {
                Long amount = manager.getMekanismIntegration().getChemicalAmount(stack, chemicalName);
                if (amount != null) {
                    return amount;
                }
            } catch (Exception ignored) {}
        }
        return 0;
    }
}