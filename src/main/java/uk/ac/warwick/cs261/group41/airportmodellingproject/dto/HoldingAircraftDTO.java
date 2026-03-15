package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EventSource;

/**
 * Data transfer object representing an aircraft in the holding pattern.
 * Contains essential information for UI display including callsign,
 * fuel level, and emergency status details.
 */
public class HoldingAircraftDTO {

    /** The unique callsign of the aircraft */
    private final String callsign;
    
    /** The current fuel level in minutes */
    private final double fuelLevel;
    
    /** The current emergency status of the aircraft */
    private final EmergencyStatus emergencyStatus;
    
    /** The source that triggered the emergency, if any */
    private final EventSource emergencySource;

    /**
     * Constructs a HoldingAircraftDTO with all fields.
     *
     * @param callsign the aircraft callsign
     * @param fuelLevel the current fuel level in minutes
     * @param emergencyStatus the emergency status
     * @param emergencySource the source of the emergency
     */
    public HoldingAircraftDTO(String callsign, double fuelLevel, EmergencyStatus emergencyStatus, EventSource emergencySource) {
        this.callsign = callsign;
        this.fuelLevel = fuelLevel;
        this.emergencyStatus = emergencyStatus;
        this.emergencySource = emergencySource;
    }

    /**
     * Gets the aircraft callsign.
     *
     * @return the callsign
     */
    public String getCallsign() { return callsign; }

    /**
     * Gets the current fuel level.
     *
     * @return the fuel level in minutes
     */
    public double getFuelLevel() { return fuelLevel; }

    /**
     * Gets the emergency status.
     *
     * @return the emergency status
     */
    public EmergencyStatus getEmergencyStatus() { return emergencyStatus; }

    /**
     * Gets the source of the emergency.
     *
     * @return the emergency source
     */
    public EventSource getEmergencySource() { return emergencySource; }
}