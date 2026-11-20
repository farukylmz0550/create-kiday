package com.farukylmz.createkiday.gameplay;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.registry.ModDamageTypes;
import com.farukylmz.createkiday.registry.ModMobEffects;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class WaqwaqBlessingEvents {
    private WaqwaqBlessingEvents() {}

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().hasEffect(ModMobEffects.WAQWAQ_BLESSING)
                && (event.getSource().is(DamageTypes.LIGHTNING_BOLT)
                || event.getSource().is(ModDamageTypes.LEI_GONG_ELECTRICITY))) {
            event.setNewDamage(0.0F);
        }
    }
}
