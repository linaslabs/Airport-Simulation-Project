package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.AircraftEvent;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayEvent;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationEvent;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EventLogger.
 *
 * Validates that the logger correctly stores simulation events in insertion order
 * and returns them via getEventLog() without any reordering or filtering.
 */
class EventLoggerTest {

    /**
     * Verifies that a freshly constructed EventLogger contains no events.
     */
    @Test
    void newLogger_shouldBeEmpty() {
        EventLogger logger = new EventLogger();

        assertTrue(logger.getEventLog().isEmpty(),
                "A new EventLogger should have an empty event log.");
    }

    /**
     * Verifies that addEvent stores a single event and getEventLog returns it.
     */
    @Test
    void addEvent_shouldStoreAndReturnSingleEvent() {
        EventLogger logger = new EventLogger();
        AircraftEvent event = new AircraftEvent(1, "BA001", AircraftEventType.MANUAL_EMERGENCY, EmergencyStatus.FUEL);

        logger.addEvent(event);

        List<SimulationEvent> log = logger.getEventLog();
        assertEquals(1, log.size());
        assertSame(event, log.get(0),
                "The stored event should be the exact object that was added.");
    }

    /**
     * Verifies that multiple events are stored in the order they were added.
     */
    @Test
    void addEvent_shouldPreserveInsertionOrder() {
        EventLogger logger = new EventLogger();
        AircraftEvent e1 = new AircraftEvent(1, "BA001", AircraftEventType.MANUAL_EMERGENCY, EmergencyStatus.FUEL);
        RunwayEvent   e2 = new RunwayEvent(2, 0, RunwayStatus.INSPECTION, RunwayMode.LANDING, RunwayEventType.SCHEDULED_CHANGE, 5);
        AircraftEvent e3 = new AircraftEvent(3, "BA002", AircraftEventType.DIVERSION, EmergencyStatus.FUEL);

        logger.addEvent(e1);
        logger.addEvent(e2);
        logger.addEvent(e3);

        List<SimulationEvent> log = logger.getEventLog();
        assertEquals(3, log.size());
        assertSame(e1, log.get(0), "First event should be at index 0.");
        assertSame(e2, log.get(1), "Second event should be at index 1.");
        assertSame(e3, log.get(2), "Third event should be at index 2.");
    }

    /**
     * Verifies that AircraftEvent and RunwayEvent subtypes can be mixed in the same log.
     */
    @Test
    void addEvent_shouldAcceptMixedEventSubtypes() {
        EventLogger logger = new EventLogger();

        logger.addEvent(new AircraftEvent(1, "BA001", AircraftEventType.DIVERSION, EmergencyStatus.FUEL));
        logger.addEvent(new RunwayEvent(2, 0, RunwayStatus.SNOW_CLEARANCE, RunwayMode.MIXED, RunwayEventType.RANDOMLY_GENERATED_CLOSURE, 10));

        assertEquals(2, logger.getEventLog().size(),
                "Logger should hold both an AircraftEvent and a RunwayEvent.");
    }
}
