package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EventSource;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Airport.
 *
 * Covers: constructor duplicate-ID rejection, inbound/outbound aircraft acceptance,
 * runway assignment for LANDING, TAKEOFF, and MIXED modes, emergency prioritisation
 * over regular holding aircraft, null-field partial updates, and runway state
 * snapshot and update operations.
 */
class AirportTest {

    /**
     * Creates a RunwayConfig with AVAILABLE status and the specified ID and mode.
     *
     * @param id   the runway ID
     * @param mode the operating mode for the runway
     * @return a RunwayConfig with RunwayStatus.AVAILABLE
     */
    private static RunwayConfig rc(int id, RunwayMode mode) {
        return new RunwayConfig(id, RunwayStatus.AVAILABLE, mode);
    }

    /**
     * Creates an Airport named "TestAirport" with the given runway configurations,
     * a maxWaitTime of 10, a landing duration of 3, and a fresh Statistics instance.
     *
     * @param configs the runway configurations for the airport
     * @return a new Airport instance
     */
    private static Airport makeAirport(RunwayConfig... configs) {
        return new Airport("TestAirport", List.of(configs), 10, 3, new Statistics());
    }

    /**
     * Creates an ARRIVAL Aircraft with the given callsign and entry tick, and 40 units of fuel.
     *
     * @param callsign  the aircraft callsign
     * @param entryTick the tick the aircraft enters the system
     * @return a new ARRIVAL Aircraft
     */
    private static Aircraft arrival(String callsign, int entryTick) {
        return new Aircraft(callsign, "OP", "SRC", "DST", 40.0, entryTick, entryTick, FlightType.ARRIVAL);
    }

    /**
     * Creates a DEPARTURE Aircraft with the given callsign and entry tick, and 0 units of fuel.
     *
     * @param callsign  the aircraft callsign
     * @param entryTick the tick the aircraft enters the system
     * @return a new DEPARTURE Aircraft
     */
    private static Aircraft departure(String callsign, int entryTick) {
        return new Aircraft(callsign, "OP", "SRC", "DST", 0.0, entryTick, entryTick, FlightType.DEPARTURE);
    }

    /**
     * Verifies that the constructor throws when two runways share the same ID,
     * preventing silent misconfiguration.
     */
    @Test
    void constructor_shouldThrow_whenDuplicateRunwayId() {
        List<RunwayConfig> configs = List.of(
                new RunwayConfig(1, RunwayStatus.AVAILABLE, RunwayMode.LANDING),
                new RunwayConfig(1, RunwayStatus.AVAILABLE, RunwayMode.TAKEOFF)
        );
        assertThrows(IllegalArgumentException.class,
                () -> new Airport("TestAirport", configs, 10, 3, new Statistics()));
    }

    /**
     * Verifies that acceptInboundAircraft places the aircraft in the holding pattern.
     */
    @Test
    void acceptInboundAircraft_shouldPlaceAircraftInHoldingPattern() {
        Airport airport = makeAirport(rc(0, RunwayMode.LANDING));

        airport.acceptInboundAircraft(arrival("BA001", 0));

        assertEquals(1, airport.getHoldingPattern().getSize(),
                "Holding pattern should contain the accepted inbound aircraft.");
    }

    /**
     * Verifies that acceptOutboundAircraft places the aircraft in the take-off queue.
     */
    @Test
    void acceptOutboundAircraft_shouldPlaceAircraftInTakeOffQueue() {
        Airport airport = makeAirport(rc(0, RunwayMode.TAKEOFF));

        airport.acceptOutboundAircraft(departure("BA002", 0));

        assertEquals(1, airport.getTakeOffQueue().getSize(),
                "Take-off queue should contain the accepted outbound aircraft.");
    }

    /**
     * Verifies that a LANDING runway is assigned the next aircraft from the holding pattern,
     * leaving the holding pattern empty afterwards.
     */
    @Test
    void assignRunways_shouldAssignHoldingAircraft_toLandingRunway() {
        Airport airport = makeAirport(rc(0, RunwayMode.LANDING));
        airport.acceptInboundAircraft(arrival("BA003", 0));

        airport.assignRunways(0);

        assertEquals(0, airport.getHoldingPattern().getSize(),
                "Holding pattern should be empty after the aircraft is assigned to the LANDING runway.");
    }

    /**
     * Verifies that a TAKEOFF runway is assigned the next aircraft from the take-off queue,
     * leaving the take-off queue empty afterwards.
     */
    @Test
    void assignRunways_shouldAssignTakeoffAircraft_toTakeoffRunway() {
        Airport airport = makeAirport(rc(0, RunwayMode.TAKEOFF));
        airport.acceptOutboundAircraft(departure("BA004", 0));

        airport.assignRunways(0);

        assertEquals(0, airport.getTakeOffQueue().getSize(),
                "Take-off queue should be empty after the aircraft is assigned to the TAKEOFF runway.");
    }

    /**
     * Verifies that a LANDING runway does not serve the take-off queue,
     * so a departure waiting in the queue remains when only LANDING runways exist.
     */
    @Test
    void assignRunways_shouldNotAssignDeparture_toLandingOnlyRunway() {
        Airport airport = makeAirport(rc(0, RunwayMode.LANDING));
        airport.acceptOutboundAircraft(departure("BA005", 0));

        airport.assignRunways(0);

        assertEquals(1, airport.getTakeOffQueue().getSize(),
                "Departure should remain queued when no TAKEOFF or MIXED runway is available.");
    }

    /**
     * Verifies that an emergency aircraft in the holding pattern is assigned to the
     * first available LANDING runway before any non-emergency aircraft, satisfying SR-FR-6.
     * With two LANDING runways and one emergency + one regular aircraft, both are cleared.
     */
    @Test
    void assignRunways_shouldPrioritiseEmergency_overRegularAircraft() {
        Airport airport = makeAirport(rc(0, RunwayMode.LANDING), rc(1, RunwayMode.LANDING));
        airport.acceptInboundAircraft(arrival("BA006", 0));
        airport.acceptInboundAircraft(arrival("BA007", 0));
        airport.updateAircraftStatus("BA007", EmergencyStatus.FUEL, EventSource.MANUAL);

        airport.assignRunways(0);

        assertEquals(0, airport.getHoldingPattern().getSize(),
                "Both aircraft should be cleared: emergency on runway 0, regular on runway 1.");
    }

    /**
     * Verifies that when only a LANDING runway exists and only an emergency aircraft is present,
     * the emergency is served and the holding pattern is emptied.
     */
    @Test
    void assignRunways_shouldAssignEmergencyAircraft_onLandingRunway() {
        Airport airport = makeAirport(rc(0, RunwayMode.LANDING));
        airport.acceptInboundAircraft(arrival("BA008", 0));
        airport.updateAircraftStatus("BA008", EmergencyStatus.MECHANICAL, EventSource.MANUAL);

        airport.assignRunways(0);

        assertEquals(0, airport.getHoldingPattern().getSize(),
                "Emergency aircraft should be assigned to the LANDING runway.");
    }

    /**
     * Verifies that updateRunway correctly updates both status and mode for a known runway ID.
     */
    @Test
    void updateRunway_shouldUpdateStatusAndMode_forKnownId() {
        Airport airport = makeAirport(rc(0, RunwayMode.LANDING));

        airport.updateRunway(0, RunwayStatus.INSPECTION, RunwayMode.TAKEOFF, EventSource.MANUAL);

        RunwayConfig snapshot = airport.getRunwaySnapshot(0);
        assertEquals(RunwayStatus.INSPECTION, snapshot.getStatus(),
                "Runway status should be updated to INSPECTION.");
        assertEquals(RunwayMode.TAKEOFF, snapshot.getMode(),
                "Runway mode should be updated to TAKEOFF.");
    }

    /**
     * Verifies that updateRunway with null status/mode leaves those fields unchanged,
     * only modifying the non-null parameter.
     */
    @Test
    void updateRunway_shouldLeaveNullFieldsUnchanged() {
        Airport airport = makeAirport(rc(0, RunwayMode.LANDING));

        airport.updateRunway(0, RunwayStatus.INSPECTION, null, EventSource.MANUAL);

        RunwayConfig snapshot = airport.getRunwaySnapshot(0);
        assertEquals(RunwayStatus.INSPECTION, snapshot.getStatus());
        assertEquals(RunwayMode.LANDING, snapshot.getMode(),
                "Mode should remain LANDING when null is passed for mode.");
    }

