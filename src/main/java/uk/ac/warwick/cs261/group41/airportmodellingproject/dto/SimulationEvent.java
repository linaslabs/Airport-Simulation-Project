package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(AircraftEvent.class),
        @JsonSubTypes.Type(RunwayEvent.class)
})
public abstract class SimulationEvent {
    protected int tick;

    // Default constructor required for Jackson.
    public SimulationEvent() {
    }

    // Parameterised constructor used in the constructors of children, called by
    // EventManager.
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
