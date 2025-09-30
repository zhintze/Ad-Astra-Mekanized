package com.hecookin.adastramekanized.common.items.armor.materials;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;

public class NetheriteSpaceSuitMaterial {

    public static final ArmorMaterial MATERIAL = new ArmorMaterial(
        // Defense values map for each armor type
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 3);
            map.put(ArmorItem.Type.LEGGINGS, 6);
            map.put(ArmorItem.Type.CHESTPLATE, 8);
            map.put(ArmorItem.Type.HELMET, 3);
            map.put(ArmorItem.Type.BODY, 6);
        }),
        15, // Enchantability
        SoundEvents.ARMOR_EQUIP_NETHERITE, // Equip sound
        () -> Ingredient.of(Items.NETHERITE_INGOT), // Repair ingredient
        java.util.List.of(
            new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "netherite_space_suit")
            )
        ), // Layers
        3.0f, // Toughness
        0.1f  // Knockback resistance
    );
}