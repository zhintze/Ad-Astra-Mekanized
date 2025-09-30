package com.hecookin.adastramekanized.common.items.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NetheriteSpaceSuitItem extends SpaceSuitItem {

    public NetheriteSpaceSuitItem(Holder<ArmorMaterial> material, ArmorItem.Type type, long tankSize, Item.Properties properties) {
        super(material, type, tankSize, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        // Netherite space suits provide fire resistance
        if (!level.isClientSide() && entity instanceof LivingEntity livingEntity) {
            if (hasFullNetheriteSet(livingEntity)) {
                livingEntity.clearFire();
            }
        }
    }
}