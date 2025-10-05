package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers network packets for the mod
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModPackets {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(AdAstraMekanized.MOD_ID)
            .versioned("1.0.0");

        // Register oxygen visualization packet (server -> client)
        registrar.playToClient(
            OxygenVisualizationPacket.TYPE,
            OxygenVisualizationPacket.CODEC,
            new DirectionalPayloadHandler<>(
                OxygenVisualizationPacket::handle,
                null // No server handler needed
            )
        );

        // Register oxygen distributor button packet (client -> server)
        registrar.playToServer(
            OxygenDistributorButtonPacket.TYPE,
            OxygenDistributorButtonPacket.CODEC,
            new DirectionalPayloadHandler<>(
                null, // No client handler needed
                OxygenDistributorButtonPacket::handle
            )
        );

        // Register oxygen controller packet (client -> server)
        registrar.playToServer(
            OxygenControllerPacket.TYPE,
            OxygenControllerPacket.CODEC,
            new DirectionalPayloadHandler<>(
                null, // No client handler needed
                OxygenControllerPacket::handle
            )
        );

        // Register vehicle control packet (client -> server)
        registrar.playToServer(
            VehicleControlPacket.TYPE,
            VehicleControlPacket.CODEC,
            new DirectionalPayloadHandler<>(
                null, // No client handler needed
                VehicleControlPacket::handle
            )
        );

        // Register planet selection packets
        registrar.playToClient(
            OpenPlanetSelectionPacket.TYPE,
            OpenPlanetSelectionPacket.CODEC,
            new DirectionalPayloadHandler<>(
                OpenPlanetSelectionPacket::handle,
                null // No server handler needed
            )
        );

        registrar.playToServer(
            PlanetTeleportPacket.TYPE,
            PlanetTeleportPacket.CODEC,
            new DirectionalPayloadHandler<>(
                null, // No client handler needed
                PlanetTeleportPacket::handle
            )
        );

        AdAstraMekanized.LOGGER.info("Registered network packets");
    }
}