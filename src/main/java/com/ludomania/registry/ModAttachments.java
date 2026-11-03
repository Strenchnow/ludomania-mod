package com.ludomania.registry;

import com.ludomania.Ludomania;
import com.ludomania.streak.StreakData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Данные, прикреплённые к сущностям.
 * Здесь хранится стрик-статистика игрока (серии побед/поражений).
 */
public final class ModAttachments {
    private ModAttachments() {
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Ludomania.MODID);

    /** Стрики игрока. Сохраняются в мире и переживают смерть (copyOnDeath). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<StreakData>> STREAK =
        ATTACHMENTS.register("streak",
            () -> AttachmentType.builder(StreakData::new)
                .serialize(StreakData.CODEC)
                .copyOnDeath()
                .build());
}
