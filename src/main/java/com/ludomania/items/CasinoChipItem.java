package com.ludomania.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Казино-фишка — валюта мода. Крафтится из изумруда, обратно тоже.
 */
public class CasinoChipItem extends Item {
    public CasinoChipItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ludomania.casino_chip.desc").withStyle(ChatFormatting.GRAY));
    }
}
