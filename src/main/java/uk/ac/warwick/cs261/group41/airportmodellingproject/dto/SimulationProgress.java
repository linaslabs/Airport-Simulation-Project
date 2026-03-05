package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SimulationProgress {
    private final Integer currentTick;
    private final double progressPercent;

    public SimulationProgress(int currentTick, double progressPercent) {
        this.currentTick = currentTick;
        this.progressPercent = progressPercent;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public double getProgressPercent() {
        return progressPercent;
    }
}
