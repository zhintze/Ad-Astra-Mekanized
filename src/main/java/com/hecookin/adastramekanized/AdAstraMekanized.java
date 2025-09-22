package com.hecookin.adastramekanized;

import com.hecookin.adastramekanized.common.commands.ModCommands;
import com.hecookin.adastramekanized.common.teleportation.PlanetTeleportationSystem;
import com.hecookin.adastramekanized.common.planets.PlanetManager;
import com.hecookin.adastramekanized.common.planets.PlanetNetworking;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
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
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
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
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Register mod setup
        modEventBus.addListener(this::commonSetup);

        // Register networking
        modEventBus.addListener(this::registerNetworking);

        // Register planet manager events
        NeoForge.EVENT_BUS.register(PlanetManager.class);

        // Register command handlers
        NeoForge.EVENT_BUS.register(ModCommands.class);

        // Register server events for planet services
        NeoForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);

        // Static planet system - no runtime generation needed

        LOGGER.info("Ad Astra Mekanized initialization complete!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Ad Astra Mekanized common setup started");

        event.enqueueWork(() -> {
            // Initialize mod integrations
            integrationManager = ModIntegrationManager.getInstance();
            LOGGER.info("Integration manager initialized: {}", integrationManager.getIntegrationStatus());

            // Early planet generation removed - planets are now pre-generated static datapacks
            LOGGER.info("Using pre-generated static planets from datapack system");

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
     * Planet commands removed - using static planet system
     * To generate new planets, use: gradle generatePlanets
     */

    /**
     * Server startup logging - planet generation now handled by gradle generatePlanets
     */
    private void onServerAboutToStart(final ServerAboutToStartEvent event) {
        LOGGER.info("Static planet system active - planets loaded from datapacks");
        LOGGER.info("To generate new planets, run: gradle generatePlanets");
    }

    /**
     * Initialize planet services when server starts
     */
    private void onServerStarted(final ServerStartedEvent event) {
        LOGGER.info("Initializing static planet services...");

        // Initialize planet manager
        PlanetManager planetManager = PlanetManager.getInstance();
        planetManager.initialize(event.getServer());

        // Initialize teleportation system
        PlanetTeleportationSystem teleportSystem = PlanetTeleportationSystem.getInstance();
        teleportSystem.initialize(event.getServer());

        LOGGER.info("Static planet services initialization complete");
    }

    /**
     * Clean up planet services when server stops
     */
    private void onServerStopped(final ServerStoppedEvent event) {
        LOGGER.info("Shutting down planet services...");

        // Clear teleportation cache
        PlanetTeleportationSystem teleportSystem = PlanetTeleportationSystem.getInstance();
        teleportSystem.clearCache();

        LOGGER.info("Planet services shutdown complete");
    }

    /**
     * Get the integration manager instance
     * @return ModIntegrationManager instance, or null if not yet initialized
     */
    public static ModIntegrationManager getIntegrationManager() {
        return integrationManager;
    }
}