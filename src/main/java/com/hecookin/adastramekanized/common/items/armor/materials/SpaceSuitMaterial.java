package com.hecookin.adastramekanized.common.items.armor.materials;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.tags.ModItemTags;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;

public class SpaceSuitMaterial {

    public static final ArmorMaterial MATERIAL = new ArmorMaterial(
        // Defense values map for each armor type
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 2);
            map.put(ArmorItem.Type.LEGGINGS, 5);
            map.put(ArmorItem.Type.CHESTPLATE, 6);
            map.put(ArmorItem.Type.HELMET, 2);
            map.put(ArmorItem.Type.BODY, 5);
        }),
        14, // Enchantability
        SoundEvents.ARMOR_EQUIP_LEATHER, // Equip sound
        () -> Ingredient.of(ModItemTags.STEEL_INGOTS), // Repair ingredient
        java.util.List.of(
            new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "space_suit")
            )
        ), // Layers
        0.0f, // Toughness
        0.0f  // Knockback resistance
    );
}