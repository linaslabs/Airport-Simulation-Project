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

    /**
     * Creates a Runway with ID 1 and the given mode and status.
     *
     * @param mode   the operating mode for the runway
     * @param status the initial status for the runway
     * @return a Runway instance with ID 1
     */
    private static Runway newRunway(RunwayMode mode, RunwayStatus status) {
        return new Runway(1, mode, status);
    }

    /**
     * Verifies that a runway is available when status is AVAILABLE and it is not occupied.
     */
    @Test
    void isAvailable_shouldBeTrue_whenStatusAvailable_andNotOccupied() {
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        assertTrue(runway.isAvailable(0), "Expected runway to be available at tick 0 when not occupied.");
        assertTrue(runway.isAvailable(5), "Expected runway to be available at later ticks when not occupied.");
    }

    /**
     * Verifies that non-AVAILABLE runway statuses block availability even when not occupied.
     */
    @Test
    void isAvailable_shouldBeFalse_whenStatusNotAvailable_evenIfNotOccupied() {
        Runway runwayInspection = newRunway(RunwayMode.MIXED, RunwayStatus.INSPECTION);
        Runway runwaySnow = newRunway(RunwayMode.MIXED, RunwayStatus.SNOW_CLEARANCE);
        Runway runwayFailure = newRunway(RunwayMode.MIXED, RunwayStatus.EQUIPMENT_FAILURE);

        assertFalse(runwayInspection.isAvailable(100), "Runway should not be available during INSPECTION.");
        assertFalse(runwaySnow.isAvailable(100), "Runway should not be available during SNOW_CLEARANCE.");
        assertFalse(runwayFailure.isAvailable(100), "Runway should not be available during EQUIPMENT_FAILURE.");
    }

    /**
     * Verifies that assigning an aircraft stores the aircraft reference, sets occupiedUntil, and records lastAircraftType.
     */
    @Test
    void assignAircraft_shouldSetCurrentAircraft_andOccupiedUntil_andLastAircraftType() {
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        Aircraft a = mock(Aircraft.class);
        when(a.getFlightType()).thenReturn(FlightType.ARRIVAL);

        runway.assignAircraft(a, 10);

        assertSame(a, runway.getCurrentAircraft(), "Expected currentAircraft to be set after assignment.");
        assertEquals(10, runway.getOccupiedUntil(), "Expected occupiedUntil to be set to the provided untilTick.");
        assertEquals(FlightType.ARRIVAL, runway.getLastAircraftType(), "Expected lastAircraftType to match aircraft type.");
    }

    /**
     * Verifies that the runway is unavailable before occupiedUntil and available at or after it.
     */
    @Test
    void isAvailable_shouldBeFalse_beforeOccupiedUntil_andTrueAtOrAfter() {
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        Aircraft a = mock(Aircraft.class);
        when(a.getFlightType()).thenReturn(FlightType.DEPARTURE);

        runway.assignAircraft(a, 10);

        assertFalse(runway.isAvailable(0), "Expected runway to be unavailable immediately after assignment.");
        assertFalse(runway.isAvailable(9), "Expected runway to be unavailable before occupiedUntil.");
        assertTrue(runway.isAvailable(10), "Expected runway to become available at occupiedUntil.");
        assertTrue(runway.isAvailable(11), "Expected runway to remain available after occupiedUntil.");
    }

    /**
     * Verifies that update clears the current aircraft once the occupation time has expired, preventing ghost aircraft.
     */
    @Test
    void update_shouldClearCurrentAircraft_whenOccupationExpires() {
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        Aircraft a = mock(Aircraft.class);
        when(a.getFlightType()).thenReturn(FlightType.ARRIVAL);

        runway.assignAircraft(a, 10);

        runway.update(9);
        assertNotNull(runway.getCurrentAircraft(), "Aircraft should still be present before occupiedUntil.");

        runway.update(10);
        assertNull(runway.getCurrentAircraft(), "Aircraft should be cleared when currentTick >= occupiedUntil.");
    }

    /**
     * Verifies that update is safe when no aircraft has been assigned to the runway.
     */
    @Test
    void update_shouldNotClear_whenNoAircraftAssigned() {
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        assertNull(runway.getCurrentAircraft(), "Precondition: no aircraft should be assigned.");
        runway.update(100);
        assertNull(runway.getCurrentAircraft(), "update() should not introduce an aircraft.");
    }

    /**
     * Verifies that the mode and status setters update the runway correctly.
     */
    @Test
    void setters_shouldUpdateModeAndStatus() {
        Runway runway = newRunway(RunwayMode.MIXED, RunwayStatus.AVAILABLE);

        runway.setMode(RunwayMode.LANDING);
        runway.setStatus(RunwayStatus.EQUIPMENT_FAILURE);

        assertEquals(RunwayMode.LANDING, runway.getMode(), "Expected mode setter to update runway mode.");
        assertEquals(RunwayStatus.EQUIPMENT_FAILURE, runway.getStatus(), "Expected status setter to update runway status.");
        assertFalse(runway.isAvailable(100), "EQUIPMENT_FAILURE status should block availability.");
    }
}