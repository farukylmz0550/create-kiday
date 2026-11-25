package io.github.createkiday.gameplay;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.item.LightningStaffItem;
import io.github.createkiday.advancement.KidayAdvancements;
import io.github.createkiday.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class LightningStaffEvents {
    private LightningStaffEvents() { }

    @SubscribeEvent
    public static void onLeftClickEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.getMainHandItem().is(io.github.createkiday.registry.ModItems.LIGHTNING_STAFF.get())) return;
        event.setCanceled(true);
        if (event.getTarget() instanceof LivingEntity target)
            LightningStaffItem.castAtTarget(player, target);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.getMainHandItem().is(io.github.createkiday.registry.ModItems.LIGHTNING_STAFF.get())
                || event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;
        event.setCanceled(true);
        BlockPos pos = event.getPos();
        LightningStaffItem.castLightning(player,
                new net.minecraft.world.phys.Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    @SubscribeEvent
    public static void onStaffCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getCrafting().is(ModItems.LIGHTNING_STAFF.get()))
            KidayAdvancements.award(player, "lightning_staff", "complete");
    }

    @SubscribeEvent
    public static void onStaffKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player
                && player.getMainHandItem().is(ModItems.LIGHTNING_STAFF.get()))
            KidayAdvancements.award(player, "lightning_staff_kill", "kill");
    }
}
