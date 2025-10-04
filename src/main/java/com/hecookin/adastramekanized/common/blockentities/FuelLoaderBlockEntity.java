package com.hecookin.adastramekanized.common.blockentities;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.hecookin.adastramekanized.common.menus.FuelLoaderMenu;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.utils.FluidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Block entity for fuel loader.
 * Stores large amounts of rocket fuel and automatically transfers it to nearby rockets.
 */
public class FuelLoaderBlockEntity extends BlockEntity implements MenuProvider {

    private static final int FUEL_CAPACITY = 10000; // 10 buckets
    private static final int TRANSFER_RATE = 100; // mB per tick
    private static final double DETECTION_RANGE = 5.0; // blocks

    private final FluidTank fluidTank;
    private final ItemStackHandler itemHandler;

    // Slot indices
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    public FuelLoaderBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.FUEL_LOADER.get(), pos, blockState);

        this.fluidTank = new FluidTank(FUEL_CAPACITY) {
            @Override
            protected void onContentsChanged() {
                setChanged();
                sync();
            }
        };

        this.itemHandler = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                sync();
            }
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // Handle bucket transfer every tick
        handleBucketTransfer();

        // Try to fuel nearby rockets every 10 ticks
        if (level.getGameTime() % 10 == 0) {
            transferFuelToNearbyRockets(level, pos);
        }
    }

    /**
     * Handles transferring fluid between buckets and tank.
     */
    private void handleBucketTransfer() {
        ItemStack inputStack = itemHandler.getStackInSlot(INPUT_SLOT);
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);

        // Try to fill tank from input bucket
        if (!inputStack.isEmpty()) {
            FluidUtils.moveItemToContainer(itemHandler, fluidTank, INPUT_SLOT, OUTPUT_SLOT);
        }

        // Try to drain tank to output bucket
        if (!outputStack.isEmpty()) {
            FluidUtils.moveContainerToItem(itemHandler, fluidTank, INPUT_SLOT, OUTPUT_SLOT);
        }
    }

    /**
     * Finds nearby rockets and transfers fuel to them.
     */
    private void transferFuelToNearbyRockets(Level level, BlockPos pos) {
        if (fluidTank.getFluidAmount() == 0) return;

        AABB searchBox = new AABB(pos).inflate(DETECTION_RANGE);
        List<Rocket> rockets = level.getEntitiesOfClass(Rocket.class, searchBox);

        for (Rocket rocket : rockets) {
            IFluidHandler rocketTank = rocket.getCapability(Capabilities.FluidHandler.ENTITY, null);
            if (rocketTank == null) continue;

            // Try to transfer fuel
            FluidStack drained = fluidTank.drain(TRANSFER_RATE, IFluidHandler.FluidAction.SIMULATE);
            if (drained.isEmpty()) return;

            int filled = rocketTank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
            if (filled > 0) {
                FluidStack actualDrain = fluidTank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                rocketTank.fill(actualDrain, IFluidHandler.FluidAction.EXECUTE);

                AdAstraMekanized.LOGGER.debug("Transferred {} mB of fuel to rocket at {}",
                    filled, rocket.blockPosition());
            }
        }
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, inventory);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.adastramekanized.fuel_loader");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FuelLoaderMenu(containerId, playerInventory, this);
    }
}
