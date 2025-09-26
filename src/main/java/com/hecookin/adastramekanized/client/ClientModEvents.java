package com.hecookin.adastramekanized.client;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.renderers.blocks.MekanismOxygenDistributorRenderer;
import com.hecookin.adastramekanized.client.renderers.blocks.OxygenDistributorBlockEntityRenderer;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register block entity renderers
            BlockEntityRenderers.register(ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(), (context) -> new OxygenDistributorBlockEntityRenderer());
            // TODO: Update renderer for SimpleMekanismOxygenDistributor
            // BlockEntityRenderers.register(ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(), (context) -> new MekanismOxygenDistributorRenderer());
        });
    }
}