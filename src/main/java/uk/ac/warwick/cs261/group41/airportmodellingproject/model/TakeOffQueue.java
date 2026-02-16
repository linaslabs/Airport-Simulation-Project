package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class TakeOffQueue {

    private Deque<Aircraft> queue;
    private int maxWaitTime;

    public TakeOffQueue(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
        this.queue = new ArrayDeque<>();
    }

    public void enqueue(Aircraft a) {
        queue.add(a);
    }

    public Aircraft peek() {
        return queue.peek();
    }

    public Aircraft dequeue() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void update(int currentTick) {
        Iterator<Aircraft> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Aircraft aircraft = iterator.next();
            int waitTime = currentTick - aircraft.getEntryTick();
            if (waitTime >= maxWaitTime) {
                aircraft.setState(AircraftState.CANCELLED);
                iterator.remove();
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
