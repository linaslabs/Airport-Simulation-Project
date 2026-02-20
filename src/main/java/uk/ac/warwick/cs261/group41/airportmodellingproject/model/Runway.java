package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

public class Runway {

    private int runwayID;
    private int length;
    private int bearing;
    private RunwayMode mode;
    private RunwayStatus status;
    private Aircraft currentAircraft;
    private FlightType lastAircraftType;
    private int occupiedUntil;

    public Runway(int runwayID, RunwayMode mode, RunwayStatus status) {
        this.runwayID = runwayID;
        this.mode = mode;
        this.status = status;
        this.length = 3000;
        this.bearing = 90;
        this.occupiedUntil = 0;
        this.currentAircraft = null;
        this.lastAircraftType = FlightType.DEPARTURE; // Make sure to prioritise arrivals if anything at the start
    }

    public boolean isAvailable(int currentTick) {
        return status == RunwayStatus.AVAILABLE && currentTick >= occupiedUntil;
    }

    public void assignAircraft(Aircraft a, int untilTick) {
        this.currentAircraft = a;
        this.occupiedUntil = untilTick;
        this.lastAircraftType = a.getFlightType();
    }

    public void update(int currentTick) {
        // If the occupation time has expired, clear the aircraft from the runway (make sure it doesn't hold a ghost aircraft)
        if (currentTick >= occupiedUntil && currentAircraft != null) {
            this.currentAircraft = null;
        }
    }

    public int getRunwayID() {
        return runwayID;
    }

    public void setRunwayID(int runwayID) {
        this.runwayID = runwayID;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getBearing() {
        return bearing;
    }

    public void setBearing(int bearing) {
        this.bearing = bearing;
    }

    public RunwayMode getMode() {
        return mode;
    }

    public void setMode(RunwayMode mode) {
        this.mode = mode;
    }

    public RunwayStatus getStatus() {
        return status;
    }

    public void setStatus(RunwayStatus status) {
        this.status = status;
    }

    public Aircraft getCurrentAircraft() {
        return currentAircraft;
    }

    public void setCurrentAircraft(Aircraft currentAircraft) {
        this.currentAircraft = currentAircraft;
    }

    public FlightType getLastAircraftType() {
        return lastAircraftType;
    }

    public void setLastAircraftType(FlightType lastAircraftType) {
        this.lastAircraftType = lastAircraftType;
    }

    public int getOccupiedUntil() {
        return occupiedUntil;
    }

    public void setOccupiedUntil(int occupiedUntil) {
        this.occupiedUntil = occupiedUntil;
    }
}
