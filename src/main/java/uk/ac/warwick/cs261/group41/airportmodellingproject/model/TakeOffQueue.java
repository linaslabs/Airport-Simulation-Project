package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventManager;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;

public class TakeOffQueue implements AircraftQueue {

    private Deque<Aircraft> queue;
    private int maxWaitTime;
    private final Statistics stats;
    private final EventManager eventManager;

    public TakeOffQueue(int maxWaitTime, Statistics stats, EventManager eventManager) {
        this.maxWaitTime = maxWaitTime;
        this.queue = new ArrayDeque<>();
        this.stats = stats;
        this.eventManager = eventManager;
    }

    @Override
    public void addAircraft(Aircraft aircraft) {
        queue.add(aircraft);
    }

    @Override
    public Optional<Aircraft> peekNextAircraft() {
        return Optional.ofNullable(queue.peek());
    }

    @Override
    public Optional<Aircraft> getNextAircraft() {
        return Optional.ofNullable(queue.poll());
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
            int waitTime = currentTick - aircraft.getEntryTick();
            if (waitTime >= maxWaitTime) {
                aircraft.setState(AircraftState.CANCELLED);
                iterator.remove();
                // Log cancellation.
                stats.recordCancellation();
                // Report diversion
                this.eventManager.reportCancellation(aircraft.getCallsign(), currentTick);
            }
        }
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }
}
