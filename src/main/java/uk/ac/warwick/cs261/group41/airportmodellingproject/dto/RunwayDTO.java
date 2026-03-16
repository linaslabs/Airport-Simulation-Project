package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EventSource;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

/**
 * Data transfer object representing the current state of a runway.
 * Contains runtime information including status, mode, current aircraft,
 * and lock state. Used for real-time UI updates during simulation.
 */
public class RunwayDTO {

    /** Unique identifier for the runway */
    private final int runwayID;
    
    /** Current operational status of the runway */
    private final RunwayStatus status;
    
    /** Current operational mode of the runway */
    private final RunwayMode mode;
    
    /** Callsign of aircraft currently on the runway, or null if empty */
    private final String aircraftCallsign;
    
    /** Simulation tick until which the runway is occupied */
    private final int occupiedUntil;
    
    /** Source that locked this runway, if any */
    private final EventSource lockSource;

    /**
     * Constructs a RunwayDTO with all fields.
     *
     * @param runwayID the runway identifier
     * @param status the operational status
     * @param mode the operational mode
     * @param aircraftCallsign the callsign of aircraft on runway, or null
     * @param occupiedUntil the tick until which runway is occupied
     * @param lockSource the source that locked this runway
     */
    public RunwayDTO(int runwayID, RunwayStatus status, RunwayMode mode, String aircraftCallsign, int occupiedUntil, EventSource lockSource) {
        this.runwayID = runwayID;
        this.status = status;
        this.mode = mode;
        this.aircraftCallsign = aircraftCallsign;
        this.occupiedUntil = occupiedUntil;
        this.lockSource = lockSource;
    }

    /**
     * Gets the runway ID.
     *
     * @return the runway identifier
     */
    public int getRunwayID() { return runwayID; }

    /**
     * Gets the runway status.
     *
     * @return the operational status
     */
    public RunwayStatus getStatus() { return status; }

    /**
     * Gets the runway mode.
     *
     * @return the operational mode
     */
    public RunwayMode getMode() { return mode; }

    /**
     * Gets the callsign of the aircraft on the runway.
     *
     * @return the aircraft callsign, or null if runway is empty
     */
    public String getAircraftCallsign() { return aircraftCallsign; }

    /**
     * Gets the tick until which the runway is occupied.
     *
     * @return the occupied until tick
     */
    public int getOccupiedUntil() { return occupiedUntil; }

    /**
     * Gets the source that locked this runway.
     *
     * @return the lock source
     */
    public EventSource getLockSource() { return lockSource; }
}