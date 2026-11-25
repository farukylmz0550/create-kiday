package io.github.createkiday.gameplay;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

/** Finds the connected Storm Grove chunk component inside the bounded biome patch. */
public final class StormGroveChunkRegion {
    private final int minChunkX;
    private final int minChunkZ;
    private final int width;
    private final Set<Long> chunks;

    private StormGroveChunkRegion(int minChunkX, int minChunkZ, int width, Set<Long> chunks) {
        this.minChunkX = minChunkX;
        this.minChunkZ = minChunkZ;
        this.width = width;
        this.chunks = Set.copyOf(chunks);
    }

    public static StormGroveChunkRegion scan(int minChunkX, int minChunkZ, int width,
            int startChunkX, int startChunkZ, BiPredicate<Integer, Integer> isStormGrove) {
        if (width <= 0) throw new IllegalArgumentException("region width must be positive");
        if (isStormGrove == null) throw new IllegalArgumentException("biome sampler is required");
        if (startChunkX < minChunkX || startChunkZ < minChunkZ
                || startChunkX >= minChunkX + width || startChunkZ >= minChunkZ + width
                || !isStormGrove.test(startChunkX, startChunkZ)) {
            return new StormGroveChunkRegion(minChunkX, minChunkZ, width, Set.of());
        }

        Set<Long> visited = new HashSet<>();
        ArrayDeque<Long> pending = new ArrayDeque<>();
        long start = key(startChunkX, startChunkZ);
        visited.add(start);
        pending.add(start);
        while (!pending.isEmpty()) {
            long current = pending.removeFirst();
            int x = chunkX(current);
            int z = chunkZ(current);
            add(x - 1, z, minChunkX, minChunkZ, width, isStormGrove, visited, pending);
            add(x + 1, z, minChunkX, minChunkZ, width, isStormGrove, visited, pending);
            add(x, z - 1, minChunkX, minChunkZ, width, isStormGrove, visited, pending);
            add(x, z + 1, minChunkX, minChunkZ, width, isStormGrove, visited, pending);
        }
        return new StormGroveChunkRegion(minChunkX, minChunkZ, width, visited);
    }

    public int size() {
        return chunks.size();
    }

    public boolean contains(int chunkX, int chunkZ) {
        return chunks.contains(key(chunkX, chunkZ));
    }

    public int minChunkX() { return minChunkX; }
    public int minChunkZ() { return minChunkZ; }
    public int width() { return width; }

    private static void add(int x, int z, int minX, int minZ, int width,
            BiPredicate<Integer, Integer> sampler, Set<Long> visited, ArrayDeque<Long> pending) {
        if (x < minX || z < minZ || x >= minX + width || z >= minZ + width) return;
        long key = key(x, z);
        if (!visited.contains(key) && sampler.test(x, z)) {
            visited.add(key);
            pending.addLast(key);
        }
    }

    private static long key(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }

    private static int chunkX(long key) { return (int) (key >> 32); }
    private static int chunkZ(long key) { return (int) key; }
}
