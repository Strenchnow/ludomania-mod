package com.ludomania.net;

import com.ludomania.Ludomania;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Запрос крутки колеса удачи (клиент -> сервер).
 */
public record WheelSpinPayload(BlockPos pos, int stake) implements CustomPacketPayload {
    public static final Type<WheelSpinPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Ludomania.MODID, "wheel_spin"));

    public static final StreamCodec<FriendlyByteBuf, WheelSpinPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, WheelSpinPayload::pos,
        ByteBufCodecs.INT, WheelSpinPayload::stake,
        WheelSpinPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
