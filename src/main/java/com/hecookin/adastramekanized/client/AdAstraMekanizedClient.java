package com.hecookin.adastramekanized.client;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.dimension.MoonDimensionEffects;
import com.hecookin.adastramekanized.client.dimension.MarsDimensionEffects;
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
        // Register dimension effects
        event.register(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "moon"),
            new MoonDimensionEffects()
        );
        event.register(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "mars"),
            new MarsDimensionEffects()
        );
        AdAstraMekanized.LOGGER.info("Registered planetary dimension effects");
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