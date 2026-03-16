package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

/**
 * Data transfer object representing the configuration of a single runway.
 * Contains the runway's identifier, operational status, and mode.
 * Used for both initial configuration and runtime state snapshots.
 */
public class RunwayConfig {
    /** Unique identifier for the runway (0-9) */
    @Range(min = 0, max = 9, message = "Runway ID must be between 0 and 9")
    private int runwayID;

    /** The operational status of the runway */
    @NotNull(message = "Runway status must be specified (Available, Inspection, Snow Clearance, Mechanical Failure)")
    private RunwayStatus status;

    /** The operational mode of the runway */
    @NotNull(message = "Runway mode must be specified (Landing, Takeoff, Mixed)")
    private RunwayMode mode;

    /**
     * Default constructor required for Jackson deserialization.
     */
    public RunwayConfig() {}

    /**
     * Constructs a RunwayConfig with all fields.
     * Used for testing and event system snapshots.
     *
     * @param runwayID the unique runway identifier
     * @param status the operational status
     * @param mode the operational mode
     */
    public RunwayConfig(int runwayID, RunwayStatus status, RunwayMode mode) {
        this.runwayID = runwayID;
        this.status = status;
        this.mode = mode;
    }

    /**
     * Gets the runway ID.
     *
     * @return the runway identifier
     */
    public int getRunwayID() {
        return runwayID;
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
     * Gets the runway mode.
     *
     * @return the operational mode
     */
    public RunwayMode getMode() {
        return mode;
    }

    /**
     * Sets the runway mode.
     *
     * @param mode the operational mode
     */
    public void setMode(RunwayMode mode) {
        this.mode = mode;
    }

    /**
     * Gets the runway status.
     *
     * @return the operational status
     */
    public RunwayStatus getStatus() {
        return status;
    }

    /**
     * Sets the runway status.
     *
     * @param status the operational status
     */
    public void setStatus(RunwayStatus status) {
        this.status = status;
    }
}
