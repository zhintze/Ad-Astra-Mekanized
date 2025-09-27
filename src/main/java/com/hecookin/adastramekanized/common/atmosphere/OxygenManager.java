package com.hecookin.adastramekanized.common.atmosphere;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.atmosphere.OxygenApi;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.registry.ModDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages oxygen presence in the world.
 * Tracks oxygenated zones created by distributors and planetary atmospheres.
 */
public class OxygenManager implements OxygenApi {

    private static final OxygenManager INSTANCE = new OxygenManager();

    // Track oxygenated positions per dimension
    private final Map<ResourceLocation, Set<BlockPos>> oxygenatedZones = new ConcurrentHashMap<>();

    // Cache for atmosphere checks
    private final Map<ResourceLocation, Boolean> atmosphereCache = new ConcurrentHashMap<>();

    private OxygenManager() {}

    public static OxygenManager getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean hasOxygen(Level level) {
        if (level == null) return true;

        ResourceLocation dimId = level.dimension().location();

        // Check cache first
        Boolean cached = atmosphereCache.get(dimId);
        if (cached != null) return cached;

        // Not our dimension? Has oxygen by default
        if (!dimId.getNamespace().equals(AdAstraMekanized.MOD_ID)) {
            atmosphereCache.put(dimId, true);
            return true;
        }

        // Check planet data
        PlanetRegistry registry = PlanetRegistry.getInstance();
        if (!registry.isDataLoaded()) {
            return true; // Default to breathable if data not loaded
        }

        Planet planet = registry.getPlanet(dimId);
        boolean breathable = planet != null && planet.atmosphere() != null && planet.atmosphere().breathable();

        atmosphereCache.put(dimId, breathable);
        AdAstraMekanized.LOGGER.debug("Cached oxygen status {} for dimension {}", breathable, dimId);

        return breathable;
    }

    @Override
    public boolean hasOxygen(Level level, BlockPos pos) {
        // First check if the dimension has breathable atmosphere
        if (hasOxygen(level)) {
            return true;
        }

        // Check if position is in an oxygenated zone
        ResourceLocation dimId = level.dimension().location();
        Set<BlockPos> zones = oxygenatedZones.get(dimId);
        return zones != null && zones.contains(pos);
    }

    @Override
    public boolean hasOxygen(Entity entity) {
        if (entity == null) return true;

        BlockPos pos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        boolean hasOxy = hasOxygen(entity.level(), pos);

        // Debug log every 2 seconds for players
        if (entity.tickCount % 40 == 0 && entity instanceof net.minecraft.world.entity.player.Player) {
            ResourceLocation dimId = entity.level().dimension().location();
            Set<BlockPos> zones = oxygenatedZones.get(dimId);
            AdAstraMekanized.LOGGER.debug("OxygenManager.hasOxygen for {} at {}: {}, zones in dim: {}",
                entity.getName().getString(), pos, hasOxy, zones != null ? zones.size() : 0);
        }

        return hasOxy;
    }

    @Override
    public void setOxygen(Level level, BlockPos pos, boolean hasOxygen) {
        if (level.isClientSide()) return;

        ResourceLocation dimId = level.dimension().location();
        Set<BlockPos> zones = oxygenatedZones.computeIfAbsent(dimId, k -> ConcurrentHashMap.newKeySet());

        if (hasOxygen) {
            zones.add(pos.immutable());
        } else {
            zones.remove(pos);
        }
    }

    /**
     * Set oxygen for multiple positions at once (more efficient for distributors)
     */
    public void setOxygen(Level level, Set<BlockPos> positions, boolean hasOxygen) {
        if (level.isClientSide() || positions == null || positions.isEmpty()) {
            AdAstraMekanized.LOGGER.debug("OxygenManager.setOxygen skipped: clientSide={}, positions={}",
                level != null && level.isClientSide(), positions);
            return;
        }

        ResourceLocation dimId = level.dimension().location();
        Set<BlockPos> zones = oxygenatedZones.computeIfAbsent(dimId, k -> ConcurrentHashMap.newKeySet());

        if (hasOxygen) {
            int sizeBefore = zones.size();
            positions.forEach(pos -> zones.add(pos.immutable()));
            AdAstraMekanized.LOGGER.info("OxygenManager: Added {} positions to dimension {}, total zones: {} -> {}",
                positions.size(), dimId, sizeBefore, zones.size());
        } else {
            int sizeBefore = zones.size();
            zones.removeAll(positions);
            AdAstraMekanized.LOGGER.info("OxygenManager: Removed {} positions from dimension {}, total zones: {} -> {}",
                positions.size(), dimId, sizeBefore, zones.size());
        }
    }

    @Override
    public void applyOxygenEffects(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;

        // Skip if entity has oxygen
        if (hasOxygen(entity)) {
            // Clear freeze effect when in oxygen
            entity.setTicksFrozen(0);
            return;
        }

        // TODO: Check for space suit or oxygen mask

        // Apply oxygen damage with pulsing freeze effect
        if (entity.tickCount % 40 == 0) { // Every 2 seconds for slower rhythm
            // Create custom oxygen damage source (not freeze damage)
            DamageSource oxygenDamage = ModDamageSources.oxygenDeprivation(entity.level());

            // Store current velocity to prevent knockback
            var originalMotion = entity.getDeltaMovement();

            // Apply damage
            entity.hurt(oxygenDamage, 2.0F);

            // Restore velocity to prevent knockback
            entity.setDeltaMovement(originalMotion);

            entity.setAirSupply(entity.getAirSupply() - 20); // Reduce air supply

            // Pulse the freeze effect to MAXIMUM when taking damage for dramatic effect
            entity.setTicksFrozen(entity.getTicksRequiredToFreeze()); // Full freeze visual
            AdAstraMekanized.LOGGER.trace("Applied oxygen damage to {}", entity.getType());
        } else {
            // Smooth fade of the ice effect between damage ticks
            int currentFrozen = entity.getTicksFrozen();
            if (currentFrozen > 0) {
                // Slower, smoother fade - reduce by smaller amount for consistent rhythm
                int fadeAmount = Math.max(4, entity.getTicksRequiredToFreeze() / 30);
                int newFrozen = currentFrozen - fadeAmount;

                // Let it fade to nearly nothing (10% visibility) for maximum contrast
                int minFrozen = entity.getTicksRequiredToFreeze() / 10;
                entity.setTicksFrozen(Math.max(minFrozen, newFrozen));
            } else {
                // Keep minimal visibility between pulses
                entity.setTicksFrozen(entity.getTicksRequiredToFreeze() / 10);
            }
        }
    }

    /**
     * Clear cached data when dimensions reload
     */
    public void clearCache() {
        atmosphereCache.clear();
        AdAstraMekanized.LOGGER.info("Cleared oxygen atmosphere cache");
    }

    /**
     * Clear oxygenated zones for a dimension
     */
    public void clearDimensionZones(ResourceLocation dimensionId) {
        oxygenatedZones.remove(dimensionId);
    }
}