package io.github.createkiday.gameplay;

import io.github.createkiday.registry.ModBiomes;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;

public final class StormGroveBiomeResolver {
    private static volatile Holder<Biome> stormGrove;

    private StormGroveBiomeResolver() {
    }

    public static void bind(MinecraftServer server) {
        stormGrove = server.registryAccess().holderOrThrow(ModBiomes.STORM_GROVE);
    }

    public static void clear() {
        stormGrove = null;
    }

    public static Holder<Biome> get() {
        return stormGrove;
    }
}
