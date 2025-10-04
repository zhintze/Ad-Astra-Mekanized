package com.hecookin.adastramekanized.common.entities.vehicles;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

/**
 * Configuration properties for rocket tiers.
 * @param tier Rocket tier (1-4)
 * @param item The rocket item that drops when destroyed
 * @param ridingOffset Y-offset for passenger position
 * @param fuelTag Fluid tag for compatible fuels
 */
public record RocketProperties(
    int tier,
    Item item,
    float ridingOffset,
    TagKey<Fluid> fuelTag
) {
}
