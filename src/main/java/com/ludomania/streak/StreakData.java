package com.ludomania.streak;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Данные стриков игрока: серии побед и поражений подряд.
 * Хранится как NeoForge-аттачмент на игроке (см. ModAttachments).
 */
public class StreakData {
    private int winStreak;
    private int loseStreak;
    private int totalSpins;
    private int biggestWin;

    /** Кодек для сохранения в NBT мира. */
    public static final Codec<StreakData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("win_streak").forGetter(d -> d.winStreak),
        Codec.INT.fieldOf("lose_streak").forGetter(d -> d.loseStreak),
        Codec.INT.fieldOf("total_spins").forGetter(d -> d.totalSpins),
        Codec.INT.fieldOf("biggest_win").forGetter(d -> d.biggestWin)
    ).apply(instance, StreakData::new));

    public StreakData() {
        this(0, 0, 0, 0);
    }

    private StreakData(int winStreak, int loseStreak, int totalSpins, int biggestWin) {
        this.winStreak = winStreak;
        this.loseStreak = loseStreak;
        this.totalSpins = totalSpins;
        this.biggestWin = biggestWin;
    }

    /** Фиксирует победную крутку. */
    public void addWin() {
        this.winStreak++;
        this.loseStreak = 0;
    }

    /** Фиксирует проигрышную крутку. */
    public void addLose() {
        this.loseStreak++;
        this.winStreak = 0;
    }

    /** Учёт общего числа круток и рекорда выигрыша. */
    public void onSpin(int winChips) {
        this.totalSpins++;
        if (winChips > this.biggestWin) {
            this.biggestWin = winChips;
        }
    }

    public int getWinStreak() {
        return winStreak;
    }

    public int getLoseStreak() {
        return loseStreak;
    }

    public int getTotalSpins() {
        return totalSpins;
    }

    public int getBiggestWin() {
        return biggestWin;
    }

    /** Полный сброс (для команды/отладки). */
    public void reset() {
        this.winStreak = 0;
        this.loseStreak = 0;
    }
}
