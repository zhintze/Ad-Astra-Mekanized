package com.hecookin.adastramekanized.client.renderers.entities.vehicles;

import com.hecookin.adastramekanized.client.models.entities.vehicles.LanderModel;
import com.hecookin.adastramekanized.common.entities.vehicles.Lander;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Renderer for Lander entity.
 * Uses Ad Astra's lander model and texture.
 */
public class LanderRenderer extends EntityRenderer<Lander> {

    public static final ResourceLocation LANDER_TEXTURE = ResourceLocation.fromNamespaceAndPath("adastramekanized", "textures/entity/lander/lander.png");

    protected final EntityModel<Lander> model;
    private final ResourceLocation texture;

    public LanderRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new LanderModel(context.bakeLayer(LanderModel.LAYER));
        this.texture = LANDER_TEXTURE;
    }

    @Override
    public void render(Lander entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        poseStack.pushPose();

        // Lander is 1.5x scale
        float scale = 1.5F;

        // Position adjustments (Ad Astra uses 1.55F, scaled 1.5x)
        poseStack.translate(0.0F, 1.55F * scale, 0.0F);

        // Rotate to face direction
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        // Apply pitch rotation
        float xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.ZP.rotationDegrees(-xRot));

        // Scale (Ad Astra flips Y and Z, with 2x scale)
        poseStack.scale(-1.0F * scale, -1.0F * scale, 1.0F * scale);

        // Setup animation
        model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        // Render model
        VertexConsumer consumer = buffer.getBuffer(model.renderType(getTextureLocation(entity)));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Lander entity) {
        return texture;
    }
}
