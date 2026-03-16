package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.AircraftEvent;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayEvent;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationEvent;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Airport;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventManager.
 *
 * Covers: scheduled runway and aircraft event insertion (including reversion prepend),
 * processScheduledEvents dispatch to the airport, automatic reversion scheduling
 * when a timed event fires, and the diversion/cancellation report helpers that
 * both log the event and increment Statistics.
 */
class EventManagerTest {

    /**
     * Creates a RunwayConfig with AVAILABLE status and LANDING mode for the given ID.
     *
     * @param id the runway ID
     * @return a RunwayConfig with RunwayStatus.AVAILABLE and RunwayMode.LANDING
     */
    private static RunwayConfig snapshot(int id) {
        return new RunwayConfig(id, RunwayStatus.AVAILABLE, RunwayMode.LANDING);
    }

    /**
     * Creates an EventManager wired with the provided collaborators and empty event schedules.
     *
     * @param logger  the EventLogger to record events
     * @param airport the Airport mock or real instance to dispatch events to
     * @param stats   the Statistics instance for accumulating counts
     * @return a fully constructed EventManager using a seeded Random for determinism
     */
    private static EventManager makeManager(EventLogger logger, Airport airport, Statistics stats) {
        return new EventManager(logger, new HashMap<>(), new HashMap<>(), new Random(42), airport, stats);
    }

    /**
     * Verifies that a runway event is stored under the correct tick key.
     */
    @Test
    void addScheduledRunwayEvent_shouldInsertEventAtCorrectTick() {
        EventManager em = makeManager(new EventLogger(), mock(Airport.class), new Statistics());

        RunwayEvent event = new RunwayEvent(5, 0, RunwayStatus.INSPECTION, RunwayMode.LANDING,
                RunwayEventType.SCHEDULED_CHANGE, 10);
        em.addScheduledRunwayEvent(event);

        List<RunwayEvent> bucket = em.getScheduledRunwayEvents().get(5);
        assertNotNull(bucket, "There should be a bucket for tick 5.");
        assertEquals(1, bucket.size());
        assertSame(event, bucket.get(0));
    }

    /**
     * Verifies that a reversion event (duration == 0) is prepended to the front of
     * the existing list for its tick so it executes before any other event at that tick.
     */
    @Test
    void addScheduledRunwayEvent_reversionEvent_shouldBePrepended() {
        EventManager em = makeManager(new EventLogger(), mock(Airport.class), new Statistics());

        RunwayEvent normal1 = new RunwayEvent(5, 0, RunwayStatus.INSPECTION, RunwayMode.LANDING,
                RunwayEventType.SCHEDULED_CHANGE, 10);
        RunwayEvent normal2 = new RunwayEvent(5, 0, RunwayStatus.SNOW_CLEARANCE, RunwayMode.LANDING,
                RunwayEventType.SCHEDULED_CHANGE, 5);
        RunwayEvent reversion = new RunwayEvent(5, 0, RunwayStatus.AVAILABLE, RunwayMode.LANDING,
                RunwayEventType.REVERSION, 0);

        em.addScheduledRunwayEvent(normal1);
        em.addScheduledRunwayEvent(normal2);
        em.addScheduledRunwayEvent(reversion);

        List<RunwayEvent> bucket = em.getScheduledRunwayEvents().get(5);
        assertSame(reversion, bucket.get(0),
                "Reversion event (duration=0) should be at index 0 (prepended).");
    }

    /**
     * Verifies that an aircraft event is stored under the correct tick key.
     */
    @Test
    void addScheduledAircraftEvent_shouldInsertEventAtCorrectTick() {
        EventManager em = makeManager(new EventLogger(), mock(Airport.class), new Statistics());

        AircraftEvent event = new AircraftEvent(3, "BA001", AircraftEventType.SCHEDULED_EMERGENCY,
                EmergencyStatus.MECHANICAL);
        em.addScheduledAircraftEvent(event);

        List<AircraftEvent> bucket = em.getScheduledAircraftEvents().get(3);
        assertNotNull(bucket, "There should be a bucket for tick 3.");
        assertEquals(1, bucket.size());
        assertSame(event, bucket.get(0));
    }

