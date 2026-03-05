package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventLogger;

import java.util.Date;

// Adds name and date attribute to a SimulationResult object for permanent storage.
public class SimulationResultSaved extends SimulationResult{
    private String simulationName;
    private Date dateExecuted;

    // Empty constructor required for Jackson to rebuild this object from JSON.
    public SimulationResultSaved() {
        super();
    }

    public SimulationResultSaved(SimulationConfig config, StatisticsSummary stats, EventLogger log, String simulationName, Date dateExecuted) {
        super(config, stats, log);
        this.simulationName = simulationName;
        this.dateExecuted = dateExecuted;
    }

    public String getSimulationName() {
        return simulationName;
    }

    public Date getDateExecuted() {
        return dateExecuted;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    public void setDateExecuted(Date dateExecuted) {
        this.dateExecuted = dateExecuted;
    }
}
