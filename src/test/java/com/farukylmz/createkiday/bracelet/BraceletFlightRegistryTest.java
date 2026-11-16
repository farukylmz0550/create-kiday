package com.farukylmz.createkiday.bracelet;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BraceletFlightRegistryTest {
    @Test
    void onlyMatchingOwnerBraceletAndFlightCanComplete() {
        BraceletFlightRegistry registry = new BraceletFlightRegistry();
        UUID owner = UUID.randomUUID();
        UUID bracelet = UUID.randomUUID();
        var flight = registry.begin(owner, bracelet, 10).orElseThrow();

        assertTrue(registry.begin(owner, bracelet, 11).isEmpty());
        assertFalse(registry.complete(UUID.randomUUID(), bracelet, flight.flightId()));
        assertFalse(registry.complete(owner, bracelet, UUID.randomUUID()));
        assertTrue(registry.complete(owner, bracelet, flight.flightId()));
        assertFalse(registry.isActive(bracelet));
    }

    @Test
    void flightsAreUniqueAndTimeoutCleansTheLock() {
        BraceletFlightRegistry registry = new BraceletFlightRegistry();
        UUID owner = UUID.randomUUID();
        var a = registry.begin(owner, UUID.randomUUID(), 0).orElseThrow();
        var b = registry.begin(owner, UUID.randomUUID(), 0).orElseThrow();
        assertNotEquals(a.flightId(), b.flightId());
        assertEquals(2, registry.clearExpired(200, 200));
    }

    @Test
    void crossingPlayersAndWrongBraceletsCannotCloseEachOthersFlights() {
        BraceletFlightRegistry registry = new BraceletFlightRegistry();
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        UUID aliceBracelet = UUID.randomUUID();
        UUID bobBracelet = UUID.randomUUID();
        var aliceFlight = registry.begin(alice, aliceBracelet, 0).orElseThrow();
        var bobFlight = registry.begin(bob, bobBracelet, 0).orElseThrow();
        assertFalse(registry.complete(alice, bobBracelet, bobFlight.flightId()));
        assertFalse(registry.complete(bob, aliceBracelet, aliceFlight.flightId()));
        assertTrue(registry.isActive(aliceBracelet));
        assertTrue(registry.isActive(bobBracelet));
    }
}
