package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import com.hecookin.adastramekanized.common.items.OxygenNetworkController;
import com.hecookin.adastramekanized.common.menus.WirelessPowerRelayMenu;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Block entity that stores energy and distributes it wirelessly to linked oxygen distributors
 */
public class WirelessPowerRelayBlockEntity extends BlockEntity implements MenuProvider, Container {

    private static final int ENERGY_CAPACITY = 1000000; // 1M FE
    private static final int MAX_RECEIVE = 10000; // 10k FE/t input
    private static final int MAX_DISTRIBUTE_PER_DISTRIBUTOR = 1000; // 1k FE/t per distributor
    private static final int DISTRIBUTION_INTERVAL = 20; // Distribute every second

    private final EnergyStorage energyStorage;
    private final SimpleContainer controllerSlot;
    private int tickCounter = 0;
    private int lastDistributorCount = 0;
    private int lastPowerDistributed = 0;

    public WirelessPowerRelayBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.WIRELESS_POWER_RELAY.get(), pos, blockState);
        this.energyStorage = new EnergyStorage(ENERGY_CAPACITY, MAX_RECEIVE, 0);
        this.controllerSlot = new SimpleContainer(1) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return stack.getItem() instanceof OxygenNetworkController;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                WirelessPowerRelayBlockEntity.this.setChanged();
            }
        };
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        tickCounter++;

        // Distribute power every second
        if (tickCounter >= DISTRIBUTION_INTERVAL) {
            tickCounter = 0;
            distributePower();
        }
    }

    private void distributePower() {
        ItemStack controllerStack = controllerSlot.getItem(0);
        if (controllerStack.isEmpty() || !(controllerStack.getItem() instanceof OxygenNetworkController)) {
            lastDistributorCount = 0;
            lastPowerDistributed = 0;
            return;
        }

        DistributorLinkData linkData = OxygenNetworkController.getOrCreateLinkData(controllerStack);
        if (linkData.getLinkCount() == 0) {
            lastDistributorCount = 0;
            lastPowerDistributed = 0;
            return;
        }

        // Update statuses first
        OxygenNetworkController.updateDistributorStatuses(level, linkData);

        // Collect all enabled distributors that need power
        List<ImprovedOxygenDistributor> needsPower = new ArrayList<>();
        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            if (!link.isEnabled()) continue;

            BlockPos pos = link.getPos();
            if (!level.isLoaded(pos)) continue;

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ImprovedOxygenDistributor distributor) {
                IEnergyStorage storage = distributor.getEnergyStorage();
                if (storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                    needsPower.add(distributor);
                }
            }
        }

        lastDistributorCount = needsPower.size();

        if (needsPower.isEmpty() || energyStorage.getEnergyStored() == 0) {
            lastPowerDistributed = 0;
            return;
        }

        // Calculate equal distribution
        int totalAvailable = Math.min(energyStorage.getEnergyStored(), MAX_DISTRIBUTE_PER_DISTRIBUTOR * needsPower.size());
        int perDistributor = totalAvailable / needsPower.size();
        int totalDistributed = 0;

        // Distribute power equally
        for (ImprovedOxygenDistributor distributor : needsPower) {
            IEnergyStorage storage = distributor.getEnergyStorage();
            int toSend = Math.min(perDistributor, storage.getMaxEnergyStored() - storage.getEnergyStored());

            if (toSend > 0) {
                int sent = storage.receiveEnergy(toSend, false);
                totalDistributed += sent;

                AdAstraMekanized.LOGGER.debug("Sent {} FE to distributor at {}", sent, distributor.getBlockPos());
            }
        }

        // Extract the distributed power from storage
        if (totalDistributed > 0) {
            energyStorage.extractEnergy(totalDistributed, false);
            lastPowerDistributed = totalDistributed;
            setChanged();
        }
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) {
            ItemStack controller = controllerSlot.getItem(0);
            if (!controller.isEmpty()) {
                Block.popResource(level, worldPosition, controller);
                controllerSlot.clearContent();
            }
        }
    }

    // Energy storage implementation
    public class EnergyStorage implements IEnergyStorage, IStrictEnergyHandler {
        private int energy;
        private final int capacity;
        private final int maxReceive;
        private final int maxExtract;

        public EnergyStorage(int capacity, int maxReceive, int maxExtract) {
            this.capacity = capacity;
            this.maxReceive = maxReceive;
            this.maxExtract = maxExtract;
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = Math.min(toReceive, Math.min(maxReceive, capacity - energy));
            if (!simulate) {
                energy += received;
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            int extracted = Math.min(toExtract, Math.min(maxExtract, energy));
            if (!simulate) {
                energy -= extracted;
                setChanged();
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return false; // Don't allow extraction
        }

        @Override
        public boolean canReceive() {
            return maxReceive > 0;
        }

        // Mekanism IStrictEnergyHandler implementation
        @Override
        public int getEnergyContainerCount() {
            return 1;
        }

        @Override
        public long getEnergy(int container) {
            return container == 0 ? energy : 0;
        }

        @Override
        public void setEnergy(int container, long energy) {
            if (container == 0) {
                this.energy = (int) Math.min(energy, Integer.MAX_VALUE);
                setChanged();
            }
        }

        @Override
        public long getMaxEnergy(int container) {
            return container == 0 ? capacity : 0;
        }

        @Override
        public long getNeededEnergy(int container) {
            return container == 0 ? capacity - energy : 0;
        }

        @Override
        public long insertEnergy(int container, long amount, @NotNull Action action) {
            if (container != 0) return 0;
            int toReceive = (int) Math.min(amount, Math.min(maxReceive, capacity - energy));
            if (action.execute()) {
                energy += toReceive;
                setChanged();
            }
            return toReceive;
        }

        @Override
        public long extractEnergy(int container, long amount, @NotNull Action action) {
            return 0; // Don't allow extraction
        }
    }

    // Container implementation for controller slot
    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return controllerSlot.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return controllerSlot.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return controllerSlot.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return controllerSlot.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        controllerSlot.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        controllerSlot.clearContent();
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.adastramekanized.wireless_power_relay");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new WirelessPowerRelayMenu(containerId, playerInventory, this);
    }

    // Getters for GUI
    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public SimpleContainer getControllerSlot() {
        return controllerSlot;
    }

    public int getLastDistributorCount() {
        return lastDistributorCount;
    }

    public int getLastPowerDistributed() {
        return lastPowerDistributed;
    }

    // NBT serialization
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("energy", energyStorage.energy);
        tag.put("controller", controllerSlot.getItem(0).save(provider));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        energyStorage.energy = tag.getInt("energy");
        controllerSlot.setItem(0, ItemStack.parseOptional(provider, tag.getCompound("controller")));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("energy", energyStorage.energy);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        energyStorage.energy = tag.getInt("energy");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}