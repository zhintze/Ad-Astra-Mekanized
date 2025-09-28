package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import com.hecookin.adastramekanized.common.items.OxygenNetworkController;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the Oxygen Network Monitor screen
 */
public class OxygenMonitorMenu extends AbstractContainerMenu {
    private final ItemStack monitorStack;
    private final ItemStack controllerStack;
    private final DistributorLinkData linkData;

    // Client constructor
    public OxygenMonitorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory,
             findMonitorInInventory(playerInventory.player),
             findControllerInInventory(playerInventory.player));
    }

    // Server constructor
    public OxygenMonitorMenu(int containerId, Inventory playerInventory, ItemStack monitor, ItemStack controller) {
        super(ModMenuTypes.OXYGEN_MONITOR.get(), containerId);

        this.monitorStack = monitor;
        this.controllerStack = controller;
        this.linkData = OxygenNetworkController.getOrCreateLinkData(controller);
    }

    @Override
    public boolean stillValid(Player player) {
        // Check that player still has the monitor
        return player.getInventory().contains(monitorStack);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // No inventory slots in this menu
    }

    // Getters for screen access
    public DistributorLinkData getLinkData() {
        return linkData;
    }

    public ItemStack getMonitorStack() {
        return monitorStack;
    }

    public ItemStack getControllerStack() {
        return controllerStack;
    }

    // Helper methods to find items in inventory
    private static ItemStack findMonitorInInventory(Player player) {
        // Return a dummy stack since we're now using a block
        return ItemStack.EMPTY;
    }

    private static ItemStack findControllerInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof OxygenNetworkController) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}