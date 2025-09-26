package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.common.blockentities.machines.MekanismBasedOxygenDistributor;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;

public class OxygenDistributorMenu extends AbstractContainerMenu {

    private final MekanismBasedOxygenDistributor blockEntity;
    private final ContainerLevelAccess access;

    // Data slots for synchronization (we'll use 4 slots for 2 longs)
    private int energyLow, energyHigh;
    private int oxygenLow, oxygenHigh;

    // Client constructor
    public OxygenDistributorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    // Server constructor
    public OxygenDistributorMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenuTypes.OXYGEN_DISTRIBUTOR.get(), containerId);

        if (blockEntity instanceof MekanismBasedOxygenDistributor distributor) {
            this.blockEntity = distributor;
            this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        } else {
            throw new IllegalArgumentException("Invalid block entity type");
        }

        // Add player inventory slots
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        // Add data slots for synchronization
        addDataSlots();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        // Ad Astra uses Y offset 162 for player inventory
        // Standard inventory is at Y=162, with 3 rows of 9 slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 162 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        // Hotbar is 58 pixels below the inventory start (162 + 58 = 220)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 220));
        }
    }

    private void addDataSlots() {
        // Energy low bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (blockEntity.getEnergyForDebug(0) & 0xFFFF);
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
                return (int) ((blockEntity.getEnergyForDebug(0) >> 16) & 0xFFFF);
            }

            @Override
            public void set(int value) {
                energyHigh = value;
            }
        });

        // Oxygen low bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (blockEntity.getOxygenTank().getStored() & 0xFFFF);
            }

            @Override
            public void set(int value) {
                oxygenLow = value;
            }
        });

        // Oxygen high bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) ((blockEntity.getOxygenTank().getStored() >> 16) & 0xFFFF);
            }

            @Override
            public void set(int value) {
                oxygenHigh = value;
            }
        });
    }

    public MekanismBasedOxygenDistributor getBlockEntity() {
        return blockEntity;
    }

    // Data synchronization for energy - use synced values on client
    public long getEnergy() {
        if (blockEntity.getLevel().isClientSide()) {
            return ((long) energyHigh << 16) | (energyLow & 0xFFFF);
        }
        return blockEntity.getEnergyForDebug(0);
    }

    public long getMaxEnergy() {
        return blockEntity.getMaxEnergyForDebug(0);
    }

    // Data synchronization for chemicals - use synced values on client
    public long getChemicalAmount() {
        if (blockEntity.getLevel().isClientSide()) {
            return ((long) oxygenHigh << 16) | (oxygenLow & 0xFFFF);
        }
        return blockEntity.getOxygenTank().getStored();
    }

    public long getChemicalCapacity() {
        return blockEntity.getOxygenTank().getCapacity();
    }
}