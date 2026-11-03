package com.ludomania.registry;

import com.ludomania.Ludomania;
import com.ludomania.items.CasinoChipItem;
import com.ludomania.items.LootboxItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Реестр предметов мода.
 */
public final class ModItems {
    private ModItems() {
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Ludomania.MODID);

    /** Казино-фишка — валюта всех круток. */
    public static final DeferredItem<Item> CASINO_CHIP = ITEMS.register("casino_chip",
        () -> new CasinoChipItem(new Item.Properties()));

    /** Лутбокс «Сюрприз» — одноразовая крутка прямо в руке. */
    public static final DeferredItem<Item> LOOTBOX = ITEMS.register("lootbox",
        () -> new LootboxItem(new Item.Properties()));

    public static final DeferredItem<BlockItem> SLOT_MACHINE = ITEMS.register("slot_machine",
        () -> new BlockItem(ModBlocks.SLOT_MACHINE.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> WHEEL_OF_FORTUNE = ITEMS.register("wheel_of_fortune",
        () -> new BlockItem(ModBlocks.WHEEL_OF_FORTUNE.get(), new Item.Properties()));
}
