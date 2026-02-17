package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Airport;

public class SimulationEngine implements Runnable {

    private final SimulationConfig config;
    private Airport airport;
    private AircraftGenerator generator;
    private EventManager manager;
    private EventLogger eventLogger;
    private int currentTick;
    private int durationTicks;

    public SimulationEngine(SimulationConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        // This is the code the thread runs, so it will contain the tick loop.
        // Note we need the simulation running on its own thread so the other threads can handle user input etc. while the simulation is running.
    }

    public double getProgress() {
        // This will be used for the simple real-time progress update view.
        return 0.0;
    }

    public void stop() {
        // Handles simulation shutdown logic.
    }
}
