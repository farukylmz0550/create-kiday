package com.farukylmz.createkiday.gameplay;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.registry.ModBiomes;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class StormGroveWeatherEvents {
    private static final int STRIKE_INTERVAL = 80;

    private StormGroveWeatherEvents() {
    }

    @SubscribeEvent
    public static void createLocalizedStorms(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimensionType().hasSkyLight() || level.getGameTime() % STRIKE_INTERVAL != 0) continue;
            Set<ChunkPos> processedAreas = new HashSet<>();
            for (ServerPlayer player : level.players()) {
                if (!player.isAlive() || !level.getBiome(player.blockPosition()).is(ModBiomes.STORM_GROVE)) continue;
                ChunkPos area = new ChunkPos(player.blockPosition());
                if (!processedAreas.add(area) || level.random.nextFloat() >= 0.5F) continue;
                tryCreateStrike(level, player);
            }
        }
    }

    public static boolean tryCreateStrike(ServerLevel level, ServerPlayer player) {
        int x = player.getBlockX() + level.random.nextIntBetweenInclusive(-32, 32);
        int z = player.getBlockZ() + level.random.nextIntBetweenInclusive(-32, 32);
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        if (!level.isLoaded(surface) || !level.canSeeSky(surface)
                || !level.getBiome(surface).is(ModBiomes.STORM_GROVE)) return false;
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return false;
        bolt.moveTo(surface.getX() + 0.5, surface.getY(), surface.getZ() + 0.5);
        level.addFreshEntity(bolt);
        return true;
    }
}
