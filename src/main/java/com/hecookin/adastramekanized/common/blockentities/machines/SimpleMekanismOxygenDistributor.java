package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IEnergyContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simplified Oxygen Distributor that works with Mekanism API
 */
public class SimpleMekanismOxygenDistributor extends BlockEntity implements 
        IMekanismChemicalHandler, IMekanismStrictEnergyHandler, MenuProvider, IEnergyStorage {

    private static final long OXYGEN_CAPACITY = 4000; // mB
    private static final long ENERGY_CAPACITY = 40000; // FE
    private static final long ENERGY_PER_TICK = 20; // FE/tick
    private static final long OXYGEN_PER_BLOCK = 1; // mB per block
    private static final int MAX_RANGE = 16;
    private static final int DISTRIBUTION_INTERVAL = 100; // 5 seconds

    private final IChemicalTank oxygenTank;
    private long storedEnergy = 0;
    private int tickCounter = 0;
    private final Set<BlockPos> oxygenatedBlocks = new HashSet<>();
    private boolean isActive = false;

    public SimpleMekanismOxygenDistributor(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(), pos, state);
        
        // Create oxygen tank that accepts any chemical (we'll filter in insert)
        IChemicalTank tank = BasicChemicalTank.create(OXYGEN_CAPACITY, this::setChanged);
        this.oxygenTank = tank;
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        tickCounter++;

        // Check if we can function
        boolean canFunction = storedEnergy >= ENERGY_PER_TICK && !oxygenTank.isEmpty();

        if (canFunction) {
            // Consume energy
            storedEnergy -= ENERGY_PER_TICK;

            // Distribute oxygen at intervals
            if (tickCounter >= DISTRIBUTION_INTERVAL) {
                tickCounter = 0;
                distributeOxygen();
            }

            if (!isActive) {
                isActive = true;
                setChanged();
            }

            // Play sound periodically
            if (level.getGameTime() % 60 == 0) {
                level.playSound(null, worldPosition, SoundEvents.BEACON_AMBIENT,
                    SoundSource.BLOCKS, 0.3F, 1.0F);
            }
        } else if (isActive) {
            isActive = false;
            clearOxygenatedBlocks();
            setChanged();
        }
    }

    private void distributeOxygen() {
        clearOxygenatedBlocks();

        Set<BlockPos> newOxygenatedBlocks = findEnclosedArea(
            level,
            worldPosition.above(),
            500 // max blocks
        );

        if (!newOxygenatedBlocks.isEmpty()) {
            oxygenatedBlocks.addAll(newOxygenatedBlocks);

            // Consume oxygen
            long oxygenToConsume = Math.min(
                oxygenatedBlocks.size() * OXYGEN_PER_BLOCK,
                oxygenTank.getStored()
            );
            oxygenTank.shrinkStack(oxygenToConsume, Action.EXECUTE);
            
            AdAstraMekanized.LOGGER.debug("Distributed oxygen to {} blocks", oxygenatedBlocks.size());
        }
    }

    private void clearOxygenatedBlocks() {
        oxygenatedBlocks.clear();
    }

    private Set<BlockPos> findEnclosedArea(Level level, BlockPos start, int maxBlocks) {
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> toCheck = new HashSet<>();
        toCheck.add(start);

        while (!toCheck.isEmpty() && visited.size() < maxBlocks) {
            BlockPos current = toCheck.iterator().next();
            toCheck.remove(current);

            if (visited.contains(current)) {
                continue;
            }

            BlockState state = level.getBlockState(current);
            if (state.isAir()) {
                visited.add(current);

                for (Direction dir : Direction.values()) {
                    BlockPos adjacent = current.relative(dir);
                    if (!visited.contains(adjacent) && isWithinRange(adjacent)) {
                        toCheck.add(adjacent);
                    }
                }
            }
        }

        // If we hit max blocks, area is too large (not enclosed)
        if (visited.size() >= maxBlocks) {
            return new HashSet<>();
        }

        return visited;
    }

    private boolean isWithinRange(BlockPos pos) {
        return Math.abs(pos.getX() - worldPosition.getX()) <= MAX_RANGE &&
               Math.abs(pos.getY() - worldPosition.getY()) <= MAX_RANGE &&
               Math.abs(pos.getZ() - worldPosition.getZ()) <= MAX_RANGE;
    }

    // IMekanismChemicalHandler implementation
    @Override
    public @NotNull List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        return Collections.singletonList(oxygenTank);
    }

    @Override
    public void onContentsChanged() {
        setChanged();
    }

    // IChemicalHandler implementation
    @Override
    public int getChemicalTanks() {
        return 1;
    }

    @Override
    public @NotNull ChemicalStack getChemicalInTank(int tank) {
        return tank == 0 ? oxygenTank.getStack() : ChemicalStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, @NotNull ChemicalStack stack) {
        if (tank == 0) {
            oxygenTank.setStack(stack);
        }
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        return tank == 0 ? oxygenTank.getCapacity() : 0;
    }

    @Override
    public boolean isValid(int tank, @NotNull ChemicalStack stack) {
        // Only accept oxygen (check by registry name)
        return tank == 0 && stack.getChemical().getRegistryName().getPath().equals("oxygen");
    }

    @Override
    public @NotNull ChemicalStack insertChemical(int tank, @NotNull ChemicalStack stack, @NotNull Action action) {
        if (tank != 0 || !isValid(tank, stack)) {
            return stack;
        }
        return oxygenTank.insert(stack, action, AutomationType.EXTERNAL);
    }

    @Override
    public @NotNull ChemicalStack extractChemical(int tank, long amount, @NotNull Action action) {
        if (tank != 0) {
            return ChemicalStack.EMPTY;
        }
        return oxygenTank.extract(amount, action, AutomationType.EXTERNAL);
    }

    // IMekanismStrictEnergyHandler implementation
    @Override
    public @NotNull List<IEnergyContainer> getEnergyContainers(Direction side) {
        // Return a simple wrapper
        return Collections.singletonList(new SimpleEnergyContainer());
    }

    private class SimpleEnergyContainer implements IEnergyContainer {
        @Override
        public long getEnergy() {
            return storedEnergy;
        }

        @Override
        public void setEnergy(long energy) {
            storedEnergy = energy;
            setChanged();
        }

        @Override
        public long getMaxEnergy() {
            return ENERGY_CAPACITY;
        }

        @Override
        public long getNeeded() {
            return ENERGY_CAPACITY - storedEnergy;
        }

        @Override
        public long insert(long amount, Action action, AutomationType automationType) {
            long toInsert = Math.min(amount, getNeeded());
            if (action.execute()) {
                storedEnergy += toInsert;
                setChanged();
            }
            return toInsert;
        }

        @Override
        public long extract(long amount, Action action, AutomationType automationType) {
            return 0; // Don't allow extraction
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putLong("energy", storedEnergy);
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            storedEnergy = nbt.getLong("energy");
        }

        @Override
        public void onContentsChanged() {
            setChanged();
        }
    }

    // IEnergyStorage implementation for Forge Energy compatibility
    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int received = (int) Math.min(toReceive, ENERGY_CAPACITY - storedEnergy);
        if (!simulate) {
            storedEnergy += received;
            setChanged();
        }
        return received;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        return 0; // Don't allow extraction
    }

    @Override
    public int getEnergyStored() {
        return (int) storedEnergy;
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) ENERGY_CAPACITY;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.adastramekanized.oxygen_distributor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return null; // GUI not implemented yet
    }

    // NBT handling
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putLong("storedEnergy", storedEnergy);
        tag.put("oxygenTank", oxygenTank.serializeNBT(provider));
        tag.putBoolean("isActive", isActive);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        storedEnergy = tag.getLong("storedEnergy");
        oxygenTank.deserializeNBT(provider, tag.getCompound("oxygenTank"));
        isActive = tag.getBoolean("isActive");
    }

    public boolean isActive() {
        return isActive;
    }

    public long getStoredEnergy() {
        return storedEnergy;
    }

    public IChemicalTank getOxygenTank() {
        return oxygenTank;
    }

    public int getOxygenatedBlockCount() {
        return oxygenatedBlocks.size();
    }

}