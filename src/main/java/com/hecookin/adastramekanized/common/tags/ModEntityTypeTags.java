package com.hecookin.adastramekanized.common.tags;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * Entity type tags for the mod.
 * Defines which entities are exempt from oxygen damage.
 */
public final class ModEntityTypeTags {

    public static final TagKey<EntityType<?>> LIVES_WITHOUT_OXYGEN = tag("lives_without_oxygen");
    public static final TagKey<EntityType<?>> CAN_SURVIVE_IN_SPACE = tag("can_survive_in_space");

    private static TagKey<EntityType<?>> tag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, name));
    }
}