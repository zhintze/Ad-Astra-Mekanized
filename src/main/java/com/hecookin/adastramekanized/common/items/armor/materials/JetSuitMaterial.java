package com.hecookin.adastramekanized.common.items.armor.materials;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;

public class JetSuitMaterial {

    public static final ArmorMaterial MATERIAL = new ArmorMaterial(
        // Defense values map for each armor type (stronger than netherite)
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 4);
            map.put(ArmorItem.Type.LEGGINGS, 7);
            map.put(ArmorItem.Type.CHESTPLATE, 9);
            map.put(ArmorItem.Type.HELMET, 4);
            map.put(ArmorItem.Type.BODY, 7);
        }),
        20, // Enchantability
        SoundEvents.ARMOR_EQUIP_NETHERITE, // Equip sound
        () -> Ingredient.of(ModItems.CALORITE_INGOT.get()), // Repair ingredient
        java.util.List.of(
            new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "jet_suit")
            )
        ), // Layers
        4.0f, // Toughness
        0.2f  // Knockback resistance
    );
}