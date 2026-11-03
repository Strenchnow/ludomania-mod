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
 * Меню колеса удачи — аналогично слот-машине, без предметных слотов.
 */
public class WheelOfFortuneMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public WheelOfFortuneMenu(int windowId, Inventory inventory, BlockPos pos) {
        super(ModMenus.WHEEL_OF_FORTUNE.get(), windowId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(inventory.player.level(), pos);
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.WHEEL_OF_FORTUNE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