    /**
     * Verifies that processScheduledEvents dispatches a runway event to the airport
     * by calling airport.updateRunway() for the matching tick.
     */
    @Test
    void processScheduledEvents_shouldTriggerRunwayUpdate_forMatchingTick() {
        EventLogger logger = new EventLogger();
        Airport airport = mock(Airport.class);
        when(airport.getRunwaySnapshot(0)).thenReturn(snapshot(0));
        EventManager em = makeManager(logger, airport, new Statistics());

        RunwayEvent event = new RunwayEvent(3, 0, RunwayStatus.INSPECTION, RunwayMode.LANDING,
                RunwayEventType.SCHEDULED_CHANGE, -1);
        em.addScheduledRunwayEvent(event);

        em.processScheduledEvents(3);

        verify(airport).updateRunway(0, RunwayStatus.INSPECTION, RunwayMode.LANDING, EventSource.SCHEDULED);
    }

    /**
     * Verifies that processScheduledEvents does nothing to the airport for a tick
     * that has no scheduled events.
     */
    @Test
    void processScheduledEvents_shouldDoNothing_forTickWithNoEvents() {
        Airport airport = mock(Airport.class);
        EventManager em = makeManager(new EventLogger(), airport, new Statistics());

        em.processScheduledEvents(99);

        verify(airport, never()).updateRunway(anyInt(), any(), any(), any());
    }

    /**
     * Verifies that triggerRunwayEvent with a positive duration schedules a reversion
     * event at currentTick + duration, preserving the pre-event runway state.
     */
    @Test
    void triggerRunwayEvent_withPositiveDuration_shouldScheduleReversionAtCorrectTick() {
        EventLogger logger = new EventLogger();
        Airport airport = mock(Airport.class);
        when(airport.getRunwaySnapshot(0)).thenReturn(snapshot(0));
        EventManager em = makeManager(logger, airport, new Statistics());

        // Fire event at tick 1 with duration 5; reversion should be scheduled at tick 6.
        em.triggerRunwayEvent(0, RunwayStatus.INSPECTION, RunwayMode.LANDING, 1, 5, false, false);

        List<RunwayEvent> reversionBucket = em.getScheduledRunwayEvents().get(6);
        assertNotNull(reversionBucket, "A reversion event should be scheduled at tick 6.");
        assertFalse(reversionBucket.isEmpty());
        assertEquals(0, reversionBucket.get(0).getDuration(),
                "The scheduled reversion event should have duration 0.");
        assertEquals(RunwayEventType.REVERSION, reversionBucket.get(0).getType());
    }

    /**
     * Verifies that triggerRunwayEvent logs the event to the EventLogger regardless of duration.
     */
    @Test
    void triggerRunwayEvent_shouldLogEvent() {
        EventLogger logger = new EventLogger();
        Airport airport = mock(Airport.class);
        when(airport.getRunwaySnapshot(0)).thenReturn(snapshot(0));
        EventManager em = makeManager(logger, airport, new Statistics());

        em.triggerRunwayEvent(0, RunwayStatus.INSPECTION, RunwayMode.LANDING, 2, -1, false, false);

        assertEquals(1, logger.getEventLog().size(),
                "triggerRunwayEvent should log exactly one event.");
    }

    /**
     * Verifies that reportDiversion logs a DIVERSION event to the EventLogger
     * and increments the diversion counter in Statistics.
     */
    @Test
    void reportDiversion_shouldLogEventAndIncrementStats() {
        EventLogger logger = new EventLogger();
        Statistics stats = new Statistics();
        EventManager em = makeManager(logger, mock(Airport.class), stats);

        em.reportDiversion("BA001", 5);

        List<SimulationEvent> log = logger.getEventLog();
        assertEquals(1, log.size(), "Should log exactly one diversion event.");
        AircraftEvent logged = (AircraftEvent) log.get(0);
        assertEquals("BA001", logged.getCallsign());
        assertEquals(AircraftEventType.DIVERSION, logged.getType());
        assertEquals(1, stats.getDiversionCount(),
                "Statistics diversion count should be incremented to 1.");
    }

