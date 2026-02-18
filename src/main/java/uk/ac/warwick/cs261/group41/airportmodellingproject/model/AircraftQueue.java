package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import java.util.Optional; // We use this for safer null handling when queues are emtpy (which may be common).

public interface AircraftQueue {
    void addAircraft(Aircraft aircraft); // Adds an aircraft to the queue.
    Optional<Aircraft> getNextAircraft(); // Removes and returns the next aircraft to be processed.
    Optional<Aircraft> peekNextAircraft(); // Returns but does not remove the next aircraft to be processed.
    void removeAircraft(Aircraft aircraft); // Removes an aircraft from the queue, needed to remove diverted or cancelled aircraft, this will be O(n).
    int getSize(); // Needed for UI and results tracking.
    boolean isEmpty(); // Possibly needed at the end of a simulation if we decide to stop simulations when the queues are empty.
    void update(int currentTick); // The key function to update and check fuel and wait times.
}