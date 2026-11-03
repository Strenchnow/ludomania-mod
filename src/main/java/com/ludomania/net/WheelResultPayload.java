package com.ludomania.net;

import com.ludomania.Ludomania;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Результат крутки колеса удачи (сервер -> клиент).
 */
public record WheelResultPayload(int sector, int prizeChips,
                                 int winStreak, int loseStreak,
                                 boolean jackpot, boolean trip) implements CustomPacketPayload {
    public static final Type<WheelResultPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Ludomania.MODID, "wheel_result"));

    public static final StreamCodec<FriendlyByteBuf, WheelResultPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, WheelResultPayload::sector,
        ByteBufCodecs.INT, WheelResultPayload::prizeChips,
        ByteBufCodecs.INT, WheelResultPayload::winStreak,
        ByteBufCodecs.INT, WheelResultPayload::loseStreak,
        ByteBufCodecs.BOOL, WheelResultPayload::jackpot,
        ByteBufCodecs.BOOL, WheelResultPayload::trip,
        WheelResultPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
