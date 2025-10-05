package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.IContainerFactory;

import java.util.*;

/**
 * Menu provider for the planets selection screen.
 * Handles server→client data synchronization for planet information.
 */
public class PlanetsMenuProvider implements MenuProvider, IContainerFactory<AbstractContainerMenu> {

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PlanetsMenu(containerId, inventory, Set.of(), Map.of(), Set.of());
    }

    @Override
    public AbstractContainerMenu create(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        return new PlanetsMenu(containerId, inventory, buffer);
    }

    /**
     * Write planet data to network buffer for client synchronization
     */
    public void writeExtraData(ServerPlayer player, RegistryFriendlyByteBuf buffer) {
        // Write disabled planets (placeholder - empty for now)
        buffer.writeUtf("");

        PlanetRegistry registry = PlanetRegistry.getInstance();
        Collection<Planet> planets = registry.getAllPlanets();

        buffer.writeVarInt(planets.size());
        planets.forEach(planet -> {
            buffer.writeResourceLocation(planet.id());

            // Placeholder: Write space stations data (empty for now)
            buffer.writeVarInt(0); // No space stations yet
        });

        // Placeholder: Write spawn locations (empty for now)
        buffer.writeVarInt(0); // No spawn locations yet
    }

    /**
     * Read disabled planets from buffer
     */
    public static Set<ResourceLocation> createDisabledPlanetsFromBuf(RegistryFriendlyByteBuf buf) {
        Set<ResourceLocation> disabledPlanets = new HashSet<>();
        String planetsString = buf.readUtf();
        if (!planetsString.isEmpty()) {
            String[] planets = planetsString.split(",");
            for (var planet : planets) {
                disabledPlanets.add(ResourceLocation.parse(planet));
            }
        }
        return Collections.unmodifiableSet(disabledPlanets);
    }

    /**
     * Read space stations from buffer (placeholder)
     */
    public static Map<ResourceLocation, Map<UUID, Set<SpaceStation>>> createSpaceStationsFromBuf(RegistryFriendlyByteBuf buf) {
        Map<ResourceLocation, Map<UUID, Set<SpaceStation>>> spaceStationsMap = new HashMap<>();

        int planetsSize = buf.readVarInt();
        for (int i = 0; i < planetsSize; i++) {
            ResourceLocation planetId = buf.readResourceLocation();
            int spaceStationsSize = buf.readVarInt();

            Map<UUID, Set<SpaceStation>> spaceStationGroupMap = new HashMap<>();
            for (int j = 0; j < spaceStationsSize; j++) {
                int stationGroupSize = buf.readVarInt();
                Set<SpaceStation> spaceStations = new HashSet<>();

                for (int k = 0; k < stationGroupSize; k++) {
                    Component stationName = Component.literal(buf.readUtf());
                    ChunkPos stationPos = buf.readChunkPos();
                    spaceStations.add(new SpaceStation(stationPos, stationName));
                }

                UUID id = buf.readUUID();
                spaceStationGroupMap.put(id, spaceStations);
            }

            spaceStationsMap.put(planetId, spaceStationGroupMap);
        }

        return Collections.unmodifiableMap(spaceStationsMap);
    }

    /**
     * Read spawn locations from buffer (placeholder)
     */
    public static Set<GlobalPos> createSpawnLocationsFromBuf(RegistryFriendlyByteBuf buf) {
        Set<GlobalPos> locations = new HashSet<>();
        int locationCount = buf.readVarInt();
        for (int i = 0; i < locationCount; i++) {
            locations.add(buf.readGlobalPos());
        }
        return Collections.unmodifiableSet(locations);
    }

    /**
     * Placeholder: Space station data structure
     */
    public record SpaceStation(ChunkPos position, Component name) {}
}
