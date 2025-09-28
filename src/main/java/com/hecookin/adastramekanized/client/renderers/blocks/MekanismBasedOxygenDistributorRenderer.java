package com.hecookin.adastramekanized.client.renderers.blocks;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Renderer for the Mekanism-based oxygen distributor
 * Uses block model approach for the animated rotating center part
 */
public class MekanismBasedOxygenDistributorRenderer implements BlockEntityRenderer<ImprovedOxygenDistributor> {

    public static final ModelResourceLocation TOP_MODEL = ModelResourceLocation.standalone(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/oxygen_distributor_top"));

    public MekanismBasedOxygenDistributorRenderer(BlockEntityRendererProvider.Context context) {
        // No initialization needed for block model approach
    }

    @Override
    public void render(ImprovedOxygenDistributor blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        var state = blockEntity.getBlockState();
        var face = state.getValue(BlockStateProperties.ATTACH_FACE);
        var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        poseStack.pushPose();

        // Handle different attachment faces
        if (face == AttachFace.CEILING) {
            poseStack.translate(0, 1, 1);
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
        } else if (face == AttachFace.WALL) {
            // Handle wall placement
            if (facing == Direction.NORTH) {
                poseStack.translate(0, 0, 1);
            } else if (facing == Direction.SOUTH) {
                poseStack.translate(1, 0, 0);
            } else if (facing == Direction.WEST) {
                poseStack.translate(1, 0, 1);
            }
            rotateBlock(facing, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        }
        // Floor placement needs no special transformation

        // Use the isActive method directly from the block entity
        boolean isActive = blockEntity.isActive();

        // Calculate rotation based on active state
        float rotation = 0;
        if (isActive) {
            // Use a time-based rotation that's smooth
            long time = System.currentTimeMillis();
            rotation = (time % 3600) / 10f;

            // Debug logging every 2 seconds
            if (time % 2000 < 50) {
                AdAstraMekanized.LOGGER.info("Renderer: isActive={}, rotation={}, energy={}, oxygen={}",
                    isActive, rotation, blockEntity.getEnergyStorage().getEnergyStored(), blockEntity.getOxygenTank().getStored());
            }
        }

        // Always render the top part
        renderTopModel(state, rotation, poseStack, bufferSource, combinedLight, combinedOverlay);

        poseStack.popPose();
    }

    private void renderTopModel(net.minecraft.world.level.block.state.BlockState state, float yRot,
                                PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        // Get the model from the model manager
        var modelManager = Minecraft.getInstance().getModelManager();
        BakedModel blockModel = modelManager.getModel(TOP_MODEL);

        if (blockModel == null || blockModel == modelManager.getMissingModel()) {
            AdAstraMekanized.LOGGER.warn("Top model is missing or null!");
            return;
        }

        poseStack.pushPose();

        // Center rotation point and apply rotation
        poseStack.translate(0.5, 0.5, 0.5);

        // Apply rotation only if we have a non-zero value
        if (yRot != 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

            // Debug log animation details
            if (System.currentTimeMillis() % 1000 < 50) {
                AdAstraMekanized.LOGGER.info("Rendering animated top with rotation: {}", yRot);
            }
        }

        poseStack.translate(-0.5, -0.5, -0.5);

        // Render the model
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
            poseStack.last(),
            buffer.getBuffer(Sheets.cutoutBlockSheet()),
            state,
            blockModel,
            1, 1, 1,
            packedLight, packedOverlay);

        poseStack.popPose();
    }

    // Helper method for wall rotation
    private void rotateBlock(Direction facing, PoseStack poseStack) {
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
        }
    }
}