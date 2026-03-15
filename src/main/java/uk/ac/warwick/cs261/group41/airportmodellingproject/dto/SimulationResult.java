package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventLogger;

/**
 * Data transfer object wrapping a simulation's configuration, statistical results, and event log.
 * Represents the complete output of a simulation run for display and storage.
 */
public class SimulationResult {
    /** The configuration used for this simulation */
    private SimulationConfig config;
    
    /** The statistical summary of simulation results */
    private StatisticsSummary stats;
    
    /** The event log recording all simulation events */
    private EventLogger log;

    /**
     * Default constructor required for Jackson deserialization.
     */
    public SimulationResult() {}

    /**
     * Constructs a SimulationResult with all components.
     *
     * @param config the simulation configuration
     * @param stats the statistics summary
     * @param log the event log
     */
    public SimulationResult(SimulationConfig config, StatisticsSummary stats, EventLogger log) {
        this.config = config;
        this.stats = stats;
        this.log = log;
    }

    /**
     * Gets the simulation configuration.
     *
     * @return the configuration
     */
    public SimulationConfig getConfig() {
        return config;
    }

    /**
     * Gets the statistics summary.
     *
     * @return the statistics
     */
    public StatisticsSummary getStats() {
        return stats;
    }

    /**
     * Gets the event log.
     *
     * @return the event log
     */
    public EventLogger getLog() {
        return log;
    }

    /**
     * Sets the simulation configuration.
     *
     * @param config the configuration
     */
    public void setConfig(SimulationConfig config) {
        this.config = config;
    }

    /**
     * Sets the statistics summary.
     *
     * @param stats the statistics
     */
    public void setStats(StatisticsSummary stats) {
        this.stats = stats;
    }

    /**
     * Sets the event log.
     *
     * @param log the event log
     */
    public void setLog(EventLogger log) {
        this.log = log;
    }
}
