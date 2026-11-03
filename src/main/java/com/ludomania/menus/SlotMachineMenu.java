package com.ludomania.menus;

import com.ludomania.registry.ModBlocks;
import com.ludomania.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

/**
 * Меню слот-машины. Слотов под предметы нет — все ставки и выплаты
 * идут через инвентарь игрока напрямую, поэтому меню только держит
 * контекст блока (позицию для проверки расстояния).
 */
public class SlotMachineMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    /** Конструктор для сервера. */
    public SlotMachineMenu(int windowId, Inventory inventory, BlockPos pos) {
        super(ModMenus.SLOT_MACHINE.get(), windowId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(inventory.player.level(), pos);
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.SLOT_MACHINE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
