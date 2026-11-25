package io.github.createkiday.advancement;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.registry.ModBlocks;
import io.github.createkiday.registry.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class KidayAdvancements {
    private static final String[] COMPLETION_PREREQUISITES = {
            "root", "first_kiday", "first_cast", "all_tools", "master_smith", "full_armor",
            "all_casts", "storm_mechanism", "engine_running", "first_salur", "salur_full_armor",
            "salur_all_tools", "biomass_generator", "salur_disc", "tnt_disc", "encounter_storm_spirit",
            "defeat_lei_gong", "lightning_staff", "lightning_staff_kill", "lightning_necklace"
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
        award(player, "first_kiday", "drop");
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) award(player, "root", "first_login");
    }

    public static void checkCompletion(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        checkCompletion(player);
    }

    private static void checkCompletion(ServerPlayer player) {
        for (String id : COMPLETION_PREREQUISITES) {
            AdvancementHolder holder = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(
                    CreateKidayMod.MOD_ID, id));
            if (holder == null || !player.getAdvancements().getOrStartProgress(holder).isDone()) return;
        }
        award(player, "touch_grass", "all_eighteen");
    }
}
