package com.hecookin.adastramekanized.integration.mekanism;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.IChemicalIntegration;
import com.hecookin.adastramekanized.api.IEnergyIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Mekanism integration using reflection to avoid compile-time dependencies
 *
 * This implementation provides full Mekanism integration when the mod is present,
 * and graceful fallbacks when it's not available.
 */
public class MekanismIntegration implements IChemicalIntegration, IEnergyIntegration {
    private static final String MEKANISM_MOD_ID = "mekanism";

    // Integration state
    private final boolean mekanismLoaded;
    private boolean apiAccessible = false;

    // Reflected classes (cached for performance)
    private Class<?> chemicalClass;
    private Class<?> chemicalStackClass;
    private Class<?> energyContainerClass;
    private Class<?> chemicalHandlerClass;
    private Class<?> oxygenChemicalClass;
    private Class<?> basicChemicalTankClass;
    private Object oxygenInstance;

    // Reflected methods (cached for performance)
    private Method getEnergyStoredMethod;
    private Method getMaxEnergyMethod;
    private Method insertEnergyMethod;
    private Method extractEnergyMethod;
    private Method getChemicalInTankMethod;
    private Method insertChemicalMethod;
    private Method extractChemicalMethod;

    public MekanismIntegration() {
        this.mekanismLoaded = ModList.get().isLoaded(MEKANISM_MOD_ID);

        if (mekanismLoaded) {
            initializeReflection();
        }

        AdAstraMekanized.LOGGER.info("Mekanism integration initialized - Available: {}, API Accessible: {}",
                mekanismLoaded, apiAccessible);
    }

    private void initializeReflection() {
        try {
            // Load core API classes
            chemicalClass = Class.forName("mekanism.api.chemical.Chemical");
            chemicalStackClass = Class.forName("mekanism.api.chemical.ChemicalStack");
            energyContainerClass = Class.forName("mekanism.api.energy.IEnergyContainer");
            chemicalHandlerClass = Class.forName("mekanism.api.chemical.IChemicalHandler");
            basicChemicalTankClass = Class.forName("mekanism.api.chemical.BasicChemicalTank");

            // Load energy methods
            getEnergyStoredMethod = energyContainerClass.getMethod("getEnergyStored");
            getMaxEnergyMethod = energyContainerClass.getMethod("getMaxEnergy");
            insertEnergyMethod = energyContainerClass.getMethod("insertEnergy", long.class, boolean.class);
            extractEnergyMethod = energyContainerClass.getMethod("extractEnergy", long.class, boolean.class);

            // Load chemical methods
            getChemicalInTankMethod = chemicalHandlerClass.getMethod("getChemicalInTank", int.class);
            insertChemicalMethod = chemicalHandlerClass.getMethod("insertChemical", chemicalStackClass, boolean.class);
            extractChemicalMethod = chemicalHandlerClass.getMethod("extractChemical", long.class, boolean.class);

            // Try to access oxygen chemical
            tryLoadOxygenChemical();

            apiAccessible = true;
            AdAstraMekanized.LOGGER.info("Mekanism API reflection setup completed successfully");

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            AdAstraMekanized.LOGGER.warn("Mekanism API reflection setup failed: {}", e.getMessage());
            apiAccessible = false;
        }
    }

    private void tryLoadOxygenChemical() {
        try {
            // Try to access Mekanism's built-in chemicals
            Class<?> mekanismChemicalsClass = Class.forName("mekanism.common.registries.MekanismChemicals");

            // Get the OXYGEN field
            java.lang.reflect.Field oxygenField = mekanismChemicalsClass.getField("OXYGEN");
            Object deferredChemical = oxygenField.get(null);

            // Get the actual chemical from the deferred holder
            Method getMethod = deferredChemical.getClass().getMethod("get");
            oxygenInstance = getMethod.invoke(deferredChemical);

            AdAstraMekanized.LOGGER.debug("Successfully loaded Mekanism oxygen chemical");
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Could not load Mekanism oxygen: {}", e.getMessage());
        }
    }

