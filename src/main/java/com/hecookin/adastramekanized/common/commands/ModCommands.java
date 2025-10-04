package com.hecookin.adastramekanized.common.commands;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Command registration handler for Ad Astra Mekanized.
 *
 * Registers all mod commands during server startup.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        AdAstraMekanized.LOGGER.info("Registering Ad Astra Mekanized commands...");

        // Register planet debug commands
        PlanetDebugCommands.register(dispatcher);

        // Register recipe debug commands
        RecipeDebugCommand.register(dispatcher);

        AdAstraMekanized.LOGGER.info("Command registration complete");
    }
}