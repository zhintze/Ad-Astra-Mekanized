package com.hecookin.adastramekanized.common.items.armor;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
import com.hecookin.adastramekanized.common.items.GasTankItem;
import com.hecookin.adastramekanized.common.items.MekanismCompatibleItems;
import com.hecookin.adastramekanized.common.items.armor.base.ItemChemicalArmor;
import com.hecookin.adastramekanized.common.tags.ModItemTags;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Space suit chest piece that stores oxygen like Mekanism's Scuba Tank.
 * Uses Mekanism's chemical system when available.
 */
public class SpaceSuitItem extends ItemChemicalArmor {

    protected static final String OXYGEN = "oxygen";

    public SpaceSuitItem(Holder<ArmorMaterial> material, ArmorItem.Type type, long tankSize, Item.Properties properties) {
        super(material, type, tankSize, OXYGEN, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.adastramekanized.space_suit_info").withStyle(ChatFormatting.GRAY));
        // Don't add oxygen here - parent class already handles it
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        // Mark this as a chemical container for Mekanism compatibility
        MekanismCompatibleItems.createChemicalArmor(stack, capacity, OXYGEN);
        return stack;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        // Ensure the item has chemical data when crafted
        if (!hasChemicalData(stack)) {
            MekanismCompatibleItems.createChemicalArmor(stack, capacity, OXYGEN);
        }
    }

    private boolean hasChemicalData(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return !tag.isEmpty() && tag.contains("mekanism");
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return hasFullSet(entity, ModItemTags.SPACE_SUITS);
    }

    public static boolean hasFullNetheriteSet(LivingEntity entity) {
        return hasFullSet(entity, ModItemTags.NETHERITE_SPACE_SUITS);
    }

    public static boolean hasFullJetSuitSet(LivingEntity entity) {
        return hasFullSet(entity, ModItemTags.JET_SUITS);
    }

    public static boolean hasFullSet(LivingEntity entity, TagKey<Item> spaceSuitTag) {
        for (var stack : entity.getArmorSlots()) {
            if (!stack.is(spaceSuitTag)) return false;
        }
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide()) return;
        if (!(entity instanceof LivingEntity livingEntity)) return;
        if (livingEntity.getItemBySlot(EquipmentSlot.CHEST) != stack) return;

        // Auto-refill oxygen from inventory gas tanks (works in all modes including creative)
        if (entity instanceof Player player) {
            // Only refill every second to avoid performance issues
            if (entity.tickCount % 20 == 0) {
                tryRefillOxygenFromInventory(player, stack);
            }
        }

        // Don't clear freeze effect here - let OxygenManager handle it based on full suit protection
        // This was preventing the ice overlay from showing when missing other armor pieces

        // Skip oxygen consumption for creative/spectator players
        if (livingEntity instanceof Player player && (player.isCreative() || player.isSpectator())) return;

        // Every 12 ticks = 10 minutes per 1,000 mB (1 bucket) oxygen
        if (livingEntity.tickCount % 12 == 0 && hasOxygen(stack)) {
            // Allow the entity to breathe in water
            if (entity.isEyeInFluid(FluidTags.WATER)) {
                consumeOxygen(stack, 1);
                livingEntity.setAirSupply(Math.min(livingEntity.getMaxAirSupply(), livingEntity.getAirSupply() + 4 * 10));
            }
            // Note: Oxygen consumption in space is now handled by OxygenManager.consumeSpaceSuitOxygen()
        }
    }

    public void consumeOxygen(ItemStack stack, long amount) {
        useChemical(stack, amount);
    }

    public static long getOxygenAmount(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return 0;
        var stack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (!(stack.getItem() instanceof SpaceSuitItem suit)) return 0;
        return suit.getChemicalAmount(stack);
    }

    public static boolean hasOxygen(Entity entity) {
        return getOxygenAmount(entity) > 1;
    }

    public static boolean hasOxygen(ItemStack stack) {
        if (!(stack.getItem() instanceof SpaceSuitItem suit)) return false;
        return suit.hasChemical(stack);
    }

    /**
     * Try to refill oxygen from gas tanks in player inventory (instant refill when suit is equipped)
     */
    private void tryRefillOxygenFromInventory(Player player, ItemStack spaceSuit) {
        // Only refill if this suit is currently equipped
        if (player.getItemBySlot(EquipmentSlot.CHEST) != spaceSuit) {
            return;
        }

        long currentOxygen = getChemicalAmount(spaceSuit);
        long maxRefill = capacity - currentOxygen;

        if (maxRefill <= 0) {
            return; // Suit is already full
        }

        // Try to refill from gas tanks in inventory (instant transfer)
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || player.getInventory().armor.contains(stack)) continue;

            // Check if this is a Gas Tank with oxygen
            if (stack.getItem() instanceof GasTankItem gasTank) {
                String chemicalType = getGasTankChemicalType(stack);
                if ("oxygen".equalsIgnoreCase(chemicalType)) {
                    long available = gasTank.getChemicalAmount(stack);
                    if (available > 0) {
                        long toTransfer = Math.min(available, maxRefill); // Instant transfer - no rate limit
                        gasTank.consumeChemical(stack, toTransfer);
                        addOxygenToSuit(spaceSuit, toTransfer);
                        maxRefill -= toTransfer;

                        if (maxRefill <= 0) {
                            return; // Suit is now full
                        }
                    }
                }
            }
        }
    }

    private String getGasTankChemicalType(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("mekanism")) {
            CompoundTag mekData = tag.getCompound("mekanism");
            if (mekData.contains("stored")) {
                return mekData.getCompound("stored").getString("chemical");
            }
        }
        return "oxygen"; // Default
    }

    protected void addOxygenToSuit(ItemStack stack, long amount) {
        // ALWAYS update NBT directly to ensure oxygen is added
        // (Mekanism integration can fail silently, so we use direct NBT)
        try {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            // Initialize NBT structure if it doesn't exist
            if (!tag.contains("mekanism")) {
                AdAstraMekanized.LOGGER.info("Initializing mekanism NBT for space suit");
                CompoundTag mekData = new CompoundTag();
                mekData.putLong("capacity", capacity);
                mekData.putString("chemicalType", OXYGEN);

                CompoundTag storedData = new CompoundTag();
                storedData.putLong("amount", 0L);
                storedData.putString("chemical", OXYGEN);
                mekData.put("stored", storedData);

                tag.put("mekanism", mekData);
            }

            CompoundTag mekData = tag.getCompound("mekanism");

            // Initialize "stored" if it doesn't exist
            if (!mekData.contains("stored")) {
                AdAstraMekanized.LOGGER.info("Initializing stored NBT for space suit");
                CompoundTag storedData = new CompoundTag();
                storedData.putLong("amount", 0L);
                storedData.putString("chemical", OXYGEN);
                mekData.put("stored", storedData);
            }

            CompoundTag storedData = mekData.getCompound("stored");
            long currentAmount = storedData.getLong("amount");
            long newAmount = Math.min(capacity, currentAmount + amount);
            storedData.putLong("amount", newAmount);
            mekData.put("stored", storedData);
            tag.put("mekanism", mekData);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            AdAstraMekanized.LOGGER.info("Added {} mB oxygen to space suit (was {}, now {})", amount, currentAmount, newAmount);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to add oxygen to space suit: ", e);
        }
    }
}