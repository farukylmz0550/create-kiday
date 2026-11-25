package io.github.createkiday.client;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.registry.ModBiomes;
import io.github.createkiday.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Plays the Waqwaq call as a sparse, local Storm Grove ambient sound. */
@EventBusSubscriber(modid = CreateKidayMod.MOD_ID, value = Dist.CLIENT)
public final class WaqwaqFruitAmbient {
    private static final int MIN_DELAY_TICKS = 40;
    private static final int MAX_DELAY_TICKS = 60;
    private static int ticksUntilSound;
    private static boolean wasInStormGrove;

    private WaqwaqFruitAmbient() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            ticksUntilSound = 0;
            wasInStormGrove = false;
            return;
        }

        BlockPos playerPos = minecraft.player.blockPosition();
        if (!minecraft.level.getBiome(playerPos).is(ModBiomes.STORM_GROVE)) {
            wasInStormGrove = false;
            ticksUntilSound = 0;
            return;
        }

        if (!wasInStormGrove) {
            wasInStormGrove = true;
            ticksUntilSound = nextDelay(minecraft);
        }
        if (--ticksUntilSound > 0) return;

        double angle = minecraft.level.getRandom().nextDouble() * Math.PI * 2.0;
        double distance = 5.0 + minecraft.level.getRandom().nextDouble() * 7.0;
        double x = minecraft.player.getX() + Math.cos(angle) * distance;
        double z = minecraft.player.getZ() + Math.sin(angle) * distance;
        double y = minecraft.player.getY() + 1.0 + minecraft.level.getRandom().nextDouble() * 2.0;
        float pitch = 0.88F + minecraft.level.getRandom().nextFloat() * 0.24F;
        minecraft.level.playLocalSound(x, y, z, ModSounds.WAQWAQ_FRUIT_MURMUR.get(),
                SoundSource.AMBIENT, 0.12F, pitch, false);
        ticksUntilSound = nextDelay(minecraft);
    }

    private static int nextDelay(Minecraft minecraft) {
        return MIN_DELAY_TICKS + minecraft.level.getRandom().nextInt(MAX_DELAY_TICKS - MIN_DELAY_TICKS + 1);
    }
}
