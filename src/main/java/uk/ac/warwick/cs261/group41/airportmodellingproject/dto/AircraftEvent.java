package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.SimulationEvent;

public class AircraftEvent extends SimulationEvent {
    private String callsign;
    private AircraftEventType type;
    private EmergencyStatus status;

    public AircraftEvent(String callsign, AircraftEventType type, EmergencyStatus status, int tick) {
        super(tick);
        this.callsign = callsign;
        this.type = type;
        this.status = status;
    }

    public String getCallsign() {
        return this.callsign;
    }

    public AircraftEventType getType() {
        return this.type;
    }

    public EmergencyStatus getStatus() {
        return this.status;
    }
}
