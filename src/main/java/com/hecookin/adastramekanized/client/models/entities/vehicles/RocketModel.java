package com.hecookin.adastramekanized.client.models.entities.vehicles;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Rocket entity model with Tier 1 geometry.
 * Adapted from Ad Astra for Minecraft 1.21.1.
 */
public class RocketModel<T extends Rocket> extends EntityModel<T> {

    public static final ModelLayerLocation TIER_1_LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "tier_1_rocket"), "main");
    public static final ModelLayerLocation TIER_2_LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "tier_2_rocket"), "main");
    public static final ModelLayerLocation TIER_3_LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "tier_3_rocket"), "main");
    public static final ModelLayerLocation TIER_4_LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "tier_4_rocket"), "main");

    private final ModelPart root;

    public RocketModel(ModelPart root) {
        this.root = root.getChild("main");
    }

    @SuppressWarnings({"unused", "DuplicatedCode"})
    public static LayerDefinition createTier1Layer() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition rocket = modelPartData.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0f, 25.0f, 0.0f));

        PartDefinition top = rocket.addOrReplaceChild("top", CubeListBuilder.create()
            .texOffs(0, 48).addBox(10.0f, -52.0f, -10.0f, 0.0f, 2.0f, 20.0f, new CubeDeformation(0.0f))
            .texOffs(0, 68).addBox(-10.0f, -52.0f, -10.0f, 20.0f, 2.0f, 0.0f, new CubeDeformation(0.0f))
            .texOffs(0, 48).addBox(-10.0f, -52.0f, -10.0f, 0.0f, 2.0f, 20.0f, new CubeDeformation(0.0f))
            .texOffs(0, 68).addBox(-10.0f, -52.0f, 10.0f, 20.0f, 2.0f, 0.0f, new CubeDeformation(0.0f))
            .texOffs(104, 67).addBox(-3.0f, -75.0f, -3.0f, 6.0f, 8.0f, 6.0f, new CubeDeformation(0.0f))
            .texOffs(88, 69).addBox(-2.0f, -77.0f, -2.0f, 4.0f, 2.0f, 4.0f, new CubeDeformation(0.0f))
            .texOffs(80, 69).addBox(-1.0f, -89.0f, -1.0f, 2.0f, 13.0f, 2.0f, new CubeDeformation(0.0f))
            .texOffs(64, 69).addBox(-2.0f, -90.0f, -2.0f, 4.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, -1.0f, 0.0f));

        // Antenna supports
        top.addOrReplaceChild("cube_r1", CubeListBuilder.create()
            .texOffs(120, 37).addBox(-1.0f, -3.5f, -2.5f, 2.0f, 27.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -71.0f, 0.0f, -0.48f, -0.7854f, 0.0f));

        top.addOrReplaceChild("cube_r2", CubeListBuilder.create()
            .texOffs(120, 37).addBox(-1.0f, -3.5f, -2.5f, 2.0f, 27.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -71.0f, 0.0f, -0.48f, -2.3562f, 0.0f));

        top.addOrReplaceChild("cube_r3", CubeListBuilder.create()
            .texOffs(120, 37).addBox(-1.0f, -3.5f, -2.5f, 2.0f, 27.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -71.0f, 0.0f, -0.48f, 2.3562f, 0.0f));

        top.addOrReplaceChild("cube_r4", CubeListBuilder.create()
            .texOffs(120, 37).addBox(-1.0f, -3.5f, -2.5f, 2.0f, 27.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -71.0f, 0.0f, -0.48f, 0.7854f, 0.0f));

        // Nose cone panels
        top.addOrReplaceChild("cube_r5", CubeListBuilder.create()
            .texOffs(65, 45).addBox(-8.0f, -20.8f, 8.5175f, 16.0f, 24.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -51.0f, 0.0f, 0.3491f, -1.5708f, 0.0f));

        top.addOrReplaceChild("cube_r6", CubeListBuilder.create()
            .texOffs(65, 45).addBox(-8.0f, -20.8f, 8.5175f, 16.0f, 24.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -51.0f, 0.0f, 0.3491f, 0.0f, 0.0f));

        top.addOrReplaceChild("cube_r7", CubeListBuilder.create()
            .texOffs(65, 45).addBox(-8.0f, -20.8f, 8.5175f, 16.0f, 24.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -51.0f, 0.0f, 0.3491f, 1.5708f, 0.0f));

        top.addOrReplaceChild("cube_r8", CubeListBuilder.create()
            .texOffs(65, 45).addBox(-8.0f, -20.8f, 8.5175f, 16.0f, 24.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -51.0f, 0.0f, 0.3491f, 3.1416f, 0.0f));

        // Body
        PartDefinition body = rocket.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(36, 44).addBox(-6.0f, -42.0f, -10.0f, 12.0f, 12.0f, 1.0f, new CubeDeformation(0.0f))
            .texOffs(35, 58).addBox(-4.0f, -32.0f, -10.0f, 8.0f, 0.0f, 1.0f, new CubeDeformation(0.0f))
            .texOffs(63, 43).addBox(4.0f, -40.0f, -10.0f, 0.0f, 8.0f, 1.0f, new CubeDeformation(0.0f))
            .texOffs(35, 58).addBox(-4.0f, -40.0f, -10.0f, 8.0f, 0.0f, 1.0f, new CubeDeformation(0.0f))
            .texOffs(63, 43).addBox(-4.0f, -40.0f, -10.0f, 0.0f, 8.0f, 1.0f, new CubeDeformation(0.0f))
            .texOffs(0, 0).addBox(-9.0f, -51.0f, -9.0f, 18.0f, 44.0f, 0.0f, new CubeDeformation(0.0f))
            .texOffs(0, 77).addBox(-9.0f, -51.0f, -9.0f, 2.0f, 44.0f, 2.0f, new CubeDeformation(0.0f))
            .texOffs(36, -18).addBox(-9.0f, -51.0f, -9.0f, 0.0f, 44.0f, 18.0f, new CubeDeformation(0.0f))
            .texOffs(36, 0).addBox(-9.0f, -51.0f, 9.0f, 18.0f, 44.0f, 0.0f, new CubeDeformation(0.0f))
            .texOffs(36, -18).addBox(9.0f, -51.0f, -9.0f, 0.0f, 44.0f, 18.0f, new CubeDeformation(0.0f))
            .texOffs(0, 62).addBox(-10.0f, -15.0f, -10.0f, 20.0f, 5.0f, 0.0f, new CubeDeformation(0.0f))
            .texOffs(0, 42).addBox(10.0f, -15.0f, -10.0f, 0.0f, 5.0f, 20.0f, new CubeDeformation(0.0f))
            .texOffs(0, 62).addBox(-10.0f, -15.0f, 10.0f, 20.0f, 5.0f, 0.0f, new CubeDeformation(0.0f))
            .texOffs(0, 42).addBox(-10.0f, -15.0f, -10.0f, 0.0f, 5.0f, 20.0f, new CubeDeformation(0.0f))
            .texOffs(-18, 44).addBox(-9.0f, -7.0f, -9.0f, 18.0f, 0.0f, 18.0f, new CubeDeformation(0.0f))
            .texOffs(-18, 44).addBox(-9.0f, -50.0f, -9.0f, 18.0f, 0.0f, 18.0f, new CubeDeformation(0.0f))
            .texOffs(88, 0).addBox(-13.0f, -17.0f, -3.0f, 4.0f, 9.0f, 6.0f, new CubeDeformation(0.0f))
            .texOffs(0, 71).addBox(-12.0f, -20.0f, -1.0f, 3.0f, 3.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, -1.0f, 0.0f));

        // Body details
        body.addOrReplaceChild("cube_r9", CubeListBuilder.create()
            .texOffs(0, 71).addBox(-12.0f, -21.0f, -1.0f, 3.0f, 3.0f, 2.0f, new CubeDeformation(0.0f))
            .texOffs(88, 0).addBox(-13.0f, -18.0f, -3.0f, 4.0f, 9.0f, 6.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 1.0f, 0.0f, 0.0f, 3.1416f, 0.0f));

        body.addOrReplaceChild("cube_r10", CubeListBuilder.create()
            .texOffs(0, 77).addBox(-1.0f, -22.0f, -1.0f, 2.0f, 44.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(-8.0f, -29.0f, 8.0f, 0.0f, 1.5708f, 0.0f));

        body.addOrReplaceChild("cube_r11", CubeListBuilder.create()
            .texOffs(0, 77).addBox(-1.0f, -22.0f, -1.0f, 2.0f, 44.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(8.0f, -29.0f, 8.0f, 0.0f, 3.1416f, 0.0f));

        body.addOrReplaceChild("cube_r12", CubeListBuilder.create()
            .texOffs(0, 77).addBox(-1.0f, -22.0f, -1.0f, 2.0f, 44.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(8.0f, -29.0f, -8.0f, 0.0f, -1.5708f, 0.0f));

        // Bottom
        PartDefinition bottom = body.addOrReplaceChild("bottom", CubeListBuilder.create()
            .texOffs(94, 15).addBox(-8.0f, -4.0f, -8.0f, 16.0f, 4.0f, 0.0f, new CubeDeformation(0.0f))
            .texOffs(94, -1).addBox(-8.0f, -4.0f, -8.0f, 0.0f, 4.0f, 16.0f, new CubeDeformation(0.0f))
            .texOffs(94, 15).addBox(-8.0f, -4.0f, 8.0f, 16.0f, 4.0f, 0.0f, new CubeDeformation(0.0f))
            .texOffs(94, -1).addBox(8.0f, -4.0f, -8.0f, 0.0f, 4.0f, 16.0f, new CubeDeformation(0.0f))
            .texOffs(78, 22).addBox(-8.0f, -4.0f, -8.0f, 16.0f, 0.0f, 16.0f, new CubeDeformation(0.0f))
            .texOffs(80, 81).addBox(-8.0f, 0.0f, -8.0f, 16.0f, 0.0f, 16.0f, new CubeDeformation(0.0f))
            .texOffs(94, 19).addBox(-6.0f, -7.0f, 6.0f, 12.0f, 3.0f, 0.0f, new CubeDeformation(0.0f))
            .texOffs(94, 19).addBox(-6.0f, -7.0f, -6.0f, 12.0f, 3.0f, 0.0f, new CubeDeformation(0.0f))
            .texOffs(94, 7).addBox(6.0f, -7.0f, -6.0f, 0.0f, 3.0f, 12.0f, new CubeDeformation(0.0f))
            .texOffs(94, 7).addBox(-6.0f, -7.0f, -6.0f, 0.0f, 3.0f, 12.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 0.0f));

        // Fins
        PartDefinition fins = rocket.addOrReplaceChild("fins", CubeListBuilder.create(), PartPose.offset(0.0f, -1.0f, 0.0f));

        fins.addOrReplaceChild("cube_r13", CubeListBuilder.create()
            .texOffs(72, 21).addBox(-1.0f, 1.0f, 13.0f, 2.0f, 15.0f, 9.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(1.0f, 2.0f, -1.0f, 1.1345f, 2.3562f, 0.0f));

        fins.addOrReplaceChild("cube_r14", CubeListBuilder.create()
            .texOffs(72, 0).addBox(-2.0f, -17.0f, 20.0f, 4.0f, 17.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(1.0f, 1.0f, -1.0f, 0.0f, 2.3562f, 0.0f));

        fins.addOrReplaceChild("cube_r15", CubeListBuilder.create()
            .texOffs(72, 0).addBox(-2.0f, -17.0f, 20.0f, 4.0f, 17.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(-1.0f, 1.0f, -1.0f, 0.0f, -2.3562f, 0.0f));

        fins.addOrReplaceChild("cube_r16", CubeListBuilder.create()
            .texOffs(72, 21).addBox(-1.0f, 1.0f, 13.0f, 2.0f, 15.0f, 9.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(-1.0f, 2.0f, -1.0f, 1.1345f, -2.3562f, 0.0f));

        fins.addOrReplaceChild("cube_r17", CubeListBuilder.create()
            .texOffs(72, 0).addBox(-2.0f, -17.0f, 20.0f, 4.0f, 17.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(-1.0f, 1.0f, 1.0f, 0.0f, -0.7854f, 0.0f));

        fins.addOrReplaceChild("cube_r18", CubeListBuilder.create()
            .texOffs(72, 21).addBox(-1.0f, 1.0f, 13.0f, 2.0f, 15.0f, 9.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(-1.0f, 2.0f, 1.0f, 1.1345f, -0.7854f, 0.0f));

        fins.addOrReplaceChild("cube_r19", CubeListBuilder.create()
            .texOffs(72, 0).addBox(-2.0f, -17.0f, 20.0f, 4.0f, 17.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(1.0f, 1.0f, 1.0f, 0.0f, 0.7854f, 0.0f));

        fins.addOrReplaceChild("cube_r20", CubeListBuilder.create()
            .texOffs(72, 21).addBox(-1.0f, 1.0f, 13.0f, 2.0f, 15.0f, 9.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(1.0f, 2.0f, 1.0f, 1.1345f, 0.7854f, 0.0f));

        return LayerDefinition.create(modelData, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Rockets don't animate
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer,
                                int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
