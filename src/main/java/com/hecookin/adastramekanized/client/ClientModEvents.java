package com.hecookin.adastramekanized.client;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.models.armor.SpaceSuitModel;
import com.hecookin.adastramekanized.client.renderers.blocks.MekanismBasedOxygenDistributorRenderer;
import com.hecookin.adastramekanized.client.renderers.blocks.OxygenDistributorBlockEntityRenderer;
import com.hecookin.adastramekanized.client.renderers.blocks.SlidingDoorBlockEntityRenderer;
import com.hecookin.adastramekanized.client.gui.GuiOxygenDistributor;
import com.hecookin.adastramekanized.client.screens.OxygenControllerScreen;
import com.hecookin.adastramekanized.client.screens.OxygenMonitorScreen;
import com.hecookin.adastramekanized.client.screens.WirelessPowerRelayScreen;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.registry.ModItems;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register block entity renderers
            BlockEntityRenderers.register(ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(), (context) -> new OxygenDistributorBlockEntityRenderer());
            BlockEntityRenderers.register(ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(), MekanismBasedOxygenDistributorRenderer::new);
            BlockEntityRenderers.register(ModBlockEntityTypes.SLIDING_DOOR.get(), SlidingDoorBlockEntityRenderer::new);

            // Register armor model renderers
            registerArmorRenderers();
        });
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Register armor model layers
        event.registerLayerDefinition(SpaceSuitModel.SPACE_SUIT_LAYER, SpaceSuitModel::createSpaceSuitLayer);
        event.registerLayerDefinition(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER, SpaceSuitModel::createNetheriteSpaceSuitLayer);
        event.registerLayerDefinition(SpaceSuitModel.JET_SUIT_LAYER, SpaceSuitModel::createJetSuitLayer);
    }

    private static void registerArmorRenderers() {
        // Space suit pieces
        ArmorModelRegistry.register(ModItems.SPACE_HELMET.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.SPACE_SUIT.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.SPACE_PANTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.SPACE_BOOTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.SPACE_SUIT_LAYER, SpaceSuitModel::new));

        // Netherite space suit pieces
        ArmorModelRegistry.register(ModItems.NETHERITE_SPACE_HELMET.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.NETHERITE_SPACE_SUIT.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.NETHERITE_SPACE_PANTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.NETHERITE_SPACE_BOOTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER, SpaceSuitModel::new));

        // Jet suit pieces
        ArmorModelRegistry.register(ModItems.JET_SUIT_HELMET.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.JET_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.JET_SUIT.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.JET_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.JET_SUIT_PANTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.JET_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.JET_SUIT_BOOTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.JET_SUIT_LAYER, SpaceSuitModel::new));
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        // Register menu screens
        event.register(ModMenuTypes.OXYGEN_DISTRIBUTOR.get(), GuiOxygenDistributor::new);
        event.register(ModMenuTypes.OXYGEN_CONTROLLER.get(), OxygenControllerScreen::new);
        event.register(ModMenuTypes.WIRELESS_POWER_RELAY.get(), WirelessPowerRelayScreen::new);
        event.register(ModMenuTypes.OXYGEN_MONITOR.get(), OxygenMonitorScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Register the oxygen distributor top model for the animated part
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/oxygen_distributor_top")));

        // Register sliding door models
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/reinforced_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/reinforced_door_flipped")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/iron_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/steel_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/desh_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/ostrum_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/calorite_sliding_door")));

        // Register frame models too
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_reinforced_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_iron_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_steel_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_desh_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_ostrum_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_calorite_sliding_door")));
    }
}