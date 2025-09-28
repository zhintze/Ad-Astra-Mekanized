package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.common.blockentities.machines.WirelessPowerRelayBlockEntity;
import com.hecookin.adastramekanized.common.items.OxygenNetworkController;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Container menu for the Wireless Power Relay GUI
 */
public class WirelessPowerRelayMenu extends AbstractContainerMenu {

    private final WirelessPowerRelayBlockEntity blockEntity;

    // Synced data values
    private int energyLow;
    private int energyHigh;
    private int maxEnergyLow;
    private int maxEnergyHigh;
    private int lastDistributorCount;
    private int lastPowerDistributed;

    // Client constructor
    public WirelessPowerRelayMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    // Server constructor
    public WirelessPowerRelayMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenuTypes.WIRELESS_POWER_RELAY.get(), containerId);

        // For client side, create a dummy block entity
        if (blockEntity == null) {
            this.blockEntity = new WirelessPowerRelayBlockEntity(BlockPos.ZERO, null);
        } else if (!(blockEntity instanceof WirelessPowerRelayBlockEntity relay)) {
            throw new IllegalArgumentException("Block entity must be WirelessPowerRelayBlockEntity");
        } else {
            this.blockEntity = relay;
        }

        // Add controller slot
        this.addSlot(new Slot(this.blockEntity.getControllerSlot(), 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof OxygenNetworkController;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Add player inventory
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        // Add data slots for syncing
        addDataSlots();
    }

    private void addDataSlots() {
        // Energy low bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (blockEntity.getEnergyStorage().getEnergyStored() & 0xFFFF);
            }

            @Override
            public void set(int value) {
                energyLow = value;
            }
        });

        // Energy high bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) ((blockEntity.getEnergyStorage().getEnergyStored() >> 16) & 0xFFFF);
            }

            @Override
            public void set(int value) {
                energyHigh = value;
            }
        });

        // Max energy low bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (blockEntity.getEnergyStorage().getMaxEnergyStored() & 0xFFFF);
            }

            @Override
            public void set(int value) {
                maxEnergyLow = value;
            }
        });

        // Max energy high bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) ((blockEntity.getEnergyStorage().getMaxEnergyStored() >> 16) & 0xFFFF);
            }

            @Override
            public void set(int value) {
                maxEnergyHigh = value;
            }
        });

        // Last distributor count
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getLastDistributorCount();
            }

            @Override
            public void set(int value) {
                lastDistributorCount = value;
            }
        });

        // Last power distributed
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getLastPowerDistributed();
            }

            @Override
            public void set(int value) {
                lastPowerDistributed = value;
            }
        });
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            
            if (index == 0) {
                // Moving from controller slot to player inventory
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to controller slot
                if (slotStack.getItem() instanceof OxygenNetworkController) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
            
            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            
            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            
            slot.onTake(player, slotStack);
        }
        
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    public WirelessPowerRelayBlockEntity getBlockEntity() {
        return blockEntity;
    }

    // Client-side getters for synced data
    public int getEnergy() {
        return (energyHigh << 16) | (energyLow & 0xFFFF);
    }

    public int getMaxEnergy() {
        return (maxEnergyHigh << 16) | (maxEnergyLow & 0xFFFF);
    }

    public int getLastDistributorCount() {
        return lastDistributorCount;
    }

    public int getLastPowerDistributed() {
        return lastPowerDistributed;
    }
}