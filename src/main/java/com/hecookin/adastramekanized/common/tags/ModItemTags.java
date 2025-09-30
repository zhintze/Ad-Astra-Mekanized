package com.hecookin.adastramekanized.common.tags;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {

    // Metal ingot tags
    public static final TagKey<Item> STEEL_INGOTS = tag("steel_ingots");
    public static final TagKey<Item> DESH_INGOTS = tag("desh_ingots");
    public static final TagKey<Item> OSTRUM_INGOTS = tag("ostrum_ingots");
    public static final TagKey<Item> CALORITE_INGOTS = tag("calorite_ingots");

    // Space suit tags
    public static final TagKey<Item> SPACE_SUITS = tag("space_suits");
    public static final TagKey<Item> NETHERITE_SPACE_SUITS = tag("netherite_space_suits");
    public static final TagKey<Item> JET_SUITS = tag("jet_suits");

    // Heat and freeze resistant armor
    public static final TagKey<Item> HEAT_RESISTANT_ARMOR = tag("heat_resistant_armor");
    public static final TagKey<Item> FREEZE_RESISTANT_ARMOR = tag("freeze_resistant_armor");

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, name));
    }
}