package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EventSource;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

public class RunwayDTO {

    private final int runwayID;
    private final RunwayStatus status;
    private final RunwayMode mode;
    private final String aircraftCallsign;
    private final int occupiedUntil;
    private final EventSource lockSource;

    public RunwayDTO(int runwayID, RunwayStatus status, RunwayMode mode, String aircraftCallsign, int occupiedUntil, EventSource lockSource) {
        this.runwayID = runwayID;
        this.status = status;
        this.mode = mode;
        this.aircraftCallsign = aircraftCallsign;
        this.occupiedUntil = occupiedUntil;
        this.lockSource = lockSource;
    }

    public int getRunwayID() { return runwayID; }

    public RunwayStatus getStatus() { return status; }

    public RunwayMode getMode() { return mode; }

    public String getAircraftCallsign() { return aircraftCallsign; }

    public int getOccupiedUntil() { return occupiedUntil; }

    public EventSource getLockSource() { return lockSource; }
}