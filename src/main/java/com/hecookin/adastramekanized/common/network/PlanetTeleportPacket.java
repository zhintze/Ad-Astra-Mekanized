package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.teleportation.PlanetTeleportationSystem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet sent from client to server when player selects a planet destination.
 * Triggers teleportation to the selected planet.
 */
public record PlanetTeleportPacket(String planetId) implements CustomPacketPayload {

    public static final Type<PlanetTeleportPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planet_teleport")
    );

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, PlanetTeleportPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, PlanetTeleportPacket::planetId,
        PlanetTeleportPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the server side
     */
    public static void handle(PlanetTeleportPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResourceLocation planetId = ResourceLocation.parse(packet.planetId);

                // Teleport the player to the selected planet
                PlanetTeleportationSystem.getInstance()
                    .teleportToAnyPlanet(player, planetId)
                    .thenAccept(result -> {
                        switch (result) {
                            case SUCCESS:
                                AdAstraMekanized.LOGGER.info("Player {} teleported to planet {}",
                                    player.getName().getString(), planetId);
                                break;
                            case PLANET_NOT_FOUND:
                                player.displayClientMessage(
                                    net.minecraft.network.chat.Component.literal("Planet not found!"),
                                    false
                                );
                                break;
                            case DIMENSION_NOT_LOADED:
                                player.displayClientMessage(
                                    net.minecraft.network.chat.Component.literal("Planet dimension not loaded!"),
                                    false
                                );
                                break;
                            default:
                                player.displayClientMessage(
                                    net.minecraft.network.chat.Component.literal("Teleportation failed!"),
                                    false
                                );
                        }
                    });
            }
        });
    }
}
