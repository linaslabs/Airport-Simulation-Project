package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Runway.
 *
 * These tests validate the runway "resource locking" behaviour:
 * - A runway is only available if its status is AVAILABLE and its occupation time has expired.
 * - Assigning an aircraft marks it as occupied until a given tick and records lastAircraftType.
 * - When the occupation time expires, update() clears the current aircraft to avoid "ghost aircraft".
 */
class RunwayTest {

    private static Runway newRunway(RunwayMode mode, RunwayStatus status) {
        return new Runway(1, mode, status);
    }

    @Test
    void isAvailable_shouldBeTrue_whenStatusAvailable_andNotOccupied() {
        // Checks the base condition for runway availability.
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        assertTrue(runway.isAvailable(0), "Expected runway to be available at tick 0 when not occupied.");
        assertTrue(runway.isAvailable(5), "Expected runway to be available at later ticks when not occupied.");
    }

    @Test
    void isAvailable_shouldBeFalse_whenStatusNotAvailable_evenIfNotOccupied() {
        // Ensures non-AVAILABLE runway statuses block use of the runway.
        Runway runwayInspection = newRunway(RunwayMode.MIXED, RunwayStatus.INSPECTION);
        Runway runwaySnow = newRunway(RunwayMode.MIXED, RunwayStatus.SNOWCLEARANCE);
        Runway runwayFailure = newRunway(RunwayMode.MIXED, RunwayStatus.FAILURE);

        assertFalse(runwayInspection.isAvailable(100), "Runway should not be available during INSPECTION.");
        assertFalse(runwaySnow.isAvailable(100), "Runway should not be available during SNOWCLEARANCE.");
        assertFalse(runwayFailure.isAvailable(100), "Runway should not be available during FAILURE.");
    }

    @Test
    void assignAircraft_shouldSetCurrentAircraft_andOccupiedUntil_andLastAircraftType() {
        // Verifies assignment stores the aircraft and updates occupiedUntil and lastAircraftType.
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        Aircraft a = mock(Aircraft.class);
        when(a.getFlightType()).thenReturn(FlightType.ARRIVAL);

        runway.assignAircraft(a, 10);

        assertSame(a, runway.getCurrentAircraft(), "Expected currentAircraft to be set after assignment.");
        assertEquals(10, runway.getOccupiedUntil(), "Expected occupiedUntil to be set to the provided untilTick.");
        assertEquals(FlightType.ARRIVAL, runway.getLastAircraftType(), "Expected lastAircraftType to match aircraft type.");
    }

    @Test
    void isAvailable_shouldBeFalse_beforeOccupiedUntil_andTrueAtOrAfter() {
        // Ensures occupation time correctly blocks runway availability.
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        Aircraft a = mock(Aircraft.class);
        when(a.getFlightType()).thenReturn(FlightType.DEPARTURE);

        runway.assignAircraft(a, 10);

        assertFalse(runway.isAvailable(0), "Expected runway to be unavailable immediately after assignment.");
        assertFalse(runway.isAvailable(9), "Expected runway to be unavailable before occupiedUntil.");
        assertTrue(runway.isAvailable(10), "Expected runway to become available at occupiedUntil.");
        assertTrue(runway.isAvailable(11), "Expected runway to remain available after occupiedUntil.");
    }

    @Test
    void update_shouldClearCurrentAircraft_whenOccupationExpires() {
        // Prevents "ghost aircraft": when the runway is no longer occupied, the aircraft should be cleared.
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        Aircraft a = mock(Aircraft.class);
        when(a.getFlightType()).thenReturn(FlightType.ARRIVAL);

        runway.assignAircraft(a, 10);

        runway.update(9);
        assertNotNull(runway.getCurrentAircraft(), "Aircraft should still be present before occupiedUntil.");

        runway.update(10);
        assertNull(runway.getCurrentAircraft(), "Aircraft should be cleared when currentTick >= occupiedUntil.");
    }

    @Test
    void update_shouldNotClear_whenNoAircraftAssigned() {
        // Sanity check: update should be safe when no aircraft is on the runway.
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        assertNull(runway.getCurrentAircraft(), "Precondition: no aircraft should be assigned.");
        runway.update(100);
        assertNull(runway.getCurrentAircraft(), "update() should not introduce an aircraft.");
    }

    @Test
    void setters_shouldUpdateModeAndStatus() {
        // Confirms basic mutators behave as expected (used by Airport/Event logic later).
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        runway.setMode(RunwayMode.LANDING);
        runway.setStatus(RunwayStatus.FAILURE);

        assertEquals(RunwayMode.LANDING, runway.getMode(), "Expected mode setter to update runway mode.");
        assertEquals(RunwayStatus.FAILURE, runway.getStatus(), "Expected status setter to update runway status.");
        assertFalse(runway.isAvailable(100), "FAILURE status should block availability.");
    }
}