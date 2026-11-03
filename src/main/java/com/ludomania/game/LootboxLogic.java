package com.ludomania.game;

import com.ludomania.items.ChipHelper;
import com.ludomania.net.TripPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Лутбокс «Сюрприз»: серверная рулетка наград.
 * Таблица взвешенная, прямо в коде — таблицы лута датагеном не пахнут.
 */
public final class LootboxLogic {
    private LootboxLogic() {
    }

    public static void open(Player player) {
        RandomSource random = player.getRandom();
        int roll = random.nextInt(100);

        Component message;
        if (roll < 45) {
            // 45%: горсть фишек
            int chips = 6 + random.nextInt(17);
            ChipHelper.giveChips(player, chips);
            message = Component.translatable("ludomania.lootbox.chips", chips);
        } else if (roll < 60) {
            // 15%: изумруды
            giveOrDrop(player, new ItemStack(Items.EMERALD, 1 + random.nextInt(3)));
            message = Component.translatable("ludomania.lootbox.emeralds");
        } else if (roll < 70) {
            // 10%: алмаз
            giveOrDrop(player, new ItemStack(Items.DIAMOND));
            message = Component.translatable("ludomania.lootbox.diamond");
        } else if (roll < 80) {
            // 10%: золотое яблоко
            giveOrDrop(player, new ItemStack(Items.GOLDEN_APPLE));
            message = Component.translatable("ludomania.lootbox.golden_apple");
        } else if (roll < 88) {
            // 8%: порция триппа
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 1));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 1));
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new TripPayload(0.7F, 20, 0));
            }
            message = Component.translatable("ludomania.lootbox.trip");
        } else if (roll < 95) {
            // 7%: пустышка
            message = Component.translatable("ludomania.lootbox.nothing");
            player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            // 5%: фейк-джекпот — жирно, но мутит
            ChipHelper.giveChips(player, 64);
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new TripPayload(0.9F, 18, 7));
            }
            message = Component.translatable("ludomania.lootbox.fake_jackpot");
        }

        player.displayClientMessage(message.withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
