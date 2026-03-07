package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;

public class AircraftEvent extends SimulationEvent {

    @NotBlank(message = "Callsign is required for an aircraft event")
    private String callsign;

    @NotNull(message = "Aircraft event type is required")
    private AircraftEventType type;

    @NotNull(message = "Emergency status is required")
    private EmergencyStatus status;

    // Default constructor required for Jackson.
    public AircraftEvent() {
        super();
    }

    // Parameterised constructor required for EventManager.
    public AircraftEvent(int tick, String callsign, AircraftEventType type, EmergencyStatus status) {
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

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public void setType(AircraftEventType type) {
        this.type = type;
    }

    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }
}
