package com.farukylmz.createkiday;

/** Pure, deterministic balance rules shared by gameplay code and tests. */
public final class KidayBalance {
    public static final int MILLIBUCKETS_PER_UNIT = 144;
    public static final int MOLD_USES = 100;
    public static final int CAST_TICKS_PER_UNIT = 20;
    public static final int MELTING_TICKS = 100;
    public static final int MINIMUM_MIXER_RPM = 32;
    public static final int CASTING_TANK_CAPACITY = 1_152;
    public static final int LIGHTNING_CHARGE_TICKS = 24_000;
    public static final int TOOL_DURABILITY = 1_796;
    public static final int[] ARMOR_DURABILITY = {385, 560, 525, 455};

    private KidayBalance() {
    }

    public static int castingTicks(int metalUnits) {
        if (metalUnits < 1 || metalUnits > 8) {
            throw new IllegalArgumentException("metalUnits must be between 1 and 8");
        }
        return metalUnits * CAST_TICKS_PER_UNIT;
    }

    public static int requiredFluid(int metalUnits) {
        return Math.multiplyExact(metalUnits, MILLIBUCKETS_PER_UNIT);
    }

    public static int stressCapacity(int rpm) {
        if (rpm < 1 || rpm > 64) {
            throw new IllegalArgumentException("rpm must be between 1 and 64");
        }
        if (rpm <= 8) return 512;
        if (rpm <= 16) return 256;
        if (rpm <= 32) return 128;
        return 64;
    }

    public static int[] diskHitDistribution(int targetCount) {
        if (targetCount < 1) return new int[0];
        if (targetCount == 1) return new int[]{3};
        if (targetCount == 2) return new int[]{2, 1};
        return new int[]{1, 1, 1};
    }

    public static double accelerationMultiplier(int consecutiveBreaks) {
        if (consecutiveBreaks <= 0) return 1.0;
        return 1.0 + Math.min(5, consecutiveBreaks) * 0.06;
    }

    public static double adaptiveChestplateReduction(int repeatedDamageTypeHits) {
        return Math.min(3, Math.max(0, repeatedDamageTypeHits)) * 0.05;
    }

    public static long remainingCharge(long chargedTicks, long lastWorldTick, long currentWorldTick) {
        if (chargedTicks <= 0) return 0;
        if (lastWorldTick < 0 || currentWorldTick <= lastWorldTick) return chargedTicks;
        return Math.max(0, chargedTicks - (currentWorldTick - lastWorldTick));
    }
}
