package com.hecookin.adastramekanized.datagen.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.recipes.NasaWorkbenchRecipe;
import com.hecookin.adastramekanized.common.registry.ModRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for NASA Workbench recipes.
 * This builder creates recipes that match the JSON format expected by our NasaWorkbenchRecipe CODEC.
 */
public class NasaWorkbenchRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private NasaWorkbenchRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
    }

    public static NasaWorkbenchRecipeBuilder builder(RecipeCategory category, ItemLike result, int count) {
        return new NasaWorkbenchRecipeBuilder(category, result, count);
    }

    public static NasaWorkbenchRecipeBuilder builder(RecipeCategory category, ItemLike result) {
        return new NasaWorkbenchRecipeBuilder(category, result, 1);
    }

    public NasaWorkbenchRecipeBuilder addIngredient(ItemLike item) {
        return addIngredient(Ingredient.of(item));
    }

    public NasaWorkbenchRecipeBuilder addIngredient(Ingredient ingredient) {
        if (ingredients.size() >= 9) {
            throw new IllegalStateException("NASA Workbench recipes can only have up to 9 ingredients");
        }
        ingredients.add(ingredient);
        return this;
    }

    public NasaWorkbenchRecipeBuilder addIngredient(ItemLike item, int count) {
        for (int i = 0; i < count; i++) {
            addIngredient(item);
        }
        return this;
    }

    @Override
    public NasaWorkbenchRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        // NASA Workbench recipes don't use groups
        return this;
    }

    @Override
    public Item getResult() {
        return result;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        if (ingredients.isEmpty()) {
            throw new IllegalStateException("NASA Workbench recipe must have at least one ingredient");
        }

        if (criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }

        // Build advancement
        Advancement.Builder advancementBuilder = output.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .requirements(AdvancementRequirements.Strategy.OR);

        criteria.forEach(advancementBuilder::addCriterion);

        // Create the recipe using our custom serializer
        NasaWorkbenchRecipe recipe = new NasaWorkbenchRecipe(ingredients, new ItemStack(result, count));

        // Save the recipe
        output.accept(
            id,
            recipe,
            advancementBuilder.build(id.withPrefix("recipes/" + category.getFolderName() + "/"))
        );

        AdAstraMekanized.LOGGER.debug("Generated NASA Workbench recipe: {}", id);
    }

    /**
     * Alternative save method that generates the JSON directly for debugging
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        // Recipe type
        json.addProperty("type", "adastramekanized:nasa_workbench");

        // Ingredients array
        JsonArray ingredientsArray = new JsonArray();
        for (Ingredient ingredient : ingredients) {
            // In NeoForge 1.21.1, Ingredient serialization doesn't have a boolean parameter
            // We can just use the toJson() method or skip this method entirely
            // since we're using the proper save() method with RecipeOutput
            ingredientsArray.add(new JsonObject()); // Placeholder for now
        }
        json.add("ingredients", ingredientsArray);

        // Result object
        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("item", result.builtInRegistryHolder().key().location().toString());
        if (count > 1) {
            resultObject.addProperty("count", count);
        }
        json.add("result", resultObject);

        return json;
    }
}