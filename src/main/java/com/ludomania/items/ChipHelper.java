package com.ludomania.items;

import com.ludomania.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Операции с фишками в инвентаре игрока:
 * подсчёт, списание ставки, выплата выигрыша.
 */
public final class ChipHelper {
    private ChipHelper() {
    }

    /** Сколько фишек у игрока. */
    public static int countChips(Player player) {
        return player.getInventory().countItem(ModItems.CASINO_CHIP.get());
    }

    /**
     * Списывает amount фишек со всех слотов инвентаря.
     * @return true, если фишек хватило и списание прошло
     */
    public static boolean consumeChips(Player player, int amount) {
        if (countChips(player) < amount) {
            return false;
        }
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.CASINO_CHIP.get())) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) {
                    break;
                }
            }
        }
        return true;
    }

    /** Выдаёт выигрыш фишками; если инвентарь полон — роняет под игрока. */
    public static void giveChips(Player player, int amount) {
        if (amount <= 0) {
            return;
        }
        ItemStack payout = new ItemStack(ModItems.CASINO_CHIP.get(), amount);
        if (!player.getInventory().add(payout)) {
            player.drop(payout, false);
        }
    }
}
