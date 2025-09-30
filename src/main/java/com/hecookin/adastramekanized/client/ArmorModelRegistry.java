package com.hecookin.adastramekanized.client;

import com.hecookin.adastramekanized.client.models.armor.SpaceSuitModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for custom armor models
 */
public class ArmorModelRegistry {

    private static final Map<Item, ArmorRenderer> ARMOR_RENDERERS = new HashMap<>();

    public static void register(Item item, ArmorRenderer renderer) {
        ARMOR_RENDERERS.put(item, renderer);
    }

    @Nullable
    public static ArmorRenderer getRenderer(Item item) {
        return ARMOR_RENDERERS.get(item);
    }

    @FunctionalInterface
    public interface ArmorModelFactory {
        HumanoidModel<LivingEntity> create(ModelPart root, EquipmentSlot slot, ItemStack stack, HumanoidModel<LivingEntity> parent);
    }

    public record ArmorRenderer(ModelLayerLocation layer, ArmorModelFactory factory) {
    }
}