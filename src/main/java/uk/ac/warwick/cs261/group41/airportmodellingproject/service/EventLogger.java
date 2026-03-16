package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains a chronological log of all simulation events.
 * Events are appended as they occur and can be retrieved or replaced in bulk.
 */
public class EventLogger {

    /** The list of all simulation events recorded during the simulation. */
    private final List<SimulationEvent> eventLog;

    /**
     * Constructs an EventLogger with an empty event log.
     */
    public EventLogger() {
        this.eventLog = new ArrayList<>();
    }

    /**
     * Appends a single event to the log.
     * @param event the simulation event to record
     */
    public void addEvent(SimulationEvent event) {
        this.eventLog.add(event);
    }

    /**
     * Returns the full list of recorded simulation events.
     * @return the event log
     */
    public List<SimulationEvent> getEventLog() {
        return this.eventLog;
    }

    /**
     * Replaces the current event log with the provided list.
     * If the provided list is null, the log is simply cleared.
     * @param eventLog the new list of simulation events to set
     */
    public void setEventLog(List<SimulationEvent> eventLog) {
        this.eventLog.clear();
        if (eventLog != null) {
            this.eventLog.addAll(eventLog);
        }
    }
}
