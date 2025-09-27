package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
import com.hecookin.adastramekanized.common.menus.OxygenDistributorMenu;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.energy.IEnergyStorage;
import mekanism.api.energy.IStrictEnergyHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A working oxygen distributor that properly exposes capabilities
 */
public class MekanismBasedOxygenDistributor extends BlockEntity implements MenuProvider {

    private static final long OXYGEN_CAPACITY = 2000; // mB - reduced oxygen storage
    private static final int ENERGY_CAPACITY = 30000; // FE (30 kFE) - enough for ~2.5 minutes real time operation
    private static final int ENERGY_PER_DISTRIBUTION = 400; // FE per distribution (doubled from 200)
    private static final double OXYGEN_PER_BLOCK = 0.25; // mB per block - reduced consumption rate
    private static final int DISTRIBUTION_INTERVAL = 100; // ticks between oxygen distributions (every 5 seconds, doubled rate)
    
    private final IChemicalTank oxygenTank;
    private final EnergyStorage energyStorage;
    private final ChemicalHandler chemicalHandler;
    
    private int tickCounter = 0;
    private boolean isActive = false;
    private boolean oxygenBlockVisibility = false;
    private final Set<BlockPos> oxygenatedBlocks = new HashSet<>();

    public MekanismBasedOxygenDistributor(BlockPos pos, BlockState state) {
        this(ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(), pos, state);
    }

    // Protected constructor for subclasses to use their own BlockEntityType
    protected MekanismBasedOxygenDistributor(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        // Create oxygen tank - use inputModern for proper pressurized tube connections
        // This tank only accepts oxygen and allows external insertion but not extraction
        this.oxygenTank = BasicChemicalTank.inputModern(
            OXYGEN_CAPACITY,
            stack -> {
                boolean accepted = isOxygen(stack.getChemical());
                if (accepted) {
                    AdAstraMekanized.LOGGER.debug("Tank accepting oxygen: {} mB", stack.getAmount());
                }
                return accepted;
            },
            this::setChanged
        );

        // Create energy storage - allow extraction for internal use
        this.energyStorage = new EnergyStorage(ENERGY_CAPACITY, 1000, ENERGY_PER_DISTRIBUTION);

        // Create chemical handler
        this.chemicalHandler = new ChemicalHandler();
    }

    private boolean isOxygen(Chemical chemical) {
        // Check if the chemical is oxygen based on its registry name
        // Mekanism's oxygen is registered as "mekanism:oxygen"
        var registryName = chemical.getRegistryName();
        boolean isOxygen = registryName.getNamespace().equals("mekanism") && registryName.getPath().equals("oxygen");

        // Debug log to see what chemicals are being checked
        if (!isOxygen) {
            AdAstraMekanized.LOGGER.debug("Rejected chemical: {}:{}", registryName.getNamespace(), registryName.getPath());
        }

        return isOxygen;
    }

