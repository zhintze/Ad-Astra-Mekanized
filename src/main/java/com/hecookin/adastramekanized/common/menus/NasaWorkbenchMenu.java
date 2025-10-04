package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.common.blockentities.NasaWorkbenchBlockEntity;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NasaWorkbenchMenu extends AbstractContainerMenu {

    private final Container container;
    private final NasaWorkbenchBlockEntity blockEntity;

    public NasaWorkbenchMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, (Container) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public static NasaWorkbenchMenu create(int containerId, Inventory playerInventory, Container container) {
        return new NasaWorkbenchMenu(containerId, playerInventory, container);
    }

    private NasaWorkbenchMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.NASA_WORKBENCH.get(), containerId);
        this.container = container;
        this.blockEntity = container instanceof NasaWorkbenchBlockEntity be ? be : null;

        container.startOpen(playerInventory.player);

        addSlot(new Slot(container, 0, 56, 20));
        addSlot(new Slot(container, 1, 47, 38));
        addSlot(new Slot(container, 2, 65, 38));
        addSlot(new Slot(container, 3, 47, 56));
        addSlot(new Slot(container, 4, 65, 56));
        addSlot(new Slot(container, 5, 47, 74));
        addSlot(new Slot(container, 6, 65, 74));
        addSlot(new Slot(container, 7, 29, 92));
        addSlot(new Slot(container, 8, 47, 92));
        addSlot(new Slot(container, 9, 65, 92));
        addSlot(new Slot(container, 10, 83, 92));
        addSlot(new Slot(container, 11, 29, 110));
        addSlot(new Slot(container, 12, 56, 110));
        addSlot(new Slot(container, 13, 83, 110));

        addSlot(new OutputSlot(container, 14, 129, 56));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 162 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 220));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            if (index == 14) {
                return ItemStack.EMPTY;
            } else if (index < 14) {
                if (!moveItemStackTo(slotStack, 15, 51, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(slotStack, 0, 14, false)) {
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
        return container.stillValid(player);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    public NasaWorkbenchBlockEntity getBlockEntity() {
        return blockEntity;
    }

    private class OutputSlot extends Slot {
        public OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
            if (blockEntity != null && !player.level().isClientSide()) {
                blockEntity.craft();
            }
            super.onTake(player, stack);
        }
    }
}
