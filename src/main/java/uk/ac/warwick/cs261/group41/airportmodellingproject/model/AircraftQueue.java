package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import java.util.Optional; // We use this for safer null handling when queues are emtpy (which may be common).

/**
 * Interface defining the contract for aircraft queue implementations in the simulation.
 * Provides common operations for managing aircraft waiting for runway access.
 * Implementations include HoldingPattern (for arrivals) and TakeOffQueue (for departures).
 */
public interface AircraftQueue {
    /**
     * Adds an aircraft to the queue.
     *
     * @param aircraft the aircraft to add
     */
    void addAircraft(Aircraft aircraft); // Adds an aircraft to the queue.
    /**
     * Removes and returns the next aircraft to be processed.
     *
     * @return an Optional containing the next aircraft, or empty if queue is empty
     */
    Optional<Aircraft> getNextAircraft(); // Removes and returns the next aircraft to be processed.
    /**
     * Returns but does not remove the next aircraft to be processed.
     *
     * @return an Optional containing the next aircraft, or empty if queue is empty
     */
    Optional<Aircraft> peekNextAircraft(); // Returns but does not remove the next aircraft to be processed.
    /**
     * Removes a specific aircraft from the queue.
     * Used for diverted or cancelled aircraft. This operation is O(n).
     *
     * @param aircraft the aircraft to remove
     */
    void removeAircraft(Aircraft aircraft); // Removes an aircraft from the queue, needed to remove diverted or cancelled aircraft, this will be O(n).
    /**
     * Returns the current number of aircraft in the queue.
     * Used for UI display and statistics tracking.
     *
     * @return the queue size
     */
    int getSize(); // Needed for UI and results tracking.
    /**
     * Checks if the queue is empty.
     *
     * @return true if the queue contains no aircraft, false otherwise
     */
    boolean isEmpty(); // Possibly needed at the end of a simulation if we decide to stop simulations when the queues are empty.
    /**
     * Updates the queue state for the current simulation tick.
     * Handles fuel consumption, wait time checks, and triggers diversions/cancellations.
     *
     * @param currentTick the current simulation tick
     */
    void update(int currentTick); // The key function to update and check fuel and wait times.
}