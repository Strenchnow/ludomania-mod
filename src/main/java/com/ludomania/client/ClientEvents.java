package com.ludomania.client;

import com.ludomania.registry.ModMenus;
import com.ludomania.screens.SlotMachineScreen;
import com.ludomania.screens.WheelOfFortuneScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.awt.Color;

/**
 * Клиентские события: регистрация экранов меню и отрисовка
 * психоделического оверлея стриков поверх любого экрана.
 */
public final class ClientEvents {
    private ClientEvents() {
    }

    /** Привязка меню к экранам. */
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.SLOT_MACHINE.get(), SlotMachineScreen::new);
        event.register(ModMenus.WHEEL_OF_FORTUNE.get(), WheelOfFortuneScreen::new);
    }

    /**
     * Оверлей триппа: переливающийся градиент, волнистые надписи
     * и пульсирующая рамка. Рисуется поверх любого открытого экрана,
     * пока активен стрик.
     */
    public static void renderScreenOverlay(ScreenEvent.Render.Post event) {
        float alpha = ClientTripState.getAlpha();
        if (alpha <= 0.02F) {
            return;
        }
        GuiGraphics g = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        long now = System.currentTimeMillis();

        float hue = (now % 8000L) / 8000.0F;
        int top = withAlpha(Color.HSBtoRGB(hue, 0.75F, 1.0F), alpha * 0.35F);
        int bottom = withAlpha(Color.HSBtoRGB((hue + 0.35F) % 1.0F, 0.75F, 1.0F), alpha * 0.35F);
        g.fillGradient(0, 0, w, h, top, bottom);

        // Волнистые надписи
        double wave = Math.sin(now / 130.0) * 10.0;
        int textColor = ((int) (Math.min(1.0F, alpha + 0.25F) * 255) << 24) | 0xFFFFFF;
        int streak = ClientTripState.getStreak();
        String line = streak > 0
            ? "СЕРИЯ ПОБЕД x" + streak
            : "СЕРИЯ ПОРАЖЕНИЙ x" + Math.max(1, -streak);
        g.drawCenteredString(mc.font, line, w / 2, (int) (h * 0.22 + wave), textColor);
        g.drawCenteredString(mc.font, "7 7 7   К Р У Т И   Е Щ Е   7 7 7", w / 2, (int) (h * 0.72 - wave), (textColor & 0xFF000000) | 0x00E0A030);

        // Пульсирующая рамка по краям экрана
        int border = withAlpha(Color.HSBtoRGB((hue + 0.6F) % 1.0F, 0.9F, 1.0F), alpha * 0.85F);
        g.fill(0, 0, w, 3, border);
        g.fill(0, h - 3, w, h, border);
        g.fill(0, 0, 3, h, border);
        g.fill(w - 3, 0, w, h, border);
    }

    private static int withAlpha(int rgb, float alpha) {
        int clamped = (int) (Math.max(0.0F, Math.min(1.0F, alpha)) * 255);
        return (rgb & 0xFFFFFF) | (clamped << 24);
    }
}