    public void tick() {
        if (level == null) {
            return;
        }

        // Client-side animation handling
        if (level.isClientSide) {
            // Client just needs to know if it should animate
            // The isActive() method will check energy/oxygen values
            return;
        }

        // Server-side logic
        tickCounter++;

        // Debug logging every 20 ticks (once per second)
        if (tickCounter % 20 == 0) {
            AdAstraMekanized.LOGGER.debug("OxygenDistributor at {}: Energy={}/{}, Oxygen={}/{}, Active={}, OxygenatedBlocks={}",
                worldPosition, energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored(),
                oxygenTank.getStored(), oxygenTank.getCapacity(), isActive, oxygenatedBlocks.size());
        }

        // Check if we can function - only if manually activated and has resources
        boolean canFunction = isActive && energyStorage.getEnergyStored() >= ENERGY_PER_DISTRIBUTION && !oxygenTank.isEmpty();
        boolean wasActive = isActive;

        if (canFunction) {
            // Distribute oxygen and consume resources at intervals (every 100 ticks)
            if (tickCounter >= DISTRIBUTION_INTERVAL) {
                tickCounter = 0;
                // Consume energy only when distributing
                energyStorage.extractEnergy(ENERGY_PER_DISTRIBUTION, false);
                distributeOxygen();
            }
        } else if (isActive && !canFunction) {
            // Machine is on but lacks resources - turn it off automatically
            isActive = false;
            clearOxygenatedBlocks();
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        // Sync to client if state changed
        if (wasActive != isActive) {
            setChanged();
            // Force block update to sync to client
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    protected void distributeOxygen() {
        clearOxygenatedBlocks();

        // Simple flood fill to find air blocks
        Set<BlockPos> toCheck = new HashSet<>();
        toCheck.add(worldPosition.above());

        int maxBlocks = 50; // Reduced from 500 - more reasonable oxygen consumption
        while (!toCheck.isEmpty() && oxygenatedBlocks.size() < maxBlocks && oxygenTank.getStored() > 0) {
            BlockPos current = toCheck.iterator().next();
            toCheck.remove(current);

            if (oxygenatedBlocks.contains(current)) continue;
            if (!isWithinRange(current)) continue;

            BlockState state = level.getBlockState(current);
            if (state.isAir()) {
                oxygenatedBlocks.add(current);

                for (Direction dir : Direction.values()) {
                    toCheck.add(current.relative(dir));
                }
            }
        }

        // Register oxygenated blocks with OxygenManager
        if (!oxygenatedBlocks.isEmpty()) {
            OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, true);

            // Consume oxygen at reduced rate (0.25 mB per block)
            long oxygenToConsume = Math.round(oxygenatedBlocks.size() * OXYGEN_PER_BLOCK);
            if (oxygenTank.getStored() >= oxygenToConsume) {
                oxygenTank.shrinkStack(oxygenToConsume, Action.EXECUTE);
                AdAstraMekanized.LOGGER.debug("Distributed oxygen to {} blocks, consumed {} mB",
                    oxygenatedBlocks.size(), oxygenToConsume);
            } else {
                // Not enough oxygen, clear distribution
                clearOxygenatedBlocks();
            }
        }
    }
    
    protected void clearOxygenatedBlocks() {
        if (!oxygenatedBlocks.isEmpty() && level != null) {
            // Clear oxygen from OxygenManager
            OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, false);
            oxygenatedBlocks.clear();
        }
    }
    
    private boolean isWithinRange(BlockPos pos) {
        return pos.distSqr(worldPosition) <= 16 * 16;
    }

    // Capability provider method - now simplified since we use proper registration
    public <T> T getCapability(BlockCapability<T, Direction> cap, @Nullable Direction side) {
        // This method is now mostly for debugging and fallback
        // The actual capabilities are registered through OxygenDistributorCapabilityProvider
        AdAstraMekanized.LOGGER.debug("Direct getCapability called for {} with side {} (should use registered capabilities)", cap, side);

        // Standard Forge energy capability
        if (cap == Capabilities.EnergyStorage.BLOCK) {
            return (T) energyStorage;
        }

        // Try Mekanism chemical capability
        try {
            Class<?> mekCapabilities = Class.forName("mekanism.common.capabilities.Capabilities");
            java.lang.reflect.Field chemicalField = mekCapabilities.getField("CHEMICAL");
            Object chemicalCapObject = chemicalField.get(null);

            // Get the block() method to get the BlockCapability
            java.lang.reflect.Method blockMethod = chemicalCapObject.getClass().getMethod("block");
            Object blockCap = blockMethod.invoke(chemicalCapObject);

            if (cap.equals(blockCap)) {
                AdAstraMekanized.LOGGER.debug("Returning chemical handler for Mekanism");
                return (T) chemicalHandler;
            }
        } catch (Exception e) {
            // Mekanism not available
        }

        return null;
    }

    // Public getters for capability registration
    public IChemicalHandler getChemicalHandler() {
        return chemicalHandler;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    // Specific getter for JADE/Mekanism energy display
    public IStrictEnergyHandler getStrictEnergyHandler() {
        return energyStorage;
    }

    public IChemicalTank getOxygenTank() {
        return oxygenTank;
    }

    // Chemical handler implementation
    private class ChemicalHandler implements IChemicalHandler {
        @Override
        public int getChemicalTanks() {
            AdAstraMekanized.LOGGER.debug("getChemicalTanks called");
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
            // Accept oxygen - delegate to the tank's validation
            return tank == 0 && oxygenTank.isValid(stack);
        }

        @Override
        public @NotNull ChemicalStack insertChemical(int tank, @NotNull ChemicalStack stack, @NotNull Action action) {
            if (tank != 0 || !isValid(tank, stack)) {
                return stack;
            }
            ChemicalStack remainder = oxygenTank.insert(stack, action, AutomationType.EXTERNAL);
            if (action.execute() && remainder.getAmount() < stack.getAmount()) {
                // Log when oxygen is actually inserted
                AdAstraMekanized.LOGGER.debug("Inserted {} mB of oxygen, tank now has {} mB",
                    stack.getAmount() - remainder.getAmount(), oxygenTank.getStored());
            }
            return remainder;
        }

        // Allow single-tank insertion without index
        public @NotNull ChemicalStack insertChemical(@NotNull ChemicalStack stack, @NotNull Action action) {
            return insertChemical(0, stack, action);
        }

        @Override
        public @NotNull ChemicalStack extractChemical(int tank, long amount, @NotNull Action action) {
            return ChemicalStack.EMPTY; // Don't allow extraction
        }
    }
    
    // Energy storage implementation - implements both Forge and Mekanism interfaces for compatibility
    private class EnergyStorage implements IEnergyStorage, IStrictEnergyHandler {
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
            return maxExtract > 0;
        }

        @Override
        public boolean canReceive() {
            return maxReceive > 0;
        }

        // Mekanism IStrictEnergyHandler implementation for JADE compatibility
        @Override
        public int getEnergyContainerCount() {
            return 1; // Single energy container
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
            if (container != 0) {
                return 0;
            }
            int toReceive = (int) Math.min(amount, Math.min(maxReceive, capacity - energy));
            if (action.execute()) {
                energy += toReceive;
                setChanged();
            }
            return toReceive;
        }

        @Override
        public long extractEnergy(int container, long amount, @NotNull Action action) {
            if (container != 0) {
                return 0;
            }
            int toExtract = (int) Math.min(amount, Math.min(maxExtract, energy));
            if (action.execute()) {
                energy -= toExtract;
                setChanged();
            }
            return toExtract;
        }
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("energy", energyStorage.energy);
        tag.put("oxygenTank", oxygenTank.serializeNBT(provider));
        tag.putBoolean("isActive", isActive);
        tag.putBoolean("oxygenBlockVisibility", oxygenBlockVisibility);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        energyStorage.energy = tag.getInt("energy");
        oxygenTank.deserializeNBT(provider, tag.getCompound("oxygenTank"));
        isActive = tag.getBoolean("isActive");
        oxygenBlockVisibility = tag.getBoolean("oxygenBlockVisibility");
    }

    // Client-server synchronization
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("energy", energyStorage.energy);
        tag.putLong("oxygen", oxygenTank.getStored());
        tag.putBoolean("isActive", isActive);
        tag.putBoolean("oxygenBlockVisibility", oxygenBlockVisibility);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        energyStorage.energy = tag.getInt("energy");
        // For client-side, we just need to know if there's oxygen, not the exact amount
        if (tag.getLong("oxygen") > 0) {
            // Set some oxygen in the tank for animation purposes
            try {
                // Use reflection to set the stored amount directly for client display
                java.lang.reflect.Field storedField = oxygenTank.getClass().getDeclaredField("stored");
                storedField.setAccessible(true);
                storedField.set(oxygenTank, tag.getLong("oxygen"));
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.debug("Could not set client-side oxygen amount: {}", e.getMessage());
            }
        }
        isActive = tag.getBoolean("isActive");
        oxygenBlockVisibility = tag.getBoolean("oxygenBlockVisibility");
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
        handleUpdateTag(pkt.getTag(), provider);
    }
    
    public boolean isActive() {
        // Return the actual active state
        return isActive;
    }

    public void setActive(boolean active) {
        // Only allow activation if we have resources
        if (active && (energyStorage.getEnergyStored() < ENERGY_PER_DISTRIBUTION || oxygenTank.isEmpty())) {
            // Cannot activate without resources
            return;
        }

        this.isActive = active;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        if (!active) {
            clearOxygenatedBlocks();
        }
    }

    public void setOxygenBlockVisibility(boolean visible) {
        this.oxygenBlockVisibility = visible;
        setChanged();

        // Send visualization packet to nearby players
        if (level != null && !level.isClientSide) {
            sendVisualizationUpdate(visible);
        }
    }

    protected void sendVisualizationUpdate(boolean visible) {
        if (level == null || level.isClientSide) return;

        // Send packet to all players tracking this chunk
        List<BlockPos> zones = visible ? new ArrayList<>(oxygenatedBlocks) : List.of();
        var packet = new com.hecookin.adastramekanized.common.network.OxygenVisualizationPacket(visible, zones);

        // Get all players in range (64 blocks)
        level.players().stream()
            .filter(player -> player instanceof net.minecraft.server.level.ServerPlayer)
            .map(player -> (net.minecraft.server.level.ServerPlayer) player)
            .filter(player -> player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 64 * 64)
            .forEach(player -> {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
            });
    }

    // Methods for debug command - renamed to avoid conflict with IStrictEnergyHandler
    public long getEnergyForDebug(int container) {
        return container == 0 ? energyStorage.energy : 0;
    }

    public long getMaxEnergyForDebug(int container) {
        return container == 0 ? energyStorage.capacity : 0;
    }

    public int getOxygenatedBlockCount() {
        return oxygenatedBlocks.size();
    }

    // Protected getters for subclasses
    protected Set<BlockPos> getOxygenatedBlocks() {
        return oxygenatedBlocks;
    }

    // Debug methods
    public ChemicalStack getChemicalInTank(int tank) {
        return chemicalHandler.getChemicalInTank(tank);
    }

    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        return chemicalHandler.insertChemical(tank, stack, action);
    }

    public int receiveEnergy(int amount, boolean simulate) {
        return energyStorage.receiveEnergy(amount, simulate);
    }

    // Getter for animation
    public float getAnimationTick() {
        return isActive ? (System.currentTimeMillis() % 3600) / 10f : 0;
    }

    @Override
    public void setRemoved() {
        // Clean up oxygen when removed
        clearOxygenatedBlocks();
        super.setRemoved();
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.adastramekanized.oxygen_distributor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new OxygenDistributorMenu(containerId, playerInventory, this);
    }
}