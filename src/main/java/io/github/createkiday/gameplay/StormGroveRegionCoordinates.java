package io.github.createkiday.gameplay;

import io.github.createkiday.registry.ModBiomes;

/** Shared chunk-grid mapping for the periodically generated Storm Grove patches. */
public final class StormGroveRegionCoordinates {
    private static final int REGION_CHUNKS = ModBiomes.STORM_GROVE_REGION_SIZE / 16;
    private static final int GROVE_CHUNKS = ModBiomes.STORM_GROVE_WIDTH / 16;
    private static final int MARGIN_CHUNKS = (REGION_CHUNKS - GROVE_CHUNKS) / 2;

    private StormGroveRegionCoordinates() { }

    public static int regionIndex(int chunkCoordinate) {
        return Math.floorDiv(chunkCoordinate, REGION_CHUNKS);
    }

    public static int localChunk(int chunkCoordinate) {
        return Math.floorMod(chunkCoordinate, REGION_CHUNKS);
    }

    public static int groveMinChunk(int regionIndex) {
        return regionIndex * REGION_CHUNKS + MARGIN_CHUNKS;
    }

    public static int groveWidthChunks() {
        return GROVE_CHUNKS;
    }

    public static boolean isGroveChunk(int chunkCoordinate) {
        int local = localChunk(chunkCoordinate);
        return local >= MARGIN_CHUNKS && local < MARGIN_CHUNKS + GROVE_CHUNKS;
    }
}
