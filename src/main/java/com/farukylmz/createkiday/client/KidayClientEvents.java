package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.network.LaunchDiskPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID, value = Dist.CLIENT)
public final class KidayClientEvents {
    private KidayClientEvents() {
    }

    @SubscribeEvent
    public static void afterClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) return;
        while (KidayClient.LAUNCH_DISK.consumeClick()) PacketDistributor.sendToServer(LaunchDiskPayload.INSTANCE);
    }
}
