package io.github.createkiday;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.createkiday.gameplay.StormGroveSpawnLedger;
import io.github.createkiday.gameplay.StormGroveChunkRegion;
import io.github.createkiday.gameplay.StormGroveRegionCoordinates;
import org.junit.jupiter.api.Test;

class StormGroveBossDataTest {
    @Test
    void biomeAndSpawnerUseTheSameChunkScaledPatchBoundsIncludingNegativeRegions() {
        int westEdge = StormGroveRegionCoordinates.groveMinChunk(-1);
        int eastEdge = westEdge + StormGroveRegionCoordinates.groveWidthChunks() - 1;

        assertTrue(StormGroveRegionCoordinates.isGroveChunk(westEdge));
        assertTrue(StormGroveRegionCoordinates.isGroveChunk(eastEdge));
        assertFalse(StormGroveRegionCoordinates.isGroveChunk(westEdge - 1));
        assertFalse(StormGroveRegionCoordinates.isGroveChunk(eastEdge + 1));
        assertTrue(StormGroveRegionCoordinates.regionIndex(westEdge) == -1);
        assertTrue(StormGroveRegionCoordinates.regionIndex(eastEdge + 1) == -1);
        assertTrue(StormGroveRegionCoordinates.regionIndex(0) == 0);
        assertTrue(StormGroveRegionCoordinates.isGroveChunk(50));
        assertTrue(StormGroveRegionCoordinates.isGroveChunk(77));
    }

    @Test
    void regionScanFindsTheRealConnectedFootprintAndExcludesSurroundingChunks() {
        StormGroveChunkRegion grove = StormGroveChunkRegion.scan(-8, 12, 8, -6, 14,
                (x, z) -> x >= -6 && x <= -3 && z >= 14 && z <= 17);

        assertTrue(grove.size() == 16);
        assertTrue(grove.contains(-3, 17));
        assertFalse(grove.contains(-2, 14));
        assertFalse(grove.contains(-6, 13));
    }

    @Test
    void regionScanDoesNotJoinTouchingOnlyDiagonallyBiomePatches() {
        StormGroveChunkRegion grove = StormGroveChunkRegion.scan(0, 0, 4, 0, 0,
                (x, z) -> (x == 0 && z == 0) || (x == 1 && z == 1));

        assertTrue(grove.size() == 1);
        assertFalse(grove.contains(1, 1));
    }

    @Test
    void actualConnectedChunkCountControlsThe256BossThreshold() {
        StormGroveChunkRegion below = StormGroveChunkRegion.scan(0, 0, 16, 0, 0,
                (x, z) -> !(x == 15 && z == 15));
        StormGroveChunkRegion at = StormGroveChunkRegion.scan(0, 0, 16, 0, 0,
                (x, z) -> true);

        assertTrue(below.size() == 255);
        assertTrue(at.size() == 256);
        assertTrue(io.github.createkiday.entity.LeiGongBalance.bossCount(below.size()) == 1);
        assertTrue(io.github.createkiday.entity.LeiGongBalance.bossCount(at.size()) == 2);
    }

    @Test
    void spawnMarkersSurviveSerializationAndRemainIndependentByRegionAndSlot() {
        StormGroveSpawnLedger saved = new StormGroveSpawnLedger();
        long regionA = (((long) -3) << 32) | (7 & 0xffffffffL);
        long regionB = (((long) -3) << 32) | (8 & 0xffffffffL);
        saved.markSpawned(regionA, 0);
        org.junit.jupiter.api.Assertions.assertFalse(saved.markSpawned(regionA, 0),
                "A duplicate spawn marker should not mutate the ledger");
        saved.markSpawned(regionB, 1);
        long[] keys = saved.regionKeys();
        byte[] masks = saved.slotMasks(keys);
        StormGroveSpawnLedger loaded = StormGroveSpawnLedger.fromArrays(keys, masks);

        assertTrue(loaded.wasSpawned(regionA, 0));
        assertTrue(loaded.wasSpawned(regionB, 1));
        assertFalse(loaded.wasSpawned(regionA, 1));
        assertFalse(loaded.wasSpawned(regionB, 0));
        assertFalse(loaded.wasSpawned(regionA + 1, 0));
    }

    @Test
    void loadingMalformedParallelArraysKeepsOnlyValidSpawnRecords() {
        StormGroveSpawnLedger loaded = StormGroveSpawnLedger.fromArrays(new long[]{11L, 22L}, new byte[]{1});

        assertTrue(loaded.wasSpawned(11L, 0));
        assertFalse(loaded.wasSpawned(22L, 0));
    }

    @Test
    void ledgerRejectsSlotsOutsideTheTwoBossRegionCapacity() {
        StormGroveSpawnLedger ledger = new StormGroveSpawnLedger();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> ledger.markSpawned(1L, 2));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> ledger.wasSpawned(1L, -1));
    }

    @Test
    void emptyAndZeroMaskRecordsDoNotCreatePhantomSpawnedBosses() {
        StormGroveSpawnLedger empty = StormGroveSpawnLedger.fromArrays(new long[0], new byte[0]);
        assertFalse(empty.wasSpawned(1L, 0));
        StormGroveSpawnLedger zeroMask = StormGroveSpawnLedger.fromArrays(new long[]{1L}, new byte[]{0});
        assertFalse(zeroMask.wasSpawned(1L, 0));
        assertTrue(zeroMask.regionKeys().length == 0);
    }
}
