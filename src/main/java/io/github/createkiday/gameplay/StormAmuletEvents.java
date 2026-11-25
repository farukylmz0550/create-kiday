package io.github.createkiday.gameplay;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.registry.ModDamageTypes;
import io.github.createkiday.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class StormAmuletEvents {
    private StormAmuletEvents() {
    }

    @SubscribeEvent
    public static void reduceElectricDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isEquipped(player)) return;
        event.setNewDamage(event.getNewDamage() * damageMultiplier(event.getSource()));
    }

    public static float damageMultiplier(DamageSource source) {
        if (source.is(DamageTypes.LIGHTNING_BOLT)) return 0.5F;
        if (source.is(ModDamageTypes.LEI_GONG_ELECTRICITY)) return 0.75F;
        return 1.0F;
    }

    public static boolean isEquipped(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(
                        stack -> stack.is(ModItems.STORM_AMULET.get()), "necklace"))
                .isPresent();
    }
}
