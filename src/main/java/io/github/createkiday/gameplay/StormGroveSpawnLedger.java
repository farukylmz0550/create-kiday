package io.github.createkiday.gameplay;

import java.util.HashMap;
import java.util.Map;

/** Pure, compact mapping of region keys to the two boss slots already spawned there. */
public final class StormGroveSpawnLedger {
    private final Map<Long, Integer> spawnedSlots = new HashMap<>();

    public boolean wasSpawned(long regionKey, int slot) {
        validateSlot(slot);
        return (spawnedSlots.getOrDefault(regionKey, 0) & (1 << slot)) != 0;
    }

    public boolean markSpawned(long regionKey, int slot) {
        validateSlot(slot);
        int before = spawnedSlots.getOrDefault(regionKey, 0);
        int after = before | (1 << slot);
        if (before == after) return false;
        spawnedSlots.put(regionKey, after);
        return true;
    }

    public long[] regionKeys() {
        long[] keys = new long[spawnedSlots.size()];
        int i = 0;
        for (long key : spawnedSlots.keySet()) keys[i++] = key;
        return keys;
    }

    public byte[] slotMasks(long[] keys) {
        byte[] masks = new byte[keys.length];
        for (int i = 0; i < keys.length; i++) masks[i] = (byte) (int) spawnedSlots.getOrDefault(keys[i], 0);
        return masks;
    }

    public static StormGroveSpawnLedger fromArrays(long[] keys, byte[] masks) {
        StormGroveSpawnLedger ledger = new StormGroveSpawnLedger();
        for (int i = 0; i < Math.min(keys.length, masks.length); i++) {
            int mask = masks[i] & 0xff;
            if (mask != 0) ledger.spawnedSlots.put(keys[i], mask);
        }
        return ledger;
    }

    private static void validateSlot(int slot) {
        if (slot < 0 || slot > 1) throw new IllegalArgumentException("boss slot must be 0 or 1");
    }
}
