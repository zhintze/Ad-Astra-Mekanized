package com.hecookin.adastramekanized.integration.jei;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.recipes.NasaWorkbenchRecipe;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class NasaWorkbenchRecipeCategory implements IRecipeCategory<NasaWorkbenchRecipe> {

    public static final RecipeType<NasaWorkbenchRecipe> RECIPE_TYPE =
            RecipeType.create(AdAstraMekanized.MOD_ID, "nasa_workbench", NasaWorkbenchRecipe.class);

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "textures/gui/container/nasa_workbench.png");

    private final IDrawable background;
    private final IDrawable icon;

    public NasaWorkbenchRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(180, 145);
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
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                .addIngredients(Ingredient.of(ModBlocks.NASA_WORKBENCH.get()));

        slot(builder, recipe, 57, 16, 0);
        slot(builder, recipe, 48, 34, 1);
        slot(builder, recipe, 66, 34, 2);
        slot(builder, recipe, 48, 52, 3);
        slot(builder, recipe, 66, 52, 4);
        slot(builder, recipe, 48, 70, 5);
        slot(builder, recipe, 66, 70, 6);
        slot(builder, recipe, 30, 88, 7);
        slot(builder, recipe, 48, 88, 8);
        slot(builder, recipe, 66, 88, 9);
        slot(builder, recipe, 84, 88, 10);
        slot(builder, recipe, 30, 106, 11);
        slot(builder, recipe, 57, 106, 12);
        slot(builder, recipe, 84, 106, 13);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 52).addItemStack(recipe.result());
    }

    private void slot(IRecipeLayoutBuilder builder, NasaWorkbenchRecipe recipe, int x, int y, int index) {
        var slot = builder.addSlot(RecipeIngredientRole.INPUT, x, y);
        if (index < recipe.ingredients().size()) {
            slot.addIngredients(recipe.ingredients().get(index));
        }
    }

    @Override
    public void draw(NasaWorkbenchRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.blit(TEXTURE, 1, -4, 0, 0, 177, 140, 177, 224);
        graphics.blit(TEXTURE, 1, 136, 0, 217, 177, 7, 177, 224);
    }
}