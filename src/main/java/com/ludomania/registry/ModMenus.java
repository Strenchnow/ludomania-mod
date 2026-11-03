package com.ludomania.registry;

import com.ludomania.Ludomania;
import com.ludomania.menus.SlotMachineMenu;
import com.ludomania.menus.WheelOfFortuneMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IForgeMenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Реестр меню (контейнеров) игровых автоматов.
 * В меню нет слотов для предметов — фишки списываются напрямую из инвентаря,
 * поэтому синхронизация предметов между клиентом и сервером не нужна.
 */
public final class ModMenus {
    private ModMenus() {
    }

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Ludomania.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SlotMachineMenu>> SLOT_MACHINE =
        MENUS.register("slot_machine",
            () -> IForgeMenuType.create((windowId, inv, buf) -> new SlotMachineMenu(windowId, inv, buf.readBlockPos())));

    public static final DeferredHolder<MenuType<?>, MenuType<WheelOfFortuneMenu>> WHEEL_OF_FORTUNE =
        MENUS.register("wheel_of_fortune",
            () -> IForgeMenuType.create((windowId, inv, buf) -> new WheelOfFortuneMenu(windowId, inv, buf.readBlockPos())));
}
