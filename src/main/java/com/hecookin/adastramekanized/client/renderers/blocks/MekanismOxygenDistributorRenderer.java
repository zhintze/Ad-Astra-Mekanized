package com.hecookin.adastramekanized.client.renderers.blocks;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.TileEntityOxygenDistributor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MekanismOxygenDistributorRenderer implements BlockEntityRenderer<TileEntityOxygenDistributor> {

    public static final ResourceLocation TOP_MODEL = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/oxygen_distributor_top");

    @Override
    public void render(TileEntityOxygenDistributor entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();

        // Only spin if the entity is active (has power)
        float yRot = 0f;
        if (entity.isActive()) {
            // Get rotation for the spinning part with proper angle interpolation
            yRot = lerpAngle(partialTick, entity.lastYRot(), entity.yRot());
        }

        // Render the rotating top part
        renderTopPart(state, yRot, poseStack, buffer, packedLight, packedOverlay);
    }

    // Helper method to properly interpolate angles, handling wraparound
    private static float lerpAngle(float partialTick, float lastAngle, float currentAngle) {
        float diff = currentAngle - lastAngle;

        // Handle wraparound: if the difference is greater than 180, we crossed the 360/0 boundary
        if (diff > 180f) {
            diff -= 360f;
        } else if (diff < -180f) {
            diff += 360f;
        }

        return lastAngle + diff * partialTick;
    }

    private static void renderTopPart(BlockState state, float yRot, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel topModel = minecraft.getModelManager().getModel(ModelResourceLocation.standalone(TOP_MODEL));

        if (topModel == null) {
            // Fallback if model is not found
            return;
        }

        poseStack.pushPose();
        try {
            // Get the block's orientation
            AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            // Transform based on face attachment
            poseStack.translate(0.5, 0.5, 0.5);

            switch (face) {
                case FLOOR:
                    // No additional rotation needed for floor placement
                    break;
                case WALL:
                    // Rotate to face outward from wall
                    switch (facing) {
                        case NORTH:
                            poseStack.mulPose(Axis.XP.rotationDegrees(90));
                            break;
                        case SOUTH:
                            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                            break;
                        case EAST:
                            poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
                            break;
                        case WEST:
                            poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                            break;
                    }
                    break;
                case CEILING:
                    // Flip upside down for ceiling
                    poseStack.mulPose(Axis.XP.rotationDegrees(180));
                    break;
            }

            // Apply the spinning rotation
            poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));

            // Translate back
            poseStack.translate(-0.5, -0.5, -0.5);

            // Render the top model
            minecraft.getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(Sheets.cutoutBlockSheet()),
                state,
                topModel,
                1.0f, 1.0f, 1.0f,
                packedLight, packedOverlay);
        } finally {
            poseStack.popPose();
        }
    }
}