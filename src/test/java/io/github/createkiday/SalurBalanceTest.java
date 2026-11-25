package io.github.createkiday;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SalurBalanceTest {
    @Test void averagesFloorPositiveAndNegativeValues() {
        assertEquals(12, SalurBalance.averageFloor(14, 10));
        assertEquals(-2, SalurBalance.averageFloor(-3, 0));
    }

    @Test void armorUsesFloorAveragesForVanillaIronAndDiamond() {
        assertArrayEquals(new int[]{264, 384, 360, 312}, SalurBalance.ARMOR_DURABILITY);
        assertArrayEquals(new int[]{2, 7, 5, 2}, SalurBalance.ARMOR_PROTECTION);
        for (int i = 0; i < 4; i++) {
            assertEquals(SalurBalance.ARMOR_DURABILITY[i], SalurBalance.armorDurability(i));
            assertEquals(SalurBalance.ARMOR_PROTECTION[i], SalurBalance.armorProtection(i));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> SalurBalance.armorDurability(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> SalurBalance.armorProtection(4));
        assertEquals(1, SalurBalance.ARMOR_TOUGHNESS);
        assertEquals(12, SalurBalance.ENCHANTABILITY);
        assertEquals(905, SalurBalance.TOOL_DURABILITY);
        assertThrows(ArithmeticException.class, () -> SalurBalance.averageFloor(Integer.MAX_VALUE, 1));
    }

    @Test void armorMovementBonusIsPerPieceAndBounded() {
        assertEquals(0.0, SalurBalance.movementBonus(0));
        assertEquals(0.025, SalurBalance.movementBonus(1));
        assertEquals(0.10, SalurBalance.movementBonus(4));
        assertThrows(IllegalArgumentException.class, () -> SalurBalance.movementBonus(-1));
        assertThrows(IllegalArgumentException.class, () -> SalurBalance.movementBonus(5));
    }

    @Test void oreYRangeIncludesBothSpecifiedEdges() {
        assertTrue(SalurBalance.generatesAtY(0));
        assertTrue(SalurBalance.generatesAtY(64));
        assertFalse(SalurBalance.generatesAtY(-1));
        assertFalse(SalurBalance.generatesAtY(65));
    }

    @Test void fluidVolumesAreExactAndRejectInvalidAmounts() {
        assertEquals(144, SalurBalance.fluidForUnits(1));
        assertEquals(432, SalurBalance.DISK_MILLIBUCKETS);
        assertThrows(IllegalArgumentException.class, () -> SalurBalance.fluidForUnits(0));
        assertThrows(ArithmeticException.class, () -> SalurBalance.fluidForUnits(Integer.MAX_VALUE));
    }
}
