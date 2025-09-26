package com.hecookin.adastramekanized.common.blockentities.machines;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
import com.hecookin.adastramekanized.common.blocks.base.SidedMachineBlock;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.utils.FloodFillUtil;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class OxygenDistributorBlockEntity extends BlockEntity {

    private static final int MAX_OXYGEN_RANGE = 16;
    private static final int MAX_OXYGEN_BLOCKS = MAX_OXYGEN_RANGE * MAX_OXYGEN_RANGE * 2;
    private static final int OXYGEN_DISTRIBUTION_INTERVAL = 100; // ticks (5 seconds)
    private static final int ENERGY_PER_TICK = 20;
    private static final int ENERGY_CAPACITY = 100000;
    private static final int OXYGEN_PER_BLOCK = 1; // mB per block per distribution cycle

    public float yRot = 0.0f;
    public float lastYRot = 0.0f;
    private boolean isActive = false;
    private int tickCounter = 0;

    // Storage
    private final EnergyStorage energyStorage;
    private Object chemicalTank; // Mekanism oxygen tank

    // Oxygenated area tracking
    private final Set<BlockPos> oxygenatedBlocks = new HashSet<>();
    private double accumulatedOxygen = 0;

    public OxygenDistributorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(), pos, blockState);
        this.energyStorage = new EnergyStorage(ENERGY_CAPACITY);

        // Initialize Mekanism oxygen tank if available
        ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
        if (manager.hasChemicalIntegration()) {
            this.chemicalTank = manager.getMekanismIntegration().createOxygenTank(10000); // 10 buckets
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OxygenDistributorBlockEntity entity) {
        if (level.isClientSide) {
            // Client-side animation
            entity.lastYRot = entity.yRot;
            entity.isActive = entity.canFunction();

            if (entity.isActive) {
                entity.yRot += 2f; // Rotation speed (reduced for smoother animation)
                // Keep rotation within 0-360 range, but handle wraparound properly
                entity.yRot = entity.yRot % 360f;
            }
        } else {
            // Server-side oxygen distribution
            entity.tickCounter++;

            boolean canFunction = entity.canFunction();

            if (canFunction) {
                // Consume energy every tick when running
                entity.energyStorage.extractEnergy(ENERGY_PER_TICK, false);

                // Distribute oxygen at intervals
                if (entity.tickCounter >= OXYGEN_DISTRIBUTION_INTERVAL) {
                    entity.tickCounter = 0;
                    entity.distributeOxygen(level, pos);
                }

                entity.isActive = true;
            } else {
                // Clear oxygen when not functioning
                if (entity.isActive) {
                    entity.clearOxygenatedBlocks(level);
                    entity.isActive = false;
                }
            }
        }
    }

    private boolean canFunction() {
        // Check energy
        if (energyStorage.getEnergyStored() < ENERGY_PER_TICK) {
            return false;
        }

        // Check oxygen availability if Mekanism is present
        if (chemicalTank != null) {
            ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
            if (manager.hasChemicalIntegration()) {
                long oxygenAmount = manager.getMekanismIntegration().getOxygenAmount(chemicalTank);
                return oxygenAmount > 0;
            }
        }

        // If no Mekanism, check if we're in a breathable atmosphere (free oxygen)
        return OxygenManager.getInstance().hasOxygen(level);
    }

    private void distributeOxygen(Level level, BlockPos centerPos) {
        // Clear previous oxygen distribution
        clearOxygenatedBlocks(level);

        // Find enclosed area using flood fill
        Set<BlockPos> newOxygenatedBlocks = FloodFillUtil.findEnclosedArea(
            level,
            centerPos.above(), // Start from above the distributor
            MAX_OXYGEN_BLOCKS
        );

        // If we found an enclosed area, oxygenate it
        if (!newOxygenatedBlocks.isEmpty()) {
            this.oxygenatedBlocks.clear();
            this.oxygenatedBlocks.addAll(newOxygenatedBlocks);

            // Apply oxygen to the area
            OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, true);

            // Consume oxygen from tank if Mekanism is present
            if (chemicalTank != null) {
                ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
                if (manager.hasChemicalIntegration()) {
                    // Calculate oxygen consumption
                    int oxygenToConsume = Math.min(oxygenatedBlocks.size() * OXYGEN_PER_BLOCK, 100);
                    accumulatedOxygen += oxygenToConsume / 100.0; // Convert to mB

                    // Extract whole mB amounts
                    int oxygenToExtract = (int) accumulatedOxygen;
                    if (oxygenToExtract > 0) {
                        manager.getMekanismIntegration().extractOxygen(chemicalTank, oxygenToExtract);
                        accumulatedOxygen -= oxygenToExtract;
                    }
                }
            }

            AdAstraMekanized.LOGGER.debug("Oxygen distributor at {} oxygenating {} blocks", centerPos, oxygenatedBlocks.size());
        } else {
            // Fallback to simple area distribution if no enclosed space
            Set<BlockPos> simpleArea = new HashSet<>();
            Queue<BlockPos> toProcess = new ArrayDeque<>();

            toProcess.add(centerPos);
            simpleArea.add(centerPos);

            while (!toProcess.isEmpty() && simpleArea.size() < MAX_OXYGEN_BLOCKS / 4) {
                BlockPos currentPos = toProcess.poll();

                for (Direction direction : Direction.values()) {
                    BlockPos neighborPos = currentPos.relative(direction);

                    if (simpleArea.contains(neighborPos) ||
                        neighborPos.distManhattan(centerPos) > MAX_OXYGEN_RANGE / 2) {
                        continue;
                    }

                    BlockState neighborState = level.getBlockState(neighborPos);
                    if (neighborState.isAir()) {
                        simpleArea.add(neighborPos);
                        toProcess.add(neighborPos);
                    }
                }
            }

            if (!simpleArea.isEmpty()) {
                this.oxygenatedBlocks.clear();
                this.oxygenatedBlocks.addAll(simpleArea);
                OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, true);
            }
        }
    }

    private void clearOxygenatedBlocks(Level level) {
        if (!oxygenatedBlocks.isEmpty()) {
            OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, false);
            oxygenatedBlocks.clear();
        }
    }

    public float yRot() {
        return yRot;
    }

    public float lastYRot() {
        return lastYRot;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            // Restore oxygenated blocks on load
            if (!oxygenatedBlocks.isEmpty()) {
                OxygenManager.getInstance().setOxygen(level, oxygenatedBlocks, true);
            }
            // Register capabilities for Mekanism integration
            registerCapabilities();
        }
    }

    @Override
    public void setRemoved() {
        clearOxygenatedBlocks(level);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("yRot", yRot);
        tag.putBoolean("isActive", isActive);
        tag.put("Energy", energyStorage.serializeNBT(registries));
        tag.putDouble("AccumulatedOxygen", accumulatedOxygen);

        // Save oxygenated positions
        long[] positions = oxygenatedBlocks.stream()
            .mapToLong(BlockPos::asLong)
            .toArray();
        tag.putLongArray("OxygenatedBlocks", positions);

        // Save chemical tank if available
        if (chemicalTank != null) {
            ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
            if (manager.hasChemicalIntegration()) {
                CompoundTag tankTag = manager.getMekanismIntegration().serializeChemicalTank(chemicalTank);
                if (tankTag != null) {
                    tag.put("ChemicalTank", tankTag);
                }
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        yRot = tag.getFloat("yRot");
        lastYRot = yRot;
        isActive = tag.getBoolean("isActive");
        energyStorage.deserializeNBT(registries, tag.get("Energy"));
        accumulatedOxygen = tag.getDouble("AccumulatedOxygen");

        // Load oxygenated positions
        oxygenatedBlocks.clear();
        long[] positions = tag.getLongArray("OxygenatedBlocks");
        for (long pos : positions) {
            oxygenatedBlocks.add(BlockPos.of(pos));
        }

        // Load chemical tank if available
        if (chemicalTank != null && tag.contains("ChemicalTank")) {
            ModIntegrationManager manager = AdAstraMekanized.getIntegrationManager();
            if (manager.hasChemicalIntegration()) {
                manager.getMekanismIntegration().deserializeChemicalTank(chemicalTank, tag.getCompound("ChemicalTank"));
            }
        }
    }

    // Getters for status
    public int getOxygenatedBlockCount() {
        return oxygenatedBlocks.size();
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public Object getChemicalTank() {
        return chemicalTank;
    }

    // === Capability Providers for Mekanism Integration ===

    /**
     * Provide energy capability on all sides for universal cables
     */
    @Nullable
    public IEnergyStorage getEnergyCapability(@Nullable Direction side) {
        return energyStorage;
    }

    /**
     * Provide chemical capability on all sides for pressurized tubes
     */
    @Nullable
    public Object getChemicalCapability(@Nullable Direction side) {
        return chemicalTank;
    }

    /**
     * Register capabilities to allow Mekanism cables and tubes to connect
     */
    private void registerCapabilities() {
        // This will be handled by the block entity capability provider system
        // The actual registration happens through the capability system
        AdAstraMekanized.LOGGER.debug("Oxygen distributor ready for Mekanism connections at {}", worldPosition);
    }
}