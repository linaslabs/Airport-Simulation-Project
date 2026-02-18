package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Airport;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Runway;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.StatisticsGenerator;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.TakeOffQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationEngine {

    private final SimulationConfig config;
    private Airport airport;
    private AircraftGenerator generator;
    private EventManager manager;
    private EventLogger eventLogger;
    private StatisticsGenerator statisticsGenerator;
    private int currentTick;
    private int durationTicks;

    public SimulationEngine(SimulationConfig config, int durationTicks) {
        this.config = config;
        this.durationTicks = durationTicks;
    }

    public void initialiseSimulation() {

        this.statisticsGenerator = new StatisticsGenerator();

        // Pass maxWaitTime, runwaySettings, and Statistics to Airport
        // Its constructor will instantiate the queues and the runways
        this.airport = new Airport();

        this.eventLogger = new EventLogger();
        // Pass EventLogger, Airport and Statistics into the EventManager
        this.manager = new EventManager();

        // Instantiate AircraftGenerator, pass it the random seed

    }

    public boolean performTick() {
        return true;
    }

    public double getProgress() {
        // This will be used for the simple real-time progress update view.
        return 0.0;
    }

    public void stop() {
        // Handles simulation shutdown logic.
    }
}
