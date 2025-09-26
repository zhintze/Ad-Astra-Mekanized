package com.hecookin.adastramekanized.client;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.renderers.blocks.MekanismBasedOxygenDistributorRenderer;
import com.hecookin.adastramekanized.client.renderers.blocks.OxygenDistributorBlockEntityRenderer;
import com.hecookin.adastramekanized.client.gui.GuiOxygenDistributor;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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
        });
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        // Register menu screens
        event.register(ModMenuTypes.OXYGEN_DISTRIBUTOR.get(), GuiOxygenDistributor::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Register the oxygen distributor top model for the animated part
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/oxygen_distributor_top")));
    }
}