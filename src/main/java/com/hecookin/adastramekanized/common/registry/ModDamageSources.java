package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/**
 * Custom damage sources for the mod.
 */
public class ModDamageSources {

    public static final ResourceKey<DamageType> OXYGEN_DEPRIVATION = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "oxygen_deprivation")
    );

    /**
     * Create oxygen damage source for a level.
     * This damage bypasses armor and is not freeze damage.
     */
    public static DamageSource oxygenDeprivation(Level level) {
        return new DamageSource(
            level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(OXYGEN_DEPRIVATION)
        );
    }

    /**
     * Bootstrap damage types for data generation.
     */
    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(OXYGEN_DEPRIVATION, new DamageType(
            "oxygen_deprivation",  // Message ID
            0.1F                   // Exhaustion
        ));
    }
}