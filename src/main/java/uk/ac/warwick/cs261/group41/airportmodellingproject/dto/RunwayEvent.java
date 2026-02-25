package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.SimulationEvent;

public class RunwayEvent extends SimulationEvent {
    private int runwayID;
    private RunwayStatus runwayStatus;
    private RunwayMode runwayMode;
    private int duration;

    public RunwayEvent(int runwayID, RunwayStatus runwayStatus, RunwayMode runwayMode, int tick, int duration) {
        super(tick);
        this.runwayID = runwayID;
        this.runwayStatus = runwayStatus;
        this.runwayMode = runwayMode;
        this.duration = duration;
    }

    public int getRunwayId() {
        return this.runwayID;
    }

    public RunwayStatus getRunwayStatus() {
        return this.runwayStatus;
    }

    public RunwayMode getRunwayMode() {
        return this.runwayMode;
    }

    public int getDuration() {
        return this.duration;
    }
}
