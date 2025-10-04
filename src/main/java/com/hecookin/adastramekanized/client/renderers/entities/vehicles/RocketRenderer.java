package com.hecookin.adastramekanized.client.renderers.entities.vehicles;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.models.entities.vehicles.RocketModel;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders Rocket entities in world and inventory.
 * Adapted from Ad Astra for Minecraft 1.21.1.
 */
public class RocketRenderer extends EntityRenderer<Rocket> {

    public static final ResourceLocation TIER_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/entity/rocket/tier_1_rocket.png");
    public static final ResourceLocation TIER_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/entity/rocket/tier_2_rocket.png");
    public static final ResourceLocation TIER_3_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/entity/rocket/tier_3_rocket.png");
    public static final ResourceLocation TIER_4_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        AdAstraMekanized.MOD_ID, "textures/entity/rocket/tier_4_rocket.png");

    protected final EntityModel<Rocket> model;
    private final ResourceLocation texture;

    public RocketRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, ResourceLocation texture) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new RocketModel<>(context.bakeLayer(layer));
        this.texture = texture;
    }

    @Override
    public void render(Rocket entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        poseStack.pushPose();

        // Shake rocket during launch
        if (!Minecraft.getInstance().isPaused() && (entity.isLaunching() || entity.hasLaunched())) {
            entityYaw += (float) (entity.level().random.nextGaussian() * 0.3);
        }

        poseStack.translate(0.0F, 1.55F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        float xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.ZP.rotationDegrees(-xRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(model.renderType(getTextureLocation(entity)));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Rocket entity) {
        return texture;
    }

    /**
     * Item renderer for rockets in inventory/hand.
     */
    public static class ItemRenderer extends BlockEntityWithoutLevelRenderer {

        private final ModelLayerLocation layer;
        private final ResourceLocation texture;
        private EntityModel<?> model;

        public ItemRenderer(ModelLayerLocation layer, ResourceLocation texture) {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
            this.layer = layer;
            this.texture = texture;
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                                 MultiBufferSource buffer, int packedLight, int packedOverlay) {
            if (model == null) {
                model = new RocketModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
            }

            var consumer = buffer.getBuffer(RenderType.entityCutoutNoCullZOffset(texture));
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(0.0, -1.501, 0.0);
            model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }
}
