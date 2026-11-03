package com.ludomania.net;

import com.ludomania.Ludomania;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Результат крутки слот-машины (сервер -> клиент).
 * Клиент докручивает барабаны и показывает выигрыш.
 */
public record SlotResultPayload(int reel0, int reel1, int reel2,
                                int stake, int winChips,
                                int winStreak, int loseStreak,
                                boolean jackpot) implements CustomPacketPayload {
    public static final Type<SlotResultPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Ludomania.MODID, "slot_result"));

    // composite() поддерживает максимум 6 полей, поэтому пишем вручную
    public static final StreamCodec<FriendlyByteBuf, SlotResultPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeInt(payload.reel0);
            buf.writeInt(payload.reel1);
            buf.writeInt(payload.reel2);
            buf.writeInt(payload.stake);
            buf.writeInt(payload.winChips);
            buf.writeInt(payload.winStreak);
            buf.writeInt(payload.loseStreak);
            buf.writeBoolean(payload.jackpot);
        },
        buf -> new SlotResultPayload(
            buf.readInt(), buf.readInt(), buf.readInt(),
            buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
            buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
