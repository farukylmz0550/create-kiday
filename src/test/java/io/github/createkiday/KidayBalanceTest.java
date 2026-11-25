package io.github.createkiday;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KidayBalanceTest {
    @Test
    void castingUsesOneHundredFortyFourMillibucketsAndTwentyTicksPerUnit() {
        assertEquals(144, KidayBalance.requiredFluid(1));
        assertEquals(1_152, KidayBalance.requiredFluid(8));
        assertEquals(20, KidayBalance.castingTicks(1));
        assertEquals(160, KidayBalance.castingTicks(8));
        assertThrows(IllegalArgumentException.class, () -> KidayBalance.castingTicks(0));
        assertThrows(IllegalArgumentException.class, () -> KidayBalance.castingTicks(9));
    }

    @Test
    void lightningEngineSupportsFourRpmTiersWithMatchingCapacity() {
        assertEquals(256, KidayBalance.stressCapacity(16));
        assertEquals(128, KidayBalance.stressCapacity(32));
        assertEquals(64, KidayBalance.stressCapacity(64));
        assertEquals(32, KidayBalance.stressCapacity(128));
        assertArrayEquals(new int[]{16, 32, 64, 128}, java.util.stream.IntStream.range(0, 4)
                .map(KidayBalance::lightningRpm).toArray());
        assertThrows(IllegalArgumentException.class, () -> KidayBalance.stressCapacity(0));
        assertThrows(IllegalArgumentException.class, () -> KidayBalance.stressCapacity(65));
        assertThrows(IllegalArgumentException.class, () -> KidayBalance.lightningRpm(4));
    }

    @Test
    void diskDistributesExactlyThreeHits() {
        assertArrayEquals(new int[]{3}, KidayBalance.diskHitDistribution(1));
        assertArrayEquals(new int[]{2, 1}, KidayBalance.diskHitDistribution(2));
        assertArrayEquals(new int[]{1, 1, 1}, KidayBalance.diskHitDistribution(7));
        assertArrayEquals(new int[0], KidayBalance.diskHitDistribution(0));
        assertArrayEquals(new int[0], KidayBalance.diskHitDistribution(-1));
    }

    @Test
    void accelerationAndAdaptiveArmorAreCapped() {
        assertEquals(1.30, KidayBalance.accelerationMultiplier(5), 0.0001);
        assertEquals(1.30, KidayBalance.accelerationMultiplier(50), 0.0001);
        assertEquals(1.0, KidayBalance.accelerationMultiplier(0), 0.0001);
        assertEquals(0.15, KidayBalance.adaptiveChestplateReduction(8), 0.0001);
        assertEquals(0.0, KidayBalance.adaptiveChestplateReduction(-1), 0.0001);
    }

    @Test
    void lightningChargeDecreasesAcrossUnloadedChunkTime() {
        assertEquals(23_800, KidayBalance.remainingCharge(24_000, 1_000, 1_200));
        assertEquals(0, KidayBalance.remainingCharge(100, 1_000, 1_200));
        assertEquals(0, KidayBalance.remainingCharge(0, 1_000, 1_200));
        assertEquals(24_000, KidayBalance.remainingCharge(24_000, -1, 1_200));
        assertEquals(24_000, KidayBalance.remainingCharge(24_000, 1_200, 1_200));
    }
}
