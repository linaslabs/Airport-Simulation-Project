package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import java.util.Date;

// Simple wrapper around a simulation's configuration and statistical results.
public class SimulationResult {
    private SimulationConfig config;
    private StatisticsSummary stats;

    // Empty constructor required for Jackson to rebuild this object from JSON.
    public SimulationResult() {}

    public SimulationResult(SimulationConfig config, StatisticsSummary stats) {
        this.config = config;
        this.stats = stats;
    }

    public SimulationConfig getConfig() {
        return config;
    }

    public StatisticsSummary getStats() {
        return stats;
    }

    public void setConfig(SimulationConfig config) {
        this.config = config;
    }

    public void setStats(StatisticsSummary stats) {
        this.stats = stats;
    }
}
