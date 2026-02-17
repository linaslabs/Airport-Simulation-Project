package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;

public class Aircraft implements Comparable<Aircraft> {
    private String callsign;
    private String operator;
    private String origin;
    private String destination;
    private double fuel;
    private int scheduledTick;
    private int entryTick;
    private FlightType flightType;

    private EmergencyStatus status; // Currently, this is set to None by default, it will be changed and added to constructor in future when we add Events.

    private int altitude; // Set to 10,000ft for inbound and 0ft for outbound.
    private int groundSpeed; // Set to 230 knots for inbound, 0 knots for outbound.
    private AircraftState state; // Set to queued for landing or queued for takeoff depending on flightType.

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

    public String getCallsign() {
        return callsign;
    }

    public String getOperator() {
        return operator;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public double getFuel() {
        return fuel;
    }

    public void consumeFuel(double amount) {
        fuel -= amount;

        // Protection to prevent negative fuel values.
        if (fuel < 0) {
            fuel = 0;
        }
    }

    public int getScheduledTick() {
        return scheduledTick;
    }

    public int getEntryTick() {
        return entryTick;
    }

    public FlightType getFlightType() {
        return flightType;
    }

    public EmergencyStatus getStatus() {
        return status;
    }

    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public int getGroundSpeed() {
        return groundSpeed;
    }

    public void setGroundSpeed(int groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    public AircraftState getState() {
        return state;
    }

    public void setState(AircraftState state) {
        this.state = state;
    }

    // This is what the HoldingPattern uses to decide who lands first.
    // Returns -1 if the current aircraft should come before the other aircraft.
    // Returns 0 if they are equal in priority.
    // Returns 1 if the current aircraft should come after the other aircraft.
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
