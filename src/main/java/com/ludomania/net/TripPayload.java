package com.ludomania.net;

import com.ludomania.Ludomania;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Команда «включи трипп» (сервер -> клиент): интенсивность, длительность, стрик.
 * Клиент рисует психоделический оверлей поверх экрана.
 */
public record TripPayload(float intensity, int seconds, int streak) implements CustomPacketPayload {
    public static final Type<TripPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Ludomania.MODID, "trip"));

    public static final StreamCodec<FriendlyByteBuf, TripPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, TripPayload::intensity,
        ByteBufCodecs.INT, TripPayload::seconds,
        ByteBufCodecs.INT, TripPayload::streak,
        TripPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
