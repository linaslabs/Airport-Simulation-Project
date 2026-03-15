package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

/**
 * Data transfer object representing an event affecting a runway during simulation.
 * Extends SimulationEvent to include runway-specific details such as runway ID,
 * status/mode changes, event type, and duration. Used for both scheduled and logged events.
 */
public class RunwayEvent extends SimulationEvent {

    /** The ID of the affected runway */
    @Min(value = 0, message = "Runway ID must be 0 or greater")
    private int runwayID;

    /** The new status for the runway */
    private RunwayStatus runwayStatus;
    
    /** The new mode for the runway */
    private RunwayMode runwayMode;
    
    /** The type of runway event */
    private RunwayEventType type;

    /** Duration of the event in ticks (-1 for infinite) */
    @NotNull(message = "Duration must be provided or -1 for infinite")
    private Integer duration;

    /**
     * Default constructor required for Jackson deserialization.
     */
    public RunwayEvent() { super(); }

    /**
     * Constructs a RunwayEvent with all required fields.
     *
     * @param tick the simulation tick when the event occurs
     * @param runwayID the ID of the affected runway
     * @param status the new runway status
     * @param mode the new runway mode
     * @param type the type of runway event
     * @param duration the duration in ticks (-1 for infinite)
     */
    public RunwayEvent(int tick, int runwayID, RunwayStatus status,  RunwayMode mode, RunwayEventType type, Integer duration) {
        super(tick);
        this.runwayID = runwayID;
        this.runwayStatus = status;
        this.runwayMode = mode;
        this.type = type;
        this.duration = duration;
    }

    /**
     * Gets the runway ID.
     *
     * @return the runway identifier
     */
    public int getRunwayID() {
        return this.runwayID;
    }

    /**
     * Gets the new runway status.
     *
     * @return the runway status
     */
    public RunwayStatus getRunwayStatus() {
        return this.runwayStatus;
    }
    /**
     * Gets the new runway mode.
     *
     * @return the runway mode
     */
    public RunwayMode getRunwayMode() {
        return this.runwayMode;
    }

    /**
     * Gets the event type.
     *
     * @return the runway event type
     */
    public RunwayEventType getType() {
        return this.type;
    }

    /**
     * Gets the event duration.
     *
     * @return the duration in ticks, or -1 for infinite
     */
    public Integer getDuration() {
        return this.duration;
    }

    /**
     * Sets the runway ID.
     *
     * @param runwayID the runway identifier
     */
    public void setRunwayID(int runwayID) {
        this.runwayID = runwayID;
    }

    /**
     * Sets the runway status.
     *
     * @param runwayStatus the runway status
     */
    public void setRunwayStatus(RunwayStatus runwayStatus) {
        this.runwayStatus = runwayStatus;
    }

    /**
     * Sets the runway mode.
     *
     * @param runwayMode the runway mode
     */
    public void setRunwayMode(RunwayMode runwayMode) {
        this.runwayMode = runwayMode;
    }

    /**
     * Sets the event type.
     *
     * @param type the runway event type
     */
    public void setType(RunwayEventType type) {
        this.type = type;
    }

    /**
     * Sets the event duration.
     *
     * @param duration the duration in ticks
     */
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
