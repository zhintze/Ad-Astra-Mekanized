package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.hecookin.adastramekanized.common.menus.PlanetsMenuProvider.SpaceStation;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

        com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.info("PlanetsMenu created for player {} with tier {}",
            player.getName().getString(), this.tier);
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
        java.util.Collection<Planet> allPlanets = PlanetRegistry.getInstance().getAllPlanets();
        com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.info("Total planets in registry: {}", allPlanets.size());

        List<Planet> result = allPlanets.stream()
            .filter(planet -> {
                boolean notDisabled = !disabledPlanets.contains(planet.id());
                if (!notDisabled) {
                    com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.info("Planet {} is disabled", planet.id());
                }
                return notDisabled;
            })
            .filter(planet -> {
                int planetTier = getPlanetTier(planet);
                boolean accessible = tier >= planetTier;
                com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.info("Planet {} tier {} vs player tier {} = {}",
                    planet.id(), planetTier, tier, accessible);
                return accessible;
            })
            .filter(planet -> {
                boolean discovered = isPlanetDiscovered(planet);
                com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.info("Planet {} discovered: {}", planet.id(), discovered);
                return discovered;
            })
            .sorted(Comparator
                .<Planet>comparingInt(p -> p.id().getPath().equals("earth") ? -1 : 0)
                .thenComparingInt(this::getPlanetTier)
                .thenComparing(p -> getPlanetName(p.id()).getString()))
            .collect(Collectors.toList());

        com.hecookin.adastramekanized.AdAstraMekanized.LOGGER.info("Filtered planets count: {}", result.size());
        return result;
    }

    /**
     * Get planet tier (1-4 for rockets)
     */
    private int getPlanetTier(Planet planet) {
        String planetPath = planet.id().getPath();

        return switch (planetPath) {
            case "earth", "moon", "earth_orbit" -> 1;
            case "mars", "mercury" -> 2;
            case "venus", "glacio" -> 3;
            default -> 4;
        };
    }

    /**
     * Check if a planet is in the random discovery pool (not explicitly tiered)
     */
    public boolean isRandomPoolPlanet(Planet planet) {
        String planetPath = planet.id().getPath();

        return switch (planetPath) {
            case "earth", "moon", "earth_orbit", "mars", "mercury", "venus", "glacio" -> false;
            default -> true;
        };
    }

    /**
     * Check if a planet is pre-unlocked (visible without discovery)
     * Cumulative unlocks based on rocket tier:
     * - Tier 1: earth, moon, earth_orbit
     * - Tier 2: tier 1 + mars, mercury
     * - Tier 3: tier 2 + venus, glacio
     * - Tier 4: tier 3 (no additional pre-unlocked planets)
     */
    public boolean isPreUnlocked(Planet planet) {
        String planetPath = planet.id().getPath();

        return switch (planetPath) {
            case "earth", "moon", "earth_orbit" -> tier >= 1;
            case "mars", "mercury" -> tier >= 2;
            case "venus", "glacio" -> tier >= 3;
            default -> false;
        };
    }

    /**
     * Check if a planet has been discovered (visited) by any player on the server
     * - Pre-unlocked planets (tier 1-3): Always discovered if tier requirement met
     * - Random pool planets: Require tier 4+ AND visited advancement
     */
    public boolean isPlanetDiscovered(Planet planet) {
        // Pre-unlocked planets are always "discovered" if tier requirement is met
        if (isPreUnlocked(planet)) {
            return true;
        }

        // Random pool planets require tier 4+ rocket
        if (tier < 4) {
            return false;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        // Check if any player on the server has visited this planet
        ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(
            "adastramekanized",
            "planets/visited_" + planet.id().getPath()
        );

        AdvancementHolder advancement = serverPlayer.server.getAdvancements().get(advancementId);
        if (advancement == null) {
            return false;
        }

        for (ServerPlayer onlinePlayer : serverPlayer.server.getPlayerList().getPlayers()) {
            AdvancementProgress progress = onlinePlayer.getAdvancements().getOrStartProgress(advancement);
            if (progress.isDone()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get all undiscovered random pool planets
     */
    public List<Planet> getUndiscoveredRandomPlanets() {
        return PlanetRegistry.getInstance().getAllPlanets().stream()
            .filter(this::isRandomPoolPlanet)
            .filter(planet -> !isPlanetDiscovered(planet))
            .collect(Collectors.toList());
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
