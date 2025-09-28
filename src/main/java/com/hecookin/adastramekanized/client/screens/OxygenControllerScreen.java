package com.hecookin.adastramekanized.client.screens;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import com.hecookin.adastramekanized.common.menus.OxygenControllerMenu;
import com.hecookin.adastramekanized.common.network.OxygenControllerPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI screen for the Oxygen Network Controller
 */
public class OxygenControllerScreen extends AbstractContainerScreen<OxygenControllerMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/gui/oxygen_controller.png"
    );

    private static final int ENTRIES_PER_PAGE = 5;
    private int currentPage = 0;
    private int maxPage = 0;

    private EditBox renameBox;
    private int renamingIndex = -1;

    private Button prevPageButton;
    private Button nextPageButton;
    private Button masterOnButton;
    private Button masterOffButton;
    private Button clearAllButton;

    private final List<DistributorEntry> distributorEntries = new ArrayList<>();

    public OxygenControllerScreen(OxygenControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        // Update distributor statuses
        PacketDistributor.sendToServer(OxygenControllerPacket.updateStatus());

        // Navigation buttons
        prevPageButton = addRenderableWidget(Button.builder(
            Component.literal("<"),
            btn -> changePage(-1)
        ).bounds(leftPos + 10, topPos + 120, 20, 20).build());

        nextPageButton = addRenderableWidget(Button.builder(
            Component.literal(">"),
            btn -> changePage(1)
        ).bounds(leftPos + 226, topPos + 120, 20, 20).build());

        // Master control buttons
        masterOnButton = addRenderableWidget(Button.builder(
            Component.literal("Enable All"),
            btn -> {
                PacketDistributor.sendToServer(OxygenControllerPacket.masterToggle(true));
                refreshDistributors();
            }
        ).bounds(leftPos + 40, topPos + 120, 60, 20).build());

        masterOffButton = addRenderableWidget(Button.builder(
            Component.literal("Disable All"),
            btn -> {
                PacketDistributor.sendToServer(OxygenControllerPacket.masterToggle(false));
                refreshDistributors();
            }
        ).bounds(leftPos + 105, topPos + 120, 60, 20).build());

        clearAllButton = addRenderableWidget(Button.builder(
            Component.literal("Clear All"),
            btn -> {
                PacketDistributor.sendToServer(OxygenControllerPacket.clearAll());
                refreshDistributors();
            }
        ).bounds(leftPos + 170, topPos + 120, 50, 20).build());

        // Rename text box (hidden by default)
        renameBox = new EditBox(font, leftPos + 10, topPos + 145, 236, 20, Component.literal("Rename"));
        renameBox.setMaxLength(32);
        renameBox.setVisible(false);
        addWidget(renameBox);

        refreshDistributors();
    }

    private void refreshDistributors() {
        distributorEntries.clear();
        DistributorLinkData linkData = menu.getLinkData();

        int y = topPos + 20;
        for (int i = 0; i < linkData.getLinkedDistributors().size() && i < ENTRIES_PER_PAGE; i++) {
            int index = currentPage * ENTRIES_PER_PAGE + i;
            if (index >= linkData.getLinkedDistributors().size()) break;

            DistributorLinkData.LinkedDistributor link = linkData.getLinkedDistributors().get(index);
            DistributorEntry entry = new DistributorEntry(index, link, leftPos + 10, y);
            distributorEntries.add(entry);
            y += 20;
        }

        maxPage = (linkData.getLinkedDistributors().size() - 1) / ENTRIES_PER_PAGE;
        updateButtons();
    }

    private void changePage(int delta) {
        currentPage = Math.max(0, Math.min(maxPage, currentPage + delta));
        refreshDistributors();
    }

    private void updateButtons() {
        prevPageButton.active = currentPage > 0;
        nextPageButton.active = currentPage < maxPage;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render distributor entries
        for (DistributorEntry entry : distributorEntries) {
            entry.render(graphics, mouseX, mouseY);
        }

        // Render page indicator
        if (maxPage > 0) {
            String pageText = String.format("Page %d/%d", currentPage + 1, maxPage + 1);
            graphics.drawString(font, pageText, leftPos + 128 - font.width(pageText) / 2, topPos + 125, 0x404040, false);
        }

        // Render rename box if active
        if (renamingIndex >= 0 && renameBox.isVisible()) {
            renameBox.render(graphics, mouseX, mouseY, partialTick);
        }

        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check distributor entry clicks
        for (DistributorEntry entry : distributorEntries) {
            if (entry.handleClick(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Check rename box
        if (renamingIndex >= 0 && renameBox.isVisible()) {
            if (renameBox.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (renamingIndex >= 0 && renameBox.isVisible()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                // Submit rename
                String newName = renameBox.getValue();
                PacketDistributor.sendToServer(OxygenControllerPacket.renameDistributor(renamingIndex, newName));
                renamingIndex = -1;
                renameBox.setVisible(false);
                refreshDistributors();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                // Cancel rename
                renamingIndex = -1;
                renameBox.setVisible(false);
                return true;
            }
            return renameBox.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Individual distributor entry in the list
     */
    private class DistributorEntry {
        private final int index;
        private final DistributorLinkData.LinkedDistributor link;
        private final int x, y;

        private final Button toggleButton;
        private final Button removeButton;
        private final Button renameButton;

        public DistributorEntry(int index, DistributorLinkData.LinkedDistributor link, int x, int y) {
            this.index = index;
            this.link = link;
            this.x = x;
            this.y = y;

            // Toggle button
            toggleButton = Button.builder(
                Component.literal(link.isEnabled() ? "ON" : "OFF"),
                btn -> {
                    PacketDistributor.sendToServer(OxygenControllerPacket.toggleDistributor(index));
                    link.setEnabled(!link.isEnabled());
                    btn.setMessage(Component.literal(link.isEnabled() ? "ON" : "OFF"));
                }
            ).bounds(x + 180, y, 30, 18).build();

            // Remove button
            removeButton = Button.builder(
                Component.literal("X"),
                btn -> {
                    PacketDistributor.sendToServer(OxygenControllerPacket.removeDistributor(index));
                    refreshDistributors();
                }
            ).bounds(x + 213, y, 18, 18).build();

            // Rename button
            renameButton = Button.builder(
                Component.literal("✎"),
                btn -> startRename()
            ).bounds(x + 160, y, 18, 18).build();
        }

        public void render(GuiGraphics graphics, int mouseX, int mouseY) {
            // Draw distributor info
            BlockPos pos = link.getPos();
            String name = link.getCustomName() != null ? link.getCustomName() : 
                String.format("Distributor %d,%d,%d", pos.getX(), pos.getY(), pos.getZ());

            // Status indicator
            ChatFormatting statusColor = link.isOnline() ? ChatFormatting.GREEN : ChatFormatting.RED;
            graphics.drawString(font, "●", x, y + 4, statusColor.getColor(), false);

            // Name and coordinates
            graphics.drawString(font, name, x + 10, y + 4, 0x404040, false);

            // Status info
            String status = String.format("E:%d O:%d", link.getLastEnergy(), link.getLastOxygen());
            graphics.drawString(font, status, x + 80, y + 4, 0x606060, false);

            // Render buttons
            toggleButton.render(graphics, mouseX, mouseY, 0);
            removeButton.render(graphics, mouseX, mouseY, 0);
            renameButton.render(graphics, mouseX, mouseY, 0);
        }

        public boolean handleClick(double mouseX, double mouseY, int button) {
            if (toggleButton.isMouseOver(mouseX, mouseY)) {
                toggleButton.onPress();
                return true;
            }
            if (removeButton.isMouseOver(mouseX, mouseY)) {
                removeButton.onPress();
                return true;
            }
            if (renameButton.isMouseOver(mouseX, mouseY)) {
                renameButton.onPress();
                return true;
            }
            return false;
        }

        private void startRename() {
            renamingIndex = index;
            renameBox.setValue(link.getCustomName() != null ? link.getCustomName() : "");
            renameBox.setVisible(true);
            renameBox.setFocused(true);
        }
    }
}