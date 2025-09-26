package com.hecookin.adastramekanized.integration.mowziesmobs;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Integration for Mowzie's Mobs - provides controlled spawning on custom planets
 * while preventing default spawning in the overworld.
 *
 * Configuration: Set all spawn_rate values to 0 in mowziesmobs-common.toml to disable default spawning
 */
public class MowziesMobsIntegration {
    private static final String MOWZIES_MODID = "mowziesmobs";

    // Map of mob names to their entity type resource locations
    private static final Map<String, String> MOWZIE_MOBS = new HashMap<>();

    static {
        // Main mobs that can be spawned naturally
        MOWZIE_MOBS.put("foliaath", "mowziesmobs:foliaath");
        MOWZIE_MOBS.put("baby_foliaath", "mowziesmobs:baby_foliaath");
        MOWZIE_MOBS.put("umvuthana_raptor", "mowziesmobs:umvuthana_raptor");
        MOWZIE_MOBS.put("umvuthana_crane", "mowziesmobs:umvuthana_crane");
        MOWZIE_MOBS.put("umvuthana", "mowziesmobs:umvuthana");
        MOWZIE_MOBS.put("grottol", "mowziesmobs:grottol");
        MOWZIE_MOBS.put("lantern", "mowziesmobs:lantern");
        MOWZIE_MOBS.put("naga", "mowziesmobs:naga");
        MOWZIE_MOBS.put("bluff", "mowziesmobs:bluff");

        // Boss mobs (typically structure-spawned, but can be configured)
        MOWZIE_MOBS.put("frostmaw", "mowziesmobs:frostmaw");
        MOWZIE_MOBS.put("ferrous_wroughtnaut", "mowziesmobs:ferrous_wroughtnaut");
        MOWZIE_MOBS.put("umvuthi", "mowziesmobs:umvuthi");
        MOWZIE_MOBS.put("sculptor", "mowziesmobs:sculptor");
    }

    /**
     * Check if Mowzie's Mobs is loaded
     */
    public static boolean isLoaded() {
        return net.neoforged.fml.ModList.get().isLoaded(MOWZIES_MODID);
    }

    /**
     * Get the EntityType for a Mowzie's mob by name
     */
    public static Optional<EntityType<?>> getMobEntityType(String mobName) {
        if (!isLoaded()) {
            return Optional.empty();
        }

        String resourceLocation = MOWZIE_MOBS.get(mobName.toLowerCase());
        if (resourceLocation == null) {
            AdAstraMekanized.LOGGER.warn("Unknown Mowzie's mob: " + mobName);
            return Optional.empty();
        }

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(resourceLocation));
        if (entityType == null) {
            AdAstraMekanized.LOGGER.warn("Could not find EntityType for Mowzie's mob: " + resourceLocation);
            return Optional.empty();
        }

        return Optional.of(entityType);
    }

    /**
     * Get all available Mowzie's mob names
     */
    public static Map<String, String> getAvailableMobs() {
        return new HashMap<>(MOWZIE_MOBS);
    }

    /**
     * Initialize the integration
     */
    public static void init() {
        if (isLoaded()) {
            AdAstraMekanized.LOGGER.info("Mowzie's Mobs integration initialized");
            AdAstraMekanized.LOGGER.info("Available Mowzie's mobs for planet spawning: " + MOWZIE_MOBS.keySet());
            AdAstraMekanized.LOGGER.info("IMPORTANT: Set all spawn_rate values to 0 in mowziesmobs-common.toml to disable default spawning!");
        } else {
            AdAstraMekanized.LOGGER.info("Mowzie's Mobs not found - integration disabled");
        }
    }

    /**
     * Configuration instructions for disabling default spawning
     */
    public static String getDisableSpawningInstructions() {
        return """
            To disable default Mowzie's Mobs spawning and allow only controlled spawning on custom planets:

            1. Edit config/mowziesmobs-common.toml
            2. Set all spawn_rate values to 0:
               - mobs.foliaath.spawn_config.spawn_rate = 0
               - mobs.umvuthana.spawn_config.spawn_rate = 0
               - mobs.grottol.spawn_config.spawn_rate = 0
               - mobs.lantern.spawn_config.spawn_rate = 0
               - mobs.naga.spawn_config.spawn_rate = 0
               - mobs.bluff.spawn_config.spawn_rate = 0
            3. Structure spawning (frostmaw, umvuthi, etc.) can be disabled by setting generation_distance = -1

            This ensures mobs only spawn where you specify in your custom dimensions!
            """;
    }
}