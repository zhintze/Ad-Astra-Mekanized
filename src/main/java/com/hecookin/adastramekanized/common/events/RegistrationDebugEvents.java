package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.registry.ModRecipeTypes;
import com.hecookin.adastramekanized.common.registry.ModRecipeSerializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class RegistrationDebugEvents {

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: RegisterEvent fired for key: {}",
            event.getRegistryKey().location());

        if (event.getRegistryKey().equals(Registries.RECIPE_TYPE)) {
            AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: Recipe Type registration happening NOW!");

            // Try to force get the recipe type
            if (ModRecipeTypes.NASA_WORKBENCH != null) {
                AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: NASA_WORKBENCH supplier exists");
                try {
                    var type = ModRecipeTypes.NASA_WORKBENCH.get();
                    AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: NASA_WORKBENCH type retrieved: {}", type);
                } catch (Exception e) {
                    AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: Failed to get NASA_WORKBENCH type", e);
                }
            } else {
                AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: NASA_WORKBENCH supplier is NULL!");
            }
        }

        if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
            AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: Recipe Serializer registration happening NOW!");

            if (ModRecipeSerializers.NASA_WORKBENCH != null) {
                AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: NASA_WORKBENCH serializer supplier exists");
                try {
                    var serializer = ModRecipeSerializers.NASA_WORKBENCH.get();
                    AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: NASA_WORKBENCH serializer retrieved: {}", serializer);
                } catch (Exception e) {
                    AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: Failed to get NASA_WORKBENCH serializer", e);
                }
            } else {
                AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: NASA_WORKBENCH serializer supplier is NULL!");
            }
        }
    }

    @SubscribeEvent
    public static void onConstructMod(FMLConstructModEvent event) {
        AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: FMLConstructModEvent fired");
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: FMLLoadCompleteEvent - checking final registration state");

        // Check if recipe type is registered
        var allRecipeTypes = BuiltInRegistries.RECIPE_TYPE.keySet();
        AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: Total recipe types registered: {}", allRecipeTypes.size());

        boolean found = false;
        for (var key : allRecipeTypes) {
            if (key.getNamespace().equals(AdAstraMekanized.MOD_ID)) {
                AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: Found our recipe type: {}", key);
                found = true;
            }
        }

        if (!found) {
            AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: NO RECIPE TYPES FROM OUR MOD FOUND!");
        }

        // Check if serializer is registered
        var allSerializers = BuiltInRegistries.RECIPE_SERIALIZER.keySet();
        AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: Total recipe serializers registered: {}", allSerializers.size());

        found = false;
        for (var key : allSerializers) {
            if (key.getNamespace().equals(AdAstraMekanized.MOD_ID)) {
                AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: Found our serializer: {}", key);
                found = true;
            }
        }

        if (!found) {
            AdAstraMekanized.LOGGER.error("REGISTRATION DEBUG: NO RECIPE SERIALIZERS FROM OUR MOD FOUND!");
        }
    }
}