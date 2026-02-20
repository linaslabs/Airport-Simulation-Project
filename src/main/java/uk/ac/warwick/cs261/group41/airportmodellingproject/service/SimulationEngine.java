package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationProgress;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.*;

import java.util.*;

public class SimulationEngine {

    private final SimulationConfig config;
    private Airport airport;
    private AircraftGenerator generator;
    private EventManager manager;
    private EventLogger eventLogger;
    private Statistics stats;
    private volatile int currentTick;
    private int durationTicks;

    public SimulationEngine(SimulationConfig config) {
        this.config = config;
        this.durationTicks = config.getDuration();
    }

    public void initialiseSimulation() {

        this.stats = new Statistics();

        Random random = new Random(config.getSeed());

        this.airport = new Airport("SimulationAirport", this.config.getRunwaySettings(), this.config.getMaxWaitTime(), 3, this.stats);

        this.eventLogger = new EventLogger();
        // Pass EventLogger, Airport and Statistics and random into the EventManager (Sprint 2)
        this.manager = new EventManager();

        // Instantiate AircraftGenerator, pass it the random seed
        this.generator = new AircraftGenerator(random, config.getInboundRate(), config.getOutboundRate(), "SimulationAirport");

        this.generator.initialiseSchedules(this.durationTicks);

    }

    public synchronized boolean performTick() {
        // Call event managers process scheduled events
        System.out.println("Start of perform tick, tick number: " + currentTick);
        System.out.println("Duration: " + durationTicks);

        // Get all inbound aircraft to be generated in the current tick and accept inbound into airport
        List<Aircraft> inboundAircraft = this.generator.getInboundForTick(this.currentTick);
        for (Aircraft aircraft : inboundAircraft) {
            this.airport.acceptInboundAircraft(aircraft);
        }

        // Get all outbound aircraft to be generated in the current tick and accept outbound into airport
        List<Aircraft> outboundAircraft = this.generator.getOutboundForTick(this.currentTick);
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
        System.out.println("Simulation progress: " + (double) this.currentTick / this.durationTicks);
        return new SimulationProgress((double) this.currentTick / this.durationTicks);
    }

    // Currently not to be used, getSimulationProgress takes its place for initial sprints
//    public StatisticsSummary getFinalSummary() {
//        return null;
//    }


//    public List<SimulationEvent> getEventLog() {
//
//    }


//    public void triggerRunwayEvent() {
//
//    }

//    public void triggerAircraftEmergency() {
//
//    }


    public int getCurrentTick() {
        return currentTick;
    }
    public int getDurationTicks() {
        return durationTicks;
    }

    public SimulationConfig getConfig() { return this.config; }

    public Statistics getStats() { return this.stats; }





}
