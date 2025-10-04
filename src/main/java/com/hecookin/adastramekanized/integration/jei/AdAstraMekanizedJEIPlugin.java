package com.hecookin.adastramekanized.integration.jei;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.NasaWorkbenchBlockEntity;
import com.hecookin.adastramekanized.common.menus.NasaWorkbenchMenu;
import com.hecookin.adastramekanized.common.recipes.NasaWorkbenchRecipe;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import com.hecookin.adastramekanized.common.registry.ModRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class AdAstraMekanizedJEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "jei_plugin");
    public static final ResourceLocation NASA_WORKBENCH_CATEGORY_ID = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "nasa_workbench");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        AdAstraMekanized.LOGGER.info("JEI: Registering recipe categories...");
        registration.addRecipeCategories(new NasaWorkbenchRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        AdAstraMekanized.LOGGER.info("JEI: Recipe categories registered");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        AdAstraMekanized.LOGGER.info("JEI: Starting recipe registration...");

        if (Minecraft.getInstance().level == null) {
            AdAstraMekanized.LOGGER.error("JEI: Minecraft level is null, cannot register recipes!");
            return;
        }

        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        AdAstraMekanized.LOGGER.info("JEI: Recipe manager obtained");

        if (ModRecipeTypes.NASA_WORKBENCH.get() == null) {
            AdAstraMekanized.LOGGER.error("JEI: NASA_WORKBENCH recipe type is NULL!");
            return;
        }

        List<NasaWorkbenchRecipe> nasaWorkbenchRecipes = recipeManager
                .getAllRecipesFor(ModRecipeTypes.NASA_WORKBENCH.get()).stream()
                .map(RecipeHolder::value)
                .toList();

        AdAstraMekanized.LOGGER.info("JEI: Found {} NASA Workbench recipes", nasaWorkbenchRecipes.size());

        if (!nasaWorkbenchRecipes.isEmpty()) {
            registration.addRecipes(NasaWorkbenchRecipeCategory.RECIPE_TYPE, nasaWorkbenchRecipes);
            AdAstraMekanized.LOGGER.info("JEI: Successfully registered {} NASA Workbench recipes", nasaWorkbenchRecipes.size());
        } else {
            AdAstraMekanized.LOGGER.warn("JEI: No NASA Workbench recipes found to register!");
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        AdAstraMekanized.LOGGER.info("JEI: Registering recipe catalysts...");
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.NASA_WORKBENCH.get()), NasaWorkbenchRecipeCategory.RECIPE_TYPE);
        AdAstraMekanized.LOGGER.info("JEI: Recipe catalysts registered");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        AdAstraMekanized.LOGGER.info("JEI: Registering recipe transfer handlers...");
        registration.addRecipeTransferHandler(NasaWorkbenchMenu.class, ModMenuTypes.NASA_WORKBENCH.get(),
                NasaWorkbenchRecipeCategory.RECIPE_TYPE, 0, 14, 15, 36);
        AdAstraMekanized.LOGGER.info("JEI: Recipe transfer handlers registered");
    }
}