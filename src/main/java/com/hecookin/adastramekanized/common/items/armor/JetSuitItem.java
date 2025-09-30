package com.hecookin.adastramekanized.common.items.armor;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.utils.KeybindManager;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
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

import java.util.List;

/**
 * Jet suit chest piece that stores both oxygen AND nitrogen for flight.
 * Works like Mekanism's jetpack when Mekanism is available.
 * Oxygen provides breathing, nitrogen provides flight fuel.
 */
public class JetSuitItem extends SpaceSuitItem {

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

        tooltip.add(Component.translatable("tooltip.adastramekanized.jet_suit_info").withStyle(ChatFormatting.GRAY));
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

        if (!(entity instanceof Player player)) return;
        if (player.getItemBySlot(EquipmentSlot.CHEST) != stack) return;

        if (player.getAbilities().flying) return;
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return;
        if (!hasFullJetSuitSet(player)) return;

        if (!KeybindManager.suitFlightEnabled(player)) return;
        if (!KeybindManager.jumpDown(player)) return;
        if (!canFly(player, stack)) return;

        if (KeybindManager.sprintDown(player)) {
            fullFlight(player);
            consumeFuel(stack, 2); // Use more fuel for full flight
        } else {
            upwardsFlight(player);
            consumeFuel(stack, 1); // Use less fuel for hover
        }
    }

    protected void upwardsFlight(Player player) {
        double acceleration = sigmoidAcceleration(player.tickCount, 5.0, 1.0, 2.0);
        acceleration /= 25.0f;
        player.addDeltaMovement(new Vec3(0, Math.max(0.0025, acceleration), 0));
        player.fallDistance = Math.max(player.fallDistance / 1.5f, 0.0f);
    }

    protected void fullFlight(Player player) {
        Vec3 movement = player.getLookAngle().normalize().scale(0.075);
        if (player.getDeltaMovement().length() > 2.0) return;
        player.addDeltaMovement(movement);
        player.fallDistance = Math.max(player.fallDistance / 1.5f, 0.0f);
        if (!player.isFallFlying()) {
            player.startFallFlying();
        }
    }

    private boolean canFly(Player player, ItemStack stack) {
        if (player.isCreative()) return true;
        // Check if we have nitrogen for flight
        return hasNitrogen(stack);
    }

    private void consumeFuel(ItemStack stack, long amount) {
        // Use nitrogen for flight
        consumeNitrogen(stack, amount);
    }

    protected boolean isFullFlightEnabled(Player player) {
        return KeybindManager.suitFlightEnabled(player) && KeybindManager.jumpDown(player) && KeybindManager.sprintDown(player);
    }

    public static double sigmoidAcceleration(double t, double peakTime, double peakAcceleration, double initialAcceleration) {
        return ((2 * peakAcceleration) / (1 + Math.exp(-t / peakTime)) - peakAcceleration) + initialAcceleration;
    }

    public void spawnParticles(Level level, LivingEntity entity, HumanoidModel<?> model, ItemStack stack) {
        if (!(entity instanceof Player player)) return;
        if (!canFly(player, stack)) return;
        if (!hasFullJetSuitSet(player)) return;
        if (!KeybindManager.suitFlightEnabled(player)) return;
        if (!KeybindManager.jumpDown(player) || (!KeybindManager.jumpDown(player) && !KeybindManager.sprintDown(player)))
            return;

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
        return entity instanceof Player player && canFly(player, stack) && isFullFlightEnabled(player);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        // JetSuit stores dual chemicals - oxygen for breathing AND nitrogen for flight
        initializeDualChemicals(stack);
        return stack;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        // Ensure the item has dual chemical data when crafted
        if (!hasChemicalData(stack)) {
            initializeDualChemicals(stack);
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