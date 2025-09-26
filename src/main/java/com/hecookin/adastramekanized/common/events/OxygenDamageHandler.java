package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
import com.hecookin.adastramekanized.common.tags.ModEntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Handles oxygen damage for entities in non-breathable environments.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class OxygenDamageHandler {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (event.getEntity().level().isClientSide()) return;

        // Skip entities that don't need oxygen
        if (living.getType().is(ModEntityTypeTags.LIVES_WITHOUT_OXYGEN)) return;
        if (living.getType().is(ModEntityTypeTags.CAN_SURVIVE_IN_SPACE)) return;

        // Additional check for Mowzie's Mobs entities by namespace
        String entityId = living.getType().getDescriptionId();
        if (entityId != null && entityId.contains("mowziesmobs")) {
            return; // All Mowzie's Mobs are exempt from oxygen damage
        }

        // Skip spectators and creative players
        if (living instanceof Player player) {
            if (player.isSpectator() || player.isCreative()) {
                return;
            }
        }

        // Apply oxygen effects
        OxygenManager.getInstance().applyOxygenEffects(living);
    }
}