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
     * Drains as much as possible from the item until it's empty or the container is full
     */
    public static void moveItemToContainer(SimpleContainer inventory, IFluidHandler fluidContainer,
                                          int inputSlot, int outputSlot, int minAmount) {
        ItemStack inputStack = inventory.getItem(inputSlot);
        if (inputStack.isEmpty()) return;

        IFluidHandlerItem itemHandler = inputStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return;

        boolean transferredAny = false;

        // Keep draining until item is empty or container is full
        while (true) {
            FluidStack drained = itemHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            if (drained.isEmpty()) break;

            int filled = fluidContainer.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            if (filled <= 0) break;

            itemHandler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            transferredAny = true;

            // If we couldn't fill the full amount, container is full
            if (filled < drained.getAmount()) break;
        }

        if (transferredAny) {
            inventory.setItem(inputSlot, ItemStack.EMPTY);
            inventory.setItem(outputSlot, itemHandler.getContainer());
        }
    }

    /**
     * Move fluid from container to item
     * Fills item as much as possible until it's full or the container is empty
     */
    public static void moveContainerToItem(SimpleContainer inventory, IFluidHandler fluidContainer,
                                          int inputSlot, int outputSlot, int minAmount) {
        ItemStack inputStack = inventory.getItem(inputSlot);
        if (inputStack.isEmpty()) return;

        IFluidHandlerItem itemHandler = inputStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return;

        boolean transferredAny = false;

        // Keep filling until item is full or container is empty
        while (true) {
            FluidStack toDrain = fluidContainer.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            if (toDrain.isEmpty()) break;

            int filled = itemHandler.fill(toDrain, IFluidHandler.FluidAction.EXECUTE);
            if (filled <= 0) break;

            fluidContainer.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            transferredAny = true;

            // If we couldn't drain the full amount, item is full
            if (filled < toDrain.getAmount()) break;
        }

        if (transferredAny) {
            inventory.setItem(inputSlot, ItemStack.EMPTY);
            inventory.setItem(outputSlot, itemHandler.getContainer());
        }
    }

    /**
     * Move fluid from item to container (ItemStackHandler variant)
     * Drains as much as possible from the item until it's empty or the container is full
     */
    public static void moveItemToContainer(ItemStackHandler inventory, IFluidHandler fluidContainer,
                                          int inputSlot, int outputSlot) {
        ItemStack inputStack = inventory.getStackInSlot(inputSlot);
        if (inputStack.isEmpty()) return;

        IFluidHandlerItem itemHandler = inputStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return;

        boolean transferredAny = false;

        // Keep draining until item is empty or container is full
        while (true) {
            FluidStack drained = itemHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            if (drained.isEmpty()) break;

            int filled = fluidContainer.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            if (filled <= 0) break;

            itemHandler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            transferredAny = true;

            // If we couldn't fill the full amount, container is full
            if (filled < drained.getAmount()) break;
        }

        if (transferredAny) {
            inventory.setStackInSlot(inputSlot, ItemStack.EMPTY);
            inventory.setStackInSlot(outputSlot, itemHandler.getContainer());
        }
    }

    /**
     * Move fluid from container to item (ItemStackHandler variant)
     * Fills item as much as possible until it's full or the container is empty
     */
    public static void moveContainerToItem(ItemStackHandler inventory, IFluidHandler fluidContainer,
                                          int inputSlot, int outputSlot) {
        ItemStack inputStack = inventory.getStackInSlot(inputSlot);
        if (inputStack.isEmpty()) return;

        IFluidHandlerItem itemHandler = inputStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return;

        boolean transferredAny = false;

        // Keep filling until item is full or container is empty
        while (true) {
            FluidStack toDrain = fluidContainer.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            if (toDrain.isEmpty()) break;

            int filled = itemHandler.fill(toDrain, IFluidHandler.FluidAction.EXECUTE);
            if (filled <= 0) break;

            fluidContainer.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            transferredAny = true;

            // If we couldn't drain the full amount, item is full
            if (filled < toDrain.getAmount()) break;
        }

        if (transferredAny) {
            inventory.setStackInSlot(inputSlot, ItemStack.EMPTY);
            inventory.setStackInSlot(outputSlot, itemHandler.getContainer());
        }
    }
}
