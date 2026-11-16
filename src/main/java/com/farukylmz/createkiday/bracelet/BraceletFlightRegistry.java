package com.farukylmz.createkiday.bracelet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Server-authoritative one-flight-per-bracelet lock table. */
public final class BraceletFlightRegistry {
    private final Map<UUID, Flight> activeByBracelet = new HashMap<>();

    public Optional<Flight> begin(UUID ownerId, UUID braceletId, long gameTick) {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(braceletId);
        if (activeByBracelet.containsKey(braceletId)) return Optional.empty();
        Flight flight = new Flight(ownerId, braceletId, UUID.randomUUID(), gameTick);
        activeByBracelet.put(braceletId, flight);
        return Optional.of(flight);
    }

    public boolean complete(UUID ownerId, UUID braceletId, UUID flightId) {
        Flight active = activeByBracelet.get(braceletId);
        if (active == null || !active.ownerId().equals(ownerId) || !active.flightId().equals(flightId)) {
            return false;
        }
        activeByBracelet.remove(braceletId);
        return true;
    }

    public void clearOwner(UUID ownerId) {
        activeByBracelet.values().removeIf(flight -> flight.ownerId().equals(ownerId));
    }

    public int clearExpired(long gameTick, long timeoutTicks) {
        int before = activeByBracelet.size();
        activeByBracelet.values().removeIf(flight -> gameTick - flight.startedAt() >= timeoutTicks);
        return before - activeByBracelet.size();
    }

    public boolean isActive(UUID braceletId) {
        return activeByBracelet.containsKey(braceletId);
    }

    public record Flight(UUID ownerId, UUID braceletId, UUID flightId, long startedAt) {
    }
}
