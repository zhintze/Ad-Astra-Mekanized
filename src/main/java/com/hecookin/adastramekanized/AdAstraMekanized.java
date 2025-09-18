package com.hecookin.adastramekanized;

import com.hecookin.adastramekanized.common.commands.PlanetCommands;
import com.hecookin.adastramekanized.common.planets.PlanetManager;
import com.hecookin.adastramekanized.common.planets.PlanetNetworking;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import com.hecookin.adastramekanized.common.registry.ModCreativeTabs;
import com.hecookin.adastramekanized.common.registry.ModItems;
import com.hecookin.adastramekanized.config.AdAstraMekanizedConfig;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Ad Astra Mekanized - Space Exploration with Mekanism Integration
 *
 * This mod combines the best features of Ad Astra (space exploration, planets, rockets)
 * with Mekanism's infrastructure (energy, chemicals, pipes, machines).
 *
 * Key Features:
 * - Rockets and space travel using Mekanism energy
 * - Oxygen system integrated with Mekanism chemical pipes
 * - Fuel system integrated with Immersive Engineering
 * - Planet generation and exploration
 * - Space stations and orbital mechanics
 */
@Mod(AdAstraMekanized.MOD_ID)
public class AdAstraMekanized {
    public static final String MOD_ID = "adastramekanized";
    public static final Logger LOGGER = LogManager.getLogger(AdAstraMekanized.class);

    private static ModIntegrationManager integrationManager;

    public AdAstraMekanized(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Ad Astra Mekanized - Space exploration with Mekanism integration!");

        // Register configuration
        AdAstraMekanizedConfig.registerConfigs(modContainer);
        modEventBus.addListener(AdAstraMekanizedConfig::onConfigLoad);

        // Register items, blocks, and creative tabs
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Register mod setup
        modEventBus.addListener(this::commonSetup);

        // Register networking
        modEventBus.addListener(this::registerNetworking);

        // Register planet manager events
        NeoForge.EVENT_BUS.register(PlanetManager.class);

        // Register command events
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        LOGGER.info("Ad Astra Mekanized initialization complete!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Ad Astra Mekanized common setup started");

        event.enqueueWork(() -> {
            // Initialize mod integrations
            integrationManager = ModIntegrationManager.getInstance();
            LOGGER.info("Integration manager initialized: {}", integrationManager.getIntegrationStatus());

            // Initialize planet system components
            LOGGER.info("Planet system initialization complete");

            LOGGER.info("Ad Astra Mekanized common setup complete");
        });
    }

    /**
     * Register networking handlers
     */
    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        LOGGER.info("Registering planet networking handlers...");
        PlanetNetworking.register(event);
        LOGGER.info("Planet networking registration complete");
    }

    /**
     * Register commands
     */
    private void registerCommands(final RegisterCommandsEvent event) {
        LOGGER.info("Registering planet commands...");
        PlanetCommands.register(event.getDispatcher());
        LOGGER.info("Planet commands registration complete");
    }

    /**
     * Get the integration manager instance
     * @return ModIntegrationManager instance, or null if not yet initialized
     */
    public static ModIntegrationManager getIntegrationManager() {
        return integrationManager;
    }
}