package com.farukylmz.createkiday.advancement;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.registry.ModBlocks;
import com.farukylmz.createkiday.registry.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

public final class KidayAdvancements {
    private static final String[] FIRST_EIGHT = {
            "root", "first_cast", "all_tools", "full_armor", "master_smith",
            "all_casts", "engine_running", "defeat_lei_gong"
    };

    private KidayAdvancements() {
    }

    public static void award(ServerPlayer player, String advancementId, String criterion) {
        AdvancementHolder holder = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(
                CreateKidayMod.MOD_ID, advancementId));
        if (holder == null) {
            return;
        }
        boolean alreadyCompleted = false;
        for (String completed : player.getAdvancements().getOrStartProgress(holder).getCompletedCriteria())
            if (completed.equals(criterion)) alreadyCompleted = true;
        if (!alreadyCompleted) {
            player.getAdvancements().award(holder, criterion);
            checkCompletion(player);
        }
    }

    public static void onKidayOreDrops(BlockDropsEvent event) {
        if (event.isCanceled() || !(event.getBreaker() instanceof ServerPlayer player)) return;
        if (!event.getState().is(ModBlocks.KIDAY_ORE.get())
                && !event.getState().is(ModBlocks.DEEPSLATE_KIDAY_ORE.get())) return;
        if (event.getDrops().stream().noneMatch(drop -> drop.getItem().is(ModItems.RAW_KIDAY.get()))) return;
        award(player, "root", "raw_kiday");
    }

    public static void checkCompletion(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        checkCompletion(player);
    }

    private static void checkCompletion(ServerPlayer player) {
        for (String id : FIRST_EIGHT) {
            AdvancementHolder holder = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(
                    CreateKidayMod.MOD_ID, id));
            if (holder == null || !player.getAdvancements().getOrStartProgress(holder).isDone()) return;
        }
        award(player, "touch_grass", "all_eight");
    }
}
