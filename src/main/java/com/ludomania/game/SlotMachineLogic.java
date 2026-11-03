package com.ludomania.game;

import com.ludomania.items.ChipHelper;
import com.ludomania.menus.SlotMachineMenu;
import com.ludomania.net.SlotResultPayload;
import com.ludomania.registry.ModSounds;
import com.ludomania.streak.StreakData;
import com.ludomania.streak.StreakEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Серверная логика слот-машины: списание ставки, честный (серверный) ролл,
 * выплата, обновление стриков и отправка результата клиенту.
 */
public final class SlotMachineLogic {
    private SlotMachineLogic() {
    }

    /** Максимальная дистанция до автомата, с которой разрешена крутка. */
    private static final double MAX_DISTANCE_SQR = 8.0 * 8.0;

    /**
     * Символы барабанов. weight — шанс выпадения (чем больше, тем чаще),
     * multiplier — выплата за три таких символа (в множителях ставки).
     */
    public enum SlotSymbol {
        SEVEN("7", 0xFFD42A2A, 3, 50),
        BAR("BAR", 0xFF2A2A2A, 6, 20),
        DIAMOND("◆", 0xFF4FD8E8, 7, 25),
        STAR("★", 0xFFF8D838, 10, 15),
        BELL("♪", 0xFFB08020, 14, 10),
        EMERALD("▲", 0xFF30C060, 18, 8),
        CHERRY("♥", 0xFFE03050, 24, 5);

        private final String label;
        private final int color;
        private final int weight;
        private final int multiplier;

        SlotSymbol(String label, int color, int weight, int multiplier) {
            this.label = label;
            this.color = color;
            this.weight = weight;
            this.multiplier = multiplier;
        }

        public String label() {
            return label;
        }

        public int color() {
            return color;
        }
    }

    /** Результат одной крутки. */
    public record Roll(int reel0, int reel1, int reel2, int winChips, boolean jackpot) {
        public boolean isWin() {
            return winChips > 0;
        }
    }

    /** Случайный индекс символа с учётом весов. */
    public static int rollSymbolIndex(RandomSource random) {
        int totalWeight = 0;
        for (SlotSymbol symbol : SlotSymbol.values()) {
            totalWeight += symbol.weight;
        }
        int roll = random.nextInt(totalWeight);
        for (int i = 0; i < SlotSymbol.values().length; i++) {
            roll -= SlotSymbol.values()[i].weight;
            if (roll < 0) {
                return i;
            }
        }
        return 0;
    }

    /** Серверный ролл: три независимых барабана + расчёт выплаты. */
    public static Roll roll(RandomSource random, int stake) {
        int a = rollSymbolIndex(random);
        int b = rollSymbolIndex(random);
        int c = rollSymbolIndex(random);

        int winChips = 0;
        boolean jackpot = false;
        if (a == b && b == c) {
            // Три в ряд — выплата по символу
            winChips = stake * SlotSymbol.values()[a].multiplier;
            jackpot = SlotSymbol.values()[a] == SlotSymbol.SEVEN;
        } else if (a == b || b == c || a == c) {
            // Пара — возврат ставки (отбитие)
            winChips = stake;
        }
        return new Roll(a, b, c, winChips, jackpot);
    }

    /** Обработка запроса крутки от игрока. */
    public static void handleSpin(ServerPlayer player, BlockPos pos, int stake) {
        // Ставка: только разрешённые номиналы
        if (stake != 1 && stake != 10 && stake != 100) {
            return;
        }
        // Игрок должен стоять у открытого меню именно этого автомата
        if (!(player.containerMenu instanceof SlotMachineMenu menu) || !menu.getPos().equals(pos)) {
            return;
        }
        if (player.blockPosition().distSqr(pos) > MAX_DISTANCE_SQR) {
            return;
        }

        Level level = player.level();

        if (!ChipHelper.consumeChips(player, stake)) {
            level.playSound(null, pos, ModSounds.SLOT_LOSE.get(), SoundSource.BLOCKS, 1.0F, 0.8F);
            player.displayClientMessage(Component.translatable("ludomania.not_enough_chips"), true);
            return;
        }

        Roll roll = roll(player.getRandom(), stake);
        if (roll.isWin()) {
            ChipHelper.giveChips(player, roll.winChips());
        }

        StreakData streak = StreakEffects.onSpinResult(player, roll.isWin(), roll.winChips(), roll.jackpot());

        level.playSound(null, pos, ModSounds.SLOT_SPIN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (roll.jackpot() && level instanceof ServerLevel serverLevel) {
            level.playSound(null, pos, ModSounds.JACKPOT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.FIREWORK,
                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                80, 0.4, 0.6, 0.4, 0.05);
        }

        player.displayClientMessage(buildActionBarMessage(roll, streak), true);

        PacketDistributor.sendToPlayer(player, new SlotResultPayload(
            roll.reel0(), roll.reel1(), roll.reel2(),
            stake, roll.winChips(),
            streak.getWinStreak(), streak.getLoseStreak(),
            roll.jackpot()));
    }

    private static Component buildActionBarMessage(Roll roll, StreakData streak) {
        if (roll.jackpot()) {
            return Component.translatable("ludomania.jackpot");
        }
        if (roll.isWin()) {
            return Component.translatable("ludomania.win", roll.winChips());
        }
        int loseStreak = streak.getLoseStreak();
        if (loseStreak >= 3) {
            return Component.translatable("ludomania.lose_streak", loseStreak);
        }
        return Component.translatable("ludomania.lose");
    }
}
