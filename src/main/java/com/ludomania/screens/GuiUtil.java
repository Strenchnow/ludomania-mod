package com.ludomania.screens;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Утилиты отрисовки GUI, которых нет в ваниле.
 */
public final class GuiUtil {
    private GuiUtil() {
    }

    /**
     * Заливка многоугольника сканлайнами (для секторов колеса).
     * Работает для выпуклых многоугольников.
     */
    public static void fillPolygon(GuiGraphics g, double[] xs, double[] ys, int color) {
        int n = xs.length;
        if (n < 3) {
            return;
        }
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (double y : ys) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        for (int yy = (int) Math.ceil(minY); yy <= (int) Math.floor(maxY); yy++) {
            double yc = yy + 0.5;
            double x1 = Double.MAX_VALUE;
            double x2 = -Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                double yi = ys[i];
                double yj = ys[j];
                if ((yi <= yc && yj > yc) || (yj <= yc && yi > yc)) {
                    double x = xs[i] + (yc - yi) / (yj - yi) * (xs[j] - xs[i]);
                    x1 = Math.min(x1, x);
                    x2 = Math.max(x2, x);
                }
            }
            if (x1 <= x2) {
                g.fill((int) Math.round(x1), yy, (int) Math.round(x2) + 1, yy + 1, color);
            }
        }
    }
}
