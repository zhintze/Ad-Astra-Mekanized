package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.client.components.LabeledImageButton;
import com.hecookin.adastramekanized.common.menus.PlanetsMenu;
import com.hecookin.adastramekanized.common.network.ModNetworking;
import com.hecookin.adastramekanized.common.network.PlanetTeleportPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Planet selection screen with Ad Astra's visual style.
 * Simplified for our mod (no space stations or multiple solar systems).
 */
public class PlanetsScreen extends AbstractContainerScreen<PlanetsMenu> {

    public static final ResourceLocation SELECTION_MENU = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planets/selection_menu");
    public static final ResourceLocation SMALL_SELECTION_MENU = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planets/small_selection_menu");

    public static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planets/button"),
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planets/button_highlighted")
    );

    public static final WidgetSprites BACK_BUTTON_SPRITES = new WidgetSprites(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planets/back_button"),
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planets/back_button_highlighted")
    );

    private final List<Button> planetButtons = new ArrayList<>();
    private Button backButton;
    private Button landButton;
    private double scrollAmount;

    private int pageIndex; // 0 = planet list, 1 = planet details
    @Nullable
    private Planet selectedPlanet;

    public PlanetsScreen(PlanetsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = width;
        this.imageHeight = height;
        this.pageIndex = 0;
    }

    @Override
    protected void init() {
        super.init();
        planetButtons.clear();
        scrollAmount = 0;

        if (pageIndex == 0) {
            createPlanetButtons();
        } else if (pageIndex == 1 && selectedPlanet != null) {
            createSelectedPlanetButtons();
        }

        backButton = addRenderableWidget(new LabeledImageButton(10, height / 2 - 85, 12, 12, BACK_BUTTON_SPRITES, b -> {
            scrollAmount = 0;
            pageIndex = 0;
            selectedPlanet = null;
            rebuildWidgets();
        }));
        backButton.visible = pageIndex > 0;
    }

    private void createPlanetButtons() {
        for (var planet : menu.getSortedPlanets()) {
            var button = addWidget(new LabeledImageButton(10, 0, 99, 20, BUTTON_SPRITES, b -> {
                pageIndex = 1;
                selectedPlanet = planet;
                rebuildWidgets();
            }, menu.getPlanetName(planet)));
            planetButtons.add(button);
        }
    }

    private void createSelectedPlanetButtons() {
        if (selectedPlanet == null) return;

        landButton = addRenderableWidget(new LabeledImageButton(
            114, height / 2 - 77, 99, 20, BUTTON_SPRITES,
            b -> land(selectedPlanet), Component.literal("Land")));

        landButton.setTooltip(Tooltip.create(
            Component.literal("Travel to " + menu.getPlanetName(selectedPlanet).getString())
                .withStyle(ChatFormatting.AQUA)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        renderPlanetButtons(graphics, mouseX, mouseY, partialTick);
        backButton.visible = pageIndex > 0;

        // Prevent buttons from being pressed when outside view area
        planetButtons.forEach(button -> button.active = button.getY() > height / 2 - 63 && button.getY() < height / 2 + 88);
    }

    private void renderPlanetButtons(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int scrollPixels = (int) scrollAmount;

        // Enable scissor box for scrolling area
        int scissorX = 0;
        int scissorY = height / 2 - 43;
        int scissorWidth = 112;
        int scissorHeight = 131;

        Minecraft mc = Minecraft.getInstance();
        double scale = mc.getWindow().getGuiScale();
        int scaledX = (int) (scissorX * scale);
        int scaledY = (int) ((mc.getWindow().getGuiScaledHeight() - (scissorY + scissorHeight)) * scale);
        int scaledWidth = (int) (scissorWidth * scale);
        int scaledHeight = (int) (scissorHeight * scale);

        RenderSystem.enableScissor(scaledX, scaledY, scaledWidth, scaledHeight);

        for (var button : planetButtons) {
            button.render(graphics, mouseX, mouseY, partialTick);
        }

        for (int i = 0; i < planetButtons.size(); i++) {
            var button = planetButtons.get(i);
            button.setY((i * 24 - scrollPixels) + (height / 2 - 41));
        }

        RenderSystem.disableScissor();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {}

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Dark space background
        graphics.fill(0, 0, width, height, 0xff000419);

        // Render diamond pattern lines
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        var matrix = graphics.pose().last().pose();
        for (int i = -height; i <= width; i += 24) {
            bufferBuilder.addVertex(matrix, i, 0, 0).setColor(0xff0f2559);
            bufferBuilder.addVertex(matrix, i + height, height, 0).setColor(0xff0f2559);
        }

        for (int i = width + height; i >= 0; i -= 24) {
            bufferBuilder.addVertex(matrix, i, 0, 0).setColor(0xff0f2559);
            bufferBuilder.addVertex(matrix, i - height, height, 0).setColor(0xff0f2559);
        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        renderSelectionMenu(graphics);
    }

    protected void renderSelectionMenu(GuiGraphics graphics) {
        if (pageIndex == 1) {
            graphics.blitSprite(SELECTION_MENU, 7, height / 2 - 88, 209, 177);
        } else {
            graphics.blitSprite(SMALL_SELECTION_MENU, 7, height / 2 - 88, 105, 177);
        }

        if (pageIndex == 1 && selectedPlanet != null) {
            graphics.drawCenteredString(font, menu.getPlanetName(selectedPlanet), 57, height / 2 - 60, 0xffffff);

            // Render planet info
            renderPlanetInfo(graphics);
        } else {
            graphics.drawCenteredString(font, Component.literal("Planetary Catalog"), 57, height / 2 - 60, 0xffffff);
        }
    }

    private void renderPlanetInfo(GuiGraphics graphics) {
        if (selectedPlanet == null) return;

        int infoX = 160;
        int startY = height / 2 - 50;

        graphics.drawString(font,
            Component.literal("Gravity: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1fx", selectedPlanet.properties().gravity())).withStyle(ChatFormatting.WHITE)),
            infoX, startY, 0xffffff);

        graphics.drawString(font,
            Component.literal("Temperature: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.0f°C", selectedPlanet.properties().temperature())).withStyle(ChatFormatting.WHITE)),
            infoX, startY + 12, 0xffffff);

        String atmosphere = selectedPlanet.atmosphere().hasAtmosphere() ?
            (selectedPlanet.atmosphere().breathable() ? "Breathable" : "Toxic") : "None";
        graphics.drawString(font,
            Component.literal("Atmosphere: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(atmosphere).withStyle(
                    selectedPlanet.atmosphere().breathable() ? ChatFormatting.GREEN : ChatFormatting.RED)),
            infoX, startY + 24, 0xffffff);

        graphics.drawString(font,
            Component.literal("Distance: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(selectedPlanet.properties().orbitDistance() + "M km").withStyle(ChatFormatting.WHITE)),
            infoX, startY + 36, 0xffffff);
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
        if (pageIndex > 0) {
            scrollAmount = 0;
            pageIndex = 0;
            selectedPlanet = null;
            rebuildWidgets();
            return;
        }
        super.onClose();
    }

    protected void setScrollAmount(double amount) {
        scrollAmount = Mth.clamp(amount, 0.0, Math.max(0, planetButtons.size() * 24 - 131));
    }

    public void land(Planet planet) {
        ModNetworking.sendToServer(new PlanetTeleportPacket(planet.id().toString()));
        minecraft.setScreen(null); // Close the screen
    }
}
