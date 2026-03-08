package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

public class RunwayEvent extends SimulationEvent {

    @Min(value = 0, message = "Runway ID must be 0 or greater")
    private int runwayID;

    private RunwayStatus runwayStatus;
    private RunwayMode runwayMode;
    private RunwayEventType type;

    @NotNull(message = "Duration must be provided or -1 for infinite")
    private Integer duration;

    // Default constructor required for Jackson.
    public RunwayEvent() { super(); }

    // Parameterised constructor required for EventManager.
    public RunwayEvent(int tick, int runwayID, RunwayStatus status,  RunwayMode mode, RunwayEventType type, Integer duration) {
        super(tick);
        this.runwayID = runwayID;
        this.runwayStatus = status;
        this.runwayMode = mode;
        this.type = type;
        this.duration = duration;
    }

    public int getRunwayID() {
        return this.runwayID;
    }

    public RunwayStatus getRunwayStatus() {
        return this.runwayStatus;
    }
    public RunwayMode getRunwayMode() {
        return this.runwayMode;
    }

    public RunwayEventType getType() {
        return this.type;
    }

    public Integer getDuration() {
        return this.duration;
    }

    public void setRunwayID(int runwayID) {
        this.runwayID = runwayID;
    }

    public void setRunwayStatus(RunwayStatus runwayStatus) {
        this.runwayStatus = runwayStatus;
    }

    public void setRunwayMode(RunwayMode runwayMode) {
        this.runwayMode = runwayMode;
    }

    public void setType(RunwayEventType type) {
        this.type = type;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
