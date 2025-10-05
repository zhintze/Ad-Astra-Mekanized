package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Menu for planet selection screen.
 * Syncs planet data from server to client.
 */
public class PlanetsMenu extends AbstractContainerMenu {

    protected final Player player;
    protected final int tier;
    protected final List<Planet> availablePlanets;

    public PlanetsMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        super(ModMenuTypes.PLANETS.get(), containerId);
        this.player = inventory.player;

        // Read rocket tier from buffer
        this.tier = buf.readInt();

        // Read planet count and IDs from buffer
        int planetCount = buf.readInt();
        this.availablePlanets = new ArrayList<>(planetCount);

        PlanetRegistry registry = PlanetRegistry.getInstance();
        for (int i = 0; i < planetCount; i++) {
            ResourceLocation planetId = buf.readResourceLocation();
            Planet planet = registry.getPlanet(planetId);
            if (planet != null) {
                availablePlanets.add(planet);
            }
        }
    }

    public PlanetsMenu(int containerId, Inventory inventory, int tier, List<Planet> planets) {
        super(ModMenuTypes.PLANETS.get(), containerId);
        this.player = inventory.player;
        this.tier = tier;
        this.availablePlanets = new ArrayList<>(planets);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public Player player() {
        return player;
    }

    public int tier() {
        return tier;
    }

    public List<Planet> getAvailablePlanets() {
        return availablePlanets;
    }

    public List<Planet> getSortedPlanets() {
        return availablePlanets.stream()
            .sorted(Comparator.comparingInt((Planet p) -> p.properties().orbitDistance())
                .thenComparing(p -> getPlanetName(p).getString()))
            .toList();
    }

    public Component getPlanetName(Planet planet) {
        String path = planet.id().getPath();
        String namespace = planet.id().getNamespace();
        return Component.translatableWithFallback(
            "planet.%s.%s".formatted(namespace, path),
            capitalize(path.replace("_", " "))
        );
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
            }
        }
        return result.toString().trim();
    }
}
