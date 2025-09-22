package com.hecookin.adastramekanized.client;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.dimension.*;
import com.hecookin.adastramekanized.common.planets.DimensionEffectsType;
import com.hecookin.adastramekanized.client.renderers.blocks.OxygenDistributorBlockEntityRenderer;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import com.hecookin.adastramekanized.common.registry.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.function.BiConsumer;

@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, value = Dist.CLIENT)
public class AdAstraMekanizedClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Client-side initialization
        event.enqueueWork(() -> {
            // Celestial body rendering is handled by dimension effects
        });
        AdAstraMekanized.LOGGER.info("AdAstra Mekanized client setup complete!");
    }

    @SubscribeEvent
    public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        // Register template dimension effects for dynamic planets
        for (DimensionEffectsType type : DimensionEffectsType.values()) {
            TemplateDimensionEffects effects = createTemplateEffects(type);
            event.register(type.getResourceLocation(), effects);
        }

        AdAstraMekanized.LOGGER.info("Registered planetary dimension effects: {} templates",
            DimensionEffectsType.values().length);
    }

    /**
     * Create template dimension effects instance for given type
     */
    private static TemplateDimensionEffects createTemplateEffects(DimensionEffectsType type) {
        return switch (type) {
            case MOON_LIKE -> new MoonLikeDimensionEffects();
            case ROCKY -> new RockyDimensionEffects();
            case GAS_GIANT -> new GasGiantDimensionEffects();
            case ICE_WORLD -> new IceWorldDimensionEffects();
            case VOLCANIC -> new VolcanicDimensionEffects();
            case ASTEROID_LIKE -> new MoonLikeDimensionEffects(); // Use moon-like effects for asteroids
            case ALTERED_OVERWORLD -> new RockyDimensionEffects(); // Use rocky effects for altered overworld
        };
    }

    @SubscribeEvent
    public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register block entity renderers
        event.registerBlockEntityRenderer(
            ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(),
            context -> new OxygenDistributorBlockEntityRenderer()
        );
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Register the top model for the oxygen processing station
        event.register(ModelResourceLocation.standalone(OxygenDistributorBlockEntityRenderer.TOP_MODEL));
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // Register custom item renderer for oxygen processing station
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new OxygenDistributorBlockEntityRenderer.ItemRenderer();
            }
        }, ModBlocks.OXYGEN_DISTRIBUTOR.get().asItem());
    }

    // Register special item renderers (for inventory display)
    public static void onRegisterItemRenderers(BiConsumer<Item, BlockEntityWithoutLevelRenderer> consumer) {
        // TODO: Add item renderer registration when needed
        // The block item is automatically created by the ModBlocks registration
    }
}