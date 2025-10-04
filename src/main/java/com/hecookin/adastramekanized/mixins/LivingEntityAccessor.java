package com.hecookin.adastramekanized.mixins;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin accessor to check if an entity is jumping (space key pressed).
 * Used by vehicles to detect launch input.
 */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("jumping")
    boolean isJumping();
}
