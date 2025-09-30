package com.hecookin.adastramekanized.mixins;

import com.hecookin.adastramekanized.client.ArmorModelRegistry;
import com.hecookin.adastramekanized.client.models.armor.SpaceSuitModel;
import com.hecookin.adastramekanized.common.items.armor.JetSuitItem;
import com.hecookin.adastramekanized.common.items.armor.base.CustomDyeableArmorItem;
import com.hecookin.adastramekanized.common.tags.ModItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(CustomDyeableArmorItem.class)
public abstract class CustomDyeableArmorItemMixin extends Item {

    public CustomDyeableArmorItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ArmorModelRegistry.ArmorRenderer renderer;

            @SuppressWarnings("unchecked")
            private static <T extends LivingEntity> void uncheckedCopyTo(HumanoidModel<T> from, HumanoidModel<?> to) {
                from.copyPropertiesTo((HumanoidModel<T>) to);
            }

            @SuppressWarnings("unchecked")
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (renderer == null) {
                    renderer = ArmorModelRegistry.getRenderer(stack.getItem());
                }
                if (renderer == null) return original;

                var root = Minecraft.getInstance().getEntityModels().bakeLayer(renderer.layer());
                var model = renderer.factory().create(root, slot, stack, (HumanoidModel<LivingEntity>) original);

                if (stack.getItem() instanceof JetSuitItem suit) {
                    suit.spawnParticles(entity.level(), entity, original, stack);
                }

                return model;
            }

            @Override
            public @NotNull Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                HumanoidModel<?> replacement = getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
                if (replacement != original) {
                    uncheckedCopyTo(original, replacement);
                    return replacement;
                } else {
                    return original;
                }
            }
        });
    }

    @Nullable
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, @Nullable String type) {
        if (stack.is(ModItemTags.JET_SUITS)) {
            return SpaceSuitModel.JET_SUIT_TEXTURE.toString();
        } else if (stack.is(ModItemTags.NETHERITE_SPACE_SUITS)) {
            return SpaceSuitModel.NETHERITE_SPACE_SUIT_TEXTURE.toString();
        } else if (stack.is(ModItemTags.SPACE_SUITS)) {
            return SpaceSuitModel.SPACE_SUIT_TEXTURE.toString();
        } else {
            return null;
        }
    }
}