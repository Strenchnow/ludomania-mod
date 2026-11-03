package com.ludomania.registry;

import com.ludomania.Ludomania;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Вкладка креатива мода.
 */
public final class ModCreativeTabs {
    private ModCreativeTabs() {
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ludomania.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LUDOMANIA_TAB =
        CREATIVE_TABS.register("ludomania_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ludomania"))
            .icon(() -> new ItemStack(ModItems.CASINO_CHIP.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.CASINO_CHIP.get());
                output.accept(ModItems.LOOTBOX.get());
                output.accept(ModBlocks.SLOT_MACHINE.get());
                output.accept(ModBlocks.WHEEL_OF_FORTUNE.get());
            })
            .build());
}
