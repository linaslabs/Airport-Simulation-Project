package uk.ac.warwick.cs261.group41.airportmodellingproject.enums;

/**
 * Represents the current state of an aircraft in the simulation workflow.
 * Tracks the aircraft's progress from queue entry to final disposition.
 */
public enum AircraftState {
    /** Aircraft is in the holding pattern waiting to land */
    QUEUED_FOR_LANDING,
    
    /** Aircraft is in the takeoff queue waiting to depart */
    QUEUED_FOR_TAKEOFF,
    
    /** Aircraft has diverted to another airport due to low fuel */
    DIVERTED,
    
    /** Aircraft's flight was cancelled due to excessive wait time */
    CANCELLED,
    
    /** Aircraft has successfully landed */
    LANDED,
    
    /** Aircraft has successfully taken off */
    TAKEN_OFF
}
