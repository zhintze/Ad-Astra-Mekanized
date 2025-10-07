package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.hecookin.adastramekanized.common.menus.PlanetsMenu;
import com.hecookin.adastramekanized.common.network.ModNetworking;
import com.hecookin.adastramekanized.common.network.ServerboundLandPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client-side screen for planet selection.
 * Features multi-page navigation (solar systems → planets → details).
 * Based on Ad Astra's PlanetsScreen.
 */
public class PlanetsScreen extends AbstractContainerScreen<PlanetsMenu> {

    private final List<Button> buttons = new ArrayList<>();
    private Button backButton;
    private double scrollAmount;

    private final boolean hasMultipleSolarSystems;
    private int pageIndex;
    private ResourceLocation selectedSolarSystem = ResourceLocation.fromNamespaceAndPath("adastramekanized", "solar_system");
    private Planet selectedPlanet;


    public PlanetsScreen(PlanetsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = width;
        this.imageHeight = height;

        var planets = menu.getSortedPlanets();
        hasMultipleSolarSystems = planets.stream()
            .map(menu::getSolarSystem)
            .distinct()
            .count() > 1;
        pageIndex = hasMultipleSolarSystems ? 0 : 1;
    }

    @Override
    protected void init() {
        super.init();
        buttons.clear();
        scrollAmount = 0;

        switch (pageIndex) {
            case 0 -> createSolarSystemButtons();
            case 1, 2 -> {
                createPlanetButtons();
                if (pageIndex == 2 && selectedPlanet != null) {
                    createSelectedPlanetButtons();
                }
            }
        }

        // Back button (left side)
        backButton = addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (pageIndex != 2) this.scrollAmount = 0;
            pageIndex--;
            rebuildWidgets();
        }).bounds(10, height / 2 - 85, 20, 20).build());

        backButton.visible = pageIndex > (hasMultipleSolarSystems ? 0 : 1);
    }

    private void createSolarSystemButtons() {
        selectedSolarSystem = null;

        // Get unique solar systems
        List<ResourceLocation> solarSystems = menu.getSortedPlanets().stream()
            .map(menu::getSolarSystem)
            .distinct()
            .sorted(Comparator.comparing(ResourceLocation::getPath))
            .collect(Collectors.toList());

        for (ResourceLocation solarSystem : solarSystems) {
            var button = addWidget(Button.builder(
                Component.literal(capitalize(solarSystem.getPath())),
                b -> {
                    pageIndex = 1;
                    selectedSolarSystem = solarSystem;
                    rebuildWidgets();
                }
            ).bounds(10, 0, 99, 20).build());
            buttons.add(button);
        }
    }

    private void createPlanetButtons() {
        for (var planet : menu.getSortedPlanets()) {
            if (!menu.getSolarSystem(planet).equals(selectedSolarSystem)) continue;

            buttons.add(addWidget(Button.builder(
                Component.literal(planet.displayName()),
                b -> {
                    pageIndex = 2;
                    selectedPlanet = planet;
                    rebuildWidgets();
                }
            ).bounds(10, 0, 99, 20).build()));
        }
    }

    private void createSelectedPlanetButtons() {
        if (selectedPlanet == null) return;

        // Land button
        var landButton = addRenderableWidget(Button.builder(
            Component.literal("Land"),
            b -> land(selectedPlanet.id())
        ).bounds(114, height / 2 - 77, 99, 20).build());

        // Placeholder: Space station buttons would go here
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderButtons(graphics, mouseX, mouseY, partialTick);
        backButton.visible = pageIndex > (hasMultipleSolarSystems ? 0 : 1);

        // Prevent buttons from being pressed when outside view area
        buttons.forEach(button -> button.active = button.getY() > height / 2 - 63 && button.getY() < height / 2 + 88);
    }

    private void renderButtons(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int scrollPixels = (int) scrollAmount;

        // Scissor box for scrollable button area
        graphics.enableScissor(0, height / 2 - 43, 112, height / 2 + 88);

        for (var button : buttons) {
            button.render(graphics, mouseX, mouseY, partialTick);
        }

        for (int i = 0; i < buttons.size(); i++) {
            var button = buttons.get(i);
            button.setY((i * 24 - scrollPixels) + (height / 2 - 41));
        }

        graphics.disableScissor();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {}

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Dark space background
        graphics.fill(0, 0, width, height, 0xFF000419);

        // Static starfield background
        renderStarfield(graphics);

        // Selection menu box
        renderSelectionMenu(graphics);
    }

    protected void renderSelectionMenu(GuiGraphics graphics) {
        int menuX = 7;
        int menuY = height / 2 - 88;
        int menuWidth = pageIndex == 2 ? 240 : 105;
        int menuHeight = 177;

        // Menu background (semi-transparent dark box)
        graphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xCC000000);

        // Menu border (subtle outline)
        graphics.fill(menuX, menuY, menuX + menuWidth, menuY + 1, 0xFF0F2559); // Top
        graphics.fill(menuX, menuY + menuHeight - 1, menuX + menuWidth, menuY + menuHeight, 0xFF0F2559); // Bottom
        graphics.fill(menuX, menuY, menuX + 1, menuY + menuHeight, 0xFF0F2559); // Left
        graphics.fill(menuX + menuWidth - 1, menuY, menuX + menuWidth, menuY + menuHeight, 0xFF0F2559); // Right

        // Title
        Component titleText;
        if (pageIndex == 2 && selectedPlanet != null) {
            titleText = Component.literal(selectedPlanet.displayName());
        } else if (pageIndex == 1 && selectedSolarSystem != null) {
            titleText = Component.literal(capitalize(selectedSolarSystem.getPath()));
        } else {
            titleText = Component.literal("Solar System Catalog");
        }

        graphics.drawCenteredString(font, titleText, 57, height / 2 - 60, 0xFFFFFF);

        // Space Station label (if on details page)
        if (pageIndex == 2) {
            graphics.drawCenteredString(font, Component.literal("Space Stations"), 163, height / 2 - 15, 0xFFFFFF);
        }

        // Planet info (when hovering over selected planet)
        if (pageIndex == 2 && selectedPlanet != null) {
            renderPlanetInfo(graphics);
        }
    }

    private void renderPlanetInfo(GuiGraphics graphics) {
        if (selectedPlanet == null) return;

        // Position info box below Land button with padding
        int boxX = 114;
        int boxY = height / 2 - 52;  // 5 pixels below Land button (which ends at -57)
        int boxWidth = 126;  // Wider to fit text comfortably
        int boxHeight = 54;  // Height for 3 lines + padding

        // Draw info box background (semi-transparent with border)
        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xCC000000);

        // Border
        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + 1, 0xFF0F2559); // Top
        graphics.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, 0xFF0F2559); // Bottom
        graphics.fill(boxX, boxY, boxX + 1, boxY + boxHeight, 0xFF0F2559); // Left
        graphics.fill(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF0F2559); // Right

        // Text starts with padding from top and left
        int textX = boxX + 6;
        int textY = boxY + 8;
        int lineSpacing = 14;

        // Gravity
        graphics.drawString(font,
            Component.literal("Gravity: " + String.format("%.1fx", selectedPlanet.properties().gravity()))
                .withStyle(ChatFormatting.GRAY),
            textX, textY, 0xAAAAAA);

        // Temperature
        graphics.drawString(font,
            Component.literal("Temperature: " + String.format("%.0f°C", selectedPlanet.properties().temperature()))
                .withStyle(ChatFormatting.GRAY),
            textX, textY + lineSpacing, 0xAAAAAA);

        // Atmosphere
        String atmosphere = selectedPlanet.atmosphere().hasAtmosphere() ?
            (selectedPlanet.atmosphere().breathable() ? "Breathable" : "Toxic") : "None";
        graphics.drawString(font,
            Component.literal("Atmosphere: " + atmosphere).withStyle(ChatFormatting.GRAY),
            textX, textY + lineSpacing * 2, 0xAAAAAA);
    }

    private void renderStarfield(GuiGraphics graphics) {
        // Static starfield
        int starCount = 400;
        var starRandom = net.minecraft.util.RandomSource.create(10842);

        // Generate static star positions
        for (int i = 0; i < starCount; i++) {
            // Generate star position
            int starX = (int) (starRandom.nextFloat() * width);
            int starY = (int) (starRandom.nextFloat() * height);

            // Star size (1-2 pixels)
            int size = 1 + starRandom.nextInt(2);

            // Star brightness variation
            float brightness = 0.6f + starRandom.nextFloat() * 0.4f;
            int alpha = (int) (brightness * 255);
            int color = (alpha << 24) | 0xFFFFFF;

            // Draw star as small filled rectangle
            graphics.fill(starX, starY, starX + size, starY + size, color);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < 112 && mouseX > 6 && mouseY > height / 2f - 43 && mouseY < height / 2f + 88) {
            setScrollAmount(scrollAmount - scrollY * 16 / 2f);
        }
        return true;
    }

    @Override
    public void onClose() {
        // Multi-level back navigation
        if (pageIndex > 0) {
            if (pageIndex != 2) this.scrollAmount = 0;
            pageIndex--;
            rebuildWidgets();
            return;
        }

        Player player = menu.player();
        // Only allow closing if not in rocket (creative/spectator can always close)
        if (player.isCreative() || player.isSpectator()) {
            super.onClose();
        } else if (!(player.getVehicle() instanceof Rocket)) {
            super.onClose();
        }
    }

    protected void setScrollAmount(double amount) {
        scrollAmount = Mth.clamp(amount, 0.0, Math.max(0, buttons.size() * 24 - 131));
    }

    public void land(ResourceLocation planetId) {
        ModNetworking.sendToServer(new ServerboundLandPacket(planetId.toString(), true));
        close();
    }

    protected void close() {
        pageIndex = 0;
        onClose();
    }

    private static String capitalize(String string) {
        if (string == null || string.isEmpty()) return string;
        String[] words = string.replace("_", " ").split(" ");
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
