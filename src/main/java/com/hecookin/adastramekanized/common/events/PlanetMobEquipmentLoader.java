package com.hecookin.adastramekanized.common.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Loads mob equipment configurations from generated planet files
 */
public class PlanetMobEquipmentLoader {

    private static final String EQUIPMENT_PATH = "data/adastramekanized/planet_equipment/";
    private static final String RESOURCE_PACK_PATH = "planet_equipment";

    /**
     * Load all equipment configurations from generated files
     */
    public static Map<ResourceLocation, Map<String, PlanetMobSpawnHandler.EquipmentConfig>> loadEquipmentConfigs() {
        Map<ResourceLocation, Map<String, PlanetMobSpawnHandler.EquipmentConfig>> configs = new HashMap<>();

        // First try to load from the generated resources directory (for development)
        Path resourcesPath = Paths.get("src/main/resources/" + EQUIPMENT_PATH);
        if (Files.exists(resourcesPath)) {
            try (Stream<Path> paths = Files.walk(resourcesPath)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> path.toString().endsWith("_equipment.json"))
                     .forEach(path -> loadConfigFile(path, configs));
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to load equipment configs from resources directory", e);
            }
        }

        // Also try to load from the jar/mod resources (for production)
        // This would need ResourceManager access which happens during data loading

        AdAstraMekanized.LOGGER.info("Loaded equipment configurations for " + configs.size() + " dimensions");
        return configs;
    }

    /**
     * Load equipment from resource manager (called during reload)
     */
    public static Map<ResourceLocation, Map<String, PlanetMobSpawnHandler.EquipmentConfig>> loadFromResourceManager(ResourceManager resourceManager) {
        Map<ResourceLocation, Map<String, PlanetMobSpawnHandler.EquipmentConfig>> configs = new HashMap<>();

        // Find all equipment files in the resource pack
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
            RESOURCE_PACK_PATH,
            path -> path.getPath().endsWith("_equipment.json")
        );

        AdAstraMekanized.LOGGER.info("Found " + resources.size() + " equipment files in resource pack");

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            AdAstraMekanized.LOGGER.debug("Loading equipment file: " + entry.getKey());
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                parseEquipmentJson(json, configs);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.error("Failed to load equipment config: " + entry.getKey(), e);
            }
        }

        return configs;
    }

    private static void loadConfigFile(Path path, Map<ResourceLocation, Map<String, PlanetMobSpawnHandler.EquipmentConfig>> configs) {
        try {
            String content = Files.readString(path);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            parseEquipmentJson(json, configs);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to load equipment config file: " + path, e);
        }
    }

    private static void parseEquipmentJson(JsonObject json, Map<ResourceLocation, Map<String, PlanetMobSpawnHandler.EquipmentConfig>> configs) {
        String dimension = json.get("dimension").getAsString();
        ResourceLocation dimId = ResourceLocation.tryParse(dimension);
        if (dimId == null) {
            AdAstraMekanized.LOGGER.warn("Invalid dimension ID in equipment config: " + dimension);
            return;
        }

        Map<String, PlanetMobSpawnHandler.EquipmentConfig> mobConfigs = new HashMap<>();

        JsonArray mobs = json.getAsJsonArray("mobs");
        for (JsonElement element : mobs) {
            JsonObject mobConfig = element.getAsJsonObject();
            String mobId = mobConfig.get("mob_id").getAsString();

            PlanetMobSpawnHandler.EquipmentConfig equipment = new PlanetMobSpawnHandler.EquipmentConfig();

            // Parse equipment slots
            JsonObject equipmentJson = mobConfig.getAsJsonObject("equipment");
            if (equipmentJson.has("helmet")) {
                equipment.withHelmet(equipmentJson.get("helmet").getAsString(), 1.0f);
            }
            if (equipmentJson.has("chestplate")) {
                equipment.withChestplate(equipmentJson.get("chestplate").getAsString(), 1.0f);
            }
            if (equipmentJson.has("leggings")) {
                equipment.withLeggings(equipmentJson.get("leggings").getAsString(), 1.0f);
            }
            if (equipmentJson.has("boots")) {
                equipment.withBoots(equipmentJson.get("boots").getAsString(), 1.0f);
            }
            if (equipmentJson.has("mainhand")) {
                equipment.withWeapon(equipmentJson.get("mainhand").getAsString(), 1.0f);
            }
            if (equipmentJson.has("offhand")) {
                equipment.withOffhand(equipmentJson.get("offhand").getAsString(), 1.0f);
            }

            // Get drop chance (default 0)
            float dropChance = mobConfig.has("drop_chance") ? mobConfig.get("drop_chance").getAsFloat() : 0.0f;
            equipment.setDropChance(dropChance);

            mobConfigs.put(mobId, equipment);
        }

        configs.put(dimId, mobConfigs);
        AdAstraMekanized.LOGGER.info("Loaded " + mobConfigs.size() + " mob equipment configs for dimension " + dimId);
    }
}