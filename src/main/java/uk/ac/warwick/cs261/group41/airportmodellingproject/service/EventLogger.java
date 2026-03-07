package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationEvent;

import java.util.ArrayList;
import java.util.List;

public class EventLogger {
    private final List<SimulationEvent> eventLog;

    public EventLogger() {
        this.eventLog = new ArrayList<>();
    }

    public void addEvent(SimulationEvent event) {
        this.eventLog.add(event);
    }

    public  List<SimulationEvent> getEventLog() {
        return this.eventLog;
    }
}
