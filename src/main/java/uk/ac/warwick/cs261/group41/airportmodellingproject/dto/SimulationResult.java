package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventLogger;

// Simple wrapper around a simulation's configuration, statistical results and event log.
public class SimulationResult {
    private SimulationConfig config;
    private StatisticsSummary stats;
    private EventLogger log;

    // Empty constructor required for Jackson to rebuild this object from JSON.
    public SimulationResult() {}

    public SimulationResult(SimulationConfig config, StatisticsSummary stats, EventLogger log) {
        this.config = config;
        this.stats = stats;
        this.log = log;
    }

    public SimulationConfig getConfig() {
        return config;
    }

    public StatisticsSummary getStats() {
        return stats;
    }

    public EventLogger getLog() {
        return log;
    }

    public void setConfig(SimulationConfig config) {
        this.config = config;
    }

    public void setStats(StatisticsSummary stats) {
        this.stats = stats;
    }

    public void setLog(EventLogger log) {
        this.log = log;
    }
}
