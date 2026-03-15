package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventManager;

import java.util.*;

/**
 * Represents the airport in the simulation, managing runways, queues, and aircraft operations.
 * Coordinates landing and takeoff operations, handles runway assignments, and tracks statistics.
 * Acts as the central hub connecting the holding pattern, takeoff queue, and runways.
 */
public class Airport {
    /** Name of this airport */
    private final String airportName;
    
    /** Statistics tracker for simulation metrics */
    private final Statistics stats;
    
    /** Map of runway IDs to Runway objects */
    private final Map<Integer, Runway> runways;
    
    /** Queue for arriving aircraft waiting to land */
    private final HoldingPattern holdingPattern;
    
    /** Queue for departing aircraft waiting to take off */
    private final TakeOffQueue takeOffQueue;
    
    /** Time in ticks that a runway is occupied during landing/takeoff */
    private final int runwayOccupationTime;

    /**
     * Constructs a new Airport with the specified configuration.
     *
     * @param airportName name of the airport
     * @param runways list of runway configurations
     * @param maxWaitTime maximum wait time before aircraft are cancelled
     * @param runwayOccupationTime time a runway is occupied per operation
     * @param stats statistics tracker for the simulation
     */
    public Airport(String airportName, List<RunwayConfig> runways, int maxWaitTime, int runwayOccupationTime, Statistics stats) {
        this.airportName = airportName; // This isn't actually used, but it might in future, and I feel like it should have this attribute.
        this.stats = stats;
        holdingPattern = new HoldingPattern();
        takeOffQueue = new TakeOffQueue(maxWaitTime);
        this.runwayOccupationTime = runwayOccupationTime;

        // Loop through the runway configurations set by the user, and instantiate the runways into a linked map (so order of adding is order of searching)
        this.runways = new LinkedHashMap<>();
        for (RunwayConfig runwayConfig : runways) {
            int runwayId = runwayConfig.getRunwayID();

            // Make sure that all runways are unique
            if (this.runways.containsKey(runwayId)) {
                throw new IllegalArgumentException("Duplicate runway ID in configuration: " + runwayId);
            }

            this.runways.put(runwayId, new Runway(runwayId, runwayConfig.getMode(), runwayConfig.getStatus()));
        }
    }

    /**
     * Accepts an inbound aircraft and adds it to the holding pattern.
     * Records the holding pattern size for statistics.
     *
     * @param aircraft the arriving aircraft to add
     */
    public void acceptInboundAircraft(Aircraft aircraft) {
        holdingPattern.addAircraft(aircraft);

        stats.recordHoldingSize(holdingPattern.getSize());
    }

    /**
     * Accepts an outbound aircraft and adds it to the takeoff queue.
     * Records the queue size for statistics.
     *
     * @param aircraft the departing aircraft to add
     */
    public void acceptOutboundAircraft(Aircraft aircraft) {

        takeOffQueue.addAircraft(aircraft);
        stats.recordTakeOffQueueSize(takeOffQueue.getSize());
    }

