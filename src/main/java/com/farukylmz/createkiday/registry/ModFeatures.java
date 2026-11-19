package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.worldgen.VakvakTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, CreateKidayMod.MOD_ID);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> VAKVAK_TREE = FEATURES.register(
            "vakvak_tree", () -> new VakvakTreeFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}
