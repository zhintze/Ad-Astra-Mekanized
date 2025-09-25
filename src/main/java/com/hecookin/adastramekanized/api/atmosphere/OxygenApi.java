package com.hecookin.adastramekanized.api.atmosphere;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * API for managing oxygen presence in the world.
 * Handles both planetary atmospheres and distributed oxygen zones.
 */
public interface OxygenApi {

    /**
     * Check if a dimension has breathable atmosphere
     */
    boolean hasOxygen(Level level);

    /**
     * Check if a specific position has oxygen
     */
    boolean hasOxygen(Level level, BlockPos pos);

    /**
     * Check if an entity is in an oxygenated area
     */
    boolean hasOxygen(Entity entity);

    /**
     * Set oxygen presence at a position
     */
    void setOxygen(Level level, BlockPos pos, boolean hasOxygen);

    /**
     * Apply oxygen damage to an entity if needed
     */
    void applyOxygenEffects(LivingEntity entity);
}