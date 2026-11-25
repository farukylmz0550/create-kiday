package io.github.createkiday.registry;

import io.github.createkiday.CreateKidayMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ModDataComponents {
    private static final DeferredRegister<DataComponentType<?>> TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CreateKidayMod.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> METAL_TYPE = TYPES.register("metal_type",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());

    private ModDataComponents() { }
    public static void register(IEventBus bus) { TYPES.register(bus); }
}
