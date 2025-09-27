package com.hecookin.adastramekanized.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Utility class for sending network packets
 */
public class ModNetworking {

    /**
     * Send a packet from client to server
     */
    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    /**
     * Send a packet from server to a specific player
     */
    public static void sendToPlayer(net.minecraft.server.level.ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    /**
     * Send a packet from server to all players tracking a chunk
     */
    public static void sendToAllTracking(net.minecraft.world.level.chunk.LevelChunk chunk, CustomPacketPayload packet) {
        if (chunk.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunk.getPos(), packet);
        }
    }
}