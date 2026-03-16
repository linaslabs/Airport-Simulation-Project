package uk.ac.warwick.cs261.group41.airportmodellingproject.enums;

/**
 * Defines the operational mode of a runway.
 * Determines which types of aircraft operations the runway can handle.
 */
public enum RunwayMode {
    /** Runway is dedicated to landing operations only */
    LANDING,
    
    /** Runway is dedicated to takeoff operations only */
    TAKEOFF,
    
    /** Runway can handle both landing and takeoff operations */
    MIXED
}
