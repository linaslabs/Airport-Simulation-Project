package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.jspecify.annotations.NonNull;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationProgress;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.StatisticsSummary;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.*;

import java.util.*;

public class SimulationEngine {

    private final SimulationConfig config;
    private Airport airport;
    private AircraftGenerator generator;
    private EventManager eventManager;
    private EventLogger eventLogger;
    private Statistics stats;
    private volatile int currentTick;
    private final int durationTicks;
    private final Random random;

    public SimulationEngine(@NonNull SimulationConfig config) {
        this.config = config;
        this.durationTicks = config.getDuration();
        this.random = new Random(config.getSeed());
    }

    public void initialiseSimulation() {

        this.stats = new Statistics();

        this.airport = new Airport("SimulationAirport", this.config.getRunwaySettings(), this.config.getMaxWaitTime(), 3, this.stats);

        this.eventLogger = new EventLogger();

        this.eventManager = new EventManager(this.eventLogger, this.config.getScheduledRunwayEvents(), this.config.getScheduledAircraftEvents(), this.random, this.airport, this.stats);

        // Give the queues the event manager to access and report diversions and cancellations
        this.airport.setEventManager(this.eventManager);

        this.generator = new AircraftGenerator(this.random, config.getInboundRate(), config.getOutboundRate(), "SimulationAirport");

        this.generator.initialiseSchedules(this.durationTicks);

    }

    public synchronized boolean performTick() {
        // Call event managers process scheduled events

        // Process scheduled events for this tick
        this.eventManager.processScheduledEvents(this.currentTick);

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
        return new SimulationProgress((double) this.currentTick / this.durationTicks);
    }

    // Currently not to be used, getSimulationProgress takes its place for initial sprints
    public StatisticsSummary getFinalSummary() {
        StatisticsSummary summary = stats.generateSummary(durationTicks);
        summary.setSimulationID(this.config.getSimulationID());
        return summary;
    }


    public List<SimulationEvent> getEventLog() { return this.eventLogger.getEventLog(); }

    public Airport getAirport() { return this.airport; }

    public int getCurrentTick() {
        return currentTick;
    }
    public int getDurationTicks() {
        return durationTicks;
    }

    public SimulationConfig getConfig() { return this.config; }

    public Statistics getStats() { return this.stats; }





}
