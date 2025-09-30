package com.hecookin.adastramekanized.common.items.armor;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Jet suit chest piece that stores both oxygen AND nitrogen for flight.
 * Implements Mekanism's IJetpackItem and IModeItem interfaces directly for seamless integration.
 * Oxygen provides breathing, nitrogen provides flight fuel.
 */
public class JetSuitItem extends SpaceSuitItem implements IJetpackItem, IModeItem.IAttachmentBasedModeItem<IJetpackItem.JetpackMode> {

    protected static final String NITROGEN = "nitrogen";
    private final long nitrogenCapacity;

    public JetSuitItem(Holder<ArmorMaterial> material, ArmorItem.Type type, long oxygenTankSize, long nitrogenTankSize, Item.Properties properties) {
        super(material, type, oxygenTankSize, properties);
        this.nitrogenCapacity = nitrogenTankSize;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        // Don't call super to avoid duplicate "Provides oxygen in space" text
        // Instead, directly add the oxygen percentage
        long oxygenAmount = getOxygenAmount(stack);
        int oxygenPercentage = capacity > 0 ? (int)((oxygenAmount * 100L) / capacity) : 0;
        tooltip.add(Component.literal("Oxygen: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(oxygenPercentage + "%").withStyle(ChatFormatting.AQUA)));

        // Add nitrogen display as percentage
        long nitrogenAmount = getNitrogenAmount(stack);
        int nitrogenPercentage = nitrogenCapacity > 0 ? (int)((nitrogenAmount * 100L) / nitrogenCapacity) : 0;
        tooltip.add(Component.literal("Nitrogen: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(nitrogenPercentage + "%").withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0xE1B4B8)))));

        // Show jetpack mode like Mekanism
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));

        tooltip.add(Component.translatable("tooltip.adastramekanized.jet_suit_info").withStyle(ChatFormatting.GRAY));
    }

    /**
     * Override to read from dual-tank oxygen storage
     */
    @Override
    public long getChemicalAmount(ItemStack stack) {
        return getOxygenAmount(stack);
    }

    public long getOxygenAmount(ItemStack stack) {
        // Try to read directly from NBT first (most reliable)
        try {
            net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
            net.minecraft.nbt.CompoundTag tag = customData.copyTag();
            if (!tag.isEmpty() && tag.contains("mekanism")) {
                net.minecraft.nbt.CompoundTag mekData = tag.getCompound("mekanism");
                if (mekData.contains("oxygen")) {
                    return mekData.getCompound("oxygen").getLong("amount");
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Failed to read oxygen amount from NBT: {}", e.getMessage());
        }

        // Fall back to parent method if dual tank structure not found
        return super.getChemicalAmount(stack);
    }

    public long getNitrogenAmount(ItemStack stack) {
        // Try to read directly from NBT first (most reliable)
        try {
            net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
            net.minecraft.nbt.CompoundTag tag = customData.copyTag();
            if (!tag.isEmpty() && tag.contains("mekanism")) {
                net.minecraft.nbt.CompoundTag mekData = tag.getCompound("mekanism");
                if (mekData.contains("nitrogen")) {
                    return mekData.getCompound("nitrogen").getLong("amount");
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Failed to read nitrogen amount from NBT: {}", e.getMessage());
        }

        // Fall back to Mekanism integration if available
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager.isMekanismAvailable()) {
            try {
                Long amount = manager.getMekanismIntegration().getChemicalAmount(stack, NITROGEN);
                if (amount != null) {
                    return amount;
                }
            } catch (Exception ignored) {}
        }
        return 0;
    }

    public boolean hasNitrogen(ItemStack stack) {
        return getNitrogenAmount(stack) > 0;
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        // For jet suits, we show both chemicals - prioritize showing nitrogen for the main bar
        // since it's the more critical resource for flight
        long nitrogenAmount = getNitrogenAmount(stack);
        if (nitrogenAmount > 0) {
            // Nitrogen orange color
            return 0xE1B4B8;
        }
        // Fall back to oxygen cyan if no nitrogen
        return super.getBarColor(stack);
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        // Show nitrogen level primarily, or oxygen if no nitrogen
        long nitrogenAmount = getNitrogenAmount(stack);
        if (nitrogenAmount > 0 || nitrogenCapacity > 0) {
            float fillPercent = (float)nitrogenAmount / (float)nitrogenCapacity;
            return Math.round(13.0F * fillPercent);
        }
        return super.getBarWidth(stack);
    }

    @Override
    public void consumeOxygen(ItemStack stack, long amount) {
        // Override to consume from the oxygen tank specifically
        try {
            net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
            net.minecraft.nbt.CompoundTag tag = customData.copyTag();

            if (!tag.isEmpty() && tag.contains("mekanism")) {
                net.minecraft.nbt.CompoundTag mekData = tag.getCompound("mekanism");
                if (mekData.contains("oxygen")) {
                    net.minecraft.nbt.CompoundTag oxygenData = mekData.getCompound("oxygen");
                    long currentAmount = oxygenData.getLong("amount");
                    long newAmount = Math.max(0, currentAmount - amount);
                    oxygenData.putLong("amount", newAmount);
                    mekData.put("oxygen", oxygenData);
                    tag.put("mekanism", mekData);
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to use oxygen from NBT: ", e);
        }
    }

    public void consumeNitrogen(ItemStack stack, long amount) {
        // Update NBT directly for nitrogen usage
        try {
            net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
            net.minecraft.nbt.CompoundTag tag = customData.copyTag();

            if (!tag.isEmpty() && tag.contains("mekanism")) {
                net.minecraft.nbt.CompoundTag mekData = tag.getCompound("mekanism");
                if (mekData.contains("nitrogen")) {
                    net.minecraft.nbt.CompoundTag nitrogenData = mekData.getCompound("nitrogen");
                    long currentAmount = nitrogenData.getLong("amount");
                    long newAmount = Math.max(0, currentAmount - amount);
                    nitrogenData.putLong("amount", newAmount);
                    mekData.put("nitrogen", nitrogenData);
                    tag.put("mekanism", mekData);
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to use nitrogen from NBT: ", e);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // Handle oxygen consumption from parent
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        // Jetpack flight is handled by Mekanism's CommonPlayerTickHandler
        // We don't need to implement it here - it will automatically detect our IJetpackItem interface
    }

    // ===== IJetpackItem Interface Implementation =====

    @Override
    public boolean canUseJetpack(ItemStack stack) {
        return hasNitrogen(stack);
    }

    @Override
    public JetpackMode getJetpackMode(ItemStack stack) {
        return getMode(stack);
    }

    @Override
    public double getJetpackThrust(ItemStack stack) {
        return 0.15; // Same as Mekanism's default jetpack thrust
    }

    @Override
    public void useJetpackFuel(ItemStack stack) {
        consumeNitrogen(stack, 1);
    }

    // ===== IModeItem.IAttachmentBasedModeItem Implementation =====

    @Override
    public DataComponentType<JetpackMode> getModeDataType() {
        return MekanismDataComponents.JETPACK_MODE.get();
    }

    @Override
    public JetpackMode getDefaultMode() {
        return JetpackMode.NORMAL;
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        JetpackMode mode = getMode(stack);
        JetpackMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, player, newMode);
            displayChange.sendMessage(player, newMode, MekanismLang.JETPACK_MODE_CHANGE::translate);
        }
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return slotType == EquipmentSlot.CHEST;
    }

    public void spawnParticles(Level level, LivingEntity entity, HumanoidModel<?> model, ItemStack stack) {
        if (!(entity instanceof Player player)) return;
        if (!canUseJetpack(stack)) return;
        if (!hasFullJetSuitSet(player)) return;

        // Check if jetpack mode is active (not DISABLED)
        JetpackMode mode = getJetpackMode(stack);
        if (mode == JetpackMode.DISABLED) return;

        spawnParticles(level, entity, model.rightArm.xRot + 0.05, entity.isFallFlying() ? 0.0 : 0.8, -0.45);
        spawnParticles(level, entity, model.leftArm.xRot + 0.05, entity.isFallFlying() ? 0.0 : 0.8, 0.45);
        spawnParticles(level, entity, model.rightLeg.xRot + 0.05, entity.isFallFlying() ? 0.1 : 0.0, -0.1);
        spawnParticles(level, entity, model.leftLeg.xRot + 0.05, entity.isFallFlying() ? 0.1 : 0.0, 0.1);
    }

    private void spawnParticles(Level level, LivingEntity entity, double pitch, double yOffset, double zOffset) {
        double yRot = entity.yBodyRot;
        double forwardOffsetX = Math.cos(yRot * Math.PI / 180) * zOffset;
        double forwardOffsetZ = Math.sin(yRot * Math.PI / 180) * zOffset;
        double sideOffsetX = Math.cos((yRot - 90) * Math.PI / 180) * pitch;
        double sideOffsetZ = Math.sin((yRot - 90) * Math.PI / 180) * pitch;

        level.addParticle(ParticleTypes.FLAME, true,
            entity.getX() + forwardOffsetX + sideOffsetX,
            entity.getY() + yOffset,
            entity.getZ() + sideOffsetZ + forwardOffsetZ,
            0, 0, 0);
    }

    @SuppressWarnings("unused") // NeoForge
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (entity.level().isClientSide()) return true;
        if (this.type != Type.CHESTPLATE) return true;
        int nextFlightTick = flightTicks + 1;
        if (nextFlightTick % 10 != 0) return true;

        if (nextFlightTick % 20 == 0) {
            stack.hurtAndBreak(1, entity, EquipmentSlot.CHEST);
        }

        entity.gameEvent(GameEvent.ELYTRA_GLIDE);
        return true;
    }

    @SuppressWarnings("unused") // NeoForge
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player player)) return false;
        if (!canUseJetpack(stack)) return false;

        // Check if in NORMAL or VECTOR mode
        JetpackMode mode = getJetpackMode(stack);
        // Allow elytra flight in NORMAL and VECTOR modes when falling/flying
        return (mode == JetpackMode.NORMAL || mode == JetpackMode.VECTOR) && player.isFallFlying();
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        // JetSuit stores dual chemicals - oxygen for breathing AND nitrogen for flight
        initializeDualChemicals(stack);
        // Set default jetpack mode to NORMAL
        stack.set(getModeDataType(), getDefaultMode());
        return stack;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        // Ensure the item has dual chemical data when crafted
        if (!hasChemicalData(stack)) {
            initializeDualChemicals(stack);
        }
        // Ensure jetpack mode is set
        if (!stack.has(getModeDataType())) {
            stack.set(getModeDataType(), getDefaultMode());
        }
    }

    public void initializeDualChemicals(ItemStack stack) {
        var customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();

        // Only initialize if not already present
        if (!tag.contains("mekanism")) {
            // Create Mekanism data structure for dual chemicals
            var mekData = new CompoundTag();

            // Oxygen storage (tank 0)
            var oxygenData = new CompoundTag();
            oxygenData.putLong("amount", 0L);
            oxygenData.putLong("capacity", capacity);
            mekData.put("oxygen", oxygenData);

            // Nitrogen storage (tank 1)
            var nitrogenData = new CompoundTag();
            nitrogenData.putLong("amount", 0L);
            nitrogenData.putLong("capacity", nitrogenCapacity);
            mekData.put("nitrogen", nitrogenData);

            // Mark as accepting both chemicals
            mekData.putString("acceptedChemicals", "oxygen,nitrogen");
            mekData.putInt("tanks", 2);

            tag.put("mekanism", mekData);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            AdAstraMekanized.LOGGER.debug("Initialized dual chemical tanks for jet suit: oxygen capacity={}, nitrogen capacity={}",
                capacity, nitrogenCapacity);
        }
    }

    private boolean hasChemicalData(ItemStack stack) {
        var customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        return !tag.isEmpty() && tag.contains("mekanism");
    }
}