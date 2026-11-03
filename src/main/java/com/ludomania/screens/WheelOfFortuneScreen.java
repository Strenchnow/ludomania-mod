package com.ludomania.screens;

import com.ludomania.game.WheelLogic;
import com.ludomania.items.ChipHelper;
import com.ludomania.menus.WheelOfFortuneMenu;
import com.ludomania.net.WheelResultPayload;
import com.ludomania.net.WheelSpinPayload;
import com.ludomania.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Экран колеса удачи: восьмисекторное колесо, ставка x1/x5/x25,
 * кручение с затуханием и приземлением на выигрышный сектор.
 * Угол: 0° — указатель наверху, вращение по часовой.
 */
public class WheelOfFortuneScreen extends AbstractContainerScreen<WheelOfFortuneMenu> {
    private static final int IMG_W = 216;
    private static final int IMG_H = 224;
    private static final int WHEEL_R = 72;
    private static final int SECTORS = 8;

    private int stake = 1;
    private boolean spinning;
    private int spinTicks;
    private double angle;
    private WheelResultPayload result;
    private boolean resultSoundPlayed;

    public WheelOfFortuneScreen(WheelOfFortuneMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IMG_W;
        this.imageHeight = IMG_H;
    }

    @Override
    protected void init() {
        super.init();
        int cx = leftPos + imageWidth / 2;
        addRenderableWidget(Button.builder(Component.literal("x1"), b -> stake = 1)
            .bounds(cx - 92, topPos + 182, 40, 18).build());
        addRenderableWidget(Button.builder(Component.literal("x5"), b -> stake = 5)
            .bounds(cx - 47, topPos + 182, 40, 18).build());
        addRenderableWidget(Button.builder(Component.literal("x25"), b -> stake = 25)
            .bounds(cx - 2, topPos + 182, 44, 18).build());
        addRenderableWidget(Button.builder(Component.literal("КРУТИТЬ!"), b -> spin())
            .bounds(cx + 52, topPos + 178, 66, 24).build());
    }

