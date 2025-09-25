package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Handles gravity modifications for planetary dimensions.
 * Applies different gravity values to entities based on the planet they're on.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class PlanetGravityHandler {

    // Gravity multiplier cache for performance
    private static final Map<ResourceLocation, Float> GRAVITY_CACHE = new ConcurrentHashMap<>();

    // Attribute modifier ID for jump strength
    private static final ResourceLocation GRAVITY_JUMP_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "gravity_jump_modifier");

    // Attribute modifier ID for safe fall distance
    private static final ResourceLocation GRAVITY_FALL_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "gravity_fall_modifier");

    /**
     * Get gravity multiplier for a dimension
     */
    private static float getGravity(Level level) {
        if (level == null || level.isClientSide()) return 1.0f;

        ResourceLocation dimId = level.dimension().location();

        // Check cache first
        Float cached = GRAVITY_CACHE.get(dimId);
        if (cached != null) return cached;

        // Not our dimension? Default gravity
        if (!dimId.getNamespace().equals(AdAstraMekanized.MOD_ID)) {
            GRAVITY_CACHE.put(dimId, 1.0f);
            return 1.0f;
        }

        // Try to get planet data
        PlanetRegistry registry = PlanetRegistry.getInstance();
        if (!registry.isDataLoaded()) {
            return 1.0f; // Data not loaded yet
        }

        Planet planet = registry.getPlanet(dimId);
        if (planet != null && planet.properties() != null) {
            float gravity = planet.properties().gravity();
            GRAVITY_CACHE.put(dimId, gravity);
            AdAstraMekanized.LOGGER.debug("Cached gravity {} for dimension {}", gravity, dimId);
            return gravity;
        }

        // Default to Earth gravity if planet not found
        GRAVITY_CACHE.put(dimId, 1.0f);
        return 1.0f;
    }

    /**
     * Clear gravity cache (call when planet data reloads)
     */
    public static void clearCache() {
        GRAVITY_CACHE.clear();
        AdAstraMekanized.LOGGER.info("Cleared gravity cache");
    }

    /**
     * Apply gravity to living entities (affects jumping)
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Level level = event.getLevel();

        if (level.isClientSide() || !(entity instanceof LivingEntity living)) return;

        float gravity = getGravity(level);
        if (gravity == 1.0f) return; // No modification needed

        // Apply jump strength modifier based on gravity
        AttributeInstance jumpAttribute = living.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpAttribute != null) {
            // Remove any existing gravity modifier
            jumpAttribute.removeModifier(GRAVITY_JUMP_MODIFIER_ID);

            // Lower gravity = higher jumps, higher gravity = lower jumps
            // Jump multiplier is inverse of gravity (roughly)
            float jumpMultiplier = gravity < 1.0f ? (2.0f - gravity) : (1.0f / gravity);

            AttributeModifier modifier = new AttributeModifier(
                GRAVITY_JUMP_MODIFIER_ID,
                jumpMultiplier - 1.0f, // Additive multiplier
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );

            jumpAttribute.addPermanentModifier(modifier);
            AdAstraMekanized.LOGGER.debug("Applied jump modifier {} to {} in dimension with gravity {}",
                jumpMultiplier, entity.getType(), gravity);
        }

        // Apply safe fall distance modifier
        AttributeInstance fallAttribute = living.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (fallAttribute != null) {
            // Remove any existing gravity modifier
            fallAttribute.removeModifier(GRAVITY_FALL_MODIFIER_ID);

            // Lower gravity = can fall further safely
            // Safe fall distance is inverse of gravity
            float fallMultiplier = gravity > 0 ? (1.0f / gravity) : 1.0f;

            AttributeModifier modifier = new AttributeModifier(
                GRAVITY_FALL_MODIFIER_ID,
                fallMultiplier - 1.0f, // Additive multiplier
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );

            fallAttribute.addPermanentModifier(modifier);
        }
    }

    /**
     * Modify fall damage based on gravity
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (level.isClientSide()) return;

        float gravity = getGravity(level);
        if (gravity == 1.0f) return; // No modification needed

        // Scale fall damage by gravity
        // Higher gravity = more damage, lower gravity = less damage
        float damageMultiplier = Math.max(0.1f, gravity); // Minimum 10% damage
        event.setDamageMultiplier(event.getDamageMultiplier() * damageMultiplier);

        AdAstraMekanized.LOGGER.debug("Modified fall damage for {} by factor {} (gravity: {})",
            entity.getType(), damageMultiplier, gravity);
    }

    /**
     * Apply gravity effects to non-living entities (items, projectiles, minecarts)
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (level.isClientSide()) return;

        // Only affect certain entity types
        boolean shouldApplyGravity = entity instanceof ItemEntity ||
                                     entity instanceof Projectile ||
                                     entity instanceof AbstractMinecart;

        if (!shouldApplyGravity) return;

        float gravity = getGravity(level);
        if (gravity == 1.0f) return; // No modification needed

        // Adjust Y velocity based on gravity difference
        Vec3 motion = entity.getDeltaMovement();

        // Standard gravity acceleration is -0.08 per tick for items
        // We need to counteract the default and apply our custom gravity
        double defaultGravity = -0.08;
        double customGravity = defaultGravity * gravity;
        double gravityAdjustment = customGravity - defaultGravity;

        // Only apply if entity is affected by gravity (not in water, etc.)
        if (!entity.isNoGravity() && !entity.isInWater() && !entity.isInLava()) {
            entity.setDeltaMovement(motion.x, motion.y + gravityAdjustment, motion.z);
        }
    }

    /**
     * Apply additional gravity effects to living entities during their tick
     */
    @SubscribeEvent
    public static void onEntityTick2(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) return;

        Level level = living.level();
        if (level.isClientSide()) return;

        float gravity = getGravity(level);
        if (gravity == 1.0f) return; // No modification needed

        // Only apply additional gravity effects when falling
        if (!living.onGround() && !living.isNoGravity() && !living.isInWater() && !living.isInLava()) {
            Vec3 motion = living.getDeltaMovement();

            // Apply slight additional downward acceleration for high gravity
            // Or reduce downward acceleration for low gravity
            if (gravity > 1.0f) {
                // High gravity - increase fall speed
                double extraGravity = (gravity - 1.0f) * 0.01; // Small adjustment
                living.setDeltaMovement(motion.x, motion.y - extraGravity, motion.z);
            } else if (gravity < 1.0f) {
                // Low gravity - slow fall speed
                double reducedGravity = (1.0f - gravity) * 0.005; // Very small adjustment
                living.setDeltaMovement(motion.x, motion.y + reducedGravity, motion.z);
            }
        }
    }
}