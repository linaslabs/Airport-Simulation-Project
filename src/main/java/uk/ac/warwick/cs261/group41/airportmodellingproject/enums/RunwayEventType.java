package uk.ac.warwick.cs261.group41.airportmodellingproject.enums;

/**
 * Defines the types of events that can affect runway status during the simulation.
 * Used for event logging and tracking the source of runway state changes.
 */
public enum RunwayEventType {
    /** Runway status change triggered by a pre-scheduled event */
    SCHEDULED_CHANGE,
    
    /** Runway status change triggered manually by the user */
    MANUAL_CHANGE,
    
    /** Runway closure generated randomly by the simulation engine */
    RANDOMLY_GENERATED_CLOSURE,
    
    /** Runway reverting to available status after a closure event ends */
    REVERSION
}
