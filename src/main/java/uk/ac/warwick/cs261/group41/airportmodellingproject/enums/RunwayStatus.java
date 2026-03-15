package uk.ac.warwick.cs261.group41.airportmodellingproject.enums;

/**
 * Represents the current operational status of a runway.
 * Determines whether the runway can accept aircraft operations.
 */
public enum RunwayStatus {
    /** Runway is open and available for operations */
    AVAILABLE,
    
    /** Runway is closed for routine inspection */
    INSPECTION,
    
    /** Runway is closed for snow clearance operations */
    SNOW_CLEARANCE,
    
    /** Runway is closed due to equipment failure */
    EQUIPMENT_FAILURE
}
