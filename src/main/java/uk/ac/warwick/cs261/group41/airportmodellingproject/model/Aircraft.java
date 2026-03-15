package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EventSource;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;

/**
 * Represents an aircraft in the airport simulation.
 * Stores state information such as fuel level, callsign, emergency status,
 * and flight details. Implements Comparable to support priority ordering
 * in the holding pattern based on emergency status and fuel levels.
 */
public class Aircraft implements Comparable<Aircraft> {
    /** Unique identifier for the aircraft (e.g., BAW123) */
    private final String callsign;
    
    /** The airline operating this aircraft (e.g., British Airways) */
    private final String operator;
    
    /** The origin airport or location of this flight */
    private final String origin;
    
    /** The destination airport or location of this flight */
    private final String destination;
    
    /** Current fuel level in minutes of flight time remaining */
    private double fuel;
    
    /** The simulation tick when this aircraft was originally scheduled */
    private final int scheduledTick;
    
    /** The actual simulation tick when this aircraft entered the system */
    private final int entryTick;
    
    /** Whether this is an arrival or departure flight */
    private final FlightType flightType;
    
    /** Source of the emergency event if one was triggered */
    private EventSource emergencySource = EventSource.NONE;

    /** Current emergency status of the aircraft (NONE, FUEL, MECHANICAL, PASSENGER, etc.) */
    private EmergencyStatus status;

    /** Current altitude in feet (10,000ft for inbound, 0ft for outbound) */
    private int altitude;
    
    /** Current ground speed in knots (230 for inbound, 0 for outbound) */
    private int groundSpeed;
    
    /** Current state of the aircraft in the simulation workflow */
    private AircraftState state;

    /**
     * Constructs a new Aircraft with the specified parameters.
     * Initialises altitude, ground speed, and state based on flight type.
     *
     * @param callsign unique identifier for the aircraft
     * @param operator the airline operating this aircraft
     * @param origin the origin airport or location
     * @param destination the destination airport or location
     * @param fuel initial fuel level in minutes
     * @param scheduledTick the tick when aircraft was scheduled
     * @param entryTick the actual tick when aircraft entered the system
     * @param flightType whether this is an ARRIVAL or DEPARTURE
     */
    public Aircraft(String callsign, String operator, String origin, String destination, double fuel, int scheduledTick, int entryTick, FlightType flightType) {
        this.callsign = callsign;
        this.operator = operator;
        this.origin = origin;
        this.destination = destination;
        this.fuel = fuel;
        this.scheduledTick = scheduledTick;
        this.entryTick = entryTick;
        this.flightType = flightType;

        this.status = EmergencyStatus.NONE;

        if (flightType == FlightType.ARRIVAL) {
            this.altitude = 10000;
            this.groundSpeed = 230;
            this.state = AircraftState.QUEUED_FOR_LANDING;
        }
        else {
            this.altitude = 0;
            this.groundSpeed = 0;
            this.state = AircraftState.QUEUED_FOR_TAKEOFF;
        }
    }

    /**
     * Gets the unique callsign identifier of this aircraft.
     *
     * @return the aircraft callsign
     */
    public String getCallsign() {
        return callsign;
    }

    /**
     * Gets the airline operator of this aircraft.
     *
     * @return the operator name
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Gets the origin location of this flight.
     *
     * @return the origin airport or location
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Gets the destination location of this flight.
     *
     * @return the destination airport or location
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Gets the current fuel level of this aircraft.
     *
     * @return the fuel level in minutes of flight time
     */
    public double getFuel() {
        return fuel;
    }

    /**
     * Reduces the aircraft's fuel level by the specified amount.
     * Fuel level will not go below zero.
     *
     * @param amount the amount of fuel to consume
     */
    public void consumeFuel(double amount) {
        fuel -= amount;

        // Protection to prevent negative fuel values.
        if (fuel < 0) {
            fuel = 0;
        }
    }

    /**
     * Gets the simulation tick when this aircraft was originally scheduled.
     *
     * @return the scheduled tick
     */
    public int getScheduledTick() {
        return scheduledTick;
    }

    /**
     * Gets the actual simulation tick when this aircraft entered the system.
     *
     * @return the entry tick
     */
    public int getEntryTick() {
        return entryTick;
    }

    /**
     * Gets the flight type of this aircraft.
     *
     * @return ARRIVAL or DEPARTURE
     */
    public FlightType getFlightType() {
        return flightType;
    }

    /**
     * Gets the current emergency status of this aircraft.
     *
     * @return the emergency status
     */
    public EmergencyStatus getStatus() {
        return status;
    }

    /**
     * Sets the emergency status of this aircraft.
     *
     * @param status the new emergency status
     */
    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }

    /**
     * Gets the current altitude of this aircraft in feet.
     *
     * @return the altitude in feet
     */
    public int getAltitude() {
        return altitude;
    }

    /**
     * Sets the altitude of this aircraft.
     *
     * @param altitude the new altitude in feet
     */
    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    /**
     * Gets the current ground speed of this aircraft in knots.
     *
     * @return the ground speed in knots
     */
    public int getGroundSpeed() {
        return groundSpeed;
    }

    /**
     * Sets the ground speed of this aircraft.
     *
     * @param groundSpeed the new ground speed in knots
     */
    public void setGroundSpeed(int groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    /**
     * Gets the current state of this aircraft in the simulation.
     *
     * @return the aircraft state
     */
    public AircraftState getState() {
        return state;
    }

    /**
     * Sets the state of this aircraft in the simulation.
     *
     * @param state the new aircraft state
     */
    public void setState(AircraftState state) {
        this.state = state;
    }

    /**
     * Gets the source of the emergency event that triggered this aircraft's status.
     *
     * @return the event source
     */
    public EventSource getEmergencySource() { return this.emergencySource; }

    /**
     * Sets the source of the emergency event for this aircraft.
     *
     * @param emergencySource the event source to set
     */
    public void setEmergencySource(EventSource emergencySource) { this.emergencySource = emergencySource; }

    /**
     * Compares this aircraft to another for priority ordering in the holding pattern.
     * Priority is determined by: emergency status first, then fuel level for emergencies,
     * then entry tick for non-emergencies.
     *
     * @param other the other aircraft to compare to
     * @return negative if this aircraft has higher priority, positive if lower, zero if equal
     */
    @Override
    public int compareTo(Aircraft other) {
        int statusComparison = status.compareTo(other.getStatus());

        // If the aircraft have different emergency status, simply return the result of compareTo on the order of the Enum.
        if (statusComparison != 0) {
            return statusComparison;
        }

        // If their emergency status is the same, but not None, then order by fuel levels.
        if (status != EmergencyStatus.NONE) {
            return Double.compare(fuel, other.getFuel());
        }

        // If they both have an emergency status of None, then order by entryTick.
        return Integer.compare(entryTick, other.getEntryTick());
    }
}
