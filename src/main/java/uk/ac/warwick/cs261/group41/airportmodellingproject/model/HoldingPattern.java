package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventManager;

import java.util.*;

public class HoldingPattern implements AircraftQueue{
    private final PriorityQueue<Aircraft> queue;
    private int minFuelLevel = 10;
    // Maybe add an emergency fuel level, e.g. 20 minutes, below which the plane goes into EmergencyStatus.FUEL.
    private final Statistics stats;
    private final EventManager eventManager;

    public HoldingPattern(Statistics stats, EventManager eventManager) {
        this.queue = new PriorityQueue<>();
        this.stats = stats;
        this.eventManager = eventManager;
    }

    @Override
    public void addAircraft(Aircraft aircraft) {
        queue.add(aircraft);
    }

    @Override
    public Optional<Aircraft> getNextAircraft(){
        return Optional.ofNullable(queue.poll());
    }

    @Override
    public Optional<Aircraft> peekNextAircraft(){
        return Optional.ofNullable(queue.peek());
    }

    @Override
    public void removeAircraft(Aircraft aircraft) {
        queue.remove(aircraft);
    }

    @Override
    public int getSize() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public void update(int currentTick) {
        Iterator<Aircraft> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Aircraft aircraft = iterator.next();
            aircraft.consumeFuel(1.0);

            if (aircraft.getFuel() <= minFuelLevel) {
                aircraft.setState(AircraftState.DIVERTED);
                iterator.remove();
                // Handle logging for diverted aircraft here.
                stats.recordDiversion();
                // Report diversion
                eventManager.reportDiversion(aircraft.getCallsign(), currentTick);
            }

        }
        // after updating all aircraft, for those still in the queue update their altitudes.
        updateAltitudes();
    }

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

    // Note to maintain the order in the priority queue, when we want to change the status of an aircraft,
    // we must remove it, make changes, then reinsert it, so it is ordered into the correct place.
    public void updateAircraftStatus(String callsign, EmergencyStatus newStatus) {
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
            queue.add(foundAircraft);

            // Queue order likely changed so update altitudes again.
            updateAltitudes();
        }
        else {
            System.out.println("Aircraft " + callsign + " not found in Holding Pattern.");
        }
    }

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

    // Returns a list of aircraft, sorted with highest severity first.
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
}
