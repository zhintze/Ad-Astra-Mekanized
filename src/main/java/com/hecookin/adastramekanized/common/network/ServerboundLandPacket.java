package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.constants.RocketConstants;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet sent from client to server when player selects a planet to land on.
 * Triggers landing animation sequence.
 */
public record ServerboundLandPacket(String planetId, boolean tryPreviousLocation) implements CustomPacketPayload {

    public static final Type<ServerboundLandPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "land")
    );

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, ServerboundLandPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ServerboundLandPacket::planetId,
        ByteBufCodecs.BOOL, ServerboundLandPacket::tryPreviousLocation,
        ServerboundLandPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the server side - initiates landing sequence
     */
    public static void handle(ServerboundLandPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.getVehicle() instanceof Rocket rocket)) return;

            ResourceLocation planetId = ResourceLocation.parse(packet.planetId);
            Planet planet = PlanetRegistry.getInstance().getPlanet(planetId);

            if (planet == null) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Planet not found!"),
                    false
                );
                return;
            }

            // Create dimension key from planet's dimension type (e.g., minecraft:overworld for Earth)
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, planet.dimension().dimensionType());
            ServerLevel targetLevel = player.server.getLevel(dimensionKey);
            if (targetLevel == null) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Planet dimension not loaded!"),
                    false
                );
                return;
            }

            // Teleport rocket and player to atmosphere height in target dimension
            BlockPos targetPos = player.blockPosition();
            Vec3 landingPos = new Vec3(targetPos.getX(), RocketConstants.ATMOSPHERE_LEAVE_HEIGHT, targetPos.getZ());

            // Stop riding temporarily for teleportation
            player.stopRiding();
            player.teleportTo(targetLevel, landingPos.x, landingPos.y, landingPos.z, player.getYRot(), player.getXRot());

            // Create new rocket in target dimension with same properties
            Rocket newRocket = (Rocket) rocket.getType().create(targetLevel);
            if (newRocket != null) {
                newRocket.setPos(landingPos);
                newRocket.setYRot(rocket.getYRot());

                // Transfer inventory
                for (int i = 0; i < rocket.inventory().getContainerSize(); i++) {
                    newRocket.inventory().setItem(i, rocket.inventory().getItem(i));
                }

                // Transfer fuel
                newRocket.fluidContainer().fill(rocket.fluidContainer().getFluidInTank(0), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

                // Set landing mode
                newRocket.setLanding(true);

                targetLevel.addFreshEntity(newRocket);
                player.startRiding(newRocket);

                // Remove old rocket
                rocket.discard();

                AdAstraMekanized.LOGGER.info("Player {} landing on planet {}", player.getName().getString(), planetId);
            }
        });
    }
}
