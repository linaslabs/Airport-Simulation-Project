package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EventSource;

public class HoldingAircraftDTO {

    private final String callsign;
    private final double fuelLevel;
    private final EmergencyStatus emergencyStatus;
    private final EventSource emergencySource;

    public HoldingAircraftDTO(String callsign, double fuelLevel, EmergencyStatus emergencyStatus, EventSource emergencySource) {
        this.callsign = callsign;
        this.fuelLevel = fuelLevel;
        this.emergencyStatus = emergencyStatus;
        this.emergencySource = emergencySource;
    }

    public String getCallsign() { return callsign; }

    public double getFuelLevel() { return fuelLevel; }

    public EmergencyStatus getEmergencyStatus() { return emergencyStatus; }

    public EventSource getEmergencySource() { return emergencySource; }
}