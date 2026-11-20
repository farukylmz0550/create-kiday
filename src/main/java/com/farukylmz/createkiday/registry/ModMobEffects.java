package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMobEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, CreateKidayMod.MOD_ID);
    public static final DeferredHolder<MobEffect, MobEffect> STUN = EFFECTS.register("stun",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x75D8F2) { });
    public static final DeferredHolder<MobEffect, MobEffect> WAQWAQ_BLESSING = EFFECTS.register("waqwaq_blessing",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x173D52) { });

    private ModMobEffects() {
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
