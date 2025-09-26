package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.utils.FloodFillUtil;
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
import mekanism.api.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Mekanism-compatible oxygen distributor tile entity.
 * This distributes oxygen from pressurized tubes to an enclosed area.
 */
public class TileEntityOxygenDistributor extends BlockEntity implements
        IMekanismChemicalHandler,
        IMekanismStrictEnergyHandler,
        IContentsListener,
        MenuProvider {

    // Constants
    private static final int MAX_OXYGEN_RANGE = 16;
    private static final int MAX_OXYGEN_BLOCKS = MAX_OXYGEN_RANGE * MAX_OXYGEN_RANGE * 2; // ~500 blocks
    private static final int OXYGEN_DISTRIBUTION_INTERVAL = 100; // 5 seconds
    private static final long ENERGY_PER_TICK = 20L; // Base energy usage (will scale with blocks)
    private static final long ENERGY_CAPACITY = 100_000L; // 100k FE storage
    private static final long OXYGEN_CAPACITY = 4_000L; // 4000 mB oxygen storage
    private static final long OXYGEN_PER_BLOCK = 1L; // mB per block per distribution cycle

    // State
    private boolean active = false;
    private int tickCounter = 0;

    // Animation
    public float yRot = 0.0f;
    public float lastYRot = 0.0f;

    // Storage
    private final IChemicalTank oxygenTank;
    private final MekanismEnergyContainer energyContainer;

    // Oxygenated area tracking
    private final Set<BlockPos> oxygenatedBlocks = new HashSet<>();
    private int oxygenatedBlockCount = 0;
    private boolean visualizeOxygen = false;

    // Side configuration (true = input enabled)
    private final Map<Direction, SideConfig> sideConfig = new EnumMap<>(Direction.class);

    // Upgrades
    private boolean hasMufflingUpgrade = false;
    private boolean hasAnchorUpgrade = false;

    // Redstone control
    private RedstoneControl redstoneMode = RedstoneControl.IGNORED;

    // Security
    private UUID ownerUUID = null;
    private SecurityMode securityMode = SecurityMode.PUBLIC;

    // Sound
    private long lastSoundTime = 0;
    private static final int SOUND_INTERVAL = 60; // 3 seconds

    public TileEntityOxygenDistributor(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(), pos, blockState);

        // Initialize oxygen tank - accepts oxygen only
        this.oxygenTank = BasicChemicalTank.input(
            OXYGEN_CAPACITY,
            chemical -> true, // Accept all chemicals, filter on insertion
            this
        );

        // Initialize energy container
        this.energyContainer = new MekanismEnergyContainer(ENERGY_CAPACITY, ENERGY_CAPACITY, ENERGY_CAPACITY, this);

        // Initialize side configuration - all sides input by default
        for (Direction dir : Direction.values()) {
            sideConfig.put(dir, new SideConfig(true, true)); // energy input, chemical input
        }
    }

    // === Main Logic ===

    public static void tickServer(Level level, BlockPos pos, BlockState state, TileEntityOxygenDistributor entity) {
        entity.tickCounter++;

        // Check redstone control
        if (!entity.canOperate()) {
            if (entity.active) {
                entity.clearOxygenatedBlocks();
                entity.active = false;
                entity.setChanged();
            }
            return;
        }

        boolean canFunction = entity.canFunction();
        boolean wasActive = entity.active;

        if (canFunction) {
            // Calculate energy usage based on oxygenated blocks
            long energyUsage = entity.calculateEnergyPerTick();

            // Consume energy
            long extracted = entity.energyContainer.extract(energyUsage, Action.EXECUTE, AutomationType.INTERNAL);

            if (extracted >= energyUsage) {
                // Distribute oxygen at intervals
                if (entity.tickCounter >= OXYGEN_DISTRIBUTION_INTERVAL) {
                    entity.tickCounter = 0;
                    entity.distributeOxygen();
                }

                entity.active = true;

                // Play sound periodically if not muffled
                if (!entity.hasMufflingUpgrade && level.getGameTime() - entity.lastSoundTime > SOUND_INTERVAL) {
                    entity.lastSoundTime = level.getGameTime();
                    level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.3F, 1.0F);
                }
            } else {
                entity.active = false;
            }
        } else {
            // Clear oxygen when not functioning
            if (wasActive) {
                entity.clearOxygenatedBlocks();
                entity.active = false;
            }
        }

        if (entity.active != wasActive) {
            entity.setChanged();
        }
    }

    public static void tickClient(Level level, BlockPos pos, BlockState state, TileEntityOxygenDistributor entity) {
        // Client-side animation
        entity.lastYRot = entity.yRot;

        if (entity.active) {
            entity.yRot += 2f; // Rotation speed
            entity.yRot = entity.yRot % 360f;
        }
    }

    private boolean canOperate() {
        if (redstoneMode == RedstoneControl.IGNORED) {
            return true;
        }

        boolean powered = level != null && level.hasNeighborSignal(worldPosition);

        return switch (redstoneMode) {
            case HIGH -> powered;
            case LOW -> !powered;
            default -> true;
        };
    }

    private boolean canFunction() {
        // Check energy
        if (energyContainer.getEnergy() < calculateEnergyPerTick()) {
            return false;
        }

        // Check oxygen availability
        if (!oxygenTank.isEmpty()) {
            return oxygenTank.getStored() > 0;
        }

        // If no oxygen in tank, check if we're in a breathable atmosphere (free oxygen)
        return OxygenManager.getInstance().hasOxygen(level);
    }

    private long calculateEnergyPerTick() {
        // Scale energy usage with oxygenated blocks (like Ad Astra)
        // Minimum 20 FE/t, +1 FE/t per 50 blocks
        return Math.max(ENERGY_PER_TICK, oxygenatedBlocks.size() / 50);
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
                long oxygenToConsume = Math.min(oxygenatedBlocks.size() * OXYGEN_PER_BLOCK, oxygenTank.getStored());
                oxygenTank.extract(oxygenToConsume, Action.EXECUTE, AutomationType.INTERNAL);
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

    private boolean isOxygenChemical(ChemicalStack stack) {
        if (stack.isEmpty()) return false;

        // Check if it's Mekanism's oxygen chemical
        String chemicalName = stack.getChemical().toString().toLowerCase();
        return chemicalName.contains("oxygen");
    }

    // === IMekanismChemicalHandler Implementation ===

    @Override
    public List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        if (side == null || sideConfig.get(side).acceptsChemical) {
            return List.of(oxygenTank);
        }
        return List.of();
    }

    @Override
    public ChemicalStack insertChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        if (side != null && !sideConfig.get(side).acceptsChemical) {
            return stack;
        }
        return insertChemical(0, stack, action);
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        if (tank == 0 && isOxygenChemical(stack)) {
            return oxygenTank.insert(stack, action, AutomationType.EXTERNAL);
        }
        return stack;
    }

    @Override
    public ChemicalStack extractChemical(long amount, @Nullable Direction side, Action action) {
        // No extraction allowed - distribution only
        return ChemicalStack.EMPTY;
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        // No extraction allowed - distribution only
        return ChemicalStack.EMPTY;
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        return tank == 0 ? oxygenTank.getStack() : ChemicalStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, ChemicalStack stack) {
        if (tank == 0 && isOxygenChemical(stack)) {
            oxygenTank.setStack(stack);
        }
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        return tank == 0 ? oxygenTank.getCapacity() : 0;
    }

    public boolean isChemicalValid(int tank, ChemicalStack stack) {
        return tank == 0 && isOxygenChemical(stack);
    }

    @Override
    public int getChemicalTanks() {
        return 1;
    }

    // === IMekanismStrictEnergyHandler Implementation ===

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        if (side == null || sideConfig.get(side).acceptsEnergy) {
            return List.of(energyContainer);
        }
        return List.of();
    }

    @Override
    public long insertEnergy(long toInsert, @Nullable Direction side, Action action) {
        if (side != null && !sideConfig.get(side).acceptsEnergy) {
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

    // === Configuration Management ===

    public CompoundTag getConfigurationData() {
        CompoundTag data = new CompoundTag();

        // Save side configuration
        CompoundTag sidesTag = new CompoundTag();
        for (Map.Entry<Direction, SideConfig> entry : sideConfig.entrySet()) {
            CompoundTag sideTag = new CompoundTag();
            sideTag.putBoolean("energy", entry.getValue().acceptsEnergy);
            sideTag.putBoolean("chemical", entry.getValue().acceptsChemical);
            sidesTag.put(entry.getKey().getName(), sideTag);
        }
        data.put("SideConfig", sidesTag);

        // Save redstone mode
        data.putString("RedstoneMode", redstoneMode.name());

        return data;
    }

    public void setConfigurationData(CompoundTag data) {
        // Load side configuration
        if (data.contains("SideConfig")) {
            CompoundTag sidesTag = data.getCompound("SideConfig");
            for (Direction dir : Direction.values()) {
                if (sidesTag.contains(dir.getName())) {
                    CompoundTag sideTag = sidesTag.getCompound(dir.getName());
                    sideConfig.put(dir, new SideConfig(
                        sideTag.getBoolean("energy"),
                        sideTag.getBoolean("chemical")
                    ));
                }
            }
        }

        // Load redstone mode
        if (data.contains("RedstoneMode")) {
            try {
                redstoneMode = RedstoneControl.valueOf(data.getString("RedstoneMode"));
            } catch (IllegalArgumentException e) {
                redstoneMode = RedstoneControl.IGNORED;
            }
        }

        setChanged();
    }

    // === MenuProvider Implementation ===

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.adastramekanized.oxygen_distributor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        // TODO: Create GUI container
        return null;
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

    public void toggleSide(Direction side, boolean energy, boolean chemical) {
        sideConfig.put(side, new SideConfig(energy, chemical));
        setChanged();
    }

    public SideConfig getSideConfig(Direction side) {
        return sideConfig.get(side);
    }

    public RedstoneControl getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneControl mode) {
        this.redstoneMode = mode;
        setChanged();
    }

    public boolean hasMufflingUpgrade() {
        return hasMufflingUpgrade;
    }

    public void setMufflingUpgrade(boolean muffling) {
        this.hasMufflingUpgrade = muffling;
        setChanged();
    }

    public boolean hasAnchorUpgrade() {
        return hasAnchorUpgrade;
    }

    public void setAnchorUpgrade(boolean anchor) {
        this.hasAnchorUpgrade = anchor;
        setChanged();
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

        tag.putBoolean("Active", active);
        tag.putFloat("YRot", yRot);
        tag.putBoolean("VisualizeOxygen", visualizeOxygen);
        tag.putInt("OxygenatedBlockCount", oxygenatedBlockCount);

        // Save oxygen tank
        tag.put("OxygenTank", oxygenTank.serializeNBT(provider));

        // Save energy
        tag.putLong("Energy", energyContainer.getEnergy());

        // Save side configuration
        CompoundTag sidesTag = new CompoundTag();
        for (Map.Entry<Direction, SideConfig> entry : sideConfig.entrySet()) {
            CompoundTag sideTag = new CompoundTag();
            sideTag.putBoolean("energy", entry.getValue().acceptsEnergy);
            sideTag.putBoolean("chemical", entry.getValue().acceptsChemical);
            sidesTag.put(entry.getKey().getName(), sideTag);
        }
        tag.put("SideConfig", sidesTag);

        // Save upgrades
        tag.putBoolean("MufflingUpgrade", hasMufflingUpgrade);
        tag.putBoolean("AnchorUpgrade", hasAnchorUpgrade);

        // Save redstone mode
        tag.putString("RedstoneMode", redstoneMode.name());

        // Save security
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
            tag.putString("SecurityMode", securityMode.name());
        }

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

        active = tag.getBoolean("Active");
        yRot = tag.getFloat("YRot");
        lastYRot = yRot;
        visualizeOxygen = tag.getBoolean("VisualizeOxygen");
        oxygenatedBlockCount = tag.getInt("OxygenatedBlockCount");

        // Load oxygen tank
        oxygenTank.deserializeNBT(provider, tag.getCompound("OxygenTank"));

        // Load energy
        energyContainer.setEnergy(tag.getLong("Energy"));

        // Load side configuration
        if (tag.contains("SideConfig")) {
            CompoundTag sidesTag = tag.getCompound("SideConfig");
            for (Direction dir : Direction.values()) {
                if (sidesTag.contains(dir.getName())) {
                    CompoundTag sideTag = sidesTag.getCompound(dir.getName());
                    sideConfig.put(dir, new SideConfig(
                        sideTag.getBoolean("energy"),
                        sideTag.getBoolean("chemical")
                    ));
                }
            }
        }

        // Load upgrades
        hasMufflingUpgrade = tag.getBoolean("MufflingUpgrade");
        hasAnchorUpgrade = tag.getBoolean("AnchorUpgrade");

        // Load redstone mode
        if (tag.contains("RedstoneMode")) {
            try {
                redstoneMode = RedstoneControl.valueOf(tag.getString("RedstoneMode"));
            } catch (IllegalArgumentException e) {
                redstoneMode = RedstoneControl.IGNORED;
            }
        }

        // Load security
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
            try {
                securityMode = SecurityMode.valueOf(tag.getString("SecurityMode"));
            } catch (IllegalArgumentException e) {
                securityMode = SecurityMode.PUBLIC;
            }
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

    // === Inner Classes ===

    public static class SideConfig {
        public final boolean acceptsEnergy;
        public final boolean acceptsChemical;

        public SideConfig(boolean acceptsEnergy, boolean acceptsChemical) {
            this.acceptsEnergy = acceptsEnergy;
            this.acceptsChemical = acceptsChemical;
        }
    }

    public enum RedstoneControl {
        IGNORED,
        HIGH,
        LOW
    }

    public enum SecurityMode {
        PUBLIC,
        PRIVATE,
        TRUSTED
    }

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

        @Override
        public boolean isEmpty() {
            return stored == 0;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putLong("stored", stored);
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
            stored = tag.getLong("stored");
        }

        @Override
        public void onContentsChanged() {
            listener.onContentsChanged();
        }
    }
}