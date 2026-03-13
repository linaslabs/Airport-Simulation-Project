package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventManager;

import java.util.*;

/**
 * Represents a queue for aircraft waiting to take off from the airport.
 * Implements the AircraftQueue interface and manages departure aircraft in FIFO order.
 * Aircraft that exceed the maximum wait time are automatically cancelled.
 */
public class TakeOffQueue implements AircraftQueue {

    /** The underlying queue data structure holding aircraft waiting for takeoff */
    private Deque<Aircraft> queue;
    
    /** Maximum time (in ticks) an aircraft can wait before being cancelled */
    private int maxWaitTime;
    
    /** Event manager for reporting cancellations and other events */
    private EventManager eventManager;

    /**
     * Constructs a new TakeOffQueue with the specified maximum wait time.
     *
     * @param maxWaitTime the maximum number of ticks an aircraft can wait before cancellation
     */
    public TakeOffQueue(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
        this.queue = new ArrayDeque<>();
    }

    /**
     * Adds an aircraft to the end of the takeoff queue.
     *
     * @param aircraft the aircraft to add to the queue
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        queue.add(aircraft);
    }

    /**
     * Returns the next aircraft in the queue without removing it.
     *
     * @return an Optional containing the next aircraft, or empty if queue is empty
     */
    @Override
    public Optional<Aircraft> peekNextAircraft() {
        return Optional.ofNullable(queue.peek());
    }

    /**
     * Retrieves and removes the next aircraft from the front of the queue.
     *
     * @return an Optional containing the next aircraft, or empty if queue is empty
     */
    @Override
    public Optional<Aircraft> getNextAircraft() {
        return Optional.ofNullable(queue.poll());
    }

    /**
     * Removes a specific aircraft from the queue.
     *
     * @param aircraft the aircraft to remove from the queue
     */
    @Override
    public void removeAircraft(Aircraft aircraft) {
        queue.remove(aircraft);
    }

    /**
     * Returns the current number of aircraft in the takeoff queue.
     *
     * @return the size of the queue
     */
    @Override
    public int getSize() {
        return queue.size();
    }

    /**
     * Checks if the takeoff queue is empty.
     *
     * @return true if the queue contains no aircraft, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Updates the queue state for the current simulation tick.
     * Checks all aircraft wait times and cancels any that have exceeded the maximum wait time.
     * Cancelled aircraft are removed from the queue and reported to the event manager.
     *
     * @param currentTick the current simulation tick
     */
    @Override
    public void update(int currentTick) {
        Iterator<Aircraft> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Aircraft aircraft = iterator.next();
            int waitTime = currentTick - aircraft.getEntryTick();
            if (waitTime >= maxWaitTime) {
                aircraft.setState(AircraftState.CANCELLED);
                iterator.remove();
                // Report cancellation to the event manager
                this.eventManager.reportCancellation(aircraft.getCallsign(), currentTick);
            }
        }
    }

    /**
     * Sets the event manager for reporting cancellations and other events.
     *
     * @param eventManager the event manager to use
     */
    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Gets the maximum wait time before aircraft are cancelled.
     *
     * @return the maximum wait time in ticks
     */
    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     * Sets the maximum wait time before aircraft are cancelled.
     *
     * @param maxWaitTime the maximum wait time in ticks
     */
    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    /**
     * Returns a copy of all aircraft currently in the queue.
     *
     * @return a list containing all aircraft in the queue
     */
    public List<Aircraft> getAircraftInQueue() { return new ArrayList<>(this.queue); }
}
