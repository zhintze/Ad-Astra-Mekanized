package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Handles gravity modifications for planetary dimensions.
 * Uses Minecraft's built-in gravity attribute system for clean implementation.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class PlanetGravityHandler {

    // Gravity multiplier cache for performance
    private static final Map<ResourceLocation, Float> GRAVITY_CACHE = new ConcurrentHashMap<>();

    // Attribute modifier ID for gravity
    private static final ResourceLocation GRAVITY_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planet_gravity");

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
     * Clear gravity cache when dimensions reload
     */
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            GRAVITY_CACHE.clear();
            AdAstraMekanized.LOGGER.info("Cleared gravity cache on world load");
        }
    }

    /**
     * Apply gravity attribute to entities when they join a world
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Level level = event.getLevel();

        if (level.isClientSide()) return;

        float gravity = getGravity(level);

        // Handle living entities with attributes
        if (entity instanceof LivingEntity living) {
            applyLivingEntityGravity(living, gravity);
        }
        // Handle projectiles differently - they don't have attributes
        else if (entity instanceof Projectile || entity instanceof ItemEntity) {
            // Store gravity value in entity's persistent data for use in tick event
            entity.getPersistentData().putFloat("adastramekanized:planet_gravity", gravity);
        }
    }

    /**
     * Apply gravity to living entities using attributes
     */
    private static void applyLivingEntityGravity(LivingEntity living, float gravity) {

        // Apply gravity attribute modifier
        AttributeInstance gravityAttribute = living.getAttribute(Attributes.GRAVITY);
        if (gravityAttribute != null) {
            // Remove any existing gravity modifier from our mod
            gravityAttribute.removeModifier(GRAVITY_MODIFIER_ID);

            if (gravity != 1.0f) {
                // Gravity attribute works as a multiplier
                // Default is 0.08, so we multiply by our gravity ratio
                double gravityModifier = gravity - 1.0f;

                AttributeModifier modifier = new AttributeModifier(
                    GRAVITY_MODIFIER_ID,
                    gravityModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );

                gravityAttribute.addPermanentModifier(modifier);
                AdAstraMekanized.LOGGER.debug("Applied gravity modifier {} to {} ",
                    gravity, living.getType());
            }
        }

        // Also apply safe fall distance modifier for better gameplay
        AttributeInstance fallAttribute = living.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (fallAttribute != null) {
            // Remove any existing fall modifier
            ResourceLocation fallModifierId = ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID, "planet_fall_distance"
            );
            fallAttribute.removeModifier(fallModifierId);

            if (gravity != 1.0f && gravity > 0) {
                // Lower gravity = can fall further safely
                // Higher gravity = less safe fall distance
                double fallModifier = (1.0f / gravity) - 1.0f;

                AttributeModifier modifier = new AttributeModifier(
                    fallModifierId,
                    fallModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );

                fallAttribute.addPermanentModifier(modifier);
            }
        }

        // Apply fall damage multiplier for even better feel
        AttributeInstance fallDamageAttribute = living.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
        if (fallDamageAttribute != null) {
            // Remove any existing fall damage modifier
            ResourceLocation fallDamageModifierId = ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID, "planet_fall_damage"
            );
            fallDamageAttribute.removeModifier(fallDamageModifierId);

            if (gravity != 1.0f) {
                // Scale fall damage by gravity
                // Lower gravity = less damage, higher gravity = more damage
                double damageModifier = gravity - 1.0f;

                AttributeModifier modifier = new AttributeModifier(
                    fallDamageModifierId,
                    damageModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );

                fallDamageAttribute.addPermanentModifier(modifier);
            }
        }
    }

    /**
     * Apply gravity effects to projectiles and items during their tick
     * Since they don't have gravity attributes, we modify their motion directly
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();

        // Only process on server side
        if (entity.level().isClientSide()) return;

        // Only process projectiles and dropped items
        if (!(entity instanceof Projectile) && !(entity instanceof ItemEntity)) return;

        // Skip if already processed this tick (prevent double processing)
        if (entity.getPersistentData().getBoolean("adastramekanized:gravity_processed_this_tick")) {
            return;
        }

        // Get stored gravity value
        float gravity = entity.getPersistentData().getFloat("adastramekanized:planet_gravity");
        if (gravity == 0.0f) {
            // If not set yet, get it from the level
            gravity = getGravity(entity.level());
            entity.getPersistentData().putFloat("adastramekanized:planet_gravity", gravity);
        }

        if (gravity == 1.0f) return; // No modification needed for Earth gravity

        // Don't modify if in water/lava or no gravity
        if (entity.isInWater() || entity.isInLava() || entity.isNoGravity()) return;

        Vec3 motion = entity.getDeltaMovement();

        // Calculate gravity adjustment
        // Different entity types have different base gravity values
        double gravityAdjustment = 0.0;

        if (entity instanceof ThrowableProjectile) {
            // For throwable projectiles (snowballs, eggs, ender pearls)
            // They typically have 0.03 gravity
            gravityAdjustment = 0.03 * (gravity - 1.0);
        } else if (entity instanceof ItemEntity) {
            // Items have 0.04 gravity
            gravityAdjustment = 0.04 * (gravity - 1.0);
        } else if (entity instanceof Projectile) {
            // Other projectiles (arrows, etc) have varying gravity
            // Arrows have about 0.05 gravity
            gravityAdjustment = 0.05 * (gravity - 1.0);
        }

        // Apply the gravity adjustment to Y velocity
        if (gravityAdjustment != 0.0) {
            entity.setDeltaMovement(motion.x, motion.y - gravityAdjustment, motion.z);

            // Mark as processed to avoid double-processing
            entity.getPersistentData().putBoolean("adastramekanized:gravity_processed_this_tick", true);

            // Debug logging for testing
            if (entity.tickCount % 20 == 0) { // Log once per second
                AdAstraMekanized.LOGGER.debug("Applying gravity {} to {} at Y velocity {}",
                    gravity, entity.getType(), motion.y - gravityAdjustment);
            }
        }
    }

    /**
     * Reset the gravity processed flag at the end of each tick
     */
    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if ((entity instanceof Projectile || entity instanceof ItemEntity) && !entity.level().isClientSide()) {
            entity.getPersistentData().remove("adastramekanized:gravity_processed_this_tick");
        }
    }
}