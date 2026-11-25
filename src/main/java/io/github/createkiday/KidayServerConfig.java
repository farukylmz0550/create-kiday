package io.github.createkiday;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-owned balance values used by kinetic sources and recipe integrations. */
public final class KidayServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue BIOMASS_FUEL_TICKS;
    public static final ModConfigSpec.IntValue LIGHTNING_CHARGE_TICKS;
    public static final ModConfigSpec.IntValue LIGHTNING_MAX_CHARGE_TICKS;
    public static final ModConfigSpec.IntValue LIGHTNING_CAPACITY_16;
    public static final ModConfigSpec.IntValue LIGHTNING_CAPACITY_32;
    public static final ModConfigSpec.IntValue LIGHTNING_CAPACITY_64;
    public static final ModConfigSpec.IntValue LIGHTNING_CAPACITY_128;
    public static final ModConfigSpec.DoubleValue STORM_ASSEMBLY_CHANCE;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("motors");
        BIOMASS_FUEL_TICKS = BUILDER.defineInRange("biomassFuelTicks", 2_400, 1, 1_728_000);
        LIGHTNING_CHARGE_TICKS = BUILDER.defineInRange("lightningChargeTicks", 12_000, 1, 72_000);
        LIGHTNING_MAX_CHARGE_TICKS = BUILDER.defineInRange("lightningMaxChargeTicks", 72_000, 1, 1_728_000);
        LIGHTNING_CAPACITY_16 = BUILDER.defineInRange("lightningCapacity16", 256, 1, 1_000_000);
        LIGHTNING_CAPACITY_32 = BUILDER.defineInRange("lightningCapacity32", 128, 1, 1_000_000);
        LIGHTNING_CAPACITY_64 = BUILDER.defineInRange("lightningCapacity64", 64, 1, 1_000_000);
        LIGHTNING_CAPACITY_128 = BUILDER.defineInRange("lightningCapacity128", 32, 1, 1_000_000);
        BUILDER.pop();
        BUILDER.push("recipes");
        STORM_ASSEMBLY_CHANCE = BUILDER.defineInRange("stormAssemblyChance", 0.8, 0.0, 1.0);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private KidayServerConfig() {}

    public static int lightningChargeTicks() { return LIGHTNING_CHARGE_TICKS.get(); }
    public static int lightningMaxChargeTicks() { return Math.max(LIGHTNING_CHARGE_TICKS.get(), LIGHTNING_MAX_CHARGE_TICKS.get()); }
    public static int lightningCapacity(int rpm) {
        return switch (rpm) {
            case 16 -> LIGHTNING_CAPACITY_16.get();
            case 32 -> LIGHTNING_CAPACITY_32.get();
            case 64 -> LIGHTNING_CAPACITY_64.get();
            case 128 -> LIGHTNING_CAPACITY_128.get();
            default -> throw new IllegalArgumentException("Unsupported lightning engine RPM: " + rpm);
        };
    }
}