    /**
     * Assigns aircraft to available runways based on mode and priority.
     * Handles emergency aircraft first, then dedicated runways, then mixed mode.
     *
     * @param currentTick the current simulation tick
     */
    public void assignRunways(int currentTick) {
        // Clear all runways where the planes have finished their operations
        for (Runway runway : runways.values()) {
            runway.update(currentTick);
        }

        // Emergency aircraft will be at the front of the holding pattern.
        // However, because of mixed mode runways, we still need to check if there are any emergency aircraft in
        // the holding pattern.

        // Because all these checks are occurring in the same tick, we need to get all the available runways
        // once at the start and remove runways from our available list when we assign to runways in this function.
        List<Runway> landingRunways = getAvailableRunwaysOfMode(RunwayMode.LANDING, currentTick);
        List<Runway> takeOffRunways = getAvailableRunwaysOfMode(RunwayMode.TAKEOFF, currentTick);
        List<Runway> mixedRunways = getAvailableRunwaysOfMode(RunwayMode.MIXED, currentTick);

        // First check if there are emergency aircraft in the holding pattern.
        List<Aircraft> emergencies = holdingPattern.getEmergencyAircraft(); // These are sorted in descending order of severity.
        List<Runway> landingCompatibleRunways = new ArrayList<>(landingRunways);
        landingCompatibleRunways.addAll(mixedRunways);

        for (Runway runway : landingCompatibleRunways) {
            if (!emergencies.isEmpty()) {
                Aircraft priorityAircraft = emergencies.remove(0);

                // Remove from holding pattern and call landing process.
                holdingPattern.removeAircraft(priorityAircraft);
                landAircraft(runway, priorityAircraft, currentTick);

                // Must remove this runway from the other local lists so it isn't used again.
                landingRunways.remove(runway);
                mixedRunways.remove(runway);
            }
            else {
                // If there are no (more) emergencies to assign, then break out the loop and assign regular aircraft.
                break;
            }
        }

        // Then for dedicated LANDING runways, assign planes to land.
        for (Runway runway : landingRunways) {
            Optional<Aircraft> maybeAircraft = holdingPattern.getNextAircraft();

            if (maybeAircraft.isPresent()) {
                Aircraft currentAircraft = maybeAircraft.get();
                landAircraft(runway, currentAircraft, currentTick);
            }
            else {
                break; // No more planes to land, so stop looking at runways for landing.
            }
        }

        // Then for dedicated TAKEOFF runways, assign planes to takeoff.
        for (Runway runway : takeOffRunways) {
            Optional<Aircraft> maybeAircraft = takeOffQueue.getNextAircraft();

            if (maybeAircraft.isPresent()) {
                Aircraft currentAircraft = maybeAircraft.get();
                takeOffAircraft(runway, currentAircraft, currentTick);
            }
            else {
                break; // No more planes to land, so stop looking at runways for landing.
            }
        }

        // Finally, alternate assignment of planes to the mixed mode runways.
        // Note, ensure that when the mixed mode runways are initialised, their lastAircraftType is alternated.
        for (Runway runway : mixedRunways) {
            Optional<Aircraft> maybeTakeOffAircraft = takeOffQueue.peekNextAircraft();
            Optional<Aircraft> maybeLandingAircraft = holdingPattern.peekNextAircraft();
            if (maybeTakeOffAircraft.isPresent() && maybeLandingAircraft.isPresent()) {
                // If both queues are not empty, decide what to allocate based on the previous type of flight landed.
                FlightType lastAircraftType = runway.getLastAircraftType();
                if (lastAircraftType == FlightType.ARRIVAL) {
                    // If the last aircraft this runway took was a landing, it should now take a take-off.
                    Aircraft aircraft = takeOffQueue.getNextAircraft().get();
                    takeOffAircraft(runway, aircraft, currentTick);
                }
                else {
                    // If the last aircraft this runway took was a take-off, it should now take a landing.
                    Aircraft aircraft = holdingPattern.getNextAircraft().get();
                    landAircraft(runway, aircraft, currentTick);
                }
            }
            else if (maybeTakeOffAircraft.isPresent()) {
                // If only the take-off queue is not empty, allocate an aircraft to take-off.
                Aircraft aircraft = takeOffQueue.getNextAircraft().get();
                takeOffAircraft(runway, aircraft, currentTick);
            }
            else if (maybeLandingAircraft.isPresent()) {
                // If only the holding pattern is not empty, allocate an aircraft to land.
                Aircraft aircraft = holdingPattern.getNextAircraft().get();
                landAircraft(runway, aircraft, currentTick);
            }
            else {
                break; // Both queues are empty so stop iterating through runways.
            }
        }
    }

    /**
     * Updates both the holding pattern and takeoff queue for the current tick.
     * Handles fuel consumption, diversions, and cancellations.
     *
     * @param currentTick the current simulation tick
     */
    public void updateQueues(int currentTick) {
        holdingPattern.update(currentTick);
        stats.recordHoldingSize(holdingPattern.getSize());

        takeOffQueue.update(currentTick);
        stats.recordTakeOffQueueSize(takeOffQueue.getSize());
    }

    /**
     * Processes an aircraft landing on the specified runway.
     * Updates runway occupation and records landing statistics.
     *
     * @param runway the runway to land on
     * @param aircraft the aircraft landing
     * @param currentTick the current simulation tick
     */
    private void landAircraft(Runway runway, Aircraft aircraft, int currentTick) {
        runway.assignAircraft(aircraft, currentTick + runwayOccupationTime);

        // Update the statistics for this landing.
        stats.recordLanding();
        stats.recordHoldingTime(Math.max(0, currentTick - aircraft.getEntryTick()));
        stats.recordArrivalDelay(Math.max(0, currentTick - aircraft.getScheduledTick()));
    }

