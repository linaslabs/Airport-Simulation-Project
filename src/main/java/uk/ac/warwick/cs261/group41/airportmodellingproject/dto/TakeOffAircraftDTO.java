package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;

/**
 * Data transfer object representing an aircraft in the takeoff queue.
 * Contains essential information for UI display including callsign,
 * entry time, and current status.
 */
public class TakeOffAircraftDTO {

    /** The unique callsign of the aircraft */
    private final String callsign;
    
    /** The tick when the aircraft entered the queue */
    private final int entryTick;
    
    /** The current state of the aircraft */
    private final AircraftState status;

    /**
     * Constructs a TakeOffAircraftDTO with all fields.
     *
     * @param callsign the aircraft callsign
     * @param entryTick the tick when aircraft entered the queue
     * @param status the current aircraft state
     */
    public TakeOffAircraftDTO(String callsign, int entryTick, AircraftState status) {
        this.callsign = callsign;
        this.entryTick = entryTick;
        this.status = status;
    }

    /**
     * Gets the aircraft callsign.
     *
     * @return the callsign
     */
    public String getCallsign() { return callsign; }

    /**
     * Gets the entry tick.
     *
     * @return the tick when aircraft entered the queue
     */
    public int getEntryTick() { return entryTick; }

    /**
     * Gets the aircraft status.
     *
     * @return the current state
     */
    public AircraftState getStatus() { return status; }
}