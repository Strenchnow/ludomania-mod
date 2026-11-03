package com.ludomania.screens;

import com.ludomania.game.SlotMachineLogic;
import com.ludomania.items.ChipHelper;
import com.ludomania.menus.SlotMachineMenu;
import com.ludomania.net.SlotResultPayload;
import com.ludomania.net.SlotSpinPayload;
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
 * Экран слот-машины: три барабана, выбор ставки (x1/x10/x100) и кнопка
 * «КРУТИТЬ!». Сервер решает исход; клиент лишь анимирует остановку
 * барабанов и проигрывает звуки.
 */
public class SlotMachineScreen extends AbstractContainerScreen<SlotMachineMenu> {
    private static final int IMG_W = 232;
    private static final int IMG_H = 208;
    private static final int REEL_W = 48;
    private static final int REEL_H = 60;
    /** Через сколько тиков останавливается последний барабан. */
    private static final int SPIN_END_TICKS = 42;

    private int stake = 1;
    private boolean spinning;
    private int spinTicks;
    private SlotResultPayload result;
    private boolean resultSoundPlayed;
    private final int[] shown = new int[3];

    public SlotMachineScreen(SlotMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IMG_W;
        this.imageHeight = IMG_H;
    }

    @Override
    protected void init() {
        super.init();
        int cx = leftPos + imageWidth / 2;
        addRenderableWidget(Button.builder(Component.literal("x1"), b -> stake = 1)
            .bounds(cx - 104, topPos + 156, 42, 18).build());
        addRenderableWidget(Button.builder(Component.literal("x10"), b -> stake = 10)
            .bounds(cx - 58, topPos + 156, 42, 18).build());
        addRenderableWidget(Button.builder(Component.literal("x100"), b -> stake = 100)
            .bounds(cx - 12, topPos + 156, 46, 18).build());
        addRenderableWidget(Button.builder(Component.literal("КРУТИТЬ!"), b -> spin())
            .bounds(cx + 44, topPos + 150, 72, 24).build());
    }

    private void spin() {
        if (spinning) {
            return;
        }
        PacketDistributor.sendToServer(new SlotSpinPayload(menu.getPos(), stake));
        spinning = true;
        spinTicks = 0;
        result = null;
        resultSoundPlayed = false;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.SLOT_SPIN.get(), 1.0F));
    }

    /** Вызывается сетевым обработчиком при получении результата крутки. */
    public void onSpinResult(SlotResultPayload payload) {
        this.result = payload;
    }

    @Override
    protected void containerTick() {
        if (!spinning) {
            return;
        }
        spinTicks++;
        // Барабаны останавливаются по очереди: левый раньше, правый позже
        for (int i = 0; i < 3; i++) {
            if (spinTicks <= 18 + i * 12) {
                shown[i] = (shown[i] + 1) % SlotMachineLogic.SlotSymbol.values().length;
            }
        }
        if (result != null && spinTicks > SPIN_END_TICKS) {
            spinning = false;
            shown[0] = result.reel0();
            shown[1] = result.reel1();
            shown[2] = result.reel2();
            playResultSound();
        }
    }

    private void playResultSound() {
        if (resultSoundPlayed || result == null) {
            return;
        }
        resultSoundPlayed = true;
        var soundManager = Minecraft.getInstance().getSoundManager();
        if (result.jackpot()) {
            soundManager.play(SimpleSoundInstance.forUI(ModSounds.JACKPOT.get(), 1.0F));
        } else if (result.winChips() > 0) {
            soundManager.play(SimpleSoundInstance.forUI(ModSounds.SLOT_WIN.get(), 1.0F));
        } else {
            soundManager.play(SimpleSoundInstance.forUI(ModSounds.SLOT_LOSE.get(), 1.0F));
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Корпус автомата
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF01A0A0E);
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + 2, 0xFFD42A2A);
        g.fill(leftPos, topPos + imageHeight - 2, leftPos + imageWidth, topPos + imageHeight, 0xFFD42A2A);
        g.fill(leftPos, topPos, leftPos + 2, topPos + imageHeight, 0xFFD42A2A);
        g.fill(leftPos + imageWidth - 2, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFD42A2A);

        g.drawCenteredString(font, title, leftPos + imageWidth / 2, topPos + 10, 0xFFFFE080);

        // Три окна барабанов
        int reelY = topPos + 34;
        for (int i = 0; i < 3; i++) {
            int reelX = leftPos + 40 + i * (REEL_W + 4);
            g.fill(reelX - 2, reelY - 2, reelX + REEL_W + 2, reelY + REEL_H + 2, 0xFF000000);
            g.fill(reelX, reelY, reelX + REEL_W, reelY + REEL_H, 0xFFFAFAF5);
            drawSymbolColumn(g, i, reelX, reelY);
        }

        // Информационная строка
        int infoY = topPos + 108;
        var mc = Minecraft.getInstance();
        int chips = mc.player != null ? ChipHelper.countChips(mc.player) : 0;
        g.drawCenteredString(font, "Ставка: " + stake + "    Фишек: " + chips,
            leftPos + imageWidth / 2, infoY, 0xFFFFFFFF);

        int streakY = infoY + 12;
        if (result != null) {
            if (result.jackpot()) {
                g.drawCenteredString(font, "★★★ ДЖЕКПОТ!!! +" + result.winChips() + " фишек ★★★",
                    leftPos + imageWidth / 2, streakY, 0xFFFFE030);
            } else if (result.winChips() > 0) {
                g.drawCenteredString(font, "Выигрыш: +" + result.winChips() + " фишек",
                    leftPos + imageWidth / 2, streakY, 0xFF60FF60);
            } else {
                g.drawCenteredString(font, "Мимо! Серия поражений: x" + result.loseStreak(),
                    leftPos + imageWidth / 2, streakY, 0xFF909090);
            }
        } else {
            g.drawCenteredString(font, "Крути и проверь удачу",
                leftPos + imageWidth / 2, streakY, 0xFF909090);
        }
    }

    /** Барабан: центральный символ и при вращении — соседние с затуханием. */
    private void drawSymbolColumn(GuiGraphics g, int reel, int x, int y) {
        SlotMachineLogic.SlotSymbol[] symbols = SlotMachineLogic.SlotSymbol.values();
        int center = shown[reel];
        drawSymbol(g, symbols[center], x, y + 18, 0xFF000000, 0xFFFFFFFF);

        boolean moving = spinning && spinTicks <= 18 + reel * 12;
        if (moving) {
            int prev = (center - 1 + symbols.length) % symbols.length;
            int next = (center + 1) % symbols.length;
            drawSymbol(g, symbols[prev], x, y - 2, 0x60000000, 0x60FFFFFF);
            drawSymbol(g, symbols[next], x, y + 38, 0x60000000, 0x60FFFFFF);
        }
    }

    private void drawSymbol(GuiGraphics g, SlotMachineLogic.SlotSymbol symbol, int x, int y, int frameColor, int textColor) {
        g.fill(x + 2, y, x + REEL_W - 2, y + 22, frameColor);
        g.fill(x + 3, y + 1, x + REEL_W - 3, y + 21, symbol.color());
        g.drawCenteredString(font, symbol.label(), x + REEL_W / 2, y + 7, textColor);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Подписи рисуем сами в renderBg — ванильные не нужны
    }
}
