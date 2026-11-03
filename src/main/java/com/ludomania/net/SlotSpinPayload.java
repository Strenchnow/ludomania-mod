package com.ludomania.net;

import com.ludomania.Ludomania;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Запрос крутки слот-машины (клиент -> сервер).
 */
public record SlotSpinPayload(BlockPos pos, int stake) implements CustomPacketPayload {
    public static final Type<SlotSpinPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Ludomania.MODID, "slot_spin"));

    public static final StreamCodec<FriendlyByteBuf, SlotSpinPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, SlotSpinPayload::pos,
        ByteBufCodecs.INT, SlotSpinPayload::stake,
        SlotSpinPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
