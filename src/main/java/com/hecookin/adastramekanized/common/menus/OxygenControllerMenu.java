package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import com.hecookin.adastramekanized.common.items.OxygenNetworkController;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Container menu for the Oxygen Network Controller GUI
 */
public class OxygenControllerMenu extends AbstractContainerMenu {

    private final ItemStack controllerStack;
    private final DistributorLinkData linkData;
    private final Player player;

    // Client constructor
    public OxygenControllerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, findControllerInInventory(playerInventory.player));
    }

    // Server constructor
    public OxygenControllerMenu(int containerId, Inventory playerInventory, ItemStack controllerStack) {
        super(ModMenuTypes.OXYGEN_CONTROLLER.get(), containerId);
        this.player = playerInventory.player;
        this.controllerStack = controllerStack;
        this.linkData = OxygenNetworkController.getOrCreateLinkData(controllerStack);

        // Update distributor statuses
        if (!player.level().isClientSide) {
            OxygenNetworkController.updateDistributorStatuses(player.level(), linkData);
        }

        // Add player inventory slots (standard layout)
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        // Player inventory at Y=140 (adjust as needed for your GUI)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        // Hotbar at Y=198
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No quick move for this container
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        // Check if player still has the controller
        return player.getMainHandItem() == controllerStack ||
               player.getOffhandItem() == controllerStack ||
               player.getInventory().contains(controllerStack);
    }

    public DistributorLinkData getLinkData() {
        return linkData;
    }

    public ItemStack getControllerStack() {
        return controllerStack;
    }

    public void saveLinkData() {
        OxygenNetworkController.saveLinkData(controllerStack, linkData);
    }

    /**
     * Handle button clicks from the client
     */
    public void handleButtonClick(int buttonId, int distributorIndex) {
        if (distributorIndex < 0 || distributorIndex >= linkData.getLinkedDistributors().size()) {
            return;
        }

        DistributorLinkData.LinkedDistributor link = linkData.getLinkedDistributors().get(distributorIndex);

        switch (buttonId) {
            case 0 -> {
                // Toggle individual distributor
                link.setEnabled(!link.isEnabled());
                saveLinkData();
            }
            case 1 -> {
                // Remove distributor
                linkData.removeLink(link.getPos());
                saveLinkData();
            }
        }
    }

    /**
     * Handle rename action from client
     */
    public void handleRename(int distributorIndex, String newName) {
        if (distributorIndex < 0 || distributorIndex >= linkData.getLinkedDistributors().size()) {
            return;
        }

        DistributorLinkData.LinkedDistributor link = linkData.getLinkedDistributors().get(distributorIndex);
        link.setCustomName(newName);
        saveLinkData();
    }

    /**
     * Handle master toggle from client
     */
    public void handleMasterToggle(boolean enableAll) {
        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            link.setEnabled(enableAll);
        }
        saveLinkData();
    }

    /**
     * Handle clear all from client
     */
    public void handleClearAll() {
        linkData.clearAll();
        saveLinkData();
    }

    private static ItemStack findControllerInInventory(Player player) {
        // Check main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof OxygenNetworkController) {
            return mainHand;
        }

        // Check off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof OxygenNetworkController) {
            return offHand;
        }

        // Check inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof OxygenNetworkController) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}