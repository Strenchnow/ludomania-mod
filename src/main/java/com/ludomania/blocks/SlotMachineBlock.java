package com.ludomania.blocks;

import com.ludomania.menus.SlotMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Слот-машина. ПКМ открывает интерфейс с барабанами.
 * Логика крутки живёт на сервере (SlotMachineLogic).
 */
public class SlotMachineBlock extends Block {
    public SlotMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MenuProvider menuProvider = new SimpleMenuProvider(
                (windowId, inventory, p) -> new SlotMachineMenu(windowId, inventory, pos),
                Component.translatable("container.ludomania.slot_machine"));
            // без extraData буфер на клиенте придёт null — нужно писать позицию блока
            serverPlayer.openMenu(menuProvider, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
