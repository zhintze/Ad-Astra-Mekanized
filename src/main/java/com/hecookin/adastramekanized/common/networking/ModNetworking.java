package com.hecookin.adastramekanized.common.networking;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.networking.packets.OxygenDistributorButtonPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");

        // Register oxygen distributor button packet
        registrar.playToServer(
            OxygenDistributorButtonPacket.TYPE,
            OxygenDistributorButtonPacket.STREAM_CODEC,
            OxygenDistributorButtonPacket::handle
        );
    }

    // Client to server packet sending
    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    // Server to client packet sending for a specific player
    public static void sendToPlayer(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    // Server to all clients packet sending
    public static void sendToAllClients(CustomPacketPayload packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }
}