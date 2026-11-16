package com.farukylmz.createkiday.bracelet;

import com.farukylmz.createkiday.entity.HunterDiskEntity;
import com.farukylmz.createkiday.item.HunterBraceletItem;
import com.farukylmz.createkiday.registry.ModEntities;
import com.farukylmz.createkiday.registry.ModItems;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class BraceletFlightService {
    public static final BraceletFlightRegistry FLIGHTS = new BraceletFlightRegistry();

    private BraceletFlightService() {
    }

    public static void launch(ServerPlayer player) {
        Optional<ItemStack> bracelet = equippedBracelet(player);
        if (bracelet.isEmpty()) return;
        launchWithBracelet(player, bracelet.get());
    }

    /** Launch entry separated for deterministic GameTests and creative-mode concurrency. */
    public static boolean launchWithBracelet(ServerPlayer player, ItemStack bracelet) {
        UUID braceletId = HunterBraceletItem.getOrCreateId(bracelet);
        if (player.isCreative()) {
            spawnDisk(player, braceletId, UUID.randomUUID());
            return true;
        }
        Optional<BraceletFlightRegistry.Flight> flight = FLIGHTS.begin(
                player.getUUID(), braceletId, player.level().getGameTime());
        if (flight.isEmpty()) return false;
        spawnDisk(player, flight.get().braceletId(), flight.get().flightId());
        return true;
    }

    private static void spawnDisk(ServerPlayer player, UUID braceletId, UUID flightId) {
        HunterDiskEntity disk = new HunterDiskEntity(ModEntities.HUNTER_DISK.get(), player.level());
        disk.launch(player, braceletId, flightId);
        player.level().addFreshEntity(disk);
    }

    public static boolean acceptReturn(ServerPlayer player, UUID braceletId, UUID flightId) {
        Optional<ItemStack> equipped = equippedBracelet(player);
        if (equipped.isEmpty() || !HunterBraceletItem.getOrCreateId(equipped.get()).equals(braceletId)) return false;
        return FLIGHTS.complete(player.getUUID(), braceletId, flightId);
    }

    public static Optional<ItemStack> equippedBracelet(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(stack -> stack.is(ModItems.HUNTER_BRACELET.get()), "wrist"))
                .map(result -> result.stack());
    }
}
