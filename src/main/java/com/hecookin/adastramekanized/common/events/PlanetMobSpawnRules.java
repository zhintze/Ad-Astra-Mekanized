package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
// import com.hecookin.adastramekanized.common.registry.PlanetRegistry; // Not needed
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Handles custom spawn placement rules for planetary dimensions.
 * Based on Dimension Expansion's proven approach.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class PlanetMobSpawnRules {

    /**
     * Register custom spawn placement rules for all monster entities.
     * This bypasses light level checks for planetary dimensions.
     */
    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        AdAstraMekanized.LOGGER.info("Registering planetary mob spawn rules");

        // Register custom spawn rules for all monster entities
        for (EntityType<?> rawType : BuiltInRegistries.ENTITY_TYPE) {
            if (rawType.getCategory() != MobCategory.MONSTER) continue;

            @SuppressWarnings("unchecked")
            EntityType<Mob> mobType = (EntityType<Mob>) rawType;

            // Skip slimes as they have special spawn rules
            if (rawType == EntityType.SLIME) continue;

            // Special handling for ghasts - they need ground placement in overworld-like dimensions
            if (rawType == EntityType.GHAST) {
                event.register(
                    mobType,
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    PlanetMobSpawnRules::checkPlanetaryMonsterSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
                );
                continue;
            }

            // Special handling for wither skeletons
            if (rawType == EntityType.WITHER_SKELETON) {
                event.register(
                    mobType,
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    PlanetMobSpawnRules::checkPlanetaryMonsterSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
                );
                continue;
            }

            // Special handling for guardians - water mobs
            if (rawType == EntityType.GUARDIAN) {
                event.register(
                    mobType,
                    SpawnPlacementTypes.IN_WATER,
                    Heightmap.Types.OCEAN_FLOOR,
                    (type, level, reason, pos, random) ->
                        level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.WATER),
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
                );
                continue;
            }

            // Default rule for all other monsters
            event.register(
                mobType,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PlanetMobSpawnRules::checkPlanetaryMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        }

        AdAstraMekanized.LOGGER.info("Planetary mob spawn rules registered successfully");
    }

    /**
     * Custom spawn rules for planetary dimensions.
     * Bypasses light level checks for registered planetary dimensions.
     */
    public static <T extends Mob> boolean checkPlanetaryMonsterSpawnRules(
            EntityType<T> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {

        // Peaceful mode check
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;

        if (level instanceof ServerLevelAccessor serverLevel) {
            ResourceKey<Level> dimension = serverLevel.getLevel().dimension();

            // Check if this is a planetary dimension
            boolean isPlanetaryDimension = isPlanetaryDimension(dimension);

            if (isPlanetaryDimension && type.getCategory() == MobCategory.MONSTER) {
                // Use vanilla helper that skips light level checks
                // This is the key to allowing monsters to spawn regardless of light
                return Monster.checkAnyLightMonsterSpawnRules(
                    (EntityType<? extends Monster>) type,
                    serverLevel,
                    spawnType,
                    pos,
                    random
                );
            }

            // Fallback to vanilla rules for non-planetary dimensions
            return Mob.checkMobSpawnRules(type, serverLevel, spawnType, pos, random);
        }

        return false;
    }

    /**
     * Check if a dimension is a registered planetary dimension.
     */
    private static boolean isPlanetaryDimension(ResourceKey<Level> dimension) {
        // Check against our planetary dimensions
        String dimPath = dimension.location().getPath();

        // List of our planetary dimensions
        return dimPath.equals("moon") ||
               dimPath.equals("mars") ||
               dimPath.equals("hemphy") ||
               dimPath.equals("oretest") ||
               dimPath.equals("cavetest") ||
               // Add any other planetary dimensions here
               dimension.location().getNamespace().equals(AdAstraMekanized.MOD_ID);
    }
}