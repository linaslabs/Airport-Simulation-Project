package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;

public class TakeOffAircraftDTO {

    private final String callsign;
    private final int entryTick;
    private final AircraftState status;

    public TakeOffAircraftDTO(String callsign, int entryTick, AircraftState status) {
        this.callsign = callsign;
        this.entryTick = entryTick;
        this.status = status;
    }

    public String getCallsign() { return callsign; }

    public int getEntryTick() { return entryTick; }

    public AircraftState getStatus() { return status; }
}