    /**
     * Verifies that updateRunway throws for an unknown runway ID.
     */
    @Test
    void updateRunway_shouldThrow_forUnknownId() {
        Airport airport = makeAirport(rc(0, RunwayMode.LANDING));

        assertThrows(IllegalArgumentException.class,
            () -> airport.updateRunway(99, RunwayStatus.INSPECTION, null, EventSource.MANUAL));
    }

    /**
     * Verifies that getRunwaySnapshot returns the correct ID, status, and mode.
     */
    @Test
    void getRunwaySnapshot_shouldReturnCorrectData_forKnownId() {
        Airport airport = makeAirport(rc(2, RunwayMode.MIXED));

        RunwayConfig snapshot = airport.getRunwaySnapshot(2);

        assertEquals(2, snapshot.getRunwayID());
        assertEquals(RunwayStatus.AVAILABLE, snapshot.getStatus());
        assertEquals(RunwayMode.MIXED, snapshot.getMode());
    }

    /**
     * Verifies that getRunwaySnapshot throws for an unknown runway ID.
     */
    @Test
    void getRunwaySnapshot_shouldThrow_forUnknownId() {
        Airport airport = makeAirport(rc(0, RunwayMode.LANDING));

        assertThrows(IllegalArgumentException.class,
                () -> airport.getRunwaySnapshot(99));
    }

    /**
     * Verifies MIXED runway alternation (SR-FR-9):
     * the runway initialises with lastAircraftType = DEPARTURE, so the first assignment
     * serves the arrival; after that lastAircraftType = ARRIVAL, so the next assignment
     * serves the departure.
     */
    @Test
    void assignRunways_mixedRunway_shouldAlternateArrivalThenDeparture() {
        // runwayOccupationTime = 3 (from makeAirport)
        Airport airport = makeAirport(rc(0, RunwayMode.MIXED));
        airport.acceptInboundAircraft(arrival("BA010", 0));
        airport.acceptOutboundAircraft(departure("BA011", 0));

        // Runway default lastAircraftType = DEPARTURE → should serve the arrival first.
        airport.assignRunways(0);

        assertEquals(0, airport.getHoldingPattern().getSize(),
                "MIXED runway should serve the arrival first (lastType was DEPARTURE).");
        assertEquals(1, airport.getTakeOffQueue().getSize(),
                "Departure should remain queued after tick 0.");

        // Runway is occupied until tick 3. Add another arrival so both queues are non-empty.
        // At tick 3, lastAircraftType = ARRIVAL → should serve the departure next.
        airport.acceptInboundAircraft(arrival("BA012", 3));
        airport.assignRunways(3);

        assertEquals(0, airport.getTakeOffQueue().getSize(),
                "MIXED runway should serve the departure next (lastType was ARRIVAL).");
        assertEquals(1, airport.getHoldingPattern().getSize(),
                "Arrival BA012 should remain queued after the departure is served.");
    }

    /**
     * Verifies that updateQueues() removes a departure whose wait time has reached maxWaitTime
     * and reports the cancellation to EventManager.
     */
    @Test
    void updateQueues_shouldCancelExpiredDeparture_andReportToEventManager() {
        // makeAirport uses maxWaitTime = 10
        Airport airport = makeAirport(rc(0, RunwayMode.TAKEOFF));
        EventManager em = mock(EventManager.class);
        airport.setEventManager(em);

        airport.acceptOutboundAircraft(departure("BA013", 0));

        // waitTime = 10 - 0 = 10 >= maxWaitTime(10) → cancellation
        airport.updateQueues(10);

        assertEquals(0, airport.getTakeOffQueue().getSize(),
                "Expired departure should be removed from the take-off queue by updateQueues.");
        verify(em).reportCancellation("BA013", 10);
    }
}
