package io.github.createkiday.gameplay;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/** Compact world-persistent spawn flags, one byte for each explored Storm Grove region. */
public final class StormGroveBossData extends SavedData {
    public static final String DATA_ID = "create_kiday_storm_grove_bosses";
    private static final Factory<StormGroveBossData> FACTORY = new Factory<>(
            StormGroveBossData::new, StormGroveBossData::load, DataFixTypes.LEVEL);

    private StormGroveSpawnLedger ledger = new StormGroveSpawnLedger();

    public static StormGroveBossData get(net.minecraft.server.level.ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    public boolean wasSpawned(long regionKey, int slot) {
        return ledger.wasSpawned(regionKey, slot);
    }

    public void markSpawned(long regionKey, int slot) {
        if (ledger.markSpawned(regionKey, slot)) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long[] keys = ledger.regionKeys();
        byte[] masks = ledger.slotMasks(keys);
        tag.putLongArray("regions", keys);
        tag.putByteArray("spawned_slots", masks);
        return tag;
    }

    public static StormGroveBossData load(CompoundTag tag, HolderLookup.Provider registries) {
        StormGroveBossData data = new StormGroveBossData();
        data.ledger = StormGroveSpawnLedger.fromArrays(
                tag.getLongArray("regions"), tag.getByteArray("spawned_slots"));
        return data;
    }
}
