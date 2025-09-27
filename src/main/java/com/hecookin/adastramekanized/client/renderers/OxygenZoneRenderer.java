package com.hecookin.adastramekanized.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders oxygen zones as translucent ice-textured blocks on the client side.
 * Used to visualize oxygen distribution from oxygen distributors.
 * Supports multiple distributors with different colored ice for debugging.
 */
@OnlyIn(Dist.CLIENT)
public class OxygenZoneRenderer {

    private static final OxygenZoneRenderer INSTANCE = new OxygenZoneRenderer();

    // Track oxygen zones per distributor with different colors
    private final Map<BlockPos, DistributorZone> distributorZones = new ConcurrentHashMap<>();
    private final Map<BlockPos, BlockPos> blockOwnership = new ConcurrentHashMap<>(); // Which distributor owns which block
    private boolean renderingEnabled = false;

    // Color palette for different distributors - VIBRANT colors to stand out against blue ice
    private static final float[][] DISTRIBUTOR_COLORS = {
        {0.0f, 1.0f, 1.0f}, // Cyan - bright cyan (no red)
        {1.0f, 0.0f, 0.0f}, // Red - pure red (no green/blue)
        {0.0f, 1.0f, 0.0f}, // Green - pure green (no red/blue)
        {1.0f, 1.0f, 0.0f}, // Yellow - bright yellow (no blue)
        {1.0f, 0.0f, 1.0f}, // Magenta - bright magenta (no green)
        {0.4f, 0.4f, 1.0f}  // Blue - slightly darker blue to contrast with ice
    };

    // Use vanilla ice texture for oxygen visualization
    private static final ResourceLocation ICE_TEXTURE = ResourceLocation.withDefaultNamespace("block/ice");
    private static final float ALPHA = 0.5f; // Semi-transparent - increased for better color visibility

    // Helper class to track zones per distributor
    private static class DistributorZone {
        final BlockPos distributorPos;
        final Set<BlockPos> oxygenBlocks;
        float[] color;
        int colorIndex;
        boolean visible;
        long lastUpdateTime;

        DistributorZone(BlockPos pos, int colorIndex) {
            this.distributorPos = pos;
            this.oxygenBlocks = ConcurrentHashMap.newKeySet();
            this.colorIndex = colorIndex;
            this.color = DISTRIBUTOR_COLORS[colorIndex % DISTRIBUTOR_COLORS.length];
            this.visible = false;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        void setColorIndex(int index) {
            this.colorIndex = index;
            this.color = DISTRIBUTOR_COLORS[index % DISTRIBUTOR_COLORS.length];
        }
    }

    private OxygenZoneRenderer() {}

    public static OxygenZoneRenderer getInstance() {
        return INSTANCE;
    }

    /**
     * Update oxygen zones for a specific distributor.
     * Each distributor gets its own color for debugging.
     * Prevents overlap by checking block ownership.
     */
    public void updateDistributorZones(BlockPos distributorPos, Set<BlockPos> zones, int colorIndex) {
        DistributorZone zone = distributorZones.computeIfAbsent(distributorPos, pos ->
            new DistributorZone(pos, colorIndex)
        );

        // Update color if changed
        if (zone.colorIndex != colorIndex) {
            zone.setColorIndex(colorIndex);
        }

        // Remove old ownership claims
        for (BlockPos oldPos : zone.oxygenBlocks) {
            if (blockOwnership.get(oldPos) == distributorPos) {
                blockOwnership.remove(oldPos);
            }
        }

        // Clear and rebuild with new positions
        zone.oxygenBlocks.clear();
        if (zones != null && !zones.isEmpty()) {
            // Add all zones from server - server already handles conflict resolution
            // Don't do client-side filtering as it can cause desync
            zone.oxygenBlocks.addAll(zones);
            for (BlockPos pos : zones) {
                blockOwnership.put(pos, distributorPos);
            }
        }

        zone.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Update zones without color change
     */
    public void updateDistributorZones(BlockPos distributorPos, Set<BlockPos> zones) {
        DistributorZone zone = distributorZones.get(distributorPos);
        if (zone != null) {
            updateDistributorZones(distributorPos, zones, zone.colorIndex);
        } else {
            // Default to cyan if not set
            updateDistributorZones(distributorPos, zones, 0);
        }
    }

    /**
     * Toggle visibility for a specific distributor
     */
    public void setDistributorVisibility(BlockPos distributorPos, boolean visible) {
        DistributorZone zone = distributorZones.get(distributorPos);
        if (zone != null) {
            zone.visible = visible;
        }
    }

    /**
     * Remove a distributor and its zones
     */
    public void removeDistributor(BlockPos distributorPos) {
        DistributorZone zone = distributorZones.remove(distributorPos);
        if (zone != null) {
            // Remove ownership claims
            for (BlockPos pos : zone.oxygenBlocks) {
                if (blockOwnership.get(pos) == distributorPos) {
                    blockOwnership.remove(pos);
                }
            }
        }
    }

    /**
     * Clear all oxygen zones from all distributors
     */
    public void clearAllZones() {
        distributorZones.clear();
        blockOwnership.clear();
    }

    /**
     * Toggle global rendering on/off
     */
    public void setRenderingEnabled(boolean enabled) {
        this.renderingEnabled = enabled;
        if (!enabled) {
            clearAllZones();
        }
    }

    /**
     * Check if rendering is enabled globally
     */
    public boolean isRenderingEnabled() {
        return renderingEnabled;
    }

    /**
     * Get all oxygen blocks across all distributors for face culling
     */
    private Set<BlockPos> getAllOxygenBlocks() {
        Set<BlockPos> allBlocks = new HashSet<>();
        for (DistributorZone zone : distributorZones.values()) {
            if (zone.visible) {
                allBlocks.addAll(zone.oxygenBlocks);
            }
        }
        return allBlocks;
    }

    /**
     * Render all visible oxygen zones in the world
     * Called from world render events
     */
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos) {
        if (!renderingEnabled || distributorZones.isEmpty()) {
            return;
        }

        // Get the ice texture sprite from the block atlas
        TextureAtlasSprite iceSprite = Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(ICE_TEXTURE);

        // Use translucent render type for see-through effect
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        // Get all oxygen blocks for face culling
        Set<BlockPos> allOxygenBlocks = getAllOxygenBlocks();

        // Render each distributor's zones with its own color
        for (DistributorZone zone : distributorZones.values()) {
            if (zone.visible && !zone.oxygenBlocks.isEmpty()) {
                renderDistributorZone(vertexConsumer, matrix, zone, cameraPos, iceSprite, allOxygenBlocks);
            }
        }
    }

    /**
     * Render a single distributor's oxygen zone
     */
    private void renderDistributorZone(VertexConsumer vertexConsumer, Matrix4f matrix,
                                      DistributorZone zone, Vec3 cameraPos, TextureAtlasSprite sprite,
                                      Set<BlockPos> allOxygenBlocks) {
        for (BlockPos pos : zone.oxygenBlocks) {
            renderOxygenBlock(vertexConsumer, matrix, pos, cameraPos, sprite,
                            zone.color[0], zone.color[1], zone.color[2], allOxygenBlocks);
        }
    }

    /**
     * Render a single oxygen block as a translucent ice-textured cube
     * Only renders faces that are exposed (not adjacent to other oxygen blocks)
     */
    private void renderOxygenBlock(VertexConsumer vertexConsumer, Matrix4f matrix, BlockPos pos,
                                  Vec3 cameraPos, TextureAtlasSprite sprite,
                                  float red, float green, float blue,
                                  Set<BlockPos> allOxygenBlocks) {
        float x1 = (float) (pos.getX() - cameraPos.x);
        float y1 = (float) (pos.getY() - cameraPos.y);
        float z1 = (float) (pos.getZ() - cameraPos.z);
        float x2 = x1 + 1.0f;
        float y2 = y1 + 1.0f;
        float z2 = z1 + 1.0f;

        // Slightly shrink the cube to avoid z-fighting
        float shrink = 0.001f;
        x1 += shrink;
        y1 += shrink;
        z1 += shrink;
        x2 -= shrink;
        y2 -= shrink;
        z2 -= shrink;

        // Only render faces that don't have an adjacent oxygen block
        // This creates a cleaner look when many oxygen blocks are adjacent

        // Top face (Y+)
        if (!allOxygenBlocks.contains(pos.above())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y2, z1,
                x2, y2, z1,
                x2, y2, z2,
                x1, y2, z2,
                0, 1, 0,  // Normal pointing up
                red, green, blue
            );
        }

        // Bottom face (Y-)
        if (!allOxygenBlocks.contains(pos.below())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y1, z2,
                x2, y1, z2,
                x2, y1, z1,
                x1, y1, z1,
                0, -1, 0,  // Normal pointing down
                red, green, blue
            );
        }

        // North face (Z-)
        if (!allOxygenBlocks.contains(pos.north())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y1, z1,
                x2, y1, z1,
                x2, y2, z1,
                x1, y2, z1,
                0, 0, -1,  // Normal pointing north
                red, green, blue
            );
        }

        // South face (Z+)
        if (!allOxygenBlocks.contains(pos.south())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x2, y1, z2,
                x1, y1, z2,
                x1, y2, z2,
                x2, y2, z2,
                0, 0, 1,  // Normal pointing south
                red, green, blue
            );
        }

        // West face (X-)
        if (!allOxygenBlocks.contains(pos.west())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y1, z2,
                x1, y1, z1,
                x1, y2, z1,
                x1, y2, z2,
                -1, 0, 0,  // Normal pointing west
                red, green, blue
            );
        }

        // East face (X+)
        if (!allOxygenBlocks.contains(pos.east())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x2, y1, z1,
                x2, y1, z2,
                x2, y2, z2,
                x2, y2, z1,
                1, 0, 0,  // Normal pointing east
                red, green, blue
            );
        }
    }

    /**
     * Render a textured quad face using the ice texture with custom color tint
     */
    private void renderTexturedQuad(VertexConsumer vertexConsumer, Matrix4f matrix, TextureAtlasSprite sprite,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float x3, float y3, float z3,
                                   float x4, float y4, float z4,
                                   float nx, float ny, float nz,
                                   float red, float green, float blue) {

        // Get texture UV coordinates from sprite
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        // Add vertices with texture coordinates from the ice sprite
        vertexConsumer.addVertex(matrix, x1, y1, z1)
            .setColor(red, green, blue, ALPHA)
            .setUv(minU, minV)
            .setLight(0xF000F0)  // Full bright
            .setNormal(nx, ny, nz);

        vertexConsumer.addVertex(matrix, x2, y2, z2)
            .setColor(red, green, blue, ALPHA)
            .setUv(maxU, minV)
            .setLight(0xF000F0)
            .setNormal(nx, ny, nz);

        vertexConsumer.addVertex(matrix, x3, y3, z3)
            .setColor(red, green, blue, ALPHA)
            .setUv(maxU, maxV)
            .setLight(0xF000F0)
            .setNormal(nx, ny, nz);

        vertexConsumer.addVertex(matrix, x4, y4, z4)
            .setColor(red, green, blue, ALPHA)
            .setUv(minU, maxV)
            .setLight(0xF000F0)
            .setNormal(nx, ny, nz);
    }

    // Legacy methods for backward compatibility
    @Deprecated
    public void updateOxygenZones(Set<BlockPos> zones) {
        // Legacy single-distributor method - assigns to a default distributor
        BlockPos defaultPos = BlockPos.ZERO;
        updateDistributorZones(defaultPos, zones, 0);
        if (zones != null && !zones.isEmpty()) {
            setDistributorVisibility(defaultPos, true);
        }
    }

    @Deprecated
    public void clearOxygenZones() {
        clearAllZones();
    }
}