package com.hecookin.adastramekanized.common.items.armor;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.capabilities.EnergyCapableItem;
import com.hecookin.adastramekanized.common.capabilities.ItemEnergyStorage;
import com.hecookin.adastramekanized.common.items.MekanismCompatibleItems;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Jet suit chest piece that stores both oxygen AND hydrogen for flight.
 * Works like Mekanism's jetpack when Mekanism is available.
 * Oxygen provides breathing, hydrogen provides flight fuel.
 */
public class JetSuitItem extends SpaceSuitItem implements EnergyCapableItem {

    protected static final String HYDROGEN = "hydrogen";
    private final long energyCapacity;

    public JetSuitItem(Holder<ArmorMaterial> material, ArmorItem.Type type, int oxygenTankSize, int energy, Item.Properties properties) {
        super(material, type, oxygenTankSize, properties);
        this.energyCapacity = energy;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        // Add oxygen display from parent
        super.appendHoverText(stack, context, tooltip, flag);

        // Add hydrogen/fuel display
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager.isMekanismAvailable()) {
            try {
                Long hydrogenAmount = manager.getMekanismIntegration().getChemicalAmount(stack, HYDROGEN);
                if (hydrogenAmount != null) {
                    tooltip.add(Component.literal("Hydrogen: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(hydrogenAmount + " mB").withStyle(ChatFormatting.YELLOW)));
                }
            } catch (Exception e) {
                // Fall back to energy display
                addEnergyTooltip(stack, tooltip);
            }
        } else {
            addEnergyTooltip(stack, tooltip);
        }

        tooltip.add(Component.translatable("tooltip.adastramekanized.jet_suit_info").withStyle(ChatFormatting.GRAY));
    }

    private void addEnergyTooltip(ItemStack stack, List<Component> tooltip) {
        var energy = getEnergyStorage(stack);
        tooltip.add(Component.literal("Energy: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(energy.getEnergyStored() + " / " + energyCapacity + " FE").withStyle(ChatFormatting.YELLOW)));
    }

    @Override
    public IEnergyStorage getEnergyStorage(ItemStack holder) {
        var capability = holder.getCapability(Capabilities.EnergyStorage.ITEM);
        if (capability != null) {
            return capability;
        }
        return new ItemEnergyStorage(holder, (int) energyCapacity, 1000, 1000, true, true);
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

        // Check if we have hydrogen for Mekanism-style flight
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager.isMekanismAvailable()) {
            try {
                return manager.getMekanismIntegration().hasChemical(stack, HYDROGEN);
            } catch (Exception ignored) {}
        }

        // Fall back to energy check
        return getEnergyStorage(stack).getEnergyStored() > 0;
    }

    private void consumeFuel(ItemStack stack, long amount) {
        // Try to use hydrogen first if Mekanism is available
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager.isMekanismAvailable()) {
            try {
                manager.getMekanismIntegration().useChemical(stack, HYDROGEN, amount);
                return;
            } catch (Exception ignored) {}
        }

        // Fall back to energy consumption
        var energyStorage = getEnergyStorage(stack);
        energyStorage.extractEnergy((int) (amount * 50), false);
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
        // JetSuit can store both oxygen for breathing AND hydrogen for flight
        // The NBT will indicate it accepts both chemicals
        MekanismCompatibleItems.createChemicalArmor(stack, capacity, "oxygen,hydrogen");
        return stack;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        // Ensure the item has chemical data when crafted
        if (!hasChemicalData(stack)) {
            MekanismCompatibleItems.createChemicalArmor(stack, capacity, "oxygen,hydrogen");
        }
    }

    private boolean hasChemicalData(ItemStack stack) {
        var customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        return !tag.isEmpty() && tag.contains("mekanism");
    }
}