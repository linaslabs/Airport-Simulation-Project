package uk.ac.warwick.cs261.group41.airportmodellingproject.enums;

/**
 * Defines the types of events that can affect aircraft during the simulation.
 * Used for event logging and tracking the source of aircraft state changes.
 */
public enum AircraftEventType {
    /** Emergency triggered by a pre-scheduled event */
    SCHEDULED_EMERGENCY,
    
    /** Emergency triggered manually by the user */
    MANUAL_EMERGENCY,
    
    /** Emergency generated randomly by the simulation engine */
    RANDOMLY_GENERATED_EMERGENCY,
    
    /** Emergency triggered naturally when aircraft fuel drops below threshold */
    NATURAL_FUEL_EMERGENCY,
    
    /** Aircraft flight was cancelled due to excessive wait time */
    CANCELLATION,
    
    /** Aircraft diverted to another airport due to low fuel */
    DIVERSION
}
