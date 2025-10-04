package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for entity types
 */
public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, AdAstraMekanized.MOD_ID);

    // ========== ROCKETS ==========

    public static final Supplier<EntityType<Rocket>> TIER_1_ROCKET = ENTITY_TYPES.register("tier_1_rocket",
            () -> EntityType.Builder.<Rocket>of(Rocket::new, MobCategory.MISC)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("tier_1_rocket"));

    public static final Supplier<EntityType<Rocket>> TIER_2_ROCKET = ENTITY_TYPES.register("tier_2_rocket",
            () -> EntityType.Builder.<Rocket>of(Rocket::new, MobCategory.MISC)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("tier_2_rocket"));

    public static final Supplier<EntityType<Rocket>> TIER_3_ROCKET = ENTITY_TYPES.register("tier_3_rocket",
            () -> EntityType.Builder.<Rocket>of(Rocket::new, MobCategory.MISC)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("tier_3_rocket"));

    public static final Supplier<EntityType<Rocket>> TIER_4_ROCKET = ENTITY_TYPES.register("tier_4_rocket",
            () -> EntityType.Builder.<Rocket>of(Rocket::new, MobCategory.MISC)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("tier_4_rocket"));

    /**
     * Initialize rocket tier properties after entity types are registered
     */
    public static void initRocketTiers() {
        Rocket.RocketTier.init(
            TIER_1_ROCKET.get(),
            TIER_2_ROCKET.get(),
            TIER_3_ROCKET.get(),
            TIER_4_ROCKET.get()
        );
    }

    /**
     * Register all entity types
     */
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        AdAstraMekanized.LOGGER.info("Registered {} mod entity types", ENTITY_TYPES.getEntries().size());
    }
}
