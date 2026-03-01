package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.SimulationEvent;

public class RunwayEvent extends SimulationEvent {
    @Min(value = 0, message = "Runway ID must be 0 or greater")
    private final int runwayID;

    private final RunwayStatus runwayStatus;
    private final RunwayMode runwayMode;

    @NotNull(message = "Runway event type is required")
    private final RunwayEventType type;

    @NotNull(message = "Duration must be provided or -1 for infinite")
    private final int duration;

    @JsonCreator
    public RunwayEvent(@JsonProperty("tick") int tick,
                       @JsonProperty("runwayID") int runwayID,
                       @JsonProperty("status") RunwayStatus status,
                       @JsonProperty("mode") RunwayMode mode,
                       @JsonProperty("type") RunwayEventType type,
                       @JsonProperty("duration") Integer duration) {
        super(tick);
        this.runwayID = runwayID;
        this.runwayStatus = status;
        this.runwayMode = mode;
        this.type = type;
        this.duration = duration;
    }

    public int getRunwayID() {
        return this.runwayID;
    }

    public RunwayStatus getRunwayStatus() {
        return this.runwayStatus;
    }

    public RunwayMode getRunwayMode() {
        return this.runwayMode;
    }

    public RunwayEventType getType() {
        return this.type;
    }


    public int getDuration() {
        return this.duration;
    }
}
