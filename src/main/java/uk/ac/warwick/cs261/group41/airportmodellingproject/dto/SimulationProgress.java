package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

/**
 * Data transfer object representing the current progress of a simulation.
 * Contains the current tick and percentage completion for progress bar updates.
 */
public class SimulationProgress {
    /** The current simulation tick */
    private final Integer currentTick;
    
    /** The percentage of simulation completed (0-100) */
    private final double progressPercent;

    /**
     * Constructs a SimulationProgress with tick and percentage.
     *
     * @param currentTick the current simulation tick
     * @param progressPercent the completion percentage
     */
    public SimulationProgress(int currentTick, double progressPercent) {
        this.currentTick = currentTick;
        this.progressPercent = progressPercent;
    }

    /**
     * Gets the current tick.
     *
     * @return the current simulation tick
     */
    public int getCurrentTick() {
        return currentTick;
    }

    /**
     * Gets the progress percentage.
     *
     * @return the completion percentage
     */
    public double getProgressPercent() {
        return progressPercent;
    }
}
