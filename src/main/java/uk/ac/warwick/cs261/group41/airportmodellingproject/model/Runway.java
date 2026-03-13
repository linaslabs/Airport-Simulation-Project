package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EventSource;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

/**
 * Represents a runway at the airport that can be used for aircraft landings and takeoffs.
 * Each runway has a mode (landing, takeoff, or mixed), status (available, closed, etc.),
 * and can be occupied by an aircraft for a specified duration.
 */
public class Runway {

    /** Unique identifier for this runway */
    private int runwayID;
    
    /** The operational mode of the runway (LANDING, TAKEOFF, or MIXED) */
    private RunwayMode mode;
    
    /** The current status of the runway (AVAILABLE, CLOSED, etc.) */
    private RunwayStatus status;
    
    /** The aircraft currently occupying this runway, or null if unoccupied */
    private Aircraft currentAircraft;
    
    /** The type of the last aircraft that used this runway (for alternating priority) */
    private FlightType lastAircraftType;
    
    /** The simulation tick until which this runway is occupied */
    private int occupiedUntil;
    
    /** Source of event that locked this runway (prevents manual disruption during scheduled/natural events) */
    private EventSource lockSource = EventSource.NONE;

    /**
     * Constructs a new Runway with the specified ID, mode, and status.
     *
     * @param runwayID the unique identifier for this runway
     * @param mode the operational mode (LANDING, TAKEOFF, or MIXED)
     * @param status the initial status of the runway
     */
    public Runway(int runwayID, RunwayMode mode, RunwayStatus status) {
        this.runwayID = runwayID;
        this.mode = mode;
        this.status = status;
        this.occupiedUntil = 0;
        this.currentAircraft = null;
        this.lastAircraftType = FlightType.DEPARTURE; // Make sure to prioritise arrivals if anything at the start
    }

    /**
     * Checks if the runway is available for use at the current simulation tick.
     * A runway is available if its status is AVAILABLE and it is not occupied.
     *
     * @param currentTick the current simulation tick
     * @return true if the runway is available, false otherwise
     */
    public boolean isAvailable(int currentTick) {
        return status == RunwayStatus.AVAILABLE && currentTick >= occupiedUntil;
    }

    /**
     * Assigns an aircraft to this runway for a specified duration.
     * Records the aircraft type for alternating priority logic.
     *
     * @param a the aircraft to assign to this runway
     * @param untilTick the simulation tick until which the runway will be occupied
     */
    public void assignAircraft(Aircraft a, int untilTick) {
        this.currentAircraft = a;
        this.occupiedUntil = untilTick;
        this.lastAircraftType = a.getFlightType();
    }

    /**
     * Updates the runway state for the current simulation tick.
     * Clears the current aircraft if the occupation time has expired.
     *
     * @param currentTick the current simulation tick
     */
    public void update(int currentTick) {
        // If the occupation time has expired, clear the aircraft from the runway (make sure it doesn't hold a ghost aircraft)
        if (currentTick >= occupiedUntil && currentAircraft != null) {
            this.currentAircraft = null;
        }
    }

    /**
     * Gets the unique identifier of this runway.
     *
     * @return the runway ID
     */
    public int getRunwayID() {
        return runwayID;
    }

    /**
     * Sets the unique identifier of this runway.
     *
     * @param runwayID the runway ID to set
     */
    public void setRunwayID(int runwayID) {
        this.runwayID = runwayID;
    }

    /**
     * Gets the operational mode of this runway.
     *
     * @return the runway mode (LANDING, TAKEOFF, or MIXED)
     */
    public RunwayMode getMode() {
        return mode;
    }

    /**
     * Sets the operational mode of this runway.
     *
     * @param mode the runway mode to set
     */
    public void setMode(RunwayMode mode) {
        this.mode = mode;
    }

    /**
     * Gets the current status of this runway.
     *
     * @return the runway status
     */
    public RunwayStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of this runway.
     *
     * @param status the runway status to set
     */
    public void setStatus(RunwayStatus status) {
        this.status = status;
    }

    /**
     * Gets the aircraft currently occupying this runway.
     *
     * @return the current aircraft, or null if runway is unoccupied
     */
    public Aircraft getCurrentAircraft() {
        return currentAircraft;
    }

    /**
     * Sets the aircraft currently occupying this runway.
     *
     * @param currentAircraft the aircraft to set, or null to clear
     */
    public void setCurrentAircraft(Aircraft currentAircraft) {
        this.currentAircraft = currentAircraft;
    }

    /**
     * Gets the type of the last aircraft that used this runway.
     * Used for alternating priority between arrivals and departures.
     *
     * @return the last aircraft type
     */
    public FlightType getLastAircraftType() {
        return lastAircraftType;
    }

    /**
     * Sets the type of the last aircraft that used this runway.
     *
     * @param lastAircraftType the flight type to set
     */
    public void setLastAircraftType(FlightType lastAircraftType) {
        this.lastAircraftType = lastAircraftType;
    }

    /**
     * Gets the simulation tick until which this runway is occupied.
     *
     * @return the tick number when the runway becomes free
     */
    public int getOccupiedUntil() {
        return occupiedUntil;
    }

    /**
     * Sets the simulation tick until which this runway will be occupied.
     *
     * @param occupiedUntil the tick number to set
     */
    public void setOccupiedUntil(int occupiedUntil) {
        this.occupiedUntil = occupiedUntil;
    }

    /**
     * Gets the source of the event that locked this runway.
     * Locked runways cannot be manually disrupted during scheduled or natural events.
     *
     * @return the event source that locked this runway
     */
    public EventSource getLockSource() { return this.lockSource; }

    /**
     * Sets the source of the event that locked this runway.
     *
     * @param lockSource the event source to set
     */
    public void setLockSource(EventSource lockSource) { this.lockSource = lockSource; }
}
