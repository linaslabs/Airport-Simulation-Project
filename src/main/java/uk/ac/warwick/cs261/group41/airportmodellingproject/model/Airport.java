package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

import java.util.*;

public class Airport {
    private final String airportName;
    private final Statistics stats;
    private final Map<Integer, Runway> runways;
    private final HoldingPattern holdingPattern;
    private final TakeOffQueue takeOffQueue;
    private final int runwayOccupationTime;

    // I don't think we can actually use the AircraftQueue abstraction here becuase the take-off queue and
    // holding pattern have unique methods and require being constructed differently.
    // So, we would need objects of each type anyway so there's no point in using the Aircraft queue.
    public Airport(String airportName, Map<Integer, Runway> runways, int maxWaitTime, int runwayOccupationTime) {
        this.airportName = airportName; // This isn't actually used, but it might in future, and I feel like it should have this attribute.
        stats = new Statistics();
        this.runways = runways;
        holdingPattern = new HoldingPattern(stats);
        takeOffQueue = new TakeOffQueue(maxWaitTime, stats);
        this.runwayOccupationTime = runwayOccupationTime;
    }

    // First part of Airport in performTick cycle is to call acceptInbound and acceptOutbound functions.
    // These assign inbound/outbound aircraft to the queues.

    public void acceptInboundAircraft(Aircraft aircraft) {
        // I think this code is not what we need. Instead we should just add them to the queue.
        // Otherwise, if we have a mixed mode runway, with 1 plane spawning in arrival and departure
        // each tick + runwayOccupationTime, the runway will only let the landing aircraft use the runway,
        // so the takeoff queue will just build up.
//        if (holdingPattern.isEmpty()) {
//            Runway possibleRunway = getFirstAvailableRunway(currentTick);
//            if (possibleRunway != null) {
//                possibleRunway.assignAircraft(aircraft, currentTick + runwayOccupationTime);
//            }
//        }
//        else {
//            holdingPattern.addAircraft(aircraft);
//        }
        holdingPattern.addAircraft(aircraft);
    }

    public void acceptOutboundAircraft(Aircraft aircraft) {
        takeOffQueue.addAircraft(aircraft);
    }

    // Second part of Airport in performTick cycle is to assign aircraft to runways and update the queues.

    public void assignRunways(int currentTick) {
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

    public void updateQueues(int currentTick) {
        holdingPattern.update(currentTick);
        stats.recordHoldingSize(holdingPattern.getSize());

        takeOffQueue.update(currentTick);
        stats.recordTakeOffQueueSize(takeOffQueue.getSize());
    }

    // Helper function for process to land aircraft.
    private void landAircraft(Runway runway, Aircraft aircraft, int currentTick) {
        runway.assignAircraft(aircraft, currentTick + runwayOccupationTime);

        // Update the statistics for this landing.
        stats.recordLanding();
        stats.recordHoldingTime(currentTick - aircraft.getEntryTick());
        stats.recordArrivalDelay(currentTick - aircraft.getScheduledTick());
    }

    // Helper function for process to take-off aircraft.
    private void takeOffAircraft(Runway runway, Aircraft aircraft, int currentTick) {
        runway.assignAircraft(aircraft, currentTick + runwayOccupationTime);

        // Update the statistics for this take-off.
        stats.recordTakeOff();
        stats.recordTakeOffWaitTime(currentTick - aircraft.getEntryTick());
        stats.recordTakeOffDelay(currentTick - aircraft.getScheduledTick());
    }

    // Helper function to get available runways of a certain mode.
    public List<Runway> getAvailableRunwaysOfMode(RunwayMode mode, int currentTick) {
        List<Runway> output = new ArrayList<>();
        for (Runway runway : runways.values()) {
            if (runway.getMode() == mode && runway.isAvailable(currentTick)) {
                output.add(runway);
            }
        }
        return output;
    }

    // Changes the given runway's status or mode.
    // Throws exception if runwayID doesn't exist in runways Map.
    // Only changes status or mode if these parameters are not null in the function call.
    public void updateRunway(int runwayID, RunwayStatus status, RunwayMode mode) {
        Runway runway = runways.get(runwayID);
        if (runway != null) {
            if (status != null) {
                runway.setStatus(status);
            }
            if (mode != null) {
                runway.setMode(mode);
            }
        }
        else {
            throw new IllegalArgumentException("Unknown runwayID: " + runwayID);
        }
    }

    public void updateAircraftStatus(String callsign, EmergencyStatus status) {
        holdingPattern.updateAircraftStatus(callsign, status);
    }

    public String getRandomHoldingAircraft(Random random) {
        return holdingPattern.getRandomAircraft(random);
    }
}
