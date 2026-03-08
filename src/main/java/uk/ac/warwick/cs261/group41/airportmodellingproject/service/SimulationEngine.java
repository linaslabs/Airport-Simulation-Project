package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.*;


import java.util.*;

public class SimulationEngine {

    private static final Logger log = LoggerFactory.getLogger(SimulationEngine.class);

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

        if (this.config.getAutomaticGenerationEnabled()){
            log.info("--- Automatic Event Generation is ENABLED ---");
            log.info("- Runway Inspection Rate:      {}", this.config.getRunwayInspectionRate());
            log.info("- Snow Clearance Rate:         {}", this.config.getSnowClearanceRate());
            log.info("- Equipment Failure Rate:      {}", this.config.getEquipmentFailureRate());
            log.info("- Mechanical Failure Rate:     {}", this.config.getMechanicalFailureRate());
            log.info("- Passenger Health Issue Rate: {}", this.config.getPassengerHealthIssueRate());
            log.info("---------------------------------------------");
        }

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

        if (this.config.getAutomaticGenerationEnabled()){
            this.eventManager.generateRandomEventsForTick(currentTick, this.config.getRunwayInspectionRate(),
                    this.config.getSnowClearanceRate(), this.config.getEquipmentFailureRate(),
                    this.config.getMechanicalFailureRate(), this.config.getPassengerHealthIssueRate());
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
        return new SimulationProgress(this.currentTick, (double) this.currentTick / this.durationTicks);
    }

    public SimulationSnapshot getSimulationSnapshot(){
        // Get all holding aircraft
        List<HoldingAircraftDTO> holdingDTOs = new ArrayList<>();
        for (Aircraft aircraft : airport.getHoldingPattern().getAircraftInQueue()) {
            holdingDTOs.add(new HoldingAircraftDTO(aircraft.getCallsign(), aircraft.getFuel(), aircraft.getStatus()));
        }

        // Get all take off aircraft
        List<TakeOffAircraftDTO> takeoffDTOs = new ArrayList<>();
        for (Aircraft aircraft : airport.getTakeOffQueue().getAircraftInQueue()) {
            takeoffDTOs.add(new TakeOffAircraftDTO(aircraft.getCallsign(), aircraft.getEntryTick(), aircraft.getState()));
        }

        // Get all runway information
        List<RunwayDTO> runwayDTOs = new ArrayList<>();
        for (Runway runway : airport.getRunways()) {
            String callsign = (runway.getCurrentAircraft() != null) ? runway.getCurrentAircraft().getCallsign() : null;
            runwayDTOs.add(new RunwayDTO(runway.getRunwayID(), runway.getStatus(), runway.getMode(), callsign, runway.getOccupiedUntil()));
        }

        // Calculate progress as a double
        double progress = (double) this.currentTick / this.durationTicks;

        // Return the compiled snapshot
        return new SimulationSnapshot(
                this.currentTick,
                progress,
                stats.getRollingAvgHoldingTime(),
                stats.getRollingAvgWaitTime(),
                stats.getMaxWaitTime(),
                stats.getMaxTakeOffDelay(),
                stats.getRollingAvgTakeOffDelay(),
                stats.getMaxHoldingSize(),
                stats.getMaxHoldingTime(),
                stats.getMaxArrivalDelay(),
                stats.getRollingAvgArrivalDelay(),
                stats.getMaxTakeOffQueueSize(),
                holdingDTOs.size(),
                takeoffDTOs.size(),
                stats.getTotalAircraftLanded(),
                stats.getTotalAircraftDeparted(),
                stats.getDiversionCount(),
                stats.getCancellationCount(),
                runwayDTOs,
                holdingDTOs,
                takeoffDTOs
        );
    }

    public StatisticsSummary getStatistics() {
        return stats.generateSummary(durationTicks);
    }


    public EventLogger getEventLog() { return this.eventLogger; }

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