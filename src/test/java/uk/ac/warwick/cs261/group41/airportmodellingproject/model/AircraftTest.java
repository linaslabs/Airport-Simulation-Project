package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Aircraft.
 *
 * Validates initial state for ARRIVAL and DEPARTURE aircraft, fuel consumption
 * including the negative-fuel guard, and compareTo priority ordering.
 */
class AircraftTest {

    /**
     * Helper to build an ARRIVAL aircraft with the given fuel and entry/scheduled ticks.
     *
     * @param callsign      the aircraft callsign
     * @param fuel          the initial fuel level
     * @param scheduledTick the tick the aircraft was scheduled to arrive
     * @param entryTick     the tick the aircraft entered the system
     * @return a new ARRIVAL Aircraft
     */
    private static Aircraft arrival(String callsign, double fuel, int scheduledTick, int entryTick) {
        return new Aircraft(callsign, "OP", "SRC", "DST", fuel, scheduledTick, entryTick, FlightType.ARRIVAL);
    }

    /**
     * Helper to build a DEPARTURE aircraft with the given fuel and entry/scheduled ticks.
     *
     * @param callsign      the aircraft callsign
     * @param fuel          the initial fuel level
     * @param scheduledTick the tick the aircraft was scheduled to depart
     * @param entryTick     the tick the aircraft entered the system
     * @return a new DEPARTURE Aircraft
     */
    private static Aircraft departure(String callsign, double fuel, int scheduledTick, int entryTick) {
        return new Aircraft(callsign, "OP", "SRC", "DST", fuel, scheduledTick, entryTick, FlightType.DEPARTURE);
    }

    // Constructor state

    /**
     * Verifies that an ARRIVAL aircraft is initialised with altitude 10 000 ft,
     * ground speed 230 knots, state QUEUED_FOR_LANDING, and emergency status NONE.
     */
    @Test
    void constructor_arrival_shouldInitialiseCorrectFlightState() {
        Aircraft aircraft = arrival("BA001", 40.0, 10, 11);

        assertEquals(10000, aircraft.getAltitude(),
                "ARRIVAL aircraft should be initialised at 10 000 ft.");
        assertEquals(230, aircraft.getGroundSpeed(),
                "ARRIVAL aircraft should be initialised at 230 knots.");
        assertEquals(AircraftState.QUEUED_FOR_LANDING, aircraft.getState(),
                "ARRIVAL aircraft should start in QUEUED_FOR_LANDING state.");
        assertEquals(EmergencyStatus.NONE, aircraft.getStatus(),
                "Newly constructed aircraft should have EmergencyStatus.NONE.");
    }

    /**
     * Verifies that a DEPARTURE aircraft is initialised with altitude 0 ft,
     * ground speed 0 knots, state QUEUED_FOR_TAKEOFF, and emergency status NONE.
     */
    @Test
    void constructor_departure_shouldInitialiseCorrectFlightState() {
        Aircraft aircraft = departure("BA002", 40.0, 10, 10);

        assertEquals(0, aircraft.getAltitude(),
                "DEPARTURE aircraft should be initialised at 0 ft.");
        assertEquals(0, aircraft.getGroundSpeed(),
                "DEPARTURE aircraft should be initialised at 0 knots.");
        assertEquals(AircraftState.QUEUED_FOR_TAKEOFF, aircraft.getState(),
                "DEPARTURE aircraft should start in QUEUED_FOR_TAKEOFF state.");
        assertEquals(EmergencyStatus.NONE, aircraft.getStatus(),
                "Newly constructed aircraft should have EmergencyStatus.NONE.");
    }

    // Fuel consumption

    /**
     * Verifies that consumeFuel reduces the fuel level by the exact amount given.
     */
    @Test
    void consumeFuel_shouldReduceFuelByGivenAmount() {
        Aircraft aircraft = arrival("BA003", 50.0, 0, 0);

        aircraft.consumeFuel(3.5);

        assertEquals(46.5, aircraft.getFuel(), 1e-9,
                "Fuel should be reduced by exactly the consumed amount.");
    }

    /**
     * Verifies that consumeFuel never produces a negative fuel value;
     * if the consumed amount exceeds remaining fuel, fuel is clamped to 0.
     */
    @Test
    void consumeFuel_shouldClampFuelAtZero_whenConsumptionExceedsRemaining() {
        Aircraft aircraft = arrival("BA004", 2.0, 0, 0);

        aircraft.consumeFuel(10.0);

        assertEquals(0.0, aircraft.getFuel(), 1e-9,
                "Fuel should be clamped to 0 when consumption exceeds the available amount.");
    }

    // compareTo ordering

    /**
     * Verifies that an aircraft with higher-priority emergency status compares as less than
     * (i.e. comes before) an aircraft with lower-priority status.
     * FUEL is the highest-priority value in the EmergencyStatus enum.
     */
    @Test
    void compareTo_differentStatus_higherPriorityStatusShouldComeFirst() {
        Aircraft fuelEmergency = arrival("FUEL-1", 14.0, 0, 0);
        fuelEmergency.setStatus(EmergencyStatus.FUEL);

        Aircraft noEmergency = arrival("NONE-1", 40.0, 0, 0);
        noEmergency.setStatus(EmergencyStatus.NONE);

        assertTrue(fuelEmergency.compareTo(noEmergency) < 0,
                "FUEL emergency aircraft should compare as less than (come before) a NONE aircraft.");
        assertTrue(noEmergency.compareTo(fuelEmergency) > 0,
                "NONE aircraft should compare as greater than (come after) a FUEL emergency aircraft.");
    }

    /**
     * Verifies that when two aircraft share the same non-NONE emergency status,
     * the aircraft with lower fuel (more urgent) compares as less than the one with higher fuel.
     */
    @Test
    void compareTo_sameNonNoneStatus_lowerFuelShouldComeFirst() {
        Aircraft lowFuel = arrival("LOW-1", 12.0, 0, 0);
        lowFuel.setStatus(EmergencyStatus.MECHANICAL);

        Aircraft highFuel = arrival("HIGH-1", 18.0, 0, 0);
        highFuel.setStatus(EmergencyStatus.MECHANICAL);

        assertTrue(lowFuel.compareTo(highFuel) < 0,
                "Lower-fuel aircraft should compare as less than (come before) a higher-fuel aircraft with the same status.");
        assertTrue(highFuel.compareTo(lowFuel) > 0,
                "Higher-fuel aircraft should compare as greater than (come after) a lower-fuel aircraft with the same status.");
    }

    /**
     * Verifies that when two aircraft both have EmergencyStatus.NONE,
     * the aircraft that entered the holding pattern earlier (lower entryTick) compares as less than
     * the one that entered later (higher entryTick).
     */
    @Test
    void compareTo_bothNoneStatus_earlierEntryTickShouldComeFirst() {
        Aircraft earlier = arrival("EARLY-1", 40.0, 0, 5);
        Aircraft later   = arrival("LATER-1", 40.0, 0, 10);

        assertTrue(earlier.compareTo(later) < 0,
                "Aircraft with an earlier entryTick should compare as less than (come before) a later entryTick aircraft when both are NONE.");
        assertTrue(later.compareTo(earlier) > 0,
                "Aircraft with a later entryTick should compare as greater than (come after) an earlier entryTick aircraft when both are NONE.");
    }
}
