package com.ludomania.net;

import com.ludomania.client.ClientTripState;
import com.ludomania.game.SlotMachineLogic;
import com.ludomania.game.WheelLogic;
import com.ludomania.screens.SlotMachineScreen;
import com.ludomania.screens.WheelOfFortuneScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Регистрация сетевых пакетов и их обработчики.
 * Обработчики результатов выполняются только на клиенте (классы экранов
 * подгружаются лениво в момент вызова, поэтому сервер их не трогает).
 */
public final class ModNet {
    private ModNet() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Клиент -> сервер: запросы круток
        registrar.playToServer(SlotSpinPayload.TYPE, SlotSpinPayload.STREAM_CODEC, ModNet::handleSlotSpin);
        registrar.playToServer(WheelSpinPayload.TYPE, WheelSpinPayload.STREAM_CODEC, ModNet::handleWheelSpin);

        // Сервер -> клиент: результаты и триппы
        registrar.playToClient(SlotResultPayload.TYPE, SlotResultPayload.STREAM_CODEC, ModNet::handleSlotResult);
        registrar.playToClient(WheelResultPayload.TYPE, WheelResultPayload.STREAM_CODEC, ModNet::handleWheelResult);
        registrar.playToClient(TripPayload.TYPE, TripPayload.STREAM_CODEC, ModNet::handleTrip);
    }

    // ---------------- Серверные обработчики ----------------

    private static void handleSlotSpin(SlotSpinPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                SlotMachineLogic.handleSpin(serverPlayer, payload.pos(), payload.stake());
            }
        });
    }

    private static void handleWheelSpin(WheelSpinPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                WheelLogic.handleSpin(serverPlayer, payload.pos(), payload.stake());
            }
        });
    }

    // ---------------- Клиентские обработчики ----------------

    private static void handleSlotResult(SlotResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof SlotMachineScreen screen) {
                screen.onSpinResult(payload);
            }
        });
    }

    private static void handleWheelResult(WheelResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof WheelOfFortuneScreen screen) {
                screen.onSpinResult(payload);
            }
        });
    }

    private static void handleTrip(TripPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientTripState.start(payload.intensity(), payload.seconds(), payload.streak()));
    }
}
