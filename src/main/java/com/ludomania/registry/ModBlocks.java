package com.ludomania.registry;

import com.ludomania.Ludomania;
import com.ludomania.blocks.SlotMachineBlock;
import com.ludomania.blocks.WheelOfFortuneBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Реестр блоков мода.
 */
public final class ModBlocks {
    private ModBlocks() {
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Ludomania.MODID);

    /** Слот-машина — трёхбарабанные крутки за фишки. */
    public static final DeferredBlock<Block> SLOT_MACHINE = BLOCKS.register("slot_machine",
        () -> new SlotMachineBlock(BlockBehaviour.Properties.of()
            .strength(2.5F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));

    /** Колесо удачи — восьмисекторная крутилка с призами. */
    public static final DeferredBlock<Block> WHEEL_OF_FORTUNE = BLOCKS.register("wheel_of_fortune",
        () -> new WheelOfFortuneBlock(BlockBehaviour.Properties.of()
            .strength(2.5F)
            .sound(SoundType.WOOD)
            .requiresCorrectToolForDrops()));
}
