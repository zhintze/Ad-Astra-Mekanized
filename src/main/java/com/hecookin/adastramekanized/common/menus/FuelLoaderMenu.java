package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.common.blockentities.FuelLoaderBlockEntity;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Menu for fuel loader GUI.
 * Provides slots for bucket input/output and displays fluid tank level.
 */
public class FuelLoaderMenu extends AbstractContainerMenu {

    private final FuelLoaderBlockEntity blockEntity;

    public FuelLoaderMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, (FuelLoaderBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public FuelLoaderMenu(int containerId, Inventory playerInventory, FuelLoaderBlockEntity blockEntity) {
        super(ModMenuTypes.FUEL_LOADER.get(), containerId);
        this.blockEntity = blockEntity;

        // Input bucket slot (left side, top)
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 12, 22));

        // Output bucket slot (left side, bottom)
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 1, 12, 52));

        // Player inventory (matches Ad Astra oxygen loader at Y=102)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 102 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 160));
        }
    }

    public FuelLoaderBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            // From machine to player inventory
            if (index < 2) {
                if (!moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to machine
            else {
                if (!moveItemStackTo(slotStack, 0, 2, false)) {
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
        return blockEntity.getLevel() != null &&
               blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) == blockEntity &&
               player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                   blockEntity.getBlockPos().getY() + 0.5,
                   blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}
