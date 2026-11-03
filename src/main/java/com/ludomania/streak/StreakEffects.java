package com.ludomania.streak;

import com.ludomania.net.TripPayload;
import com.ludomania.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Серверная логика стриков: обновляет данные игрока после каждой крутки
 * и навешивает эффекты — от приятных бонусов до полного триппа.
 *
 * Пороги победной серии:
 *   x3+ — Скорость
 *   x5+ — Прыгучесть + лёгкий трипп на экране
 *   x7+ — Ночное зрение, Удача
 *   x8+ — сильный трипп
 *  ДЖЕКПОТ — максимальный трипп + левитация
 *
 * Пороги серии поражений:
 *   x3+ — Тошнота (мигрень проигравшего)
 *   x5+ — Замедление
 *   x8+ — Тьма + трипп
 */
public final class StreakEffects {
    private StreakEffects() {
    }

    /**
     * Обрабатывает результат крутки: обновляет стрики и применяет эффекты.
     *
     * @return актуальные данные стриков (для отправки клиенту)
     */
    public static StreakData onSpinResult(ServerPlayer player, boolean win, int winChips, boolean jackpot) {
        StreakData data = player.getData(ModAttachments.STREAK.get());

        if (win) {
            data.addWin();
        } else {
            data.addLose();
        }
        data.onSpin(winChips);
        player.setData(ModAttachments.STREAK.get(), data);

        // --- Победная серия: мир становится вкуснее ---
        if (data.getWinStreak() >= 3) {
            addEffect(player, MobEffects.MOVEMENT_SPEED, 600, Math.min(data.getWinStreak() / 3, 2));
        }
        if (data.getWinStreak() >= 5) {
            addEffect(player, MobEffects.JUMP, 600, 1);
            sendTrip(player, 0.35F, 15, data.getWinStreak());
        }
        if (data.getWinStreak() >= 7) {
            addEffect(player, MobEffects.NIGHT_VISION, 1200, 0);
            addEffect(player, MobEffects.LUCK, 1200, 0);
        }
        if (data.getWinStreak() >= 8) {
            sendTrip(player, 0.6F, 20, data.getWinStreak());
        }

        // --- Серия поражений: вселенная давит ---
        if (data.getLoseStreak() >= 3) {
            addEffect(player, MobEffects.CONFUSION, 300, 0);
        }
        if (data.getLoseStreak() >= 5) {
            addEffect(player, MobEffects.MOVEMENT_SLOWDOWN, 600, 0);
        }
        if (data.getLoseStreak() >= 8) {
            addEffect(player, MobEffects.DARKNESS, 400, 0);
            sendTrip(player, 0.5F, 20, -data.getLoseStreak());
        }

        // --- Джекпот: полный погружной трипп ---
        if (jackpot) {
            addEffect(player, MobEffects.CONFUSION, 400, 1);
            addEffect(player, MobEffects.LEVITATION, 60, 0);
            addEffect(player, MobEffects.DOLPHINS_GRACE, 1200, 0);
            sendTrip(player, 1.0F, 25, Math.max(data.getWinStreak(), 7));
        }

        return data;
    }

    private static void addEffect(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int durationTicks, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, durationTicks, amplifier));
    }

    private static void sendTrip(ServerPlayer player, float intensity, int seconds, int streak) {
        PacketDistributor.sendToPlayer(player, new TripPayload(intensity, seconds, streak));
    }
}