    /**
     * Verifies that reportCancellation logs a CANCELLATION event to the EventLogger
     * and increments the cancellation counter in Statistics.
     */
    @Test
    void reportCancellation_shouldLogEventAndIncrementStats() {
        EventLogger logger = new EventLogger();
        Statistics stats = new Statistics();
        EventManager em = makeManager(logger, mock(Airport.class), stats);

        em.reportCancellation("BA002", 7);

        List<SimulationEvent> log = logger.getEventLog();
        assertEquals(1, log.size(), "Should log exactly one cancellation event.");
        AircraftEvent logged = (AircraftEvent) log.get(0);
        assertEquals("BA002", logged.getCallsign());
        assertEquals(AircraftEventType.CANCELLATION, logged.getType());
        assertEquals(1, stats.getCancellationCount(),
                "Statistics cancellation count should be incremented to 1.");
    }

    /**
     * Verifies that triggerAircraftEmergency with a specific callsign calls
     * airport.updateAircraftStatus() for that callsign and logs a MANUAL_EMERGENCY event.
     */
    @Test
    void triggerAircraftEmergency_withSpecificCallsign_shouldUpdateAircraftAndLogManualEmergency() {
        EventLogger logger = new EventLogger();
        Airport airport = mock(Airport.class);
        EventManager em = makeManager(logger, airport, new Statistics());

        em.triggerAircraftEmergency("BA001", EmergencyStatus.MECHANICAL, 3, false);

        verify(airport).updateAircraftStatus("BA001", EmergencyStatus.MECHANICAL, EventSource.MANUAL);
        List<SimulationEvent> log = logger.getEventLog();
        assertEquals(1, log.size());
        AircraftEvent logged = (AircraftEvent) log.get(0);
        assertEquals("BA001", logged.getCallsign());
        assertEquals(AircraftEventType.MANUAL_EMERGENCY, logged.getType());
        assertEquals(EmergencyStatus.MECHANICAL, logged.getStatus());
    }

    /**
     * Verifies that triggerAircraftEmergency with a null callsign when the holding pattern
     * is empty is a no-op: no aircraft status is updated and nothing is logged.
     */
    @Test
    void triggerAircraftEmergency_withNullCallsign_andEmptyHolding_shouldBeNoOp() {
        EventLogger logger = new EventLogger();
        Airport airport = mock(Airport.class);
        when(airport.getRandomHoldingAircraft(any())).thenReturn(null);
        EventManager em = makeManager(logger, airport, new Statistics());

        em.triggerAircraftEmergency(null, EmergencyStatus.MECHANICAL, 5, true);

        verify(airport, never()).updateAircraftStatus(anyString(), any(), any());
        assertTrue(logger.getEventLog().isEmpty(),
                "No event should be logged when the holding pattern is empty.");
    }

    /**
     * Verifies that processScheduledEvents dispatches a scheduled aircraft event by choosing
     * a random holding aircraft and logging a SCHEDULED_EMERGENCY event.
     */
    @Test
    void processScheduledEvents_shouldTriggerAircraftEmergency_forMatchingTick() {
        EventLogger logger = new EventLogger();
        Airport airport = mock(Airport.class);
        when(airport.getRandomHoldingAircraft(any())).thenReturn("BA002");
        EventManager em = makeManager(logger, airport, new Statistics());

        AircraftEvent event = new AircraftEvent(7, "ignored", AircraftEventType.SCHEDULED_EMERGENCY,
                EmergencyStatus.PASSENGER);
        em.addScheduledAircraftEvent(event);

        em.processScheduledEvents(7);

        verify(airport).updateAircraftStatus("BA002", EmergencyStatus.PASSENGER, EventSource.SCHEDULED);
        assertEquals(1, logger.getEventLog().size(),
                "processScheduledEvents should log the aircraft emergency event.");
    }
}
