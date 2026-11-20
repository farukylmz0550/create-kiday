package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, CreateKidayMod.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> WAQWAQ_FRUIT_MURMUR = SOUNDS.register("waqwaq_fruit_murmur",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "waqwaq_fruit_murmur")));

    private ModSounds() {}

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
