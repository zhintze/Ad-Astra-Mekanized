package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.common.container.VehicleContainer;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class RocketMenu extends AbstractContainerMenu {

    private final Rocket rocket;
    private final VehicleContainer rocketInventory;

    public RocketMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (Rocket) playerInventory.player.level().getEntity(extraData.readVarInt()));
    }

    public RocketMenu(int containerId, Inventory playerInventory, Rocket rocket) {
        super(ModMenuTypes.ROCKET.get(), containerId);
        this.rocket = rocket;
        this.rocketInventory = rocket.inventory();

        // 8 storage slots in 2x4 grid (indices 2-9)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new Slot(rocketInventory, row * 4 + col + 2, 78 + col * 18, 31 + row * 18));
            }
        }

        // Fuel input bucket slot (index 0)
        addSlot(new Slot(rocketInventory, 0, 12, 24));

        // Fuel output bucket slot (index 1)
        addSlot(new Slot(rocketInventory, 1, 12, 54));

        // Player inventory - matches Ad Astra offsets (X=0, Y=92)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, col * 18, 92 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInventory, col, col * 18, 150));
        }
    }

    public Rocket getRocket() {
        return rocket;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            // From rocket inventory (storage + fuel slots) to player inventory
            if (index < 10) {
                if (!moveItemStackTo(slotStack, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to rocket
            else {
                // Check if item contains valid fuel for this rocket tier
                if (containsValidFuel(slotStack)) {
                    // Try to move to fuel input slot first (index 0)
                    if (!moveItemStackTo(slotStack, 0, 1, false)) {
                        // If fuel slot is full, try storage slots
                        if (!moveItemStackTo(slotStack, 2, 10, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // Regular items go to storage slots
                else {
                    if (!moveItemStackTo(slotStack, 2, 10, false)) {
                        return ItemStack.EMPTY;
                    }
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

    /**
     * Checks if an ItemStack contains a fluid that is valid fuel for this rocket tier
     */
    private boolean containsValidFuel(ItemStack stack) {
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler == null) {
            return false;
        }

        // Check if the item contains any fluid
        for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            FluidStack fluidStack = fluidHandler.getFluidInTank(tank);
            if (!fluidStack.isEmpty()) {
                // Check if this fluid is valid for the rocket's fuel tank (tank 0)
                return rocket.fluidContainer().isFluidValid(0, fluidStack);
            }
        }

        return false;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !rocket.isRemoved() && rocket.distanceToSqr(player) < 64.0;
    }
}
