package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;

/**
 * Data transfer object representing an event affecting an aircraft during simulation.
 * Extends SimulationEvent to include aircraft-specific details such as callsign,
 * event type, and emergency status. Used for both scheduled and logged events.
 */
public class AircraftEvent extends SimulationEvent {

    /** The callsign of the affected aircraft */
    @NotBlank(message = "Callsign is required for an aircraft event")
    private String callsign;

    /** The type of aircraft event */
    @NotNull(message = "Aircraft event type is required")
    private AircraftEventType type;

    /** The emergency status resulting from this event */
    @NotNull(message = "Emergency status is required")
    private EmergencyStatus status;

    /**
     * Default constructor required for Jackson deserialization.
     */
    public AircraftEvent() {
        super();
    }

    /**
     * Constructs an AircraftEvent with all required fields.
     *
     * @param tick the simulation tick when the event occurs
     * @param callsign the callsign of the affected aircraft
     * @param type the type of aircraft event
     * @param status the emergency status resulting from this event
     */
    public AircraftEvent(int tick, String callsign, AircraftEventType type, EmergencyStatus status) {
        super(tick);
        this.callsign = callsign;
        this.type = type;
        this.status = status;
    }

    /**
     * Gets the callsign of the affected aircraft.
     *
     * @return the aircraft callsign
     */
    public String getCallsign() {
        return this.callsign;
    }

    /**
     * Gets the type of this aircraft event.
     *
     * @return the event type
     */
    public AircraftEventType getType() {
        return this.type;
    }

    /**
     * Gets the emergency status resulting from this event.
     *
     * @return the emergency status
     */
    public EmergencyStatus getStatus() {
        return this.status;
    }

    /**
     * Sets the callsign of the affected aircraft.
     *
     * @param callsign the aircraft callsign
     */
    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    /**
     * Sets the type of this aircraft event.
     *
     * @param type the event type
     */
    public void setType(AircraftEventType type) {
        this.type = type;
    }

    /**
     * Sets the emergency status resulting from this event.
     *
     * @param status the emergency status
     */
    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }
}
