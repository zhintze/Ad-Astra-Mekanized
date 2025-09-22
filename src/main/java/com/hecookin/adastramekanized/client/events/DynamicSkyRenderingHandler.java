package com.hecookin.adastramekanized.client.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.rendering.DynamicCelestialRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Handles rendering of celestial bodies for dynamic planets.
 * Integrates with the dynamic planet system to show appropriate sky objects.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, value = Dist.CLIENT)
public class DynamicSkyRenderingHandler {

    /**
     * Render celestial bodies after the sky is rendered
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Only render during the AFTER_SKY stage
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        Camera camera = event.getCamera();

        try {
            // Render celestial bodies for dynamic planets
            DynamicCelestialRenderer.renderCelestialBodies(
                event.getPoseStack(),
                event.getProjectionMatrix(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false),
                level,
                camera
            );

        } catch (Exception e) {
            // Log error but don't crash - sky rendering should be resilient
            AdAstraMekanized.LOGGER.warn("Error rendering dynamic celestial bodies: {}", e.getMessage());
            if (AdAstraMekanized.LOGGER.isDebugEnabled()) {
                AdAstraMekanized.LOGGER.debug("Celestial rendering error details:", e);
            }
        }
    }
}