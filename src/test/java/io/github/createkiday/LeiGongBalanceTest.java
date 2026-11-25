package io.github.createkiday;

import static org.junit.jupiter.api.Assertions.*;
import io.github.createkiday.entity.LeiGongBalance;
import org.junit.jupiter.api.Test;

class LeiGongBalanceTest {
    @Test void healthThresholdsAreInclusiveAtSixtyAndThirtyPercent() {
        assertEquals(LeiGongBalance.Phase.ONE, LeiGongBalance.phase(151, 250));
        assertEquals(LeiGongBalance.Phase.TWO, LeiGongBalance.phase(150, 250));
        assertEquals(LeiGongBalance.Phase.TWO, LeiGongBalance.phase(76, 250));
        assertEquals(LeiGongBalance.Phase.THREE, LeiGongBalance.phase(75, 250));
        assertEquals(LeiGongBalance.Phase.THREE, LeiGongBalance.phase(0, 250));
        assertThrows(IllegalArgumentException.class, () -> LeiGongBalance.phase(1, 0));
    }

    @Test void attackAndMovementRulesFollowPhases() {
        assertEquals(80, LeiGongBalance.attackCycleTicks(LeiGongBalance.Phase.ONE));
        assertEquals(53, LeiGongBalance.attackCycleTicks(LeiGongBalance.Phase.TWO));
        assertEquals(53, LeiGongBalance.attackCycleTicks(LeiGongBalance.Phase.THREE));
        assertEquals(1.0, LeiGongBalance.movementMultiplier(LeiGongBalance.Phase.TWO));
        assertEquals(1.25, LeiGongBalance.movementMultiplier(LeiGongBalance.Phase.THREE));
    }

    @Test void bossCountThresholdAndDropChanceBoundariesAreExplicit() {
        assertEquals(0, LeiGongBalance.bossCount(-1));
        assertEquals(0, LeiGongBalance.bossCount(0));
        assertEquals(1, LeiGongBalance.bossCount(255));
        assertEquals(2, LeiGongBalance.bossCount(256));
        assertEquals(2, LeiGongBalance.bossCount(257));
        assertTrue(LeiGongBalance.coreDrops(0.0F));
        assertTrue(LeiGongBalance.coreDrops(0.989F));
        assertFalse(LeiGongBalance.coreDrops(0.99F));
        assertFalse(LeiGongBalance.coreDrops(-0.01F));
        assertTrue(LeiGongBalance.necklaceDrops(0.049F));
        assertFalse(LeiGongBalance.necklaceDrops(0.05F));
        assertFalse(LeiGongBalance.necklaceDrops(-0.01F));
    }

    @Test void healthIsClampedAtZeroAndInvalidMaximumIsRejected() {
        assertEquals(LeiGongBalance.Phase.THREE, LeiGongBalance.phase(-1, 250));
        assertThrows(IllegalArgumentException.class, () -> LeiGongBalance.phase(1, -1));
    }
}
