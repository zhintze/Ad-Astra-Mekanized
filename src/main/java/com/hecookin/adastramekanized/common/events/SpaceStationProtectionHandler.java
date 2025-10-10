package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.common.world.SpaceStationBlockProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles space station block protection.
 * Prevents ALL damage to protected space station blocks (player breaking, explosions, pistons, etc.)
 * Protected blocks behave like bedrock - completely indestructible.
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

    /**
     * Prevent explosions from destroying protected blocks.
     * This includes TNT, creepers, ghasts, withers, etc.
     */
    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) level;
        SpaceStationBlockProtection protection = SpaceStationBlockProtection.get(serverLevel);

        // Remove all protected blocks from the explosion's affected blocks list
        List<BlockPos> affectedBlocks = event.getAffectedBlocks();
        List<BlockPos> toRemove = new ArrayList<>();

        for (BlockPos pos : affectedBlocks) {
            if (protection.isProtected(pos)) {
                toRemove.add(pos);
            }
        }

        if (!toRemove.isEmpty()) {
            affectedBlocks.removeAll(toRemove);
            com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.debug(
                "Protected {} space station blocks from explosion at {}",
                toRemove.size(),
                event.getExplosion().center()
            );
        }
    }

    /**
     * Prevent pistons from moving protected blocks.
     */
    @SubscribeEvent
    public static void onPistonMove(BlockEvent.PistonMoveBlocks event) {
        if (event.getLevel().isClientSide()) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        SpaceStationBlockProtection protection = SpaceStationBlockProtection.get(level);

        // Check if any blocks being moved are protected
        for (BlockPos pos : event.getToBeMoved()) {
            if (protection.isProtected(pos)) {
                event.setCanceled(true);
                com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.debug(
                    "Blocked piston from moving protected block at {}",
                    pos
                );
                return;
            }
        }
    }
}
