package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.hecookin.adastramekanized.common.menus.PlanetsMenuProvider.SpaceStation;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Server-side menu for planet selection.
 * Handles planet data, filtering, and landing logic.
 */
public class PlanetsMenu extends AbstractContainerMenu {

    protected final int tier;
    protected final Inventory inventory;
    protected final Player player;
    protected final Level level;
    protected final Set<ResourceLocation> disabledPlanets;
    protected final Map<ResourceLocation, Map<UUID, Set<SpaceStation>>> spaceStations;
    protected final Set<GlobalPos> spawnLocations;

    public PlanetsMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId,
            inventory,
            PlanetsMenuProvider.createDisabledPlanetsFromBuf(buf),
            PlanetsMenuProvider.createSpaceStationsFromBuf(buf),
            PlanetsMenuProvider.createSpawnLocationsFromBuf(buf));
    }

    public PlanetsMenu(int containerId,
                       Inventory inventory,
                       Set<ResourceLocation> disabledPlanets,
                       Map<ResourceLocation, Map<UUID, Set<SpaceStation>>> spaceStations,
                       Set<GlobalPos> spawnLocations) {
        super(ModMenuTypes.PLANETS.get(), containerId);
        this.inventory = inventory;
        this.player = inventory.player;
        this.level = player.level();
        this.tier = player.getVehicle() instanceof Rocket vehicle ? vehicle.tier() : 100;
        this.disabledPlanets = disabledPlanets;
        this.spaceStations = spaceStations;
        this.spawnLocations = spawnLocations;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int tier() {
        return tier;
    }

    public Player player() {
        return player;
    }

    public Set<ResourceLocation> disabledPlanets() {
        return disabledPlanets;
    }

    /**
     * Get landing position for a planet
     * Placeholder - returns player position for now
     */
    public BlockPos getLandingPos(ResourceLocation planetId, boolean tryPreviousLocation) {
        // Check spawn locations
        for (var pos : spawnLocations) {
            if (pos.dimension().location().equals(planetId)) {
                return pos.pos();
            }
        }

        // Default to player position
        return player.blockPosition();
    }

    /**
     * Get planet name for display
     */
    public Component getPlanetName(ResourceLocation planetId) {
        Planet planet = PlanetRegistry.getInstance().getPlanet(planetId);
        if (planet != null) {
            return Component.literal(planet.displayName());
        }
        return Component.literal(capitalize(planetId.getPath()));
    }

    /**
     * Get all planets sorted by tier and name
     */
    public List<Planet> getSortedPlanets() {
        return PlanetRegistry.getInstance().getAllPlanets().stream()
            .filter(planet -> !disabledPlanets.contains(planet.id()))
            .filter(planet -> tier >= getPlanetTier(planet))
            .sorted(Comparator
                .<Planet>comparingInt(p -> p.id().getPath().equals("earth") ? -1 : 0)  // Earth first
                .thenComparingInt(this::getPlanetTier)
                .thenComparing(p -> getPlanetName(p.id()).getString()))
            .collect(Collectors.toList());
    }

    /**
     * Get planet tier (1-4 for rockets)
     */
    private int getPlanetTier(Planet planet) {
        // Placeholder: All planets accessible for now
        // TODO: Add tier system to Planet data structure
        return 1;
    }

    /**
     * Get solar system ID for a planet
     * Placeholder - returns single solar system for now
     */
    public ResourceLocation getSolarSystem(Planet planet) {
        // TODO: Add solar system to Planet data structure
        return ResourceLocation.fromNamespaceAndPath("adastramekanized", "solar_system");
    }

    /**
     * Placeholder: Get owned space stations
     */
    public List<SpaceStation> getOwnedAndTeamSpaceStations(ResourceLocation planetId) {
        // Placeholder - returns empty list
        return List.of();
    }

    /**
     * Placeholder: Check if space station can be constructed
     */
    public boolean canConstruct(ResourceLocation planetId) {
        // Placeholder - always false
        return false;
    }

    /**
     * Placeholder: Check if position is claimed
     */
    public boolean isClaimed(ResourceLocation planetId) {
        // Placeholder - always false
        return false;
    }

    /**
     * Placeholder: Check if player is in space station
     */
    public boolean isInSpaceStation(ResourceLocation planetId) {
        // Placeholder - always false
        return false;
    }

    private static String capitalize(String string) {
        if (string == null || string.isEmpty()) return string;
        return Arrays.stream(string.split("_"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
            .collect(Collectors.joining(" "));
    }
}
