package com.ludomania.game;

import com.ludomania.items.ChipHelper;
import com.ludomania.menus.WheelOfFortuneMenu;
import com.ludomania.net.TripPayload;
import com.ludomania.net.WheelResultPayload;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Серверная логика колеса удачи: 8 секторов с призами,
 * взвешенный ролл и выплаты.
 */
public final class WheelLogic {
    private WheelLogic() {
    }

    private static final double MAX_DISTANCE_SQR = 8.0 * 8.0;

    /** Секторы колеса (по часовой от указателя). */
    public enum Sector {
        X2("x2", 0xFF30C060, 18, 2, false, false),
        ZERO("0", 0xFF404040, 16, 0, false, false),
        X5("x5", 0xFF4FD8E8, 10, 5, false, false),
        TRIP("TRIP", 0xFFB040D8, 8, 0, false, true),
        X10("x10", 0xFFF8D838, 6, 10, false, false),
        ZERO2("0", 0xFF404040, 16, 0, false, false),
        X3("x3", 0xFFE08030, 14, 3, false, false),
        JACKPOT("JP", 0xFFD42A2A, 2, 50, true, false);

        private final String label;
        private final int color;
        private final int weight;
        private final int multiplier;
        private final boolean jackpot;
        private final boolean trip;

        Sector(String label, int color, int weight, int multiplier, boolean jackpot, boolean trip) {
            this.label = label;
            this.color = color;
            this.weight = weight;
            this.multiplier = multiplier;
            this.jackpot = jackpot;
            this.trip = trip;
        }

        public String label() {
            return label;
        }

        public int color() {
            return color;
        }
    }

    /** Результат крутки колеса. */
    public record Roll(int sectorIndex, int prizeChips, boolean jackpot, boolean trip) {
    }

    public static Roll roll(RandomSource random, int stake) {
        int totalWeight = 0;
        for (Sector sector : Sector.values()) {
            totalWeight += sector.weight;
        }
        int roll = random.nextInt(totalWeight);
        for (int i = 0; i < Sector.values().length; i++) {
            roll -= Sector.values()[i].weight;
            if (roll < 0) {
                Sector sector = Sector.values()[i];
                return new Roll(i, stake * sector.multiplier, sector.jackpot, sector.trip);
            }
        }
        return new Roll(0, stake * 2, false, false);
    }

    /** Обработка запроса крутки колеса. */
    public static void handleSpin(ServerPlayer player, BlockPos pos, int stake) {
        if (stake != 1 && stake != 5 && stake != 25) {
            return;
        }
        if (!(player.containerMenu instanceof WheelOfFortuneMenu menu) || !menu.getPos().equals(pos)) {
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

        // Призовой сектор TRIP: вместо фишек — порция триппа
        boolean win = roll.prizeChips() > 0 || roll.trip;
        if (roll.prizeChips() > 0) {
            ChipHelper.giveChips(player, roll.prizeChips());
        }

        StreakData streak = StreakEffects.onSpinResult(player, win, roll.prizeChips(), roll.jackpot);

        if (roll.trip) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 500, 1));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 500, 1));
            PacketDistributor.sendToPlayer(player, new TripPayload(0.8F, 22, streak.getWinStreak()));
        }

        level.playSound(null, pos, ModSounds.WHEEL_SPIN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (roll.jackpot && level instanceof ServerLevel serverLevel) {
            level.playSound(null, pos, ModSounds.JACKPOT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.FIREWORK,
                pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                80, 0.4, 0.7, 0.4, 0.05);
        }

        PacketDistributor.sendToPlayer(player, new WheelResultPayload(
            roll.sectorIndex, roll.prizeChips,
            streak.getWinStreak(), streak.getLoseStreak(),
            roll.jackpot, roll.trip));
    }
}
