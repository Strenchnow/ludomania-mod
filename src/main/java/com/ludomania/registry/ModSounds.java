package com.ludomania.registry;

import com.ludomania.Ludomania;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Звуки казино: все синтезированы специально для мода.
 */
public final class ModSounds {
    private ModSounds() {
    }

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Ludomania.MODID);

    /** Тиканье вращающихся барабанов. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOT_SPIN = register("slot_spin");
    /** Выигрышная завитушка. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOT_WIN = register("slot_win");
    /** Грустный проигрышный гудок. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SLOT_LOSE = register("slot_lose");
    /** Вращение колеса удачи. */
    public static final DeferredHolder<SoundEvent, SoundEvent> WHEEL_SPIN = register("wheel_spin");
    /** ДЖЕКПОТ! */
    public static final DeferredHolder<SoundEvent, SoundEvent> JACKPOT = register("jackpot");
    /** Открытие лутбокса. */
    public static final DeferredHolder<SoundEvent, SoundEvent> LOOTBOX_OPEN = register("lootbox_open");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name,
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Ludomania.MODID, name)));
    }
}
