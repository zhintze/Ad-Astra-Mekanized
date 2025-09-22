package com.hecookin.adastramekanized.client.sky;

import com.hecookin.adastramekanized.api.planets.atmosphere.AtmosphericRendering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Renders celestial bodies in the sky.
 * Based on Ad Astra's sky rendering but simplified for our use.
 */
public class CelestialSkyRenderer {

    /**
     * Renders all celestial bodies for a given planet
     */
    public static void renderCelestialBodies(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick,
                                           ClientLevel level, Camera camera, List<SkyRenderable> celestialBodies) {
        if (celestialBodies.isEmpty()) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        for (SkyRenderable celestialBody : celestialBodies) {
            renderCelestialBody(poseStack, projectionMatrix, partialTick, level, celestialBody);
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    /**
     * Renders a single celestial body
     */
    private static void renderCelestialBody(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick,
                                          ClientLevel level, SkyRenderable celestialBody) {

        poseStack.pushPose();

        // Reset to skybox coordinate system - this is crucial for fixed celestial bodies
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0f));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));

        // Calculate time-based rotation for moving objects
        float timeOfDay = level.getTimeOfDay(partialTick);
        Vec3 rotation = calculateRotation(celestialBody, timeOfDay);

        // Apply celestial body rotations in sky coordinates
        // Use the rotation to position objects in the sky dome, not continuously move them
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees((float) rotation.x));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) rotation.y));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) rotation.z));

        // Translate to sky distance
        poseStack.translate(0.0, 100.0, 0.0);

        // Set up rendering state
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, celestialBody.texture());

        if (celestialBody.blend()) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        }

        // Set color
        float alpha = ((celestialBody.color() >> 24) & 0xFF) / 255.0f;
        float red = ((celestialBody.color() >> 16) & 0xFF) / 255.0f;
        float green = ((celestialBody.color() >> 8) & 0xFF) / 255.0f;
        float blue = (celestialBody.color() & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(red, green, blue, alpha);

        // Render the celestial body as a textured quad
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float size = celestialBody.scale() * 30.0f; // Scale factor to make objects visible

        // Create quad vertices facing the viewer
        bufferBuilder.addVertex(matrix, -size, 0.0f, -size).setUv(0.0f, 0.0f);
        bufferBuilder.addVertex(matrix, size, 0.0f, -size).setUv(1.0f, 0.0f);
        bufferBuilder.addVertex(matrix, size, 0.0f, size).setUv(1.0f, 1.0f);
        bufferBuilder.addVertex(matrix, -size, 0.0f, size).setUv(0.0f, 1.0f);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        // Reset render state
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (celestialBody.blend()) {
            RenderSystem.defaultBlendFunc();
        }

        poseStack.popPose();
    }

    /**
     * Calculates rotation based on movement type and time
     */
    private static Vec3 calculateRotation(SkyRenderable celestialBody, float timeOfDay) {
        Vec3 baseRotation = new Vec3(
            celestialBody.globalRotation().x + celestialBody.localRotation().x,
            celestialBody.globalRotation().y + celestialBody.localRotation().y,
            celestialBody.globalRotation().z + celestialBody.localRotation().z
        );

        return switch (celestialBody.movementType()) {
            case TIME_OF_DAY -> new Vec3(
                baseRotation.x + (timeOfDay * 360.0f), // Rotate around X axis for east-to-west arc movement
                baseRotation.y,
                baseRotation.z
            );
            case TIME_OF_DAY_REVERSED -> new Vec3(
                baseRotation.x - (timeOfDay * 360.0f), // Reverse east-to-west arc movement
                baseRotation.y,
                baseRotation.z
            );
            case STATIC -> baseRotation;
        };
    }

    /**
     * Renders stars in the sky based on planet configuration
     */
    public static void renderStars(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick,
                                  ClientLevel level, AtmosphericRendering.SkyConfiguration skyConfig) {
        if (!skyConfig.hasStars() || skyConfig.starCount() <= 0) {
            return;
        }

        // Check star visibility mode
        if (skyConfig.starVisibility() == AtmosphericRendering.StarVisibility.NIGHT_ONLY) {
            // Get time of day (0.0 = sunrise, 0.5 = sunset, 1.0 = next sunrise)
            float timeOfDay = level.getTimeOfDay(partialTick);
            // Normalize to 0-1 range
            timeOfDay = timeOfDay - (float) Math.floor(timeOfDay);

            // Stars visible during night (roughly 0.0-0.25 and 0.75-1.0)
            // Add fade transition zones
            boolean isNight = timeOfDay <= 0.3f || timeOfDay >= 0.7f;
            if (!isNight) {
                return;
            }
        }

        poseStack.pushPose();

        // Apply the same coordinate system transformation as celestial bodies
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0f));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));

        // Set up star rendering state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        // Use reasonable star count for performance
        int starCount = Math.min(skyConfig.starCount() / 10, 1000);

        // Set up rendering for all stars at once
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, skyConfig.starBrightness());

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        // Generate all stars using simple full-sphere distribution
        RandomSource starRandom = RandomSource.create(10842); // Use Ad Astra's seed

        for (int i = 0; i < starCount; i++) {
            // Generate uniformly distributed points on full sphere using spherical coordinates
            float u = starRandom.nextFloat(); // [0, 1)
            float v = starRandom.nextFloat(); // [0, 1)

            // Convert to spherical coordinates for full sphere coverage
            float theta = u * 2.0f * (float) Math.PI; // azimuth [0, 2π) - full horizontal rotation
            float phi = (float) Math.acos(2.0f * v - 1.0f); // inclination [0, π] - full vertical coverage

            // Convert spherical to Cartesian coordinates
            float sinPhi = Mth.sin(phi);
            float x = sinPhi * Mth.cos(theta);
            float y = Mth.cos(phi);
            float z = sinPhi * Mth.sin(theta);

            // Scale to sky distance
            float skyDistance = 100.0f;
            float starX = x * skyDistance;
            float starY = y * skyDistance;
            float starZ = z * skyDistance;

            // Star size with some variation
            float size = 0.1f + starRandom.nextFloat() * 0.2f;

            // Create a simple star quad at the calculated position
            bufferBuilder.addVertex(matrix, starX - size, starY - size, starZ);
            bufferBuilder.addVertex(matrix, starX + size, starY - size, starZ);
            bufferBuilder.addVertex(matrix, starX + size, starY + size, starZ);
            bufferBuilder.addVertex(matrix, starX - size, starY + size, starZ);
        }

        // Render all stars at once
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        // Reset render state
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}