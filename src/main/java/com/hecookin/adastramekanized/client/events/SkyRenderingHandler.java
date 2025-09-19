package com.hecookin.adastramekanized.client.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.client.sky.CelestialBodyConverter;
import com.hecookin.adastramekanized.client.sky.CelestialSkyRenderer;
import com.hecookin.adastramekanized.client.sky.SkyRenderable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

//@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, value = Dist.CLIENT)
public class SkyRenderingHandler {

    //@SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        Camera camera = event.getCamera();
        ResourceKey<Level> dimensionKey = level.dimension();

        // Convert dimension key to planet ID (e.g., "minecraft:overworld" -> "adastramekanized:earth")
        ResourceLocation planetId = mapDimensionToPlanetId(dimensionKey.location());
        Planet planet = PlanetRegistry.getInstance().getPlanet(planetId);

        if (planet == null || planet.rendering() == null) {
            return;
        }

        if (planet.rendering().celestialBodies() == null) {
            return;
        }

        List<SkyRenderable> celestialBodies = CelestialBodyConverter.convertCelestialBodies(
            planet.rendering().celestialBodies()
        );

        if (!celestialBodies.isEmpty()) {
            CelestialSkyRenderer.renderCelestialBodies(
                event.getPoseStack(),
                event.getProjectionMatrix(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false),
                level,
                camera,
                celestialBodies
            );
        }
    }

    private static ResourceLocation mapDimensionToPlanetId(ResourceLocation dimensionLocation) {
        // Simple mapping for our custom dimensions
        if (dimensionLocation.getNamespace().equals(AdAstraMekanized.MOD_ID)) {
            return dimensionLocation; // Direct mapping for our mod dimensions
        }

        // Map vanilla dimensions to our planet IDs if we have planet data for them
        return switch (dimensionLocation.toString()) {
            case "minecraft:overworld" -> ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "earth");
            case "minecraft:the_nether" -> null; // No celestial bodies in Nether
            case "minecraft:the_end" -> null; // No celestial bodies in End
            default -> null;
        };
    }
}