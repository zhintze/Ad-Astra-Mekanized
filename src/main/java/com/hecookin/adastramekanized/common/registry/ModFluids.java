package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Registry for mod fluids.
 * Note: When Mekanism is available, we use their oxygen/hydrogen chemicals instead.
 */
public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, AdAstraMekanized.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, AdAstraMekanized.MOD_ID);

    // Placeholder oxygen fluid - only used when Mekanism is not available
    // In the future we may want to add actual fluid implementations
    public static final Supplier<Fluid> OXYGEN = () -> null;
    public static final Supplier<Fluid> HYDROGEN = () -> null;

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
        AdAstraMekanized.LOGGER.info("Registered mod fluids");
    }
}