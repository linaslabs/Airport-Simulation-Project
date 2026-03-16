package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EventSource;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventManager;

import java.util.*;

/**
 * Represents the holding pattern for arriving aircraft waiting to land.
 * Implements a priority queue where aircraft are ordered by emergency status,
 * fuel level, and entry time. Handles fuel consumption, diversions, and altitude management.
 */
public class HoldingPattern implements AircraftQueue{
    /** Priority queue ordering aircraft by emergency status, fuel, and entry time */
    private final PriorityQueue<Aircraft> queue;
    
    /** Minimum fuel level before aircraft must divert */
    private final int minFuelLevel = 10;
    
    /** Fuel level threshold that triggers a fuel emergency status */
    private final int emergencyFuelLevel = 15;
    
    /** Event manager for reporting diversions and emergencies */
    private EventManager eventManager;

    /**
     * Constructs a new empty HoldingPattern.
     */
    public HoldingPattern() {
        this.queue = new PriorityQueue<>();
    }

    /**
     * Adds an aircraft to the holding pattern.
     * Aircraft is automatically ordered by priority.
     *
     * @param aircraft the aircraft to add
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        queue.add(aircraft);
    }

    /**
     * Retrieves and removes the highest priority aircraft from the holding pattern.
     *
     * @return an Optional containing the next aircraft, or empty if pattern is empty
     */
    @Override
    public Optional<Aircraft> getNextAircraft(){
        return Optional.ofNullable(queue.poll());
    }

    /**
     * Returns the highest priority aircraft without removing it.
     *
     * @return an Optional containing the next aircraft, or empty if pattern is empty
     */
    @Override
    public Optional<Aircraft> peekNextAircraft(){
        return Optional.ofNullable(queue.peek());
    }

    /**
     * Removes a specific aircraft from the holding pattern.
     *
     * @param aircraft the aircraft to remove
     */
    @Override
    public void removeAircraft(Aircraft aircraft) {
        queue.remove(aircraft);
    }

    /**
     * Returns the number of aircraft in the holding pattern.
     *
     * @return the size of the holding pattern
     */
    @Override
    public int getSize() {
        return queue.size();
    }

    /**
     * Checks if the holding pattern is empty.
     *
     * @return true if no aircraft are in the pattern, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Updates all aircraft in the holding pattern for the current tick.
     * Consumes fuel, triggers diversions for low fuel, and sets fuel emergencies.
     *
     * @param currentTick the current simulation tick
     * @throws IllegalStateException if EventManager has not been set
     */
    @Override
    public void update(int currentTick) {
        if (eventManager == null) {
            throw new IllegalStateException("EventManager has not been set for HoldingPattern");
        }

        // Need a temporary list of aircraft that should be re-prioritised due to low fuel (will re-prioritise after this iterator)
        List<String> newFuelEmergencies = new ArrayList<>();

        Iterator<Aircraft> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Aircraft aircraft = iterator.next();
            aircraft.consumeFuel(1.0);

            if (aircraft.getFuel() <= minFuelLevel) {
                aircraft.setState(AircraftState.DIVERTED);
                iterator.remove();
                // Report diversion to the event manager
                eventManager.reportDiversion(aircraft.getCallsign(), currentTick);
            } else if (aircraft.getFuel() <= emergencyFuelLevel && aircraft.getStatus() == EmergencyStatus.NONE) {
                // Add this aircraft to the list of fuel emergencies to reprioritise later (only if it doesn't already have an emergency)
                newFuelEmergencies.add(aircraft.getCallsign());
            }

        }

        // Remove and reinsert the planes after the first iterator has finished
        for (String callsign : newFuelEmergencies) {
            updateAircraftStatus(callsign, EmergencyStatus.FUEL, EventSource.NONE);
            this.eventManager.reportAircraftEmergency(callsign, EmergencyStatus.FUEL, currentTick);
        }

