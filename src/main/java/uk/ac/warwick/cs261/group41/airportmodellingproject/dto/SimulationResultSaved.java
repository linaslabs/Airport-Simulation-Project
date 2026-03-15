package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventLogger;

import java.util.Date;

/**
 * Extension of SimulationResult that adds name and date attributes for permanent storage.
 * Used when saving simulation results to JSON files for later retrieval and comparison.
 */
public class SimulationResultSaved extends SimulationResult{
    /** The user-provided name for this saved simulation */
    private String simulationName;
    
    /** The date and time when the simulation was executed */
    private Date dateExecuted;

    /**
     * Default constructor required for Jackson deserialization.
     */
    public SimulationResultSaved() {
        super();
    }

    /**
     * Constructs a SimulationResultSaved with all components.
     *
     * @param config the simulation configuration
     * @param stats the statistics summary
     * @param log the event log
     * @param simulationName the name for this saved result
     * @param dateExecuted the execution date
     */
    public SimulationResultSaved(SimulationConfig config, StatisticsSummary stats, EventLogger log, String simulationName, Date dateExecuted) {
        super(config, stats, log);
        this.simulationName = simulationName;
        this.dateExecuted = dateExecuted;
    }

    /**
     * Gets the simulation name.
     *
     * @return the simulation name
     */
    public String getSimulationName() {
        return simulationName;
    }

    /**
     * Gets the execution date.
     *
     * @return the date executed
     */
    public Date getDateExecuted() {
        return dateExecuted;
    }

    /**
     * Sets the simulation name.
     *
     * @param simulationName the simulation name
     */
    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    /**
     * Sets the execution date.
     *
     * @param dateExecuted the date executed
     */
    public void setDateExecuted(Date dateExecuted) {
        this.dateExecuted = dateExecuted;
    }
}
