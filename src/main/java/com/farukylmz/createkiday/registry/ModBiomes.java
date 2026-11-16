package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public final class ModBiomes {
    public static final int STORM_GROVE_WIDTH = 768;
    public static final int STORM_GROVE_REGION_SIZE = 2048;
    public static final ResourceKey<Biome> STORM_GROVE = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "storm_grove"));

    private ModBiomes() {
    }
}
