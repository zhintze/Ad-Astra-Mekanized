package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Controls spawning of modded mobs to restrict them to specific dimensions and biomes.
 *
 * This system:
 * - Tracks which mod namespaces are "controlled" (e.g., mowziesmobs, kobolds)
 * - Allows manual spawns (eggs, spawners, commands) EVERYWHERE for all spawn types
 * - Blocks NATURAL spawns (world generation, reinforcements) unless whitelisted
 * - For custom dimensions, only allows natural spawns if explicitly whitelisted
 * - Checks at biome granularity for fine-grained control
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class ModdedMobSpawnController {
    private static final ModdedMobWhitelistData whitelist = new ModdedMobWhitelistData();

    /**
     * Get the whitelist data instance for configuration during planet generation.
     */
    public static ModdedMobWhitelistData getWhitelist() {
        return whitelist;
    }

    /**
     * Event handler that prevents modded mob spawns based on dimension, biome, and spawn type.
     *
     * ALLOWS manual spawns (spawn eggs, spawners, commands) EVERYWHERE.
     * BLOCKS natural spawns (world generation, reinforcements) unless whitelisted.
     */
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();

        // Get mod namespace from entity type
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (entityId == null) {
            return;
        }
        String modNamespace = entityId.getNamespace();

        // Only control spawns for registered controlled mods
        if (!whitelist.isControlledMod(modNamespace)) {
            return;
        }

        // Get spawn type - this tells us HOW the mob is being spawned
        MobSpawnType spawnType = event.getSpawnType();

        // ALWAYS allow manual spawn types (spawn eggs, spawners, commands, dispensers)
        if (isManualSpawn(spawnType)) {
            AdAstraMekanized.LOGGER.debug("Allowed {} manual spawn ({}) in dimension {} (manual spawns allowed everywhere)",
                entityId, spawnType, event.getLevel().getLevel().dimension().location());
            return;
        }

        // For natural spawns, check whitelist
        Level level = event.getLevel().getLevel();
        ResourceKey<Level> dimension = level.dimension();
        BlockPos pos = mob.blockPosition();

        // Get biome at spawn location
        Holder<Biome> biomeHolder = level.getBiome(pos);
        ResourceLocation biomeId = getBiomeId(biomeHolder);

        // Check if this mod is whitelisted for this dimension+biome
        boolean allowed = whitelist.isModAllowed(dimension.location(), biomeId, modNamespace);

        if (!allowed) {
            // Cancel natural spawn - not whitelisted for this dimension+biome
            event.setSpawnCancelled(true);
            AdAstraMekanized.LOGGER.debug("Blocked {} natural spawn ({}) in dimension {} biome {} (not whitelisted)",
                entityId, spawnType, dimension.location(), biomeId);
        } else {
            AdAstraMekanized.LOGGER.debug("Allowed {} natural spawn ({}) in dimension {} biome {} (whitelisted)",
                entityId, spawnType, dimension.location(), biomeId);
        }
    }

    /**
     * Check if a spawn type is a manual spawn (player-initiated or intentional).
     * These spawn types should ALWAYS be allowed regardless of dimension/biome.
     *
     * @param spawnType The spawn type to check
     * @return true if this is a manual spawn type
     */
    private static boolean isManualSpawn(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.SPAWN_EGG ||
               spawnType == MobSpawnType.SPAWNER ||
               spawnType == MobSpawnType.TRIAL_SPAWNER ||
               spawnType == MobSpawnType.COMMAND ||
               spawnType == MobSpawnType.DISPENSER ||
               spawnType == MobSpawnType.BUCKET ||
               spawnType == MobSpawnType.BREEDING ||
               spawnType == MobSpawnType.MOB_SUMMONED ||
               spawnType == MobSpawnType.CONVERSION;
    }

    /**
     * Helper method to get biome ResourceLocation from Biome Holder.
     * Handles both registered and unregistered biomes.
     */
    private static ResourceLocation getBiomeId(Holder<Biome> biomeHolder) {
        return biomeHolder.unwrapKey()
            .map(ResourceKey::location)
            .orElse(ResourceLocation.withDefaultNamespace("unknown"));
    }

    /**
     * Initialize whitelist data from planet generation.
     * Called by PlanetMaker during planet registration.
     */
    public static void registerPlanetMobWhitelist(ResourceLocation dimensionId, String modNamespace) {
        whitelist.whitelistModForDimension(dimensionId, modNamespace);
        AdAstraMekanized.LOGGER.info("Whitelisted mod '{}' for dimension '{}'", modNamespace, dimensionId);
    }

    /**
     * Register a biome-specific whitelist entry.
     * Called by PlanetMaker for fine-grained biome control.
     */
    public static void registerBiomeMobWhitelist(ResourceLocation dimensionId, ResourceLocation biomeId, String modNamespace) {
        whitelist.whitelistModForBiome(dimensionId, biomeId, modNamespace);
        AdAstraMekanized.LOGGER.info("Whitelisted mod '{}' for dimension '{}' biome '{}'",
            modNamespace, dimensionId, biomeId);
    }

    /**
     * Add a new mod namespace to the controlled mods list.
     * Use this when adding support for new mob mods.
     *
     * @param modNamespace The mod namespace (e.g., "alexsmobs", "naturalist")
     */
    public static void addControlledMod(String modNamespace) {
        whitelist.addControlledMod(modNamespace);
        AdAstraMekanized.LOGGER.info("Added '{}' to controlled mob mods list", modNamespace);
    }

    /**
     * Allow a mod to spawn in the Overworld.
     * Use this if you want to preserve vanilla spawning behavior for a specific mod.
     *
     * @param modNamespace The mod namespace (e.g., "mowziesmobs")
     */
    public static void allowInOverworld(String modNamespace) {
        whitelist.whitelistModForDimension(ResourceLocation.withDefaultNamespace("overworld"), modNamespace);
        AdAstraMekanized.LOGGER.info("Whitelisted mod '{}' for Overworld", modNamespace);
    }

    /**
     * Allow a mod to spawn in the Nether.
     *
     * @param modNamespace The mod namespace
     */
    public static void allowInNether(String modNamespace) {
        whitelist.whitelistModForDimension(ResourceLocation.withDefaultNamespace("the_nether"), modNamespace);
        AdAstraMekanized.LOGGER.info("Whitelisted mod '{}' for Nether", modNamespace);
    }

    /**
     * Allow a mod to spawn in the End.
     *
     * @param modNamespace The mod namespace
     */
    public static void allowInEnd(String modNamespace) {
        whitelist.whitelistModForDimension(ResourceLocation.withDefaultNamespace("the_end"), modNamespace);
        AdAstraMekanized.LOGGER.info("Whitelisted mod '{}' for End", modNamespace);
    }

    /**
     * Allow a mod to spawn in all vanilla dimensions (Overworld, Nether, End).
     * Convenience method for mods that should maintain vanilla behavior.
     *
     * @param modNamespace The mod namespace
     */
    public static void allowInVanillaDimensions(String modNamespace) {
        allowInOverworld(modNamespace);
        allowInNether(modNamespace);
        allowInEnd(modNamespace);
        AdAstraMekanized.LOGGER.info("Whitelisted mod '{}' for all vanilla dimensions", modNamespace);
    }

    /**
     * Get debug information about current whitelist configuration.
     */
    public static String getDebugInfo() {
        return whitelist.getDebugInfo();
    }
}
