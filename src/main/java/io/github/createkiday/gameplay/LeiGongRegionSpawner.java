package io.github.createkiday.gameplay;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.entity.LeiGongBalance;
import io.github.createkiday.entity.LeiGongEntity;
import io.github.createkiday.registry.ModBiomes;
import io.github.createkiday.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.TickTask;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Discovers the connected Storm Grove footprint without loading or generating chunks. */
@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class LeiGongRegionSpawner {
    private static final int FIRST_ANCHOR = 50;
    private static final int SECOND_ANCHOR = 77;

    private LeiGongRegionSpawner() { }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level != level.getServer().overworld()
                || !(event.getChunk() instanceof LevelChunk chunk)) return;
        ChunkPos pos = chunk.getPos();
        int regionX = StormGroveRegionCoordinates.regionIndex(pos.x);
        int regionZ = StormGroveRegionCoordinates.regionIndex(pos.z);
        int localX = StormGroveRegionCoordinates.localChunk(pos.x);
        int localZ = StormGroveRegionCoordinates.localChunk(pos.z);
        int slot = localX == FIRST_ANCHOR && localZ == FIRST_ANCHOR ? 0
                : localX == SECOND_ANCHOR && localZ == SECOND_ANCHOR ? 1 : -1;
        if (slot < 0) return;

        // ChunkEvent.Load may occur before promotion to FULL; defer all world reads to the server thread.
        level.getServer().tell(new TickTask(level.getServer().getTickCount() + 1,
                () -> spawnSlot(level, chunk, regionX, regionZ, slot)));
    }

    private static void spawnSlot(ServerLevel level, LevelChunk chunk, int regionX, int regionZ, int slot) {
        if (chunk.getLevel() != level) return;
        if (!level.getChunkSource().hasChunk(chunk.getPos().x, chunk.getPos().z)) return;

        int groveMinX = StormGroveRegionCoordinates.groveMinChunk(regionX);
        int groveMinZ = StormGroveRegionCoordinates.groveMinChunk(regionZ);
        int groveWidth = StormGroveRegionCoordinates.groveWidthChunks();
        StormGroveChunkRegion grove = StormGroveChunkRegion.scan(groveMinX, groveMinZ, groveWidth,
                chunk.getPos().x, chunk.getPos().z,
                (chunkX, chunkZ) -> level.getBiome(new BlockPos(chunkX * 16 + 8, level.getSeaLevel(),
                        chunkZ * 16 + 8)).is(ModBiomes.STORM_GROVE));
        if (!grove.contains(chunk.getPos().x, chunk.getPos().z)
                || LeiGongBalance.bossCount(grove.size()) <= slot) return;

        int x = chunk.getPos().getMinBlockX() + 8;
        int z = chunk.getPos().getMinBlockZ() + 8;
        int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 8, 8);
        BlockPos spawnPos = new BlockPos(x, y, z);
        long key = ((long) regionX << 32) | (regionZ & 0xffffffffL);
        StormGroveBossData data = StormGroveBossData.get(level);
        if (data.wasSpawned(key, slot)) return;

        LeiGongEntity boss = ModEntities.LEI_GONG.get().create(level);
        if (boss == null) return;
        boss.moveTo(x + 0.5, y, z + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        boss.setPersistenceRequired();
        if (level.addFreshEntity(boss)) data.markSpawned(key, slot);
    }
}