        // after updating all aircraft, for those still in the queue update their altitudes.
        updateAltitudes();
    }

    /**
     * Updates the altitudes of all aircraft based on their position in the queue.
     * Higher priority aircraft are assigned lower altitudes (closer to landing).
     */
    public void updateAltitudes() {
        List<Aircraft> sortedAircraft = new ArrayList<>();
        PriorityQueue<Aircraft> queueCopy = new PriorityQueue<>(this.queue);

        // Get a sorted copy of the aircraft.
        while (!queueCopy.isEmpty()) {
            sortedAircraft.add(queueCopy.poll());
        }

        // Assign altitudes based on their position in the holding pattern.
        // Altitude = (index + 1) * 1000
        for (int i = 0; i < sortedAircraft.size(); i++) {
            int newAltitude = (i + 1) * 1000;
            sortedAircraft.get(i).setAltitude(newAltitude);
        }
    }

    /**
     * Updates the emergency status of an aircraft in the holding pattern.
     * Removes and reinserts the aircraft to maintain correct priority ordering.
     *
     * @param callsign the callsign of the aircraft to update
     * @param newStatus the new emergency status
     * @param emergencySource the source of the emergency event
     */
    public void updateAircraftStatus(String callsign, EmergencyStatus newStatus, EventSource emergencySource) {
        Aircraft foundAircraft = null;

        // Perform a linear search to find the aircraft.
        Iterator<Aircraft> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Aircraft aircraft = iterator.next();
            if (aircraft.getCallsign().equals(callsign)) {
                foundAircraft = aircraft;
                iterator.remove(); // Remove it from the queue.
                break;
            }
        }

        // Change status of aircraft and reinsert.
        if (foundAircraft != null) {
            foundAircraft.setStatus(newStatus);
            foundAircraft.setEmergencySource(emergencySource); // Set event source of this status change
            queue.add(foundAircraft);

            // Queue order likely changed so update altitudes again.
            updateAltitudes();
        }
        else {
            System.out.println("Aircraft " + callsign + " not found in Holding Pattern.");
        }
    }

    /**
     * Gets a random aircraft callsign from those without emergency status.
     *
     * @param random the random number generator
     * @return a random aircraft callsign, or null if none eligible
     */
    public String getRandomAircraft(Random random) {
        // Filter out aircraft with emergency statuses other than None.
        List<Aircraft> eligibleAircraft = new ArrayList<>();
        for (Aircraft aircraft : queue) {
            if (aircraft.getStatus() == EmergencyStatus.NONE) {
                eligibleAircraft.add(aircraft);
            }
        }

        // If all aircraft have an emergency status other than None, return null.
        if (eligibleAircraft.isEmpty()) {
            return null;
        }

        // If not, generate a random index in the eligibleAircraft list.
        int index = random.nextInt(eligibleAircraft.size());
        return eligibleAircraft.get(index).getCallsign();
    }

    /**
     * Gets all aircraft with emergency status, sorted by severity.
     *
     * @return list of emergency aircraft, highest severity first
     */
    public List<Aircraft> getEmergencyAircraft() {
        List<Aircraft> output = new ArrayList<>();
        for (Aircraft aircraft : queue) {
            if (aircraft.getStatus() != EmergencyStatus.NONE) {
                output.add(aircraft);
            }
        }
        Collections.sort(output);
        return output;
    }

    /**
     * Sets the event manager for reporting diversions and emergencies.
     *
     * @param eventManager the event manager to set
     */
    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Gets all aircraft in the holding pattern in priority order.
     *
     * @return list of aircraft sorted by priority
     */
    public List<Aircraft> getAircraftInQueue() {
        // extract elements using poll() to guarantee priority order (basically pops from the top)
        List<Aircraft> sortedAircraft = new ArrayList<>();
        PriorityQueue<Aircraft> queueCopy = new PriorityQueue<>(this.queue);

        while (!queueCopy.isEmpty()) {
            sortedAircraft.add(queueCopy.poll());
        }

        return sortedAircraft;
    }
}
