package com.ludomania.net;

import com.ludomania.Ludomania;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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

    public static final StreamCodec<FriendlyByteBuf, SlotResultPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SlotResultPayload::reel0,
        ByteBufCodecs.INT, SlotResultPayload::reel1,
        ByteBufCodecs.INT, SlotResultPayload::reel2,
        ByteBufCodecs.INT, SlotResultPayload::stake,
        ByteBufCodecs.INT, SlotResultPayload::winChips,
        ByteBufCodecs.INT, SlotResultPayload::winStreak,
        ByteBufCodecs.INT, SlotResultPayload::loseStreak,
        ByteBufCodecs.BOOL, SlotResultPayload::jackpot,
        SlotResultPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