    /**
     * Processes an aircraft takeoff from the specified runway.
     * Updates runway occupation and records takeoff statistics.
     *
     * @param runway the runway to take off from
     * @param aircraft the aircraft taking off
     * @param currentTick the current simulation tick
     */
    private void takeOffAircraft(Runway runway, Aircraft aircraft, int currentTick) {
        runway.assignAircraft(aircraft, currentTick + runwayOccupationTime);

        // Update the statistics for this take-off.
        stats.recordTakeOff();
        stats.recordTakeOffWaitTime(Math.max(0, currentTick - aircraft.getEntryTick()));
        stats.recordTakeOffDelay(Math.max(0, currentTick - aircraft.getScheduledTick()));
    }

    /**
     * Gets all available runways of the specified mode.
     *
     * @param mode the runway mode to filter by
     * @param currentTick the current simulation tick
     * @return list of available runways matching the mode
     */
    public List<Runway> getAvailableRunwaysOfMode(RunwayMode mode, int currentTick) {
        List<Runway> output = new ArrayList<>();
        for (Runway runway : runways.values()) {
            if (runway.getMode() == mode && runway.isAvailable(currentTick)) {
                output.add(runway);
            }
        }
        return output;
    }

    /**
     * Updates a runway's status and/or mode.
     * Only changes values that are not null.
     *
     * @param runwayID the ID of the runway to update
     * @param status the new status, or null to keep current
     * @param mode the new mode, or null to keep current
     * @param lockSource the event source locking this runway
     * @throws IllegalArgumentException if runwayID doesn't exist
     */
    public void updateRunway(int runwayID, RunwayStatus status, RunwayMode mode, EventSource lockSource) {
        Runway runway = runways.get(runwayID);
        if (runway != null) {
            if (status != null) {
                runway.setStatus(status);
            }
            if (mode != null) {
                runway.setMode(mode);
            }

            // Set the locked state of the runway (if it is having an event performed on it, lock it so the user can't change it)
            runway.setLockSource(lockSource);
        }
        else {
            throw new IllegalArgumentException("Unknown runwayID: " + runwayID);
        }
    }

    /**
     * Updates the emergency status of an aircraft in the holding pattern.
     *
     * @param callsign the callsign of the aircraft to update
     * @param status the new emergency status
     * @param emergencySource the source of the emergency event
     */
    public void updateAircraftStatus(String callsign, EmergencyStatus status, EventSource emergencySource) {
        holdingPattern.updateAircraftStatus(callsign, status, emergencySource);
    }

    /**
     * Gets a random aircraft callsign from the holding pattern.
     *
     * @param random the random number generator
     * @return a random aircraft callsign, or null if none available
     */
    public String getRandomHoldingAircraft(Random random) {
        return holdingPattern.getRandomAircraft(random);
    }

    /**
     * Gets a snapshot of a runway's current configuration.
     *
     * @param runwayID the ID of the runway
     * @return the runway configuration snapshot
     * @throws IllegalArgumentException if runwayID doesn't exist
     */
    public RunwayConfig getRunwaySnapshot(int runwayID) {
        Runway runway = runways.get(runwayID);
        if (runway != null) {
            return new RunwayConfig(runway.getRunwayID(), runway.getStatus(), runway.getMode());
        }
        else {
            throw new IllegalArgumentException("Unknown runwayID: " + runwayID);
        }
    }

    /**
     * Sets the event manager for both queues to enable event reporting.
     *
     * @param eventManager the event manager to set
     */
    public void setEventManager(EventManager eventManager) {
        this.holdingPattern.setEventManager(eventManager);
        this.takeOffQueue.setEventManager(eventManager);
    }

    /**
     * Gets the holding pattern queue.
     *
     * @return the holding pattern
     */
    public HoldingPattern getHoldingPattern() { return this.holdingPattern; }

    /**
     * Gets the takeoff queue.
     *
     * @return the takeoff queue
     */
    public TakeOffQueue  getTakeOffQueue() { return this.takeOffQueue; }

    /**
     * Gets an unmodifiable collection of all runways.
     *
     * @return unmodifiable collection of runways
     */
    public Collection<Runway> getRunways() { return Collections.unmodifiableCollection(this.runways.values()); }
}
