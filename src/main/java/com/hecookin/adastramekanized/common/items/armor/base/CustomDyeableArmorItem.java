package com.hecookin.adastramekanized.common.items.armor.base;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.core.component.DataComponents;

public class CustomDyeableArmorItem extends ArmorItem {

    public CustomDyeableArmorItem(Holder<ArmorMaterial> armorMaterial, ArmorItem.Type type, Item.Properties properties) {
        super(armorMaterial, type, properties.stacksTo(1));
    }

    // Makes the default color white instead of brown
    public int getColor(ItemStack stack) {
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        if (dyed != null) {
            int color = dyed.rgb();
            return color == 0xa06540 ? 0xFFFFFF : color;
        }
        return 0xFFFFFF; // Default to white
    }
}