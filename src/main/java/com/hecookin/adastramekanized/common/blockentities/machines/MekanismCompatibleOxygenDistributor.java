package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.utils.FloodFillUtil;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.functions.ConstantPredicates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Oxygen distributor with full Mekanism compatibility.
 * Implements Mekanism's chemical and energy interfaces for proper cable/tube connections.
 */
public class MekanismCompatibleOxygenDistributor extends BlockEntity implements IMekanismChemicalHandler, IMekanismStrictEnergyHandler, IContentsListener {

    private static final int MAX_OXYGEN_RANGE = 16;
    private static final int MAX_OXYGEN_BLOCKS = MAX_OXYGEN_RANGE * MAX_OXYGEN_RANGE * 2;
    private static final int OXYGEN_DISTRIBUTION_INTERVAL = 100; // ticks (5 seconds)
    private static final long ENERGY_PER_TICK = 20L;
    private static final long ENERGY_CAPACITY = 100_000L;
    private static final long OXYGEN_CAPACITY = 10_000L;
    private static final long OXYGEN_PER_BLOCK = 1L; // mB per block per distribution cycle

    // Rotation for animation
    public float yRot = 0.0f;
    public float lastYRot = 0.0f;
    private int tickCounter = 0;
    private boolean active = false;

    // Storage
    private final IChemicalTank oxygenTank;
    private final MekanismEnergyContainer energyContainer;

    // Oxygenated area tracking
    private final Set<BlockPos> oxygenatedBlocks = new HashSet<>();
    private double accumulatedOxygen = 0;
    private int oxygenatedBlockCount = 0;
    private boolean visualizeOxygen = false;

    // Side configuration
    private final boolean[] sideConfig = new boolean[6]; // true = input enabled

    public MekanismCompatibleOxygenDistributor(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(), pos, blockState);

        // Create oxygen tank that only accepts oxygen
        this.oxygenTank = BasicChemicalTank.input(
            OXYGEN_CAPACITY,
            chemical -> true, // Allow any chemical for now, we'll filter on insertion
            this
        );

        // Create energy container
        this.energyContainer = new MekanismEnergyContainer(ENERGY_CAPACITY, ENERGY_CAPACITY, ENERGY_CAPACITY, this);

        // Enable all sides by default
        for (int i = 0; i < 6; i++) {
            sideConfig[i] = true;
        }
    }

    private boolean isOxygen(ChemicalStack stack) {
        if (stack.isEmpty()) return false;

        // Use reflection to check if it's oxygen
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager.hasChemicalIntegration()) {
            try {
                // Get MekanismChemicals class and OXYGEN field
                Class<?> chemicalsClass = Class.forName("mekanism.common.registries.MekanismChemicals");
                java.lang.reflect.Field oxygenField = chemicalsClass.getField("OXYGEN");
                Object oxygenDeferredChemical = oxygenField.get(null);

                // Get the actual chemical from the deferred holder
                java.lang.reflect.Method getMethod = oxygenDeferredChemical.getClass().getMethod("get");
                Object oxygenChemical = getMethod.invoke(oxygenDeferredChemical);

                return stack.getChemical() == oxygenChemical;
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.debug("Failed to check if chemical is oxygen: {}", e.getMessage());
            }
        }

        // Fallback: check by name
        String chemicalName = stack.getChemical().toString();
        return chemicalName.toLowerCase().contains("oxygen");
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, MekanismCompatibleOxygenDistributor entity) {
        entity.tickCounter++;

        boolean canFunction = entity.canFunction();
        boolean wasActive = entity.active;

        if (canFunction) {
            // Consume energy every tick when running
            entity.energyContainer.extract(ENERGY_PER_TICK, Action.EXECUTE, AutomationType.INTERNAL);

            // Distribute oxygen at intervals
            if (entity.tickCounter >= OXYGEN_DISTRIBUTION_INTERVAL) {
                entity.tickCounter = 0;
                entity.distributeOxygen();
            }

            entity.active = true;
        } else {
            // Clear oxygen when not functioning
            if (wasActive) {
                entity.clearOxygenatedBlocks();
                entity.active = false;
            }
        }

        entity.setChanged();
    }

    public static void tickClient(Level level, BlockPos pos, BlockState state, MekanismCompatibleOxygenDistributor entity) {
        // Client-side animation
        entity.lastYRot = entity.yRot;

        if (entity.active) {
            entity.yRot += 2f; // Rotation speed
            entity.yRot = entity.yRot % 360f;
        }
    }

    private boolean canFunction() {
        // Check energy
        if (energyContainer.getEnergy() < ENERGY_PER_TICK) {
            return false;
        }

        // Check oxygen availability
        if (!oxygenTank.isEmpty()) {
            return oxygenTank.getStored() > 0;
        }

        // If no oxygen in tank, check if we're in a breathable atmosphere (free oxygen)
        return OxygenManager.getInstance().hasOxygen(level);
    }

    private void distributeOxygen() {
        // Clear previous oxygen distribution
        clearOxygenatedBlocks();

        // Find enclosed area using flood fill
        Set<BlockPos> newOxygenatedBlocks = FloodFillUtil.findEnclosedArea(
            level,
            worldPosition.above(), // Start from above the distributor
            MAX_OXYGEN_BLOCKS
        );

        // If we found an enclosed area, oxygenate it
        if (!newOxygenatedBlocks.isEmpty()) {
            this.oxygenatedBlocks.clear();
            this.oxygenatedBlocks.addAll(newOxygenatedBlocks);

            // Apply oxygen to the area
            OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, true);

            // Consume oxygen from tank
            if (!oxygenTank.isEmpty()) {
                // Calculate oxygen consumption
                long oxygenToConsume = Math.min(oxygenatedBlocks.size() * OXYGEN_PER_BLOCK, 100);
                accumulatedOxygen += oxygenToConsume / 100.0; // Convert to mB

                // Extract whole mB amounts
                long oxygenToExtract = (long) accumulatedOxygen;
                if (oxygenToExtract > 0) {
                    oxygenTank.extract(oxygenToExtract, Action.EXECUTE, AutomationType.INTERNAL);
                    accumulatedOxygen -= oxygenToExtract;
                }
            }

            oxygenatedBlockCount = oxygenatedBlocks.size();
            AdAstraMekanized.LOGGER.debug("Oxygen distributor at {} oxygenating {} blocks", worldPosition, oxygenatedBlockCount);
        } else {
            oxygenatedBlockCount = 0;
        }
    }

    private void clearOxygenatedBlocks() {
        if (!oxygenatedBlocks.isEmpty()) {
            OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, false);
            oxygenatedBlocks.clear();
            oxygenatedBlockCount = 0;
        }
    }

    // === IMekanismChemicalHandler Implementation ===

    @Override
    public List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        if (side == null || sideConfig[side.ordinal()]) {
            return List.of(oxygenTank);
        }
        return List.of();
    }

    @Override
    public ChemicalStack insertChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        if (side != null && !sideConfig[side.ordinal()]) {
            return stack;
        }
        return insertChemical(0, stack, action);
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        if (tank == 0) {
            return oxygenTank.insert(stack, action, AutomationType.EXTERNAL);
        }
        return stack;
    }

    @Override
    public ChemicalStack extractChemical(long amount, @Nullable Direction side, Action action) {
        // No extraction allowed
        return ChemicalStack.EMPTY;
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        // No extraction allowed
        return ChemicalStack.EMPTY;
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        return tank == 0 ? oxygenTank.getStack() : ChemicalStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, ChemicalStack stack) {
        if (tank == 0) {
            oxygenTank.setStack(stack);
        }
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        return tank == 0 ? oxygenTank.getCapacity() : 0;
    }

    public boolean isChemicalValid(int tank, ChemicalStack stack) {
        return tank == 0 && isOxygen(stack);
    }

    @Override
    public int getChemicalTanks() {
        return 1;
    }

    // === IMekanismStrictEnergyHandler Implementation ===

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        if (side == null || sideConfig[side.ordinal()]) {
            return List.of(energyContainer);
        }
        return List.of();
    }

    @Override
    public long insertEnergy(long toInsert, @Nullable Direction side, Action action) {
        if (side != null && !sideConfig[side.ordinal()]) {
            return toInsert;
        }
        return energyContainer.insert(toInsert, action, AutomationType.EXTERNAL);
    }

    @Override
    public long extractEnergy(long toExtract, @Nullable Direction side, Action action) {
        // No extraction allowed
        return 0;
    }

    @Override
    public long insertEnergy(int container, long toInsert, Action action) {
        if (container == 0) {
            return energyContainer.insert(toInsert, action, AutomationType.EXTERNAL);
        }
        return toInsert;
    }

    @Override
    public long extractEnergy(int container, long toExtract, Action action) {
        // No extraction allowed
        return 0;
    }

    @Override
    public long getEnergy(int container) {
        return container == 0 ? energyContainer.getEnergy() : 0;
    }

    @Override
    public void setEnergy(int container, long energy) {
        if (container == 0) {
            energyContainer.setEnergy(energy);
        }
    }

    @Override
    public long getMaxEnergy(int container) {
        return container == 0 ? energyContainer.getMaxEnergy() : 0;
    }

    @Override
    public long getNeededEnergy(int container) {
        return container == 0 ? energyContainer.getNeeded() : 0;
    }

    @Override
    public int getEnergyContainerCount() {
        return 1;
    }

    // === IContentsListener Implementation ===

    @Override
    public void onContentsChanged() {
        setChanged();
    }

    // === Getters ===

    public boolean isActive() {
        return active;
    }

    public float yRot() {
        return yRot;
    }

    public float lastYRot() {
        return lastYRot;
    }

    public int getOxygenatedBlockCount() {
        return oxygenatedBlockCount;
    }

    public boolean isVisualizingOxygen() {
        return visualizeOxygen;
    }

    public void setVisualizeOxygen(boolean visualize) {
        this.visualizeOxygen = visualize;
        setChanged();
    }

    public void toggleSide(Direction side) {
        sideConfig[side.ordinal()] = !sideConfig[side.ordinal()];
        setChanged();
    }

    public boolean isSideEnabled(Direction side) {
        return sideConfig[side.ordinal()];
    }

    // === Persistence ===

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            // Restore oxygenated blocks on load
            if (!oxygenatedBlocks.isEmpty()) {
                OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, true);
            }
        }
    }

    @Override
    public void setRemoved() {
        clearOxygenatedBlocks();
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putFloat("yRot", yRot);
        tag.putDouble("AccumulatedOxygen", accumulatedOxygen);
        tag.putBoolean("VisualizeOxygen", visualizeOxygen);
        tag.putBoolean("Active", active);
        tag.putInt("OxygenatedBlockCount", oxygenatedBlockCount);

        // Save oxygen tank
        tag.put("OxygenTank", oxygenTank.serializeNBT(provider));

        // Save energy
        tag.putLong("Energy", energyContainer.getEnergy());

        // Save side configuration
        byte sideBits = 0;
        for (int i = 0; i < 6; i++) {
            if (sideConfig[i]) {
                sideBits |= (1 << i);
            }
        }
        tag.putByte("SideConfig", sideBits);

        // Save oxygenated positions
        ListTag positionList = new ListTag();
        for (BlockPos pos : oxygenatedBlocks) {
            positionList.add(LongTag.valueOf(pos.asLong()));
        }
        tag.put("OxygenatedBlocks", positionList);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        yRot = tag.getFloat("yRot");
        lastYRot = yRot;
        accumulatedOxygen = tag.getDouble("AccumulatedOxygen");
        visualizeOxygen = tag.getBoolean("VisualizeOxygen");
        active = tag.getBoolean("Active");
        oxygenatedBlockCount = tag.getInt("OxygenatedBlockCount");

        // Load oxygen tank
        oxygenTank.deserializeNBT(provider, tag.getCompound("OxygenTank"));

        // Load energy
        energyContainer.setEnergy(tag.getLong("Energy"));

        // Load side configuration
        byte sideBits = tag.getByte("SideConfig");
        for (int i = 0; i < 6; i++) {
            sideConfig[i] = (sideBits & (1 << i)) != 0;
        }

        // Load oxygenated positions
        oxygenatedBlocks.clear();
        ListTag positionList = tag.getList("OxygenatedBlocks", Tag.TAG_LONG);
        for (Tag t : positionList) {
            if (t instanceof LongTag longTag) {
                oxygenatedBlocks.add(BlockPos.of(longTag.getAsLong()));
            }
        }
    }

    // === Energy Container Implementation ===

    private static class MekanismEnergyContainer implements IEnergyContainer, IContentsListener {
        private long stored;
        private final long maxEnergy;
        private final long maxInsert;
        private final long maxExtract;
        private final IContentsListener listener;

        public MekanismEnergyContainer(long maxEnergy, long maxInsert, long maxExtract, IContentsListener listener) {
            this.maxEnergy = maxEnergy;
            this.maxInsert = maxInsert;
            this.maxExtract = maxExtract;
            this.listener = listener;
        }

        @Override
        public long getEnergy() {
            return stored;
        }

        @Override
        public void setEnergy(long energy) {
            this.stored = Math.min(energy, maxEnergy);
            listener.onContentsChanged();
        }

        @Override
        public long getMaxEnergy() {
            return maxEnergy;
        }

        @Override
        public long getNeeded() {
            return maxEnergy - stored;
        }

        @Override
        public long insert(long amount, Action action, AutomationType automationType) {
            long toInsert = Math.min(amount, Math.min(maxInsert, getNeeded()));
            if (toInsert > 0 && action.execute()) {
                stored += toInsert;
                listener.onContentsChanged();
            }
            return amount - toInsert;
        }

        @Override
        public long extract(long amount, Action action, AutomationType automationType) {
            long toExtract = Math.min(amount, Math.min(maxExtract, stored));
            if (toExtract > 0 && action.execute()) {
                stored -= toExtract;
                listener.onContentsChanged();
            }
            return toExtract;
        }

        public long insertEnergy(long amount, Action action) {
            return insert(amount, action, AutomationType.EXTERNAL);
        }

        public long extractEnergy(long amount, Action action) {
            return extract(amount, action, AutomationType.EXTERNAL);
        }

        @Override
        public boolean isEmpty() {
            return stored == 0;
        }

        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putLong("stored", stored);
            return tag;
        }

        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
            stored = tag.getLong("stored");
        }

        @Override
        public void onContentsChanged() {
            // Handled by parent listener
        }
    }
}