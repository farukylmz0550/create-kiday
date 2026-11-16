package com.farukylmz.createkiday.gameplay;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.block.entity.LightningEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.server.level.ServerLevel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class LightningEngineEvents {
    private static final String REDIRECTED = "create_kiday.lightning_engine_redirected";
    private static final Map<ServerLevel, Set<BlockPos>> LOADED_ENGINES = Collections.synchronizedMap(new WeakHashMap<>());

    private LightningEngineEvents() {
    }

    @SubscribeEvent
    public static void beforeLightningTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LightningBolt bolt)
                || !(bolt.level() instanceof ServerLevel level)
                || bolt.getPersistentData().getBoolean(REDIRECTED)) return;
        BlockPos strike = BlockPos.containing(bolt.position());
        BlockPos enginePos = findEngineInStrikeChunk(level, strike);
        if (enginePos == null || !(level.getBlockEntity(enginePos) instanceof LightningEngineBlockEntity engine)) return;

        // Redirect before LightningBolt.tick() applies fire/entity effects, so the
        // modeled rod behaves like a real lightning attractor rather than merely
        // charging after an unrelated nearby strike.
        bolt.moveTo(enginePos.getX() + 0.5, enginePos.getY() + 2.0, enginePos.getZ() + 0.5);
        bolt.getPersistentData().putBoolean(REDIRECTED, true);
        engine.chargeFromLightning();
    }

    public static void registerEngine(ServerLevel level, BlockPos pos) {
        LOADED_ENGINES.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos.immutable());
    }

    public static void unregisterEngine(ServerLevel level, BlockPos pos) {
        Set<BlockPos> positions = LOADED_ENGINES.get(level);
        if (positions == null) return;
        positions.remove(pos);
        if (positions.isEmpty()) LOADED_ENGINES.remove(level);
    }

    private static BlockPos findEngineInStrikeChunk(ServerLevel level, BlockPos strike) {
        Set<BlockPos> positions = LOADED_ENGINES.getOrDefault(level, Set.of());
        ChunkPos strikeChunk = new ChunkPos(strike);
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (BlockPos candidate : Set.copyOf(positions)) {
            if (!new ChunkPos(candidate).equals(strikeChunk)
                    || !level.hasChunkAt(candidate)
                    || !(level.getBlockEntity(candidate) instanceof LightningEngineBlockEntity)) continue;
            double horizontalDistance = candidate.distToLowCornerSqr(strike.getX(), candidate.getY(), strike.getZ());
            if (horizontalDistance >= nearestDistance) continue;
            nearest = candidate;
            nearestDistance = horizontalDistance;
        }
        return nearest;
    }
}
