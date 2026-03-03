package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
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
 * Focus:
 * - Priority behaviour (emergencies first / ordering)
 * - Fuel consumption + diversion rule at the minimum fuel threshold
 * - Status updates re-prioritise the queue
 * - Random selection only picks aircraft with EmergencyStatus.NONE
 * - Diversions are reported via EventManager
 */
class HoldingPatternTest {

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

    private static EmergencyStatus someNonNoneEmergencyStatus() {
        for (EmergencyStatus s : EmergencyStatus.values()) {
            if (!"NONE".equalsIgnoreCase(s.name())) return s;
        }
        fail("EmergencyStatus enum has no non-NONE values; cannot test emergency ordering.");
        return EmergencyStatus.NONE;
    }

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
        hp.updateAircraftStatus("B-1", emergency);

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
        hp.updateAircraftStatus("N-1", emergency);
        hp.updateAircraftStatus("N-2", emergency);

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
}