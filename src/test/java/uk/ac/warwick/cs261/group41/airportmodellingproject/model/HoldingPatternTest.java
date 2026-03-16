package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EventSource;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventManager;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HoldingPattern.
 *
 * Validates priority ordering (emergencies first, lower fuel first, earliest entry last),
 * fuel consumption and diversion at the minimum threshold, the fuel emergency escalation rule,
 * status-driven re-prioritisation, and random aircraft selection.
 */
class HoldingPatternTest {

    /**
     * Creates an ARRIVAL Aircraft with the given callsign, fuel, scheduled tick, and entry tick.
     * Uses fixed placeholder values for operator, source, and destination.
     *
     * @param callsign      the aircraft callsign
     * @param fuel          the initial fuel level
     * @param scheduledTick the tick the aircraft was scheduled to arrive
     * @param entryTick     the tick the aircraft entered the holding pattern
     * @return a new ARRIVAL Aircraft
     */
    private static Aircraft newArrival(String callsign, double fuel, int scheduledTick, int entryTick) {
        // Matches the constructor used by AircraftGenerator in main code
        return new Aircraft(
                callsign,
                "OP",
                "AAA",
                "BBB",
                fuel,
                scheduledTick,
                entryTick,
                FlightType.ARRIVAL
        );
    }

    /**
     * Returns the first non-NONE EmergencyStatus from the enum, used to test emergency
     * priority logic without hard-coding a specific status value.
     *
     * @return a non-NONE EmergencyStatus value, or fails the test if none exist
     */
    private static EmergencyStatus someNonNoneEmergencyStatus() {
        for (EmergencyStatus s : EmergencyStatus.values()) {
            if (!"NONE".equalsIgnoreCase(s.name())) return s;
        }
        fail("EmergencyStatus enum has no non-NONE values; cannot test emergency ordering.");
        return EmergencyStatus.NONE;
    }

    /**
     * Reads the altitude field of an Aircraft via reflection, allowing the test to verify
     * altitude updates without depending on a public getter in the production API.
     *
     * @param aircraft the Aircraft whose altitude to read
     * @return the current altitude of the aircraft
     */
    private static int getAltitudeReflective(Aircraft aircraft) {
        try {
            Method m = aircraft.getClass().getMethod("getAltitude");
            Object v = m.invoke(aircraft);
            return (Integer) v;
        } catch (NoSuchMethodException e) {
            fail("Aircraft.getAltitude() not found. HoldingPattern updates altitude via setAltitude(), so a getter is expected.");
        } catch (Exception e) {
            fail("Failed to read Aircraft altitude reflectively: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Verifies that the holding pattern serves aircraft in priority order:
     * emergencies first; within equal emergency status, lower fuel first; NONE last.
     */
    @Test
    void addAircraft_andPoll_shouldReturnHighestPriorityFirst() {
        HoldingPattern hp = new HoldingPattern();

        EmergencyStatus emergency = someNonNoneEmergencyStatus();

        Aircraft a = newArrival("A-1", 15.0, 0, 0);
        a.setStatus(emergency);

        Aircraft b = newArrival("B-1", 12.0, 0, 0);
        b.setStatus(emergency);

        Aircraft c = newArrival("C-1", 50.0, 0, 0);
        c.setStatus(EmergencyStatus.NONE);

        hp.addAircraft(a);
        hp.addAircraft(b);
        hp.addAircraft(c);

        // Expected: b before a before c (same emergency => lower fuel first; NONE last)
        assertEquals("B-1", hp.peekNextAircraft().map(Aircraft::getCallsign).orElse(null));

        assertEquals("B-1", hp.getNextAircraft().map(Aircraft::getCallsign).orElse(null));
        assertEquals("A-1", hp.getNextAircraft().map(Aircraft::getCallsign).orElse(null));
        assertEquals("C-1", hp.getNextAircraft().map(Aircraft::getCallsign).orElse(null));
        assertTrue(hp.isEmpty(), "Queue should be empty after polling all aircraft.");
    }

    /**
     * Verifies fuel consumption occurs each tick, and aircraft are diverted when fuel reaches the minimum threshold.
     * Diversions should be reported to EventManager.
     */
    @Test
    void update_shouldConsumeFuel_andDivertWhenFuelAtOrBelow10() {
        HoldingPattern hp = new HoldingPattern();
        EventManager eventManager = mock(EventManager.class);
        hp.setEventManager(eventManager);

        Aircraft lowFuel = newArrival("LOW-1", 10.5, 0, 0); // after consumeFuel(1.0) => 9.5 (divert)
        lowFuel.setStatus(EmergencyStatus.NONE);

        hp.addAircraft(lowFuel);

        hp.update(0);

        assertEquals(0, hp.getSize(), "Aircraft should be removed (diverted) when fuel <= 10.");
        assertEquals(AircraftState.DIVERTED, lowFuel.getState(), "Diverted aircraft should be marked as DIVERTED.");
        verify(eventManager, times(1)).reportDiversion("LOW-1", 0);
    }

    /**
     * Verifies aircraft are not diverted if fuel remains above the minimum threshold after consumption,
     * and no diversion is reported.
     */
    @Test
    void update_shouldNotDivert_ifFuelRemainsAbove10() {
        HoldingPattern hp = new HoldingPattern();
        EventManager eventManager = mock(EventManager.class);
        hp.setEventManager(eventManager);

        Aircraft okFuel = newArrival("OK-1", 11.1, 0, 0); // after consumeFuel(1.0) => 10.1 (keep)
        okFuel.setStatus(EmergencyStatus.NONE);

        hp.addAircraft(okFuel);

        hp.update(0);

        assertEquals(1, hp.getSize(), "Aircraft should remain in holding pattern if fuel > 10 after tick.");
        assertNotEquals(AircraftState.DIVERTED, okFuel.getState(), "Aircraft should not be marked diverted.");
        verify(eventManager, never()).reportDiversion(anyString(), anyInt());
    }

    /**
     * Verifies that when fuel drops to or below the emergency threshold (15) but stays above the
     * diversion threshold (10), the aircraft receives EmergencyStatus.FUEL, remains in the queue,
     * and reportAircraftEmergency is called exactly once on the EventManager.
     */
    @Test
    void update_shouldAssignFuelEmergency_whenFuelDropsToOrBelowEmergencyLevel() {
        HoldingPattern hp = new HoldingPattern();
        EventManager eventManager = mock(EventManager.class);
        hp.setEventManager(eventManager);

        // 16.0 - 1.0 (one tick) = 15.0, which is <= emergencyFuelLevel (15) and > minFuelLevel (10).
        Aircraft aircraft = newArrival("FUELEM-1", 16.0, 0, 0);
        hp.addAircraft(aircraft);

        hp.update(5);

        // Aircraft must still be in the holding pattern — fuel is above the diversion threshold.
        assertEquals(1, hp.getSize(), "Aircraft should not be removed; fuel is above the diversion threshold.");
        assertNotEquals(AircraftState.DIVERTED, aircraft.getState(), "Aircraft should not be marked as diverted.");

        // Status must be escalated to FUEL emergency.
        assertEquals(EmergencyStatus.FUEL, aircraft.getStatus(),
                "Aircraft with fuel at or below the emergency level should have EmergencyStatus.FUEL.");

        // The emergency must be reported to EventManager.
        verify(eventManager, times(1)).reportAircraftEmergency("FUELEM-1", EmergencyStatus.FUEL, 5);

        // A diversion must NOT have been reported.
        verify(eventManager, never()).reportDiversion(anyString(), anyInt());
    }

    /**
     * Verifies that if a diversion occurs and EventManager has not been set,
     * update() throws an IllegalStateException (to prevent silent failures).
     */
    @Test
    void update_whenDiversionOccursWithoutEventManager_shouldThrow() {
        HoldingPattern hp = new HoldingPattern();

        Aircraft lowFuel = newArrival("LOW-2", 10.5, 0, 0); // will divert after consuming fuel
        lowFuel.setStatus(EmergencyStatus.NONE);
        hp.addAircraft(lowFuel);

        assertThrows(IllegalStateException.class, () -> hp.update(0),
                "Expected update() to throw if diversion occurs without an EventManager set.");
    }

    /**
     * Verifies that updating an aircraft's emergency status re-prioritises it and updates holding altitudes.
     */
    @Test
    void updateAircraftStatus_shouldReprioritiseAircraft_andUpdateAltitudes() {
        HoldingPattern hp = new HoldingPattern();

        Aircraft a = newArrival("A-1", 40.0, 0, 0);
        a.setStatus(EmergencyStatus.NONE);

        Aircraft b = newArrival("B-1", 40.0, 0, 0);
        b.setStatus(EmergencyStatus.NONE);

        hp.addAircraft(a);
        hp.addAircraft(b);

        // Force altitude assignment based on current ordering.
        hp.updateAltitudes();

        EmergencyStatus emergency = someNonNoneEmergencyStatus();
        hp.updateAircraftStatus("B-1", emergency, EventSource.MANUAL);

        Optional<Aircraft> next = hp.peekNextAircraft();
        assertTrue(next.isPresent(), "Expected an aircraft at the front of the queue.");
        assertEquals("B-1", next.get().getCallsign(), "Emergency-updated aircraft should become highest priority.");

        // Highest priority aircraft should be assigned lowest altitude slot (1000ft).
        int altitudeB = getAltitudeReflective(next.get());
        assertEquals(1000, altitudeB, "Highest priority aircraft should have altitude 1000ft.");
    }

    /**
     * Verifies getRandomAircraft only returns callsigns for aircraft with EmergencyStatus.NONE,
     * and returns null if none are eligible.
     */
    @Test
    void getRandomAircraft_shouldOnlySelectNoneStatus_andReturnNullIfNoneEligible() {
        HoldingPattern hp = new HoldingPattern();

        EmergencyStatus emergency = someNonNoneEmergencyStatus();

        Aircraft none1 = newArrival("N-1", 30.0, 0, 0);
        none1.setStatus(EmergencyStatus.NONE);

        Aircraft none2 = newArrival("N-2", 30.0, 0, 0);
        none2.setStatus(EmergencyStatus.NONE);

        Aircraft emg = newArrival("E-1", 30.0, 0, 0);
        emg.setStatus(emergency);

        hp.addAircraft(none1);
        hp.addAircraft(none2);
        hp.addAircraft(emg);

        Random r = new Random(123);

        // Call multiple times; should never return the emergency callsign.
        for (int i = 0; i < 20; i++) {
            String cs = hp.getRandomAircraft(r);
            assertNotNull(cs, "Expected a non-null callsign when NONE aircraft exist.");
            assertTrue(cs.equals("N-1") || cs.equals("N-2"),
                    "Random selection should only pick NONE aircraft. Got: " + cs);
        }

        // Make all aircraft emergencies -> no eligible aircraft.
        hp.updateAircraftStatus("N-1", emergency, EventSource.MANUAL);
        hp.updateAircraftStatus("N-2", emergency, EventSource.MANUAL);

        assertNull(hp.getRandomAircraft(new Random(1)),
                "Expected null when no aircraft with EmergencyStatus.NONE are available.");
    }

    /**
     * Verifies getEmergencyAircraft returns only aircraft with non-NONE emergency statuses,
     * and that the returned list is ordered (sorted) by the Aircraft compareTo implementation.
     */
    @Test
    void getEmergencyAircraft_shouldReturnOnlyEmergencies_sortedByPriority() {
        HoldingPattern hp = new HoldingPattern();

        EmergencyStatus emergency = someNonNoneEmergencyStatus();

        Aircraft e1 = newArrival("E-LOWFUEL", 12.0, 0, 0);
        e1.setStatus(emergency);

        Aircraft e2 = newArrival("E-HIGHFUEL", 18.0, 0, 0);
        e2.setStatus(emergency);

        Aircraft n1 = newArrival("N-1", 40.0, 0, 0);
        n1.setStatus(EmergencyStatus.NONE);

        hp.addAircraft(e2);
        hp.addAircraft(n1);
        hp.addAircraft(e1);

        List<Aircraft> emergencies = hp.getEmergencyAircraft();

        assertEquals(2, emergencies.size(), "Expected only emergency aircraft to be returned.");
        assertTrue(emergencies.stream().allMatch(a -> a.getStatus() != EmergencyStatus.NONE),
                "All returned aircraft should have a non-NONE emergency status.");

        // With same emergency status, lower fuel should have higher priority.
        assertEquals("E-LOWFUEL", emergencies.get(0).getCallsign(),
                "Expected lower-fuel emergency aircraft to appear first.");
        assertEquals("E-HIGHFUEL", emergencies.get(1).getCallsign());
    }

    /**
     * Verifies the 1000ft vertical separation rule (SR-FR-5):
     * updateAltitudes() assigns altitude = (priorityPosition + 1) * 1000 to each aircraft,
     * so the highest-priority aircraft occupies 1000ft, second gets 2000ft, third gets 3000ft, etc.
     * This guarantees the required 1000ft separation between every consecutive pair.
     *
     * NONE-status aircraft are ordered by entry tick (earliest = highest priority), so
     * distinct entry ticks provide a deterministic priority ordering here.
     */
    @Test
    void updateAltitudes_shouldAssign1000ftSeparation_toEachPriorityPosition() {
        HoldingPattern hp = new HoldingPattern();

        // All NONE status; NONE aircraft compare by entry tick (lower = higher priority).
        Aircraft first  = newArrival("FIRST",  30.0, 0, 1);  // entryTick=1 → highest priority
        Aircraft second = newArrival("SECOND", 30.0, 0, 2);  // entryTick=2 → second priority
        Aircraft third  = newArrival("THIRD",  30.0, 0, 3);  // entryTick=3 → lowest priority

        first.setStatus(EmergencyStatus.NONE);
        second.setStatus(EmergencyStatus.NONE);
        third.setStatus(EmergencyStatus.NONE);

        // Add in scrambled order to ensure updateAltitudes relies on priority, not insertion order.
        hp.addAircraft(third);
        hp.addAircraft(first);
        hp.addAircraft(second);

        hp.updateAltitudes();

        // Poll in priority order and verify each altitude slot maintains 1000ft separation.
        Aircraft a = hp.getNextAircraft().orElseThrow();
        Aircraft b = hp.getNextAircraft().orElseThrow();
        Aircraft c = hp.getNextAircraft().orElseThrow();

        assertEquals("FIRST", a.getCallsign(), "Earliest entry-tick aircraft should be served first.");
        assertEquals(1000, a.getAltitude(),
                "Highest priority aircraft should occupy the 1000ft slot.");
        assertEquals(2000, b.getAltitude(),
                "Second priority aircraft should occupy the 2000ft slot (1000ft above first).");
        assertEquals(3000, c.getAltitude(),
                "Third priority aircraft should occupy the 3000ft slot (1000ft above second).");
    }
}