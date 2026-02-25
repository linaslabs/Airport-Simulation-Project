package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.SimulationEvent;

public class RunwayEvent extends SimulationEvent {
    @Min(value = 1, message = "Runway ID must be 1 or greater")
    private final int runwayID;

    private final RunwayStatus runwayStatus;
    private final RunwayMode runwayMode;

    @NotNull(message = "Duration must be provided or -1 for infinite")
    private final int duration;

    @JsonCreator
    public RunwayEvent(@JsonProperty("runwayID") int runwayID,
                       @JsonProperty("status") RunwayStatus status,
                       @JsonProperty("mode") RunwayMode mode,
                       @JsonProperty("tick") int tick,
                       @JsonProperty("duration") Integer duration) {
        super(tick);
        this.runwayID = runwayID;
        this.runwayStatus = status;
        this.runwayMode = mode;
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

    public int getDuration() {
        return this.duration;
    }
}
