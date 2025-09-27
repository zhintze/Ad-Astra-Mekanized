package com.hecookin.adastramekanized.client;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.renderers.OxygenZoneRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Client-side render event handlers
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, value = Dist.CLIENT)
public class ClientRenderEvents {

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        // Render oxygen zones after translucent blocks
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        OxygenZoneRenderer renderer = OxygenZoneRenderer.getInstance();
        if (!renderer.isRenderingEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = camera.getPosition();

        // Get render buffers
        RenderBuffers renderBuffers = mc.renderBuffers();
        MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();

        // Render the oxygen zones
        poseStack.pushPose();
        renderer.render(poseStack, bufferSource, cameraPos);
        bufferSource.endBatch();
        poseStack.popPose();
    }
}