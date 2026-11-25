package io.github.createkiday.client;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.network.LightningStaffPayload;
import io.github.createkiday.registry.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID, value = Dist.CLIENT)
public final class LightningStaffClientEvents {
    private LightningStaffClientEvents() { }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity().getMainHandItem().is(ModItems.LIGHTNING_STAFF.get()))
            PacketDistributor.sendToServer(LightningStaffPayload.INSTANCE);
    }
}
