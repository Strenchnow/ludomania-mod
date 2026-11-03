package com.ludomania;

import com.ludomania.client.ClientEvents;
import com.ludomania.net.ModNet;
import com.ludomania.registry.ModAttachments;
import com.ludomania.registry.ModBlocks;
import com.ludomania.registry.ModCreativeTabs;
import com.ludomania.registry.ModItems;
import com.ludomania.registry.ModMenus;
import com.ludomania.registry.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Ludomania — мод про крутки и азарт.
 *
 * Фишки (casino_chip) — игровая валюта. Слот-машина и колесо удачи
 * крутятся за фишки из инвентаря, выигрыш возвращается фишками.
 * Серии побед/поражений (стрики) накапливаются на игроке и вызывают
 * все более "наркоманские" эффекты: искажение экрана, статус-эффекты,
 * салюты. Всё виртуальное — реальных ставок нет.
 */
@Mod(Ludomania.MODID)
public final class Ludomania {
    public static final String MODID = "ludomania";

    public Ludomania(IEventBus modEventBus) {
        // Регистрация всех объектов мода
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        // Сетевые пакеты (крутки и результаты)
        modEventBus.addListener(ModNet::registerPayloads);

        // Клиентские подписки — только на клиенте
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientEvents::registerScreens);
            NeoForge.EVENT_BUS.addListener(ClientEvents::renderScreenOverlay);
        }
    }
}
