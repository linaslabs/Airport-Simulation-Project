package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

public abstract class SimulationEvent {
    protected int tick;

    public SimulationEvent(int tick) {
        this.tick = tick;
    }

    public int getTick() { return tick; }
}
