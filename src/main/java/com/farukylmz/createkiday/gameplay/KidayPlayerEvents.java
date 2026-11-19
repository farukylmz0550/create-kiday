package com.farukylmz.createkiday.gameplay;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.item.KidayChronicleItem;
import com.farukylmz.createkiday.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import com.farukylmz.createkiday.item.HotKidayItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.farukylmz.createkiday.bracelet.BraceletFlightService;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class KidayPlayerEvents {
    private static final String RECEIVED_CHRONICLE = "create_kiday.received_chronicle";

    private KidayPlayerEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var data = player.getPersistentData();
        if (data.getBoolean(RECEIVED_CHRONICLE)) return;
        ItemStack chronicle = KidayChronicleItem.createStack();
        if (!player.getInventory().add(chronicle)) {
            ItemEntity dropped = player.drop(chronicle, false);
            if (dropped != null) dropped.setTarget(player.getUUID());
        }
        data.putBoolean(RECEIVED_CHRONICLE, true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % 20 == 0 && (player.getMainHandItem().getItem() instanceof HotKidayItem
                || player.getOffhandItem().getItem() instanceof HotKidayItem)) {
            player.hurt(player.damageSources().hotFloor(), 1.0F);
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.KIDAY_HELMET.get())) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, false, false, false));
        }
        if (player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.KIDAY_BOOTS.get())) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, false, false, false));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        BraceletFlightService.FLIGHTS.clearOwner(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        BraceletFlightService.FLIGHTS.clearOwner(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (event.getOriginal().getPersistentData().getBoolean(RECEIVED_CHRONICLE)) {
            event.getEntity().getPersistentData().putBoolean(RECEIVED_CHRONICLE, true);
        }
    }
}
