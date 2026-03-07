package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

public abstract class SimulationEvent {
    protected int tick;

    // Default constructor required for Jackson.
    public SimulationEvent() {}

    // Parameterised constructor used in the constructors of children, called by EventManager.
    public SimulationEvent(int tick) {
        this.tick = tick;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }
}
