package uk.ac.warwick.cs261.group41.airportmodellingproject.enums;

/**
 * Represents the emergency status of an aircraft.
 * Values are ordered by priority for the Aircraft compareTo method:
 * FUEL (highest) > MECHANICAL > PASSENGER > NONE (lowest).
 */
public enum EmergencyStatus {
    /** Low fuel emergency - highest priority */
    FUEL,
    
    /** Mechanical failure emergency */
    MECHANICAL,
    
    /** Passenger medical emergency */
    PASSENGER,
    
    /** No emergency - normal operations */
    NONE
}
