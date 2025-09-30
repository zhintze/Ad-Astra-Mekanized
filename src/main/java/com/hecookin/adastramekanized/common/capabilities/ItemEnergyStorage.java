package com.hecookin.adastramekanized.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Energy storage implementation for items.
 * Stores energy data in the ItemStack's NBT.
 */
public class ItemEnergyStorage implements IEnergyStorage {

    private final ItemStack stack;
    private final int capacity;
    private final int maxReceive;
    private final int maxExtract;
    private final boolean canReceive;
    private final boolean canExtract;

    private static final String ENERGY_TAG = "Energy";

    public ItemEnergyStorage(ItemStack stack, int capacity, int maxReceive, int maxExtract, boolean canReceive, boolean canExtract) {
        this.stack = stack;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.canReceive = canReceive;
        this.canExtract = canExtract;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (!canReceive || toReceive <= 0) {
            return 0;
        }

        int energy = getEnergyStored();
        int energyReceived = Math.min(capacity - energy, Math.min(maxReceive, toReceive));

        if (!simulate && energyReceived > 0) {
            setEnergy(energy + energyReceived);
        }

        return energyReceived;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!canExtract || toExtract <= 0) {
            return 0;
        }

        int energy = getEnergyStored();
        int energyExtracted = Math.min(energy, Math.min(maxExtract, toExtract));

        if (!simulate && energyExtracted > 0) {
            setEnergy(energy - energyExtracted);
        }

        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        return tag.getInt(ENERGY_TAG);
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return canExtract;
    }

    @Override
    public boolean canReceive() {
        return canReceive;
    }

    private void setEnergy(int energy) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        tag.putInt(ENERGY_TAG, Math.max(0, Math.min(energy, capacity)));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
    }
}