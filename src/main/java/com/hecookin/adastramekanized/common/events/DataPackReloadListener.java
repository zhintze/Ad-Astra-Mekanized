package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * Handles data pack reloading for planet equipment configurations
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class DataPackReloadListener implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        // Load equipment configurations from data packs
        var configs = PlanetMobEquipmentLoader.loadFromResourceManager(resourceManager);
        PlanetMobSpawnHandler.setEquipmentConfigs(configs);
        AdAstraMekanized.LOGGER.info("Reloaded planet mob equipment configurations");
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DataPackReloadListener());
    }
}