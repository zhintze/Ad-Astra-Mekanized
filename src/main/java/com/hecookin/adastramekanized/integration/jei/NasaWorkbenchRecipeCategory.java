package com.hecookin.adastramekanized.integration.jei;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.recipes.NasaWorkbenchRecipe;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class NasaWorkbenchRecipeCategory implements IRecipeCategory<NasaWorkbenchRecipe> {

    public static final RecipeType<NasaWorkbenchRecipe> RECIPE_TYPE =
            RecipeType.create(AdAstraMekanized.MOD_ID, "nasa_workbench", NasaWorkbenchRecipe.class);

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "textures/gui/container/nasa_workbench_jei.png");

    private final IDrawable background;
    private final IDrawable icon;

    public NasaWorkbenchRecipeCategory(IGuiHelper guiHelper) {
        // Use the NASA Workbench GUI texture
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 176, 166);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.NASA_WORKBENCH.get()));
    }

    @Override
    public RecipeType<NasaWorkbenchRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("container.adastramekanized.nasa_workbench");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, NasaWorkbenchRecipe recipe, IFocusGroup focuses) {
        // Set up the input slots based on the NASA Workbench GUI layout
        // These positions match the slot positions in NasaWorkbenchMenu

        // Nose cone slot (slot 0)
        addIngredientSlot(builder, recipe, 0, 56, 20);

        // Body slots (slots 1-6)
        addIngredientSlot(builder, recipe, 1, 47, 38);
        addIngredientSlot(builder, recipe, 2, 65, 38);
        addIngredientSlot(builder, recipe, 3, 47, 56);
        addIngredientSlot(builder, recipe, 4, 65, 56);
        addIngredientSlot(builder, recipe, 5, 47, 74);
        addIngredientSlot(builder, recipe, 6, 65, 74);

        // Engine and fin slots (slots 7-13)
        addIngredientSlot(builder, recipe, 7, 29, 92);
        addIngredientSlot(builder, recipe, 8, 47, 92);
        addIngredientSlot(builder, recipe, 9, 65, 92);
        addIngredientSlot(builder, recipe, 10, 83, 92);
        addIngredientSlot(builder, recipe, 11, 29, 110);
        addIngredientSlot(builder, recipe, 12, 56, 110);
        addIngredientSlot(builder, recipe, 13, 83, 110);

        // Output slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 129, 56)
                .addItemStack(recipe.result());

        // TODO: Warning/note message display for JEI recipe view
        // If warnings need to be displayed in the future, they can be added here as tooltip info
        // on specific slots using .addTooltipCallback() or as custom drawable elements
        // Example: builder.addSlot(...).addTooltipCallback((recipeSlotView, tooltip) -> {
        //     if (hasWarning(recipe)) {
        //         tooltip.add(Component.literal("Warning: ..."));
        //     }
        // });
    }

    private void addIngredientSlot(IRecipeLayoutBuilder builder, NasaWorkbenchRecipe recipe, int index, int x, int y) {
        if (index < recipe.ingredients().size()) {
            Ingredient ingredient = recipe.ingredients().get(index);
            if (!ingredient.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .addIngredients(ingredient);
            }
        }
    }
}