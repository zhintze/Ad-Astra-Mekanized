package com.hecookin.adastramekanized.common.utils;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Utilities for fluid/item transfer
 */
public class FluidUtils {

    /**
     * Move fluid from item to container
     */
    public static void moveItemToContainer(SimpleContainer inventory, IFluidHandler fluidContainer,
                                          int inputSlot, int outputSlot, int minAmount) {
        ItemStack inputStack = inventory.getItem(inputSlot);
        if (inputStack.isEmpty()) return;

        IFluidHandlerItem itemHandler = inputStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return;

        FluidStack drained = itemHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        if (!drained.isEmpty()) {
            int filled = fluidContainer.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                itemHandler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                inventory.setItem(outputSlot, itemHandler.getContainer());
            }
        }
    }

    /**
     * Move fluid from container to item
     */
    public static void moveContainerToItem(SimpleContainer inventory, IFluidHandler fluidContainer,
                                          int inputSlot, int outputSlot, int minAmount) {
        ItemStack inputStack = inventory.getItem(inputSlot);
        if (inputStack.isEmpty()) return;

        IFluidHandlerItem itemHandler = inputStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return;

        FluidStack toDrain = fluidContainer.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        if (!toDrain.isEmpty()) {
            int filled = itemHandler.fill(toDrain, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                fluidContainer.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                inventory.setItem(outputSlot, itemHandler.getContainer());
            }
        }
    }

    /**
     * Move fluid from item to container (ItemStackHandler variant)
     */
    public static void moveItemToContainer(ItemStackHandler inventory, IFluidHandler fluidContainer,
                                          int inputSlot, int outputSlot) {
        ItemStack inputStack = inventory.getStackInSlot(inputSlot);
        if (inputStack.isEmpty()) return;

        IFluidHandlerItem itemHandler = inputStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return;

        FluidStack drained = itemHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        if (!drained.isEmpty()) {
            int filled = fluidContainer.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                itemHandler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                inventory.setStackInSlot(inputSlot, ItemStack.EMPTY);
                inventory.setStackInSlot(outputSlot, itemHandler.getContainer());
            }
        }
    }

    /**
     * Move fluid from container to item (ItemStackHandler variant)
     */
    public static void moveContainerToItem(ItemStackHandler inventory, IFluidHandler fluidContainer,
                                          int inputSlot, int outputSlot) {
        ItemStack inputStack = inventory.getStackInSlot(inputSlot);
        if (inputStack.isEmpty()) return;

        IFluidHandlerItem itemHandler = inputStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return;

        FluidStack toDrain = fluidContainer.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        if (!toDrain.isEmpty()) {
            int filled = itemHandler.fill(toDrain, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                fluidContainer.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                inventory.setStackInSlot(inputSlot, ItemStack.EMPTY);
                inventory.setStackInSlot(outputSlot, itemHandler.getContainer());
            }
        }
    }
}
