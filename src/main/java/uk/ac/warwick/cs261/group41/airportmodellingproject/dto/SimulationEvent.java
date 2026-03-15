package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract base class for all simulation events.
 * Provides common tick timing for events and supports Jackson polymorphic
 * deserialization for AircraftEvent and RunwayEvent subtypes.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(AircraftEvent.class),
        @JsonSubTypes.Type(RunwayEvent.class)
})
public abstract class SimulationEvent {
    /** The simulation tick when this event occurs */
    protected int tick;

    /**
     * Default constructor required for Jackson deserialization.
     */
    public SimulationEvent() {
    }

    /**
     * Constructs a SimulationEvent at the specified tick.
     *
     * @param tick the simulation tick when the event occurs
     */
    public SimulationEvent(int tick) {
        this.tick = tick;
    }

    /**
     * Gets the tick when this event occurs.
     *
     * @return the simulation tick
     */
    public int getTick() {
        return tick;
    }

    /**
     * Sets the tick when this event occurs.
     *
     * @param tick the simulation tick
     */
    public void setTick(int tick) {
        this.tick = tick;
    }
}
