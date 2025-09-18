package com.hecookin.adastramekanized.integration.create;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/**
 * Create integration using reflection to avoid compile-time dependencies
 *
 * This implementation provides Create integration when the mod is present,
 * and graceful fallbacks when it's not available.
 *
 * Create integration will focus on:
 * - Stress/rotation power system for rocket assembly
 * - Mechanical contraptions for space station construction
 * - Fluid handling integration
 */
public class CreateIntegration {
    private static final String CREATE_MOD_ID = "create";

    // Integration state
    private final boolean createLoaded;
    private boolean apiAccessible = false;

    // Reflected classes (cached for performance)
    private Class<?> stressImpactClass;
    private Class<?> rotationSourceClass;
    private Class<?> mechanicalBearingClass;
    private Class<?> createFluidHandlerClass;

    // Reflected methods
    private Method getStressMethod;
    private Method getSpeedMethod;
    private Method hasStressMethod;

    public CreateIntegration() {
        this.createLoaded = ModList.get().isLoaded(CREATE_MOD_ID);

        if (createLoaded) {
            initializeReflection();
        }

        AdAstraMekanized.LOGGER.info("Create integration initialized - Available: {}, API Accessible: {}",
                createLoaded, apiAccessible);
    }

    private void initializeReflection() {
        try {
            // Load core Create classes
            Class<?> createMainClass = Class.forName("com.simibubi.create.Create");

            // Try to load kinetic system classes
            try {
                stressImpactClass = Class.forName("com.simibubi.create.content.kinetics.base.IStressValueProvider");
                rotationSourceClass = Class.forName("com.simibubi.create.content.kinetics.base.IRotate");

                AdAstraMekanized.LOGGER.debug("Create kinetic system classes loaded");
            } catch (ClassNotFoundException e) {
                AdAstraMekanized.LOGGER.debug("Create kinetic classes not accessible: {}", e.getMessage());
            }

            // Try to load bearing for contraption movement
            try {
                mechanicalBearingClass = Class.forName("com.simibubi.create.content.kinetics.bearing.MechanicalBearingBlockEntity");
                AdAstraMekanized.LOGGER.debug("Create bearing class loaded");
            } catch (ClassNotFoundException e) {
                AdAstraMekanized.LOGGER.debug("Create bearing class not accessible: {}", e.getMessage());
            }

            // Try to load fluid handler
            try {
                createFluidHandlerClass = Class.forName("com.simibubi.create.foundation.fluid.SmartFluidTank");
                AdAstraMekanized.LOGGER.debug("Create fluid handler loaded");
            } catch (ClassNotFoundException e) {
                AdAstraMekanized.LOGGER.debug("Create fluid handler not accessible: {}", e.getMessage());
            }

            apiAccessible = true;
            AdAstraMekanized.LOGGER.info("Create API reflection setup completed successfully");

        } catch (ClassNotFoundException e) {
            AdAstraMekanized.LOGGER.warn("Create API reflection setup failed: {}", e.getMessage());
            apiAccessible = false;
        }
    }

    // === Public Integration Methods ===

    /**
     * Check if Create integration is available
     * @return true if Create APIs are accessible
     */
    public boolean isCreateSystemAvailable() {
        return createLoaded && apiAccessible;
    }

    /**
     * Check if a block entity is part of Create's kinetic system
     * @param level The world level
     * @param pos Position to check
     * @return true if the block is part of kinetic system
     */
    public boolean isKineticBlock(Level level, BlockPos pos) {
        if (!isCreateSystemAvailable()) {
            return false;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                return false;
            }

            // Check if block entity implements rotation interface
            return rotationSourceClass != null && rotationSourceClass.isInstance(blockEntity);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error checking kinetic block: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if a block entity can provide stress/power
     * @param level The world level
     * @param pos Position to check
     * @return true if the block can provide power
     */
    public boolean canProvideStress(Level level, BlockPos pos) {
        if (!isCreateSystemAvailable()) {
            return false;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                return false;
            }

            // Check if block entity implements stress provider interface
            return stressImpactClass != null && stressImpactClass.isInstance(blockEntity);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error checking stress capability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if a block entity is a mechanical bearing for contraption movement
     * @param level The world level
     * @param pos Position to check
     * @return true if the block is a mechanical bearing
     */
    public boolean isMechanicalBearing(Level level, BlockPos pos) {
        if (!isCreateSystemAvailable()) {
            return false;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                return false;
            }

            return mechanicalBearingClass != null && mechanicalBearingClass.isInstance(blockEntity);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error checking mechanical bearing: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if a block entity has Create fluid handling capabilities
     * @param level The world level
     * @param pos Position to check
     * @return true if the block has Create fluid capabilities
     */
    public boolean hasCreateFluidHandler(Level level, BlockPos pos) {
        if (!isCreateSystemAvailable()) {
            return false;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                return false;
            }

            return createFluidHandlerClass != null && createFluidHandlerClass.isInstance(blockEntity);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error checking Create fluid handler: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the rotation speed at a position (for future contraption integration)
     * @param level The world level
     * @param pos Position to check
     * @return Rotation speed, or 0 if not available
     */
    public float getRotationSpeed(Level level, BlockPos pos) {
        if (!isCreateSystemAvailable()) {
            return 0f;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !isKineticBlock(level, pos)) {
                return 0f;
            }

            // Implementation depends on exact Create API structure
            // For now, return 0 indicating no speed data available
            return 0f;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error getting rotation speed: {}", e.getMessage());
            return 0f;
        }
    }

    /**
     * Check if there's sufficient stress/power for an operation
     * @param level The world level
     * @param pos Position to check
     * @param requiredStress Amount of stress required
     * @return true if sufficient stress is available
     */
    public boolean hasSufficientStress(Level level, BlockPos pos, float requiredStress) {
        if (!isCreateSystemAvailable()) {
            return false;
        }

        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !canProvideStress(level, pos)) {
                return false;
            }

            // Implementation depends on exact Create API structure
            // For now, return false indicating insufficient stress data
            return false;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error checking stress availability: {}", e.getMessage());
            return false;
        }
    }
}