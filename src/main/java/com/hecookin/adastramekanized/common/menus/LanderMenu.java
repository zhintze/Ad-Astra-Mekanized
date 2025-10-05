package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.common.container.VehicleContainer;
import com.hecookin.adastramekanized.common.entities.vehicles.Lander;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Menu for the Lander entity.
 * Contains 11 slots: slot 0 for rocket item, slots 1-10 for cargo from rocket.
 */
public class LanderMenu extends AbstractContainerMenu {

    private final Lander lander;
    private final VehicleContainer landerInventory;

    public LanderMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (Lander) playerInventory.player.level().getEntity(extraData.readVarInt()));
    }

    public LanderMenu(int containerId, Inventory playerInventory, Lander lander) {
        super(ModMenuTypes.LANDER.get(), containerId);
        this.lander = lander;
        this.landerInventory = lander.inventory();

        // Rocket item slot (index 0) - centered at top
        addSlot(new Slot(landerInventory, 0, 80, 20));

        // Cargo slots (indices 1-10) in 2x5 grid
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 5; col++) {
                addSlot(new Slot(landerInventory, row * 5 + col + 1, 26 + col * 18, 50 + row * 18));
            }
        }

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, col * 18, 100 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInventory, col, col * 18, 158));
        }
    }

    public Lander getLander() {
        return lander;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            // From lander inventory (11 slots) to player inventory
            if (index < 11) {
                if (!moveItemStackTo(slotStack, 11, 47, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to lander cargo slots (not rocket slot)
            else {
                if (!moveItemStackTo(slotStack, 1, 11, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !lander.isRemoved() && lander.distanceToSqr(player) < 64.0;
    }
}
