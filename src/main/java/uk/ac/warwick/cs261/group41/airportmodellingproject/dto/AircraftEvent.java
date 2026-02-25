package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.SimulationEvent;

public class AircraftEvent extends SimulationEvent {
    @NotBlank(message = "Callsign is required for an aircraft event")
    private final String callsign;

    @NotNull(message = "Aircraft event type is required")
    private final AircraftEventType type;

    @NotNull(message = "Emergency status is required")
    private final EmergencyStatus status;

    public AircraftEvent(@JsonProperty("callsign") String callsign,
                         @JsonProperty("type") AircraftEventType type,
                         @JsonProperty("status") EmergencyStatus status,
                         @JsonProperty("tick") int tick) {
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
