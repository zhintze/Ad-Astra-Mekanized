package com.hecookin.adastramekanized.common.container;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Simple container for vehicle fuel items only.
 * Simplified from Ad Astra - no cargo inventory, just fuel input/output slots.
 */
public class VehicleContainer extends SimpleContainer {

    public VehicleContainer(int size) {
        super(size);
    }

    public void fromTag(HolderLookup.Provider registries, ListTag containerNbt) {
        for (int i = 0; i < containerNbt.size(); i++) {
            var stack = ItemStack.parseOptional(registries, containerNbt.getCompound(i));
            setItem(i, stack);
        }
    }

    public ListTag createTag(HolderLookup.Provider registries) {
        ListTag containerNbt = new ListTag();
        for (int i = 0; i < getContainerSize(); i++) {
            var stack = getItem(i);
            containerNbt.add(stack.save(registries));
        }
        return containerNbt;
    }
}
