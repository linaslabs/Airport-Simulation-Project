package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.springframework.stereotype.Service;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.StatisticsSummary;

// Spring Boot automatically instantiates this Service as a Singleton upon application startup and injects it into the Controllers.
@Service
public class SimulationService {

    private SimulationEngine engine;

    public void startSimulation(SimulationConfig config) {
        this.engine = new SimulationEngine(config);
        // Need some logic here to start a thread and run the simulation on it.
    }

    public StatisticsSummary getStatisticsSummary() {
        // It is the role of the SimulationService to create the DTOs from the SimulationEngine data.
        // The SimulationEngine is not aware of the DTOs.
        return null;
    }

    public void shutdownEngine() {
        if (engine != null) {
            engine.stop();
        }
    }
}
