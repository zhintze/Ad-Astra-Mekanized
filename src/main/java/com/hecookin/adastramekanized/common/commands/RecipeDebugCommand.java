package com.hecookin.adastramekanized.common.commands;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.registry.ModRecipeTypes;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Map;
import java.util.stream.Collectors;

public class RecipeDebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("recipedebug")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    listAllRecipes(context.getSource());
                    return 1;
                })
                .then(Commands.literal("nasa")
                        .executes(context -> {
                            listNasaWorkbenchRecipes(context.getSource());
                            return 1;
                        }))
                .then(Commands.literal("types")
                        .executes(context -> {
                            listRecipeTypes(context.getSource());
                            return 1;
                        }))
        );

        AdAstraMekanized.LOGGER.info("Registered recipe debug command");
    }

    private static void listAllRecipes(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        RecipeManager recipeManager = level.getRecipeManager();

        source.sendSystemMessage(Component.literal("=== ALL RECIPE COUNTS BY TYPE ==="));

        Map<RecipeType<?>, Integer> recipeCounts = recipeManager.getRecipes().stream()
                .collect(Collectors.groupingBy(
                        holder -> holder.value().getType(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        recipeCounts.forEach((type, count) -> {
            String typeString = type.toString();
            source.sendSystemMessage(Component.literal(String.format("Type: %s, Count: %d", typeString, count)));

            // Log all adastramekanized recipes
            if (typeString.contains("adastramekanized")) {
                AdAstraMekanized.LOGGER.info("Found {} recipes of type: {}", count, typeString);
                recipeManager.getRecipes().stream()
                        .filter(holder -> holder.value().getType() == type)
                        .forEach(holder -> {
                            ResourceLocation id = holder.id();
                            AdAstraMekanized.LOGGER.info("  - Recipe ID: {}", id);
                            source.sendSystemMessage(Component.literal("  - " + id));
                        });
            }
        });

        long totalAdastramekanized = recipeManager.getRecipes().stream()
                .filter(holder -> holder.id().getNamespace().equals(AdAstraMekanized.MOD_ID))
                .count();

        source.sendSystemMessage(Component.literal("Total AdAstraMekanized recipes: " + totalAdastramekanized));
        AdAstraMekanized.LOGGER.info("Total AdAstraMekanized recipes: {}", totalAdastramekanized);
    }

    private static void listNasaWorkbenchRecipes(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        RecipeManager recipeManager = level.getRecipeManager();

        source.sendSystemMessage(Component.literal("=== NASA WORKBENCH RECIPES ==="));

        AdAstraMekanized.LOGGER.error("DEBUG: Checking NASA_WORKBENCH supplier...");
        if (ModRecipeTypes.NASA_WORKBENCH == null) {
            source.sendSystemMessage(Component.literal("ERROR: NASA_WORKBENCH supplier is NULL!"));
            AdAstraMekanized.LOGGER.error("NASA_WORKBENCH supplier is NULL!");
            return;
        }

        AdAstraMekanized.LOGGER.error("DEBUG: Getting NASA_WORKBENCH recipe type...");
        RecipeType<?> recipeType = null;
        try {
            recipeType = ModRecipeTypes.NASA_WORKBENCH.get();
            AdAstraMekanized.LOGGER.error("DEBUG: Got recipe type: {}", recipeType);
        } catch (Exception e) {
            source.sendSystemMessage(Component.literal("ERROR: Exception getting NASA_WORKBENCH recipe type!"));
            AdAstraMekanized.LOGGER.error("Exception getting NASA_WORKBENCH recipe type!", e);
            return;
        }

        if (recipeType == null) {
            source.sendSystemMessage(Component.literal("ERROR: NASA_WORKBENCH recipe type is NULL!"));
            AdAstraMekanized.LOGGER.error("NASA_WORKBENCH recipe type is NULL!");
            return;
        }

        var nasaRecipes = recipeManager.getAllRecipesFor((RecipeType) recipeType);

        source.sendSystemMessage(Component.literal("NASA Workbench recipes found: " + nasaRecipes.size()));
        AdAstraMekanized.LOGGER.error("NASA Workbench recipes found: {}", nasaRecipes.size());

        for (var recipeHolder : nasaRecipes) {
            RecipeHolder<?> holder = (RecipeHolder<?>) recipeHolder;
            ResourceLocation id = holder.id();
            source.sendSystemMessage(Component.literal("  - " + id));
            AdAstraMekanized.LOGGER.error("  - NASA Recipe: {}", id);
        }
    }

    private static void listRecipeTypes(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        RecipeManager recipeManager = level.getRecipeManager();

        source.sendSystemMessage(Component.literal("=== REGISTERED RECIPE TYPES ==="));

        // List all unique recipe types
        var uniqueTypes = recipeManager.getRecipes().stream()
                .map(holder -> holder.value().getType())
                .map(RecipeType::toString)
                .collect(Collectors.toSet());

        uniqueTypes.stream()
                .sorted()
                .forEach(type -> {
                    source.sendSystemMessage(Component.literal("  - " + type));
                    if (type.contains("nasa")) {
                        AdAstraMekanized.LOGGER.info("Found NASA recipe type: {}", type);
                    }
                });

        source.sendSystemMessage(Component.literal("Total recipe types: " + uniqueTypes.size()));
    }
}