package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationProgress;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.StatisticsSummary;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.*;

import java.util.*;

public class SimulationEngine {

    private final SimulationConfig config;
    private Airport airport;
    private AircraftGenerator generator;
    private EventManager manager;
    private EventLogger eventLogger;
    private Statistics stats;
    private int currentTick;
    private int durationTicks;

    public SimulationEngine(SimulationConfig config, int durationTicks) {
        this.config = config;
        this.durationTicks = durationTicks;
    }

    public void initialiseSimulation() {

        this.stats = new Statistics();

        Random random = new Random(config.getSeed());
        // Pass maxWaitTime, runwaySettings, and Statistics to Airport
        // Its constructor will instantiate the queues and the runways
        this.airport = new Airport("SimulationAirport", this.config.getRunwaySettings(), this.config.getMaxWaitTime(), 3);

        this.eventLogger = new EventLogger();
        // Pass EventLogger, Airport and Statistics and random into the EventManager
        this.manager = new EventManager();

        // Instantiate AircraftGenerator, pass it the random seed
        this.generator = new AircraftGenerator(random, config.getInboundRate(), config.getOutboundRate(), "SimulationAirport");

    }

    public boolean performTick() {
        // Call event managers process scheduled events

        // Get all inbound aircraft to be generated in the current tick and accept inbound into airport
        List<Aircraft> inboundAircraft = this.generator.getInboundForTick(this.currentTick);
        for (Aircraft aircraft : inboundAircraft) {
            this.airport.acceptInboundAircraft(aircraft);
        }

        // Get all outbound aircraft to be generated in the current tick and accept outbound into airport
        List<Aircraft> outboundAircraft = this.generator.getInboundForTick(this.currentTick);
        for (Aircraft aircraft : outboundAircraft) {
            this.airport.acceptOutboundAircraft(aircraft);
        }

        // Assign airport runways
        this.airport.assignRunways(this.currentTick);

        // Update airport queues
        this.airport.updateQueues(this.currentTick);

        // Increment to next tick
        this.currentTick++;

        // If current tick is equal to the end tick, return false (no longer continue ticks)
        return this.currentTick != this.durationTicks;

    }

    public SimulationProgress getSimulationProgress() {
        return new SimulationProgress((double) this.currentTick / this.durationTicks);
    }

    // Currently not to be used, getSimulationProgress takes its place for initial sprints
//    public StatisticsSummary getFinalSummary() {
//        return null;
//    }


//    public List<SimulationEvent> getEventLog() {
//
//    }

    public SimulationConfig getConfig() { return this.config; }

//    public void triggerRunwayEvent() {
//
//    }

//    public void triggerAircraftEmergency() {
//
//    }

    // getters and setters

}
