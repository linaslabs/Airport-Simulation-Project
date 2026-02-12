package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SimulationProgress {
    private final double progressPercentage;

    @JsonCreator
    public SimulationProgress(@JsonProperty("progressPercentage") double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }
}
