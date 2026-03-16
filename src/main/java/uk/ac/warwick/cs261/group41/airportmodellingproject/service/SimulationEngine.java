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

/**
 * Controls the tick-by-tick execution of the airport simulation.
 *
 * Responsible for initialising and coordinating all core simulation components,
 * including the airport, aircraft generator, event manager, and statistics tracker.
 * Each tick processes scheduled events, generates inbound and outbound aircraft,
 * optionally rolls for random events, assigns runways, and updates queues.
 *
 * Random event probabilities are calculated by multiplying baseline rates against
 * user-configured multipliers. See RateReferences.txt in this package for a full
 * breakdown of how baseline rates were derived.
 */
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
    // See the file "RateReferences.txt" in the same "service" package as SimulationEngine.java
    // for references and a breakdown of how these rates were calculated.
    private final double emergencyMechanicalBaselinePerTick = 0.000000135;
    private final double emergencyHealthBaselinePerTick = 0.000000556;
    private final double failureInspectionBaselinePerTick = 0.001388;
    private final double failureSnowBaselinePerTick = 0.00000951;
    private final double failureEquipmentBaselinePerTick = 0.00000278;

    /**
     * Constructor to initialise the simulation engine object
     * @param config the simulation configuration object for the simulation
     */
    public SimulationEngine(@NonNull SimulationConfig config) {
        this.config = config;
        this.durationTicks = config.getDuration();
        this.random = new Random(config.getSeed());
    }

    /**
     * Method to start the simulation by initialising the key simulation objects in order according to the simulation configuration
     */
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
        this.airport.setEventManager(this.eventManager);
        this.generator = new AircraftGenerator(this.random, config.getInboundRate(), config.getOutboundRate(), "SimulationAirport");
        this.generator.initialiseSchedules(this.durationTicks);
    }

    /**
     * Method to orchestrate each tick in the simulation
     * @return a boolean value which is true if the simulation is still alive, is false if the simulation has stopped
     */
    public synchronized boolean performTick() {
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

        // If the user has allowed random event generation, then verify the probabilities and call the event manager to generate the events
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

        this.airport.assignRunways(this.currentTick);
        this.airport.updateQueues(this.currentTick);
        this.currentTick++;

        // If current tick is equal to the end tick, return false (no longer continue)
        return this.currentTick != this.durationTicks;

    }

    /**
     * Triggers a manual runway event, called from the simulation controllers that receive UI requests
     */
    public void triggerRunwayEvent(int runwayID, RunwayStatus status, RunwayMode mode) {
        if (this.eventManager != null) {
            this.eventManager.triggerRunwayEvent(runwayID, status, mode, this.currentTick, -1, false, true);
        }
    }

    /**
     * Triggers a manual aircraft emergency event, called from the simulation controllers that receive UI requests
     */
    public void triggerAircraftEmergency(String callsign, EmergencyStatus status) {
        if (this.eventManager != null) {
            this.eventManager.triggerAircraftEmergency(callsign, status, this.currentTick, false);
        }
    }

    /**
     * Function to return a simulation progress object detailing the current simulation progress
     * Previously used in testing, is redundant now that progress is sent within the simulation snapshot but kept for future testing
     * @return simulation progress object with current tick and the simulation progress as a percentage
     */
    public SimulationProgress getSimulationProgress() {
        return new SimulationProgress(this.currentTick, (double) this.currentTick / this.durationTicks);
    }

    /**
     * Key method to compile the current state of the simulation and send as a snapshot object to the frontend
     * @return simulation snapshot object containing necessary simulation state for the UI to update
     */
    public SimulationSnapshot getSimulationSnapshot(){
        // Get all holding aircraft and package them up in data transfer objects
        List<HoldingAircraftDTO> holdingDTOs = new ArrayList<>();
        for (Aircraft aircraft : airport.getHoldingPattern().getAircraftInQueue()) {
            holdingDTOs.add(new HoldingAircraftDTO(aircraft.getCallsign(), aircraft.getFuel(), aircraft.getStatus(), aircraft.getEmergencySource()));
        }

        // Get all take off aircraft and package them up in data transfer objects
        List<TakeOffAircraftDTO> takeoffDTOs = new ArrayList<>();
        for (Aircraft aircraft : airport.getTakeOffQueue().getAircraftInQueue()) {
            takeoffDTOs.add(new TakeOffAircraftDTO(aircraft.getCallsign(), aircraft.getEntryTick(), aircraft.getState()));
        }

        // Get all runway information and package them up into data transfer objects
        List<RunwayDTO> runwayDTOs = new ArrayList<>();
        for (Runway runway : airport.getRunways()) {
            String callsign = (runway.getCurrentAircraft() != null) ? runway.getCurrentAircraft().getCallsign() : null;
            runwayDTOs.add(new RunwayDTO(runway.getRunwayID(), runway.getStatus(), runway.getMode(), callsign, runway.getOccupiedUntil(), runway.getLockSource()));
        }

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

    /**
     * Function to return the compiled summary of the current statistics object including generation of averages from running totals
     * @return statistics summary object containing summary of the statistics object for the simulation
     */
    public StatisticsSummary getStatistics() {
        return stats.generateSummary(durationTicks);
    }

    public EventLogger getEventLog() { return this.eventLogger; }

    public Airport getAirport() { return this.airport; }

    public int getCurrentTick() { return currentTick; }

    public int getDurationTicks() { return durationTicks; }

    public SimulationConfig getConfig() { return this.config; }

    public Statistics getStats() { return this.stats; }

    /**
     * Function to return the clamped probability of a given value
     * Used in random event generation to make sure rates and multipliers do not exceed 1 (100%)
     * @param rateName name of the rate being clamped, passed solely for logging purposes
     * @param value multiplier (set by the user) * baseline rate for the specific event
     * @return a clamped value that ensures the value passed in does not exceed 1
     */
    private double clampProbability(String rateName, double value) {
        double clampedValue = Math.max(0, Math.min(1, value));

        if (Double.compare(value, clampedValue) != 0) {
            log.warn("Clamped {} probability from {} to {}", rateName, value, clampedValue);
        }

        return clampedValue;
    }

}