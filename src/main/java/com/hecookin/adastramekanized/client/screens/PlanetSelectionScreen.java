package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.common.network.ModNetworking;
import com.hecookin.adastramekanized.common.network.PlanetTeleportPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Client-side screen for selecting a planet destination.
 * Displayed when rocket reaches atmosphere leave height.
 */
public class PlanetSelectionScreen extends Screen {

    private final List<Planet> availablePlanets;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 25;

    public PlanetSelectionScreen(List<Planet> availablePlanets) {
        super(Component.literal("Select Destination"));
        this.availablePlanets = availablePlanets;
    }

    @Override
    protected void init() {
        super.init();

        // Calculate starting Y position to center the buttons
        int totalHeight = availablePlanets.size() * BUTTON_SPACING;
        int startY = (height - totalHeight) / 2;

        // Create a button for each planet
        for (int i = 0; i < availablePlanets.size(); i++) {
            Planet planet = availablePlanets.get(i);
            int buttonY = startY + (i * BUTTON_SPACING);

            // Create button with planet name and info
            addRenderableWidget(Button.builder(
                Component.literal(planet.displayName()),
                button -> selectPlanet(planet)
            )
            .bounds((width - BUTTON_WIDTH) / 2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
        }

        // Add cancel button at bottom
        addRenderableWidget(Button.builder(
            Component.literal("Cancel"),
            button -> onClose()
        )
        .bounds((width - BUTTON_WIDTH) / 2, height - 40, BUTTON_WIDTH, BUTTON_HEIGHT)
        .build());
    }

    private void selectPlanet(Planet planet) {
        // Send teleport packet to server
        ModNetworking.sendToServer(new PlanetTeleportPacket(planet.id().toString()));

        // Close the screen
        onClose();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render dark background
        renderBackground(graphics, mouseX, mouseY, partialTick);

        // Render title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // Render planet info on hover
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render additional info for hovered planet
        renderPlanetInfo(graphics, mouseX, mouseY);
    }

    private void renderPlanetInfo(GuiGraphics graphics, int mouseX, int mouseY) {
        // Calculate which button is being hovered
        int totalHeight = availablePlanets.size() * BUTTON_SPACING;
        int startY = (height - totalHeight) / 2;

        for (int i = 0; i < availablePlanets.size(); i++) {
            Planet planet = availablePlanets.get(i);
            int buttonY = startY + (i * BUTTON_SPACING);
            int buttonX = (width - BUTTON_WIDTH) / 2;

            // Check if mouse is over this button
            if (mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH &&
                mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT) {

                // Render planet details below title
                int infoY = 40;
                graphics.drawCenteredString(this.font,
                    Component.literal("Gravity: " + String.format("%.1fx", planet.properties().gravity())),
                    this.width / 2, infoY, 0xAAAAAA);

                graphics.drawCenteredString(this.font,
                    Component.literal("Temperature: " + String.format("%.0f°C", planet.properties().temperature())),
                    this.width / 2, infoY + 12, 0xAAAAAA);

                String atmosphere = planet.atmosphere().hasAtmosphere() ?
                    (planet.atmosphere().breathable() ? "Breathable" : "Toxic") : "None";
                graphics.drawCenteredString(this.font,
                    Component.literal("Atmosphere: " + atmosphere),
                    this.width / 2, infoY + 24, 0xAAAAAA);

                break;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause the game
    }
}