    private void spin() {
        if (spinning) {
            return;
        }
        PacketDistributor.sendToServer(new WheelSpinPayload(menu.getPos(), stake));
        spinning = true;
        spinTicks = 0;
        result = null;
        resultSoundPlayed = false;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.WHEEL_SPIN.get(), 1.0F));
    }

    public void onSpinResult(WheelResultPayload payload) {
        this.result = payload;
    }

    @Override
    protected void containerTick() {
        if (!spinning) {
            return;
        }
        spinTicks++;
        // Разгон и плавное затухание
        double speed = Math.max(1.2, 14.0 - spinTicks * 0.18);
        angle -= speed;
        if (result != null && spinTicks > 55) {
            double target = targetAngle(result.sector());
            double diff = normalize(angle - target);
            angle -= diff * 0.12;
            if (Math.abs(normalize(angle - target)) < 0.6) {
                angle = target;
                spinning = false;
                playResultSound();
            }
        }
    }

    /** Угол поворота, при котором центр сектора оказывается у указателя. */
    private static double targetAngle(int sector) {
        return normalize(-(sector * (360.0 / SECTORS) + (360.0 / SECTORS) / 2));
    }

    /** Приведение угла к диапазону (-180, 180]. */
    private static double normalize(double deg) {
        double d = deg % 360.0;
        if (d > 180.0) {
            d -= 360.0;
        }
        if (d <= -180.0) {
            d += 360.0;
        }
        return d;
    }

    private void playResultSound() {
        if (resultSoundPlayed || result == null) {
            return;
        }
        resultSoundPlayed = true;
        var soundManager = Minecraft.getInstance().getSoundManager();
        if (result.jackpot()) {
            soundManager.play(SimpleSoundInstance.forUI(ModSounds.JACKPOT.get(), 1.0F));
        } else if (result.prizeChips() > 0 || result.trip()) {
            soundManager.play(SimpleSoundInstance.forUI(ModSounds.SLOT_WIN.get(), 1.0F));
        } else {
            soundManager.play(SimpleSoundInstance.forUI(ModSounds.SLOT_LOSE.get(), 1.0F));
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Корпус
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0100A14);
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + 2, 0xFFB08020);
        g.fill(leftPos, topPos + imageHeight - 2, leftPos + imageWidth, topPos + imageHeight, 0xFFB08020);
        g.drawCenteredString(font, title, leftPos + imageWidth / 2, topPos + 8, 0xFFFFE080);

        int cx = leftPos + imageWidth / 2;
        int cy = topPos + 104;

        // Тёмный диск под колесом (32-угольник)
        drawDisc(g, cx, cy, WHEEL_R + 4, 0xFF2A1E10);

        // Секторы
        WheelLogic.Sector[] sectors = WheelLogic.Sector.values();
        for (int k = 0; k < SECTORS; k++) {
            double a1 = angle + k * (360.0 / SECTORS);
            double a2 = angle + (k + 1) * (360.0 / SECTORS) + 1.0; // +1°, чтобы не было щелей
            double[] xs = {cx, rimX(cx, a1, WHEEL_R), rimX(cx, a2, WHEEL_R)};
            double[] ys = {cy, rimY(cy, a1, WHEEL_R), rimY(cy, a2, WHEEL_R)};
            GuiUtil.fillPolygon(g, xs, ys, sectors[k].color());

            // Подпись сектора
            double am = angle + k * (360.0 / SECTORS) + (360.0 / SECTORS) / 2;
            int lx = (int) rimX(cx, am, 46);
            int ly = (int) rimY(cy, am, 46);
            g.drawCenteredString(font, sectors[k].label(), lx, ly - 4, 0xFFFFFFFF);
        }

        // Ступица
        drawDisc(g, cx, cy, 10, 0xFF1A1208);
        drawDisc(g, cx, cy, 5, 0xFFB08020);

        // Указатель сверху
        double[] px = {cx, cx - 8, cx + 8};
        double[] py = {cy - WHEEL_R + 8, cy - WHEEL_R - 6, cy - WHEEL_R - 6};
        GuiUtil.fillPolygon(g, px, py, 0xFFD42A2A);

        // Информация
        var mc = Minecraft.getInstance();
        int chips = mc.player != null ? ChipHelper.countChips(mc.player) : 0;
        g.drawCenteredString(font, "Ставка: " + stake + "    Фишек: " + chips,
            leftPos + imageWidth / 2, topPos + 168, 0xFFFFFFFF);

        if (result != null && !spinning) {
            WheelLogic.Sector sector = sectors[result.sector()];
            String text;
            int color;
            if (result.jackpot()) {
                text = "★★★ ДЖЕКПОТ! +" + result.prizeChips() + " фишек ★★★";
                color = 0xFFFFE030;
            } else if (result.prizeChips() > 0) {
                text = "Сектор " + sector.label() + ": +" + result.prizeChips() + " фишек";
                color = 0xFF60FF60;
            } else if (result.trip()) {
                text = "Сектор TRIP — держись за реальность!";
                color = 0xFFE080FF;
            } else {
                text = "Сектор 0 — пусто. Серия поражений: x" + result.loseStreak();
                color = 0xFF909090;
            }
            g.drawCenteredString(font, text, leftPos + imageWidth / 2, topPos + 206, color);
        }
    }

    /** Координаты точки на ободе: 0° — верх, далее по часовой. */
    private static double rimX(int cx, double deg, int r) {
        return cx + r * Math.sin(Math.toRadians(deg));
    }

    private static double rimY(int cy, double deg, int r) {
        return cy - r * Math.cos(Math.toRadians(deg));
    }

    /** Заполненный круг-полигон. */
    private static void drawDisc(GuiGraphics g, int cx, int cy, int r, int color) {
        int n = 32;
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = rimX(cx, i * (360.0 / n), r);
            ys[i] = rimY(cy, i * (360.0 / n), r);
        }
        GuiUtil.fillPolygon(g, xs, ys, color);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Подписи рисуем сами в renderBg
    }
}
