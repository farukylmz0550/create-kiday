package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.registry.ModBiomes;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID, value = Dist.CLIENT)
public final class StormGroveClientWeather {
    private static boolean overridingWeather;
    private static float savedRain;
    private static float savedThunder;

    private StormGroveClientWeather() {
    }

    @SubscribeEvent
    public static void updateLocalizedWeather(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            overridingWeather = false;
            return;
        }

        boolean inStormGrove = minecraft.level.getBiome(minecraft.player.blockPosition()).is(ModBiomes.STORM_GROVE);
        if (inStormGrove) {
            if (!overridingWeather) {
                savedRain = minecraft.level.getRainLevel(1.0F);
                savedThunder = minecraft.level.getThunderLevel(1.0F);
                overridingWeather = true;
            }
            minecraft.level.setRainLevel(1.0F);
            minecraft.level.setThunderLevel(1.0F);
        } else if (overridingWeather) {
            minecraft.level.setRainLevel(savedRain);
            minecraft.level.setThunderLevel(savedThunder);
            overridingWeather = false;
        }
    }
}
