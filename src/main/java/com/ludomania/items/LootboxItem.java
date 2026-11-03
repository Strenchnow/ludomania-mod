package com.ludomania.items;

import com.ludomania.game.LootboxLogic;
import com.ludomania.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Лутбокс — мгновенная крутка прямо в руке.
 * ПКМ — открыть: рулетка наград на сервере, коробка расходуется.
 */
public class LootboxItem extends Item {
    public LootboxItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), ModSounds.LOOTBOX_OPEN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            LootboxLogic.open(player);
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ludomania.lootbox.desc").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
