package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.common.world.SpaceStationBlockProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
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
     * This includes TNT, creepers, ghasts, withers, rockets, etc.
     * Using HIGHEST priority to ensure we intercept before other handlers.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
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
            var source = event.getExplosion().getDirectSourceEntity();
            com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.info(
                "Protected {} space station blocks from explosion at {} (explosion source: {})",
                toRemove.size(),
                event.getExplosion().center(),
                source != null ? source.getClass().getSimpleName() : "Unknown"
            );
        }
    }

    /**
     * Prevent pistons from moving protected blocks.
     */
    @SubscribeEvent
    public static void onPistonPre(net.neoforged.neoforge.event.level.PistonEvent.Pre event) {
        if (event.getLevel().isClientSide()) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        SpaceStationBlockProtection protection = SpaceStationBlockProtection.get(level);

        // Get the piston position and check direction
        BlockPos pistonPos = event.getPos();
        net.minecraft.core.Direction direction = event.getDirection();

        // Check blocks in the push direction for protection
        for (int i = 1; i <= 12; i++) { // Max piston push distance is 12 blocks
            BlockPos checkPos = pistonPos.relative(direction, i);
            if (protection.isProtected(checkPos)) {
                event.setCanceled(true);
                com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.debug(
                    "Blocked piston from moving protected block at {}",
                    checkPos
                );
                return;
            }
        }
    }
}
