package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.common.world.SpaceStationBlockProtection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Handles space station block protection.
 * Prevents players from breaking protected space station blocks.
 */
@EventBusSubscriber(modid = com.hecookin.adastramekanized.AdAstraMekanized.MOD_ID)
public class SpaceStationProtectionHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        SpaceStationBlockProtection protection = SpaceStationBlockProtection.get(level);

        if (protection.isProtected(event.getPos())) {
            // Cancel the block break
            event.setCanceled(true);

            // Send message to player
            if (event.getPlayer() != null) {
                event.getPlayer().displayClientMessage(
                    Component.literal("Property of NASA. Destruction prohibited."),
                    true // actionBar = true (shows above hotbar)
                );
            }

            com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.debug(
                "Blocked player {} from breaking protected block at {}",
                event.getPlayer() != null ? event.getPlayer().getName().getString() : "Unknown",
                event.getPos()
            );
        }
    }
}
