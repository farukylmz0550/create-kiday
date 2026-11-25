package io.github.createkiday.entity;

public final class LeiGongBalance {
    public enum Phase { ONE, TWO, THREE }

    private LeiGongBalance() { }

    public static Phase phase(double health, double maxHealth) {
        if (!(maxHealth > 0.0)) throw new IllegalArgumentException("maxHealth must be positive");
        double ratio = Math.max(0.0, health) / maxHealth;
        if (ratio <= 0.30) return Phase.THREE;
        if (ratio <= 0.60) return Phase.TWO;
        return Phase.ONE;
    }

    public static int attackCycleTicks(Phase phase) { return phase == Phase.ONE ? 80 : 53; }
    public static double movementMultiplier(Phase phase) { return phase == Phase.THREE ? 1.25 : 1.0; }
    public static int bossCount(int connectedChunks) {
        if (connectedChunks < 1) return 0;
        return connectedChunks < 256 ? 1 : 2;
    }
    public static boolean coreDrops(float roll) { return roll >= 0.0F && roll < 0.99F; }
    public static boolean necklaceDrops(float roll) { return roll >= 0.0F && roll < 0.05F; }
}
