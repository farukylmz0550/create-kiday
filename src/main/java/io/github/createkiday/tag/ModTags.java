package io.github.createkiday.tag;

import io.github.createkiday.CreateKidayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    public static final TagKey<Item> BIOMASS_FUELS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "biomass_fuels"));

    private ModTags() {}
}
