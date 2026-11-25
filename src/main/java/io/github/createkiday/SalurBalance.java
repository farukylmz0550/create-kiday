package io.github.createkiday;

/** Deterministic rules for the iron-to-diamond Salur tier. */
public final class SalurBalance {
    public static final int TOOL_DURABILITY = 905;
    public static final int ENCHANTABILITY = averageFloor(14, 10);
    public static final int ARMOR_TOUGHNESS = averageFloor(0, 2);
    public static final int[] ARMOR_DURABILITY = {
            averageFloor(165, 363), averageFloor(240, 528),
            averageFloor(225, 495), averageFloor(195, 429)
    };
    public static final int[] ARMOR_PROTECTION = {
            averageFloor(2, 3), averageFloor(6, 8),
            averageFloor(5, 6), averageFloor(2, 3)
    };
    public static final int MILLIBUCKETS_PER_INGOT = 144;
    public static final int DISK_MILLIBUCKETS = 432;

    private SalurBalance() { }

    public static int averageFloor(int iron, int diamond) {
        return Math.floorDiv(Math.addExact(iron, diamond), 2);
    }

    public static int armorDurability(int armorTypeIndex) {
        return ARMOR_DURABILITY[checkedArmorIndex(armorTypeIndex)];
    }

    public static int armorProtection(int armorTypeIndex) {
        return ARMOR_PROTECTION[checkedArmorIndex(armorTypeIndex)];
    }

    public static double movementBonus(int equippedPieces) {
        if (equippedPieces < 0 || equippedPieces > 4) throw new IllegalArgumentException("equippedPieces must be 0..4");
        return equippedPieces * 0.025;
    }

    public static int fluidForUnits(int units) {
        if (units < 1) throw new IllegalArgumentException("units must be positive");
        return Math.multiplyExact(units, MILLIBUCKETS_PER_INGOT);
    }

    public static int oreMinimumY() { return 0; }
    public static int oreMaximumY() { return 64; }
    public static boolean generatesAtY(int y) { return y >= oreMinimumY() && y <= oreMaximumY(); }

    private static int checkedArmorIndex(int index) {
        if (index < 0 || index >= ARMOR_DURABILITY.length) throw new IndexOutOfBoundsException(index);
        return index;
    }
}
