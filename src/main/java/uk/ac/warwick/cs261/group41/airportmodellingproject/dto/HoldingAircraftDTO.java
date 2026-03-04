package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;

public class HoldingAircraftDTO {

    private final String callsign;
    private final double fuelLevel;
    private final EmergencyStatus emergencyStatus;

    public HoldingAircraftDTO(String callsign, double fuelLevel, EmergencyStatus emergencyStatus) {
        this.callsign = callsign;
        this.fuelLevel = fuelLevel;
        this.emergencyStatus = emergencyStatus;
    }

    public String getCallsign() { return callsign; }

    public double getFuelLevel() { return fuelLevel; }

    public EmergencyStatus getEmergencyStatus() { return emergencyStatus; }
}