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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders oxygen zones as translucent ice-textured blocks on the client side.
 * Used to visualize oxygen distribution from oxygen distributors.
 */
@OnlyIn(Dist.CLIENT)
public class OxygenZoneRenderer {

    private static final OxygenZoneRenderer INSTANCE = new OxygenZoneRenderer();

    // Track oxygen zones per distributor
    private final Set<BlockPos> visibleOxygenZones = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> oxygenPositions = ConcurrentHashMap.newKeySet();
    private boolean renderingEnabled = false;

    // Use vanilla ice texture for oxygen visualization
    private static final ResourceLocation ICE_TEXTURE = ResourceLocation.withDefaultNamespace("block/ice");

    // Tint color for the ice texture (slight cyan tint)
    private static final float RED = 0.9f;    // Slight reduction for cyan
    private static final float GREEN = 1.0f;  // Full green
    private static final float BLUE = 1.0f;   // Full blue
    private static final float ALPHA = 0.35f; // Semi-transparent

    private OxygenZoneRenderer() {}

    public static OxygenZoneRenderer getInstance() {
        return INSTANCE;
    }

    /**
     * Update the visible oxygen zones from a distributor
     */
    public void updateOxygenZones(Set<BlockPos> zones) {
        visibleOxygenZones.clear();
        oxygenPositions.clear();
        if (zones != null) {
            visibleOxygenZones.addAll(zones);
            oxygenPositions.addAll(zones);
        }
    }

    /**
     * Clear all visible oxygen zones
     */
    public void clearOxygenZones() {
        visibleOxygenZones.clear();
        oxygenPositions.clear();
    }

    /**
     * Toggle rendering on/off
     */
    public void setRenderingEnabled(boolean enabled) {
        this.renderingEnabled = enabled;
        if (!enabled) {
            clearOxygenZones();
        }
    }

    /**
     * Check if rendering is enabled
     */
    public boolean isRenderingEnabled() {
        return renderingEnabled;
    }

    /**
     * Render the oxygen zones in the world
     * Called from world render events
     */
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos) {
        if (!renderingEnabled || visibleOxygenZones.isEmpty()) {
            return;
        }

        // Get the ice texture sprite from the block atlas
        TextureAtlasSprite iceSprite = Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(ICE_TEXTURE);

        // Use translucent render type for see-through effect
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        for (BlockPos pos : visibleOxygenZones) {
            renderOxygenBlock(vertexConsumer, matrix, pos, cameraPos, iceSprite);
        }
    }

    /**
     * Render a single oxygen block as a translucent ice-textured cube
     * Only renders faces that are exposed (not adjacent to other oxygen blocks)
     */
    private void renderOxygenBlock(VertexConsumer vertexConsumer, Matrix4f matrix, BlockPos pos, Vec3 cameraPos, TextureAtlasSprite sprite) {
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
        if (!oxygenPositions.contains(pos.above())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y2, z1,
                x2, y2, z1,
                x2, y2, z2,
                x1, y2, z2,
                0, 1, 0  // Normal pointing up
            );
        }

        // Bottom face (Y-)
        if (!oxygenPositions.contains(pos.below())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y1, z2,
                x2, y1, z2,
                x2, y1, z1,
                x1, y1, z1,
                0, -1, 0  // Normal pointing down
            );
        }

        // North face (Z-)
        if (!oxygenPositions.contains(pos.north())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y1, z1,
                x2, y1, z1,
                x2, y2, z1,
                x1, y2, z1,
                0, 0, -1  // Normal pointing north
            );
        }

        // South face (Z+)
        if (!oxygenPositions.contains(pos.south())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x2, y1, z2,
                x1, y1, z2,
                x1, y2, z2,
                x2, y2, z2,
                0, 0, 1  // Normal pointing south
            );
        }

        // West face (X-)
        if (!oxygenPositions.contains(pos.west())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x1, y1, z2,
                x1, y1, z1,
                x1, y2, z1,
                x1, y2, z2,
                -1, 0, 0  // Normal pointing west
            );
        }

        // East face (X+)
        if (!oxygenPositions.contains(pos.east())) {
            renderTexturedQuad(vertexConsumer, matrix, sprite,
                x2, y1, z1,
                x2, y1, z2,
                x2, y2, z2,
                x2, y2, z1,
                1, 0, 0  // Normal pointing east
            );
        }
    }

    /**
     * Render a textured quad face using the ice texture
     */
    private void renderTexturedQuad(VertexConsumer vertexConsumer, Matrix4f matrix, TextureAtlasSprite sprite,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float x3, float y3, float z3,
                                   float x4, float y4, float z4,
                                   float nx, float ny, float nz) {

        // Get texture UV coordinates from sprite
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        // Add vertices with texture coordinates from the ice sprite
        vertexConsumer.addVertex(matrix, x1, y1, z1)
            .setColor(RED, GREEN, BLUE, ALPHA)
            .setUv(minU, minV)
            .setLight(0xF000F0)  // Full bright
            .setNormal(nx, ny, nz);

        vertexConsumer.addVertex(matrix, x2, y2, z2)
            .setColor(RED, GREEN, BLUE, ALPHA)
            .setUv(maxU, minV)
            .setLight(0xF000F0)
            .setNormal(nx, ny, nz);

        vertexConsumer.addVertex(matrix, x3, y3, z3)
            .setColor(RED, GREEN, BLUE, ALPHA)
            .setUv(maxU, maxV)
            .setLight(0xF000F0)
            .setNormal(nx, ny, nz);

        vertexConsumer.addVertex(matrix, x4, y4, z4)
            .setColor(RED, GREEN, BLUE, ALPHA)
            .setUv(minU, maxV)
            .setLight(0xF000F0)
            .setNormal(nx, ny, nz);
    }
}