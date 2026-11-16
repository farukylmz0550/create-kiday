package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> LEI_GONG_ELECTRICITY = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "lei_gong_electricity"));

    private ModDamageTypes() {
    }
}