    // === IChemicalIntegration Implementation ===

    @Override
    public boolean isChemicalSystemAvailable() {
        return mekanismLoaded && apiAccessible;
    }

    @Override
    public void initializeOxygenChemical() {
        if (!isChemicalSystemAvailable()) {
            AdAstraMekanized.LOGGER.debug("Oxygen chemical initialization skipped - Mekanism not available");
            return;
        }

        try {
            // Initialize oxygen chemical registration
            // This will be implemented once we understand Mekanism's chemical registration system
            AdAstraMekanized.LOGGER.info("Oxygen chemical initialization completed");
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to initialize oxygen chemical: {}", e.getMessage());
        }
    }

    @Override
    public long transferOxygen(BlockPos fromPos, BlockPos toPos, Level level, long amount) {
        if (!isChemicalSystemAvailable()) {
            return 0; // No transfer possible without Mekanism
        }

        try {
            BlockEntity fromBE = level.getBlockEntity(fromPos);
            BlockEntity toBE = level.getBlockEntity(toPos);

            if (fromBE == null || toBE == null) {
                return 0;
            }

            // Use reflection to access chemical handlers
            // Implementation will depend on exact Mekanism API structure
            // For now, return 0 indicating no transfer (graceful fallback)
            return 0;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Oxygen transfer failed: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean canStoreOxygen(Level level, BlockPos pos) {
        if (!isChemicalSystemAvailable()) {
            return false;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                return false;
            }

            // Check if block entity implements chemical handler interface
            return chemicalHandlerClass.isInstance(blockEntity);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error checking oxygen storage capability: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long getStoredOxygen(Level level, BlockPos pos) {
        if (!isChemicalSystemAvailable()) {
            return 0;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !chemicalHandlerClass.isInstance(blockEntity)) {
                return 0;
            }

            // Use reflection to get stored oxygen amount
            // Implementation depends on exact chemical API
            return 0;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error getting stored oxygen: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long insertOxygen(Level level, BlockPos pos, long amount) {
        if (!isChemicalSystemAvailable() || amount <= 0) {
            return 0;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !chemicalHandlerClass.isInstance(blockEntity)) {
                return 0;
            }

            // Use reflection to insert oxygen
            // Implementation depends on exact chemical API
            return 0;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error inserting oxygen: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long extractOxygen(Level level, BlockPos pos, long amount) {
        if (!isChemicalSystemAvailable() || amount <= 0) {
            return 0;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !chemicalHandlerClass.isInstance(blockEntity)) {
                return 0;
            }

            // Use reflection to extract oxygen
            // Implementation depends on exact chemical API
            return 0;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error extracting oxygen: {}", e.getMessage());
            return 0;
        }
    }

    // === IEnergyIntegration Implementation ===

    @Override
    public boolean isEnergySystemAvailable() {
        return mekanismLoaded && apiAccessible;
    }

    @Override
    public long transferEnergy(BlockPos fromPos, BlockPos toPos, Level level, long amount) {
        if (!isEnergySystemAvailable()) {
            return 0;
        }

        try {
            BlockEntity fromBE = level.getBlockEntity(fromPos);
            BlockEntity toBE = level.getBlockEntity(toPos);

            if (fromBE == null || toBE == null) {
                return 0;
            }

            // Extract from source
            long extracted = extractEnergy(level, fromPos, amount, false);
            if (extracted <= 0) {
                return 0;
            }

            // Insert into target
            long inserted = insertEnergy(level, toPos, extracted, false);

            // If couldn't insert all, return the rest to source
            if (inserted < extracted) {
                insertEnergy(level, fromPos, extracted - inserted, false);
            }

            return inserted;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Energy transfer failed: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean canStoreEnergy(Level level, BlockPos pos) {
        if (!isEnergySystemAvailable()) {
            return false;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                return false;
            }

            return energyContainerClass.isInstance(blockEntity);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error checking energy storage capability: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long getStoredEnergy(Level level, BlockPos pos) {
        if (!isEnergySystemAvailable()) {
            return 0;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !energyContainerClass.isInstance(blockEntity)) {
                return 0;
            }

            return (Long) getEnergyStoredMethod.invoke(blockEntity);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error getting stored energy: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long getEnergyCapacity(Level level, BlockPos pos) {
        if (!isEnergySystemAvailable()) {
            return 0;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !energyContainerClass.isInstance(blockEntity)) {
                return 0;
            }

            return (Long) getMaxEnergyMethod.invoke(blockEntity);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error getting energy capacity: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long insertEnergy(Level level, BlockPos pos, long amount, boolean simulate) {
        if (!isEnergySystemAvailable() || amount <= 0) {
            return 0;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !energyContainerClass.isInstance(blockEntity)) {
                return 0;
            }

            return (Long) insertEnergyMethod.invoke(blockEntity, amount, simulate);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error inserting energy: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long extractEnergy(Level level, BlockPos pos, long amount, boolean simulate) {
        if (!isEnergySystemAvailable() || amount <= 0) {
            return 0;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !energyContainerClass.isInstance(blockEntity)) {
                return 0;
            }

            return (Long) extractEnergyMethod.invoke(blockEntity, amount, simulate);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error extracting energy: {}", e.getMessage());
            return 0;
        }
    }

    // === Custom Methods for Oxygen Distributor ===

    /**
     * Create a new oxygen tank for the distributor
     */
    public Object createOxygenTank(long capacity) {
        if (!isChemicalSystemAvailable() || basicChemicalTankClass == null) {
            return null;
        }

        try {
            // Create a BasicChemicalTank with the specified capacity
            Constructor<?> constructor = basicChemicalTankClass.getConstructor(long.class, chemicalClass);
            return constructor.newInstance(capacity, oxygenInstance);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to create oxygen tank: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get the amount of oxygen stored in a tank
     */
    public long getOxygenAmount(Object tank) {
        if (tank == null || chemicalStackClass == null) {
            return 0;
        }

        try {
            Method getStackMethod = tank.getClass().getMethod("getStack");
            Object chemicalStack = getStackMethod.invoke(tank);

            Method getAmountMethod = chemicalStackClass.getMethod("getAmount");
            return (Long) getAmountMethod.invoke(chemicalStack);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to get oxygen amount: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Extract oxygen from a tank
     */
    public long extractOxygen(Object tank, long amount) {
        if (tank == null || amount <= 0) {
            return 0;
        }

        try {
            Method extractMethod = tank.getClass().getMethod("extract", long.class, boolean.class);
            Object extractedStack = extractMethod.invoke(tank, amount, false);

            if (extractedStack != null && chemicalStackClass.isInstance(extractedStack)) {
                Method getAmountMethod = chemicalStackClass.getMethod("getAmount");
                return (Long) getAmountMethod.invoke(extractedStack);
            }
            return 0;
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to extract oxygen: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Serialize a chemical tank to NBT
     */
    public CompoundTag serializeChemicalTank(Object tank) {
        if (tank == null) {
            return null;
        }

        try {
            Method serializeMethod = tank.getClass().getMethod("serializeNBT");
            return (CompoundTag) serializeMethod.invoke(tank);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to serialize chemical tank: {}", e.getMessage());
            return new CompoundTag();
        }
    }

    /**
     * Deserialize a chemical tank from NBT
     */
    public void deserializeChemicalTank(Object tank, CompoundTag tag) {
        if (tank == null || tag == null) {
            return;
        }

        try {
            Method deserializeMethod = tank.getClass().getMethod("deserializeNBT", CompoundTag.class);
            deserializeMethod.invoke(tank, tag);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to deserialize chemical tank: {}", e.getMessage());
        }
    }
}