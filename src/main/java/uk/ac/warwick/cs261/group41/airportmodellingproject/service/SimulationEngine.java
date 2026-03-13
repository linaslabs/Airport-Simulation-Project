package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;
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

    // Failure rates for random aircraft emergencies and runway failures
    private final double emergencyMechanicalBaselinePerTick = 0.000008;
    private final double emergencyHealthBaselinePerTick = 0.00008;
    private final double failureInspectionBaselinePerTick = 0.001388;
    private final double failureSnowBaselinePerTick = 0.00000476;
    private final double failureEquipmentBaselinePerTick = 0.00000278;

    public SimulationEngine(@NonNull SimulationConfig config) {
        this.config = config;
        this.durationTicks = config.getDuration();
        this.random = new Random(config.getSeed());
    }

    public void initialiseSimulation() {

        if (this.config.getAutomaticGenerationEnabled()){
            log.info("--- Automatic Event Generation is ENABLED ---");
            log.info("- Runway Inspection Multiplier:      {}x", this.config.getRunwayInspectionMultiplier());
            log.info("- Snow Clearance Multiplier:         {}x", this.config.getSnowClearanceMultiplier());
            log.info("- Equipment Failure Multiplier:      {}x", this.config.getEquipmentFailureMultiplier());
            log.info("- Mechanical Failure Multiplier:     {}x", this.config.getMechanicalFailureMultiplier());
            log.info("- Passenger Health Issue Multiplier: {}x", this.config.getPassengerHealthIssueMultiplier());
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

          // Calculate probabilities and clamp to between 0 and 1 for safety
          double mechanicalProbability = clampProbability(
              "mechanicalFailure",
              this.emergencyMechanicalBaselinePerTick * this.config.getMechanicalFailureMultiplier()
          );
          double healthProbability = clampProbability(
              "passengerHealthIssue",
              this.emergencyHealthBaselinePerTick  * this.config.getPassengerHealthIssueMultiplier()
          );
          double inspectionProbability = clampProbability(
              "runwayInspection",
              this.failureInspectionBaselinePerTick * this.config.getRunwayInspectionMultiplier()
          );
          double snowProbability = clampProbability(
              "snowClearance",
              this.failureSnowBaselinePerTick * this.config.getSnowClearanceMultiplier()
          );
          double equipmentProbability = clampProbability(
              "equipmentFailure",
              this.failureEquipmentBaselinePerTick * this.config.getEquipmentFailureMultiplier()
          );

          this.eventManager.generateRandomEventsForTick(
              currentTick,
              inspectionProbability,
              snowProbability,
              equipmentProbability,
              mechanicalProbability,
              healthProbability
          );
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

    // Triggers manual runway event from the UI
    public void triggerRunwayEvent(int runwayID, RunwayStatus status, RunwayMode mode) {
        if (this.eventManager != null) {
            this.eventManager.triggerRunwayEvent(runwayID, status, mode, this.currentTick, -1, false, true);
        }
    }

    // Triggers a manual aircraft emergency from the UI
    public void triggerAircraftEmergency(String callsign, EmergencyStatus status) {
        if (this.eventManager != null) {
            this.eventManager.triggerAircraftEmergency(callsign, status, this.currentTick, false);
        }
    }

    public SimulationProgress getSimulationProgress() {
        return new SimulationProgress(this.currentTick, (double) this.currentTick / this.durationTicks);
    }

    public SimulationSnapshot getSimulationSnapshot(){
        // Get all holding aircraft
        List<HoldingAircraftDTO> holdingDTOs = new ArrayList<>();
        for (Aircraft aircraft : airport.getHoldingPattern().getAircraftInQueue()) {
            holdingDTOs.add(new HoldingAircraftDTO(aircraft.getCallsign(), aircraft.getFuel(), aircraft.getStatus(), aircraft.getEmergencySource()));
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
            runwayDTOs.add(new RunwayDTO(runway.getRunwayID(), runway.getStatus(), runway.getMode(), callsign, runway.getOccupiedUntil(), runway.getLockSource()));
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
                config.getMaxWaitTime(),
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

    // Just to make sure that probabilities do not exceed 1 (100%)
    private double clampProbability(String rateName, double value) {
        double clampedValue = Math.max(0, Math.min(1, value));

        if (Double.compare(value, clampedValue) != 0) {
            log.warn("Clamped {} probability from {} to {}", rateName, value, clampedValue);
        }

        return clampedValue;
    }

}