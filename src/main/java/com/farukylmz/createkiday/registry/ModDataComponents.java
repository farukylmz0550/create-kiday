package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.item.TagFilterData;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(CreateKidayMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TagFilterData>> TAG_FILTER_RULES =
            COMPONENTS.registerComponentType("tag_filter_rules", builder -> builder
                    .persistent(TagFilterData.CODEC)
                    .networkSynchronized(TagFilterData.STREAM_CODEC)
                    .cacheEncoding());

    private ModDataComponents() {}

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
