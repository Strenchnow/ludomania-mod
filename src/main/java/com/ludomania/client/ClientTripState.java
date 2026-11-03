package com.ludomania.client;

/**
 * Клиентское состояние «триппа»: интенсивность, длительность и стрик.
 * Сервер присылает TripPayload, экранный оверлей читает это состояние.
 */
public final class ClientTripState {
    private static float intensity;
    private static long untilMs;
    private static int streak;

    private ClientTripState() {
    }

    public static void start(float newIntensity, int seconds, int newStreak) {
        intensity = Math.max(intensity, newIntensity);
        untilMs = System.currentTimeMillis() + seconds * 1000L;
        streak = newStreak;
    }

    /** Текущая альфа оверлея (0..1) с пульсацией. */
    public static float getAlpha() {
        long now = System.currentTimeMillis();
        if (now > untilMs) {
            return 0.0F;
        }
        long left = untilMs - now;
        float fade = Math.min(1.0F, left / 3000.0F);
        float pulse = 0.75F + 0.25F * (float) Math.sin(now / 140.0);
        return intensity * fade * pulse;
    }

    /** Стрик для надписи: > 0 — победный, < 0 — пораженческий. */
    public static int getStreak() {
        return streak;
    }
}
