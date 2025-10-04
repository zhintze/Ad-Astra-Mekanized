package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.registry.ModRecipeTypes;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class RecipeDebugEvents {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        AdAstraMekanized.LOGGER.info("=== SERVER STARTED - CHECKING RECIPES ===");

        RecipeManager recipeManager = event.getServer().getRecipeManager();

        // Count all recipes from our mod
        long totalAdAstraRecipes = recipeManager.getRecipes().stream()
                .filter(holder -> holder.id().getNamespace().equals(AdAstraMekanized.MOD_ID))
                .count();

        AdAstraMekanized.LOGGER.info("Total AdAstraMekanized recipes loaded: {}", totalAdAstraRecipes);

        // List all recipe IDs from our mod
        recipeManager.getRecipes().stream()
                .filter(holder -> holder.id().getNamespace().equals(AdAstraMekanized.MOD_ID))
                .forEach(holder -> {
                    ResourceLocation id = holder.id();
                    String type = holder.value().getType().toString();
                    AdAstraMekanized.LOGGER.info("  Recipe: {} (type: {})", id, type);
                });

        // Check NASA Workbench recipes specifically
        if (ModRecipeTypes.NASA_WORKBENCH.get() != null) {
            var nasaRecipes = recipeManager.getAllRecipesFor(ModRecipeTypes.NASA_WORKBENCH.get());
            AdAstraMekanized.LOGGER.info("NASA Workbench recipes loaded: {}", nasaRecipes.size());
            nasaRecipes.forEach(holder -> {
                AdAstraMekanized.LOGGER.info("  NASA Recipe: {}", holder.id());
            });
        } else {
            AdAstraMekanized.LOGGER.error("NASA_WORKBENCH recipe type is NULL at server start!");
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        AdAstraMekanized.LOGGER.info("=== RELOAD LISTENER EVENT - RECIPES ABOUT TO LOAD ===");
        AdAstraMekanized.LOGGER.info("NASA_WORKBENCH recipe type registered: {}", ModRecipeTypes.NASA_WORKBENCH.get() != null);
    }
}