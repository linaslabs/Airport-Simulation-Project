package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Range;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.SimulationMode;

import java.util.*;

/**
 * Data transfer object containing all configuration parameters for a simulation.
 * Includes runway settings, traffic rates, timing, event multipliers, and scheduled events.
 * Validated using Jakarta Bean Validation annotations.
 */
public class SimulationConfig {

    /** List of runway configurations for the simulation */
    @NotNull(message = "At least one runway is required")
    @Size(min = 1, max = 10, message = "Simulation requires between 1 and 10 runways.")
    @Valid
    private List<RunwayConfig> runwaySettings = new ArrayList<>(List.of(new RunwayConfig(1, RunwayStatus.AVAILABLE, RunwayMode.MIXED)));

    /** Inbound aircraft rate per hour */
    @NotNull(message = "Inbound rate is required")
    @Range(min = 0, max = 100, message = "Inbound rate must be 0 and 100.")
    private Integer inboundRate = 15;

    /** Outbound aircraft rate per hour */
    @NotNull(message = "Outbound rate is required")
    @Range(min = 0, max = 100, message = "Outbound rate must be 0 and 100.")
    private Integer outboundRate = 15;

    /** Maximum wait time before flight cancellation (minutes) */
    @NotNull(message = "Max wait time is required")
    @Min(value = 1, message = "Max wait time must be greater than 0.")
    private Integer maxWaitTime = 30;

    /** Time between simulation ticks in milliseconds */
    @NotNull(message = "Tick time is required")
    @Min(value = 1, message = "Tick time must be at least 1ms to prevent system overload.")
    @Max(value = 10000, message = "Tick time cannot exceed 10,000ms (10 seconds).")
    private Integer tickTime = 10;

    /** Simulation duration in minutes */
    @NotNull(message = "Simulation duration is required")
    @Range(min = 60, max = 1440, message = "Simulation duration must be between 60 and 1440 minutes.")
    private Integer duration = 420;

    /** Whether random events are automatically generated */
    @NotNull(message = "Automatic generation flag is required")
    private Boolean automaticGenerationEnabled = false;

    /** Multiplier for mechanical failure event probability */
    @NotNull(message = "Mechanical failure multiplier is required")
    @DecimalMin(value = "0.0", message = "Mechanical failure multiplier cannot be less than 0.0")
    @DecimalMax(value = "10000.0", message = "Mechanical failure multiplier cannot exceed 10000.0")
    private Double mechanicalFailureMultiplier = 1.0;

    /** Multiplier for passenger health issue event probability */
    @NotNull(message = "Passenger health issue multiplier is required")
    @DecimalMin(value = "0.0", message = "Passenger health issue multiplier cannot be less than 0.0")
    @DecimalMax(value = "10000.0", message = "Passenger health issue multiplier cannot exceed 10000.0")
    private Double passengerHealthIssueMultiplier = 1.0;

    /** Multiplier for runway inspection event probability */
    @NotNull(message = "Runway inspection multiplier is required")
    @DecimalMin(value = "0.0", message = "Runway inspection multiplier cannot be less than 0.0")
    @DecimalMax(value = "10000.0", message = "Runway inspection multiplier cannot exceed 10000.0")
    private Double runwayInspectionMultiplier = 1.0;

    /** Multiplier for snow clearance event probability */
    @NotNull(message = "Snow clearance multiplier is required")
    @DecimalMin(value = "0.0", message = "Snow clearance multiplier cannot be less than 0.0")
    @DecimalMax(value = "10000.0", message = "Snow clearance multiplier cannot exceed 10000.0")
    private Double snowClearanceMultiplier = 1.0;

    /** Multiplier for equipment failure event probability */
    @NotNull(message = "Equipment failure multiplier is required")
    @DecimalMin(value = "0.0", message = "Equipment failure multiplier cannot be less than 0.0")
    @DecimalMax(value = "10000.0", message = "Equipment failure multiplier cannot exceed 10000.0")
    private Double equipmentFailureMultiplier = 1.0;

    /** Random seed for reproducible simulations */
    @NotNull(message = "A random seed is required. Please enter a value or randomly generate one.")
    private Long seed;

    /** Map of tick to scheduled runway events */
    @Valid
    private Map<Integer, List<RunwayEvent>> scheduledRunwayEvents = new HashMap<>();

    /** Map of tick to scheduled aircraft events */
    @Valid
    private Map<Integer, List<AircraftEvent>> scheduledAircraftEvents = new HashMap<>();

    /** The display mode for the simulation */
    private SimulationMode simulationMode;

    /**
     * Default constructor required for Jackson deserialization.
     */
    public SimulationConfig() {}

    /**
     * Constructs a SimulationConfig with all parameters.
     * Used primarily for testing purposes.
     *
     * @param runwaySettings list of runway configurations
     * @param inboundRate inbound rate per hour
     * @param outboundRate outbound rate per hour
     * @param maxWaitTime maximum wait time before cancellation
     * @param duration simulation duration in minutes
     * @param tickTime time between ticks in milliseconds
     * @param automaticGenerationEnabled whether random events are enabled
     * @param mechanicalFailureMultiplier mechanical failure probability multiplier
     * @param passengerHealthIssueMultiplier passenger health issue probability multiplier
     * @param runwayInspectionMultiplier runway inspection probability multiplier
     * @param snowClearanceMultiplier snow clearance probability multiplier
     * @param equipmentFailureMultiplier equipment failure probability multiplier
     * @param seed random seed for reproducibility
     * @param scheduledRunwayEvents map of scheduled runway events
     * @param scheduledAircraftEvents map of scheduled aircraft events
     * @param simulationMode the display mode
     */
    public SimulationConfig(List<RunwayConfig> runwaySettings, Integer inboundRate, Integer outboundRate, Integer maxWaitTime,
                            Integer duration, Integer tickTime, Boolean automaticGenerationEnabled, Double mechanicalFailureMultiplier, Double passengerHealthIssueMultiplier,
                            Double runwayInspectionMultiplier, Double snowClearanceMultiplier, Double equipmentFailureMultiplier, Long seed,
                            Map<Integer, List<RunwayEvent>> scheduledRunwayEvents,
                            Map<Integer, List<AircraftEvent>> scheduledAircraftEvents,
                            SimulationMode simulationMode) {
        // Copy the runway list in case the original list is modified.
        this.runwaySettings = (runwaySettings != null) ? new ArrayList<>(runwaySettings) : new ArrayList<>();
        this.inboundRate = inboundRate;
        this.outboundRate = outboundRate;
        this.maxWaitTime = maxWaitTime;
        this.tickTime = tickTime;
        this.duration = duration;

        // For statistical modelling
        this.automaticGenerationEnabled = automaticGenerationEnabled;
        this.mechanicalFailureMultiplier = mechanicalFailureMultiplier;
        this.passengerHealthIssueMultiplier = passengerHealthIssueMultiplier;
        this.runwayInspectionMultiplier = runwayInspectionMultiplier;
        this.snowClearanceMultiplier = snowClearanceMultiplier;
        this.equipmentFailureMultiplier = equipmentFailureMultiplier;

        this.seed = seed;
        this.scheduledRunwayEvents = (scheduledRunwayEvents != null) ? new HashMap<>(scheduledRunwayEvents) : new HashMap<>();
        this.scheduledAircraftEvents = (scheduledAircraftEvents != null) ? new HashMap<>(scheduledAircraftEvents) : new HashMap<>();

        this.simulationMode = simulationMode;
    }

    public List<RunwayConfig> getRunwaySettings() {
        return runwaySettings;
    }

    public void setRunwaySettings(List<RunwayConfig> runwaySettings) {
        // Defensive copy to ensure the DTO owns its own data
        this.runwaySettings = (runwaySettings != null) ? new ArrayList<>(runwaySettings) : new ArrayList<>();
    }

    public Map<Integer, List<RunwayEvent>> getScheduledRunwayEvents() { return Collections.unmodifiableMap(this.scheduledRunwayEvents); }

    public void setScheduledRunwayEvents(Map<Integer, List<RunwayEvent>> scheduledRunwayEvents) {
        this.scheduledRunwayEvents = (scheduledRunwayEvents != null) ? new HashMap<>(scheduledRunwayEvents) : new HashMap<>();
    }

    public Map<Integer, List<AircraftEvent>> getScheduledAircraftEvents() { return Collections.unmodifiableMap(this.scheduledAircraftEvents); }

    public void setScheduledAircraftEvents(Map<Integer, List<AircraftEvent>> scheduledAircraftEvents) {
        this.scheduledAircraftEvents = (scheduledAircraftEvents != null) ? new HashMap<>(scheduledAircraftEvents) : new HashMap<>();
    }

    public Integer getInboundRate() {
        return inboundRate;
    }

    public void setInboundRate(Integer inboundRate) {
        this.inboundRate = inboundRate;
    }

    public Integer getOutboundRate() {
        return outboundRate;
    }

    public void setOutboundRate(Integer outboundRate) {
        this.outboundRate = outboundRate;
    }

    public Integer getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(Integer maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public Integer getTickTime() {
        return tickTime;
    }

    public void setTickTime(Integer tickTime) {
        this.tickTime = tickTime;
    }

    public Integer getDuration() { return this.duration; }

    public void setDuration(Integer duration) { this.duration = duration; }

    public Boolean getAutomaticGenerationEnabled() { return this.automaticGenerationEnabled; }

    public void setAutomaticGenerationEnabled(boolean automaticGenerationEnabled) {this.automaticGenerationEnabled = automaticGenerationEnabled; }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public Double getMechanicalFailureMultiplier() { return mechanicalFailureMultiplier; }

    public void setMechanicalFailureMultiplier(Double mechanicalFailureMultiplier) { this.mechanicalFailureMultiplier = mechanicalFailureMultiplier; }

    public Double getPassengerHealthIssueMultiplier() { return passengerHealthIssueMultiplier; }

    public void setPassengerHealthIssueMultiplier(Double passengerHealthIssueMultiplier) { this.passengerHealthIssueMultiplier = passengerHealthIssueMultiplier; }

    public Double getRunwayInspectionMultiplier() { return runwayInspectionMultiplier; }

    public void setRunwayInspectionMultiplier(Double runwayInspectionMultiplier) { this.runwayInspectionMultiplier = runwayInspectionMultiplier; }

    public Double getSnowClearanceMultiplier() { return snowClearanceMultiplier; }

    public void setSnowClearanceMultiplier(Double snowClearanceMultiplier) { this.snowClearanceMultiplier = snowClearanceMultiplier; }

    public Double getEquipmentFailureMultiplier() { return equipmentFailureMultiplier; }

    public void setEquipmentFailureMultiplier(Double equipmentFailureMultiplier) { this.equipmentFailureMultiplier = equipmentFailureMultiplier; }

    public SimulationMode getSimulationMode() {
        return simulationMode;
    }

    public void setSimulationMode(SimulationMode simulationMode) {
        this.simulationMode = simulationMode;
    }
}


