package com.hecookin.adastramekanized.common.capabilities;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

public interface EnergyCapableItem {
    IEnergyStorage getEnergyStorage(ItemStack holder);
}