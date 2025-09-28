package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.menus.OxygenDistributorMenu;
import com.hecookin.adastramekanized.common.menus.OxygenControllerMenu;
import com.hecookin.adastramekanized.common.menus.WirelessPowerRelayMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, AdAstraMekanized.MOD_ID);

    public static final Supplier<MenuType<OxygenDistributorMenu>> OXYGEN_DISTRIBUTOR =
            MENUS.register("oxygen_distributor",
                () -> IMenuTypeExtension.create(OxygenDistributorMenu::new));

    public static final Supplier<MenuType<OxygenControllerMenu>> OXYGEN_CONTROLLER =
            MENUS.register("oxygen_controller",
                () -> IMenuTypeExtension.create(OxygenControllerMenu::new));

    public static final Supplier<MenuType<WirelessPowerRelayMenu>> WIRELESS_POWER_RELAY =
            MENUS.register("wireless_power_relay",
                () -> IMenuTypeExtension.create(WirelessPowerRelayMenu::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}