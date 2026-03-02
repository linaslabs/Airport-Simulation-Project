package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Range;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

import java.util.*;


public class SimulationConfig {

    // Note we use Integer and not int, so if the user inputs an empty value, it is turned to null instead of 0.
    // The default values here are displayed when the configuration view first loads.
    // Could also provide a reset to defaults button in the configuration screen?

    // We had runway count in the UML class diagram, but since we have a list of the runway DTOs, we don't need this too as we just need to use the size of the list.
    // @Range(min = 1, max = 10, message = "Number of runways must be between 1 and 10 (inclusive)")
    // private int runwayCount = 1;

    // This means the UI can allow the user to remove all the runways while they are editing,
    // however if they start the simulation with no runways, it will return an error.
    @NotNull(message = "At least one runway is required")
    @Size(min = 1, max = 10, message = "Simulation requires between 1 and 10 runways.")
    @Valid // This runs bean validation on each component of the list.
    private List<RunwayConfig> runwaySettings = new ArrayList<>(List.of(new RunwayConfig(1, RunwayStatus.AVAILABLE, RunwayMode.MIXED)));

    @NotNull(message = "Inbound rate is required")
    @Range(min = 0, max = 100, message = "Inbound rate must be 0 and 100.")
    private Integer inboundRate = 15;

    @NotNull(message = "Outbound rate is required")
    @Range(min = 0, max = 100, message = "Outbound rate must be 0 and 100.")
    private Integer outboundRate = 15;

    @NotNull(message = "Max wait time is required")
    @Min(value = 1, message = "Max wait time must be greater than 0.")
    private Integer maxWaitTime = 30;

    // This isn't in class diagram, but it would be good to include in configuration page.
    @NotNull(message = "Tick time is required")
    @Min(value = 100, message = "Tick time must be at least 100ms to prevent system overload.")
    @Max(value = 10000, message = "Tick time cannont exceed 10,000ms (10 seconds).")
    private Integer tickTime = 1000;

    @NotNull(message = "Simulation duration is required")
    @Range(min = 60, max = 1440, message = "Simulation duration must be between 60 and 1440 minutes.")
    private Integer duration = 420;

    private Boolean isAutomaticGenerationEnabled;

    @DecimalMin(value = "0.0", message = "Mechanical failure rate cannot be less than 0.0")
    @DecimalMax(value = "0.1", message = "Mechanical failure rate cannot exceed 0.1")
    private Double mechanicalFailureRate = 0.0;

    @DecimalMin(value = "0.0", message = "Passenger health issue rate cannot be less than 0.0")
    @DecimalMax(value = "0.1", message = "Passenger health issue rate cannot exceed 0.1")
    private Double passengerHealthIssueRate = 0.0;

    @DecimalMin(value = "0.0", message = "Runway inspection rate cannot be less than 0.0")
    @DecimalMax(value = "0.1", message = "Runway inspection rate cannot exceed 0.1")
    private Double runwayInspectionRate = 0.0;

    @DecimalMin(value = "0.0", message = "Snow clearance rate cannot be less than 0.0")
    @DecimalMax(value = "0.1", message = "Snow clearance rate cannot exceed 0.1")
    private Double snowClearanceRate = 0.0;

    @DecimalMin(value = "0.0", message = "Equipment failure rate cannot be less than 0.0")
    @DecimalMax(value = "0.1", message = "Equipment failure rate cannot exceed 0.1")
    private Double equipmentFailureRate = 0.0;

    // Added seed as a Long object.
    @NotNull(message = "A random seed is required. Please enter a value or randomly generate one.")
    private Long seed;

    @Valid
    private Map<Integer, List<RunwayEvent>> scheduledRunwayEvents = new HashMap<>();

    @Valid
    private Map<Integer, List<AircraftEvent>> scheduledAircraftEvents = new HashMap<>();

    // Default constructor required for Jackson to turn the JSON into this object.
    public SimulationConfig() {}

    // Parameterised constructor exclusively used for testing purposes.
    public SimulationConfig(List<RunwayConfig> runwaySettings, Integer inboundRate, Integer outboundRate, Integer maxWaitTime,
                            Integer duration, Integer tickTime, Boolean isAutomaticGenerationEnabled, Double mechanicalFailureRate, Double passengerHealthIssueRate,
                            Double runwayInspectionRate, Double snowClearanceRate, Double equipmentFailureRate, Long seed,
                            Map<Integer, List<RunwayEvent>> scheduledRunwayEvents,
                            Map<Integer, List<AircraftEvent>> scheduledAircraftEvents) {
        // Copy the runway list in case the original list is modified.
        this.runwaySettings = (runwaySettings != null) ? new ArrayList<>(runwaySettings) : new ArrayList<>();
        this.inboundRate = inboundRate;
        this.outboundRate = outboundRate;
        this.maxWaitTime = maxWaitTime;
        this.tickTime = tickTime;
        this.duration = duration;

        // For statistical modelling
        this.isAutomaticGenerationEnabled = isAutomaticGenerationEnabled;
        this.mechanicalFailureRate = mechanicalFailureRate;
        this.passengerHealthIssueRate = passengerHealthIssueRate;
        this.runwayInspectionRate = runwayInspectionRate;
        this.snowClearanceRate = snowClearanceRate;
        this.equipmentFailureRate = equipmentFailureRate;

        this.seed = seed;
        this.scheduledRunwayEvents = (scheduledRunwayEvents != null) ? new HashMap<>(scheduledRunwayEvents) : new HashMap<>();
        this.scheduledAircraftEvents = (scheduledAircraftEvents != null) ? new HashMap<>(scheduledAircraftEvents) : new HashMap<>();
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

    public boolean getIsAutomaticGenerationEnabled() { return this.isAutomaticGenerationEnabled; }

    public void setIsAutomaticGenerationEnabled(boolean automaticGenerationEnabled) {this.isAutomaticGenerationEnabled = automaticGenerationEnabled; }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public String getSimulationID() { return simulationID; }

    public void setSimulationID(String simulationID) { this.simulationID = simulationID; }

    public Double getMechanicalFailureRate() { return mechanicalFailureRate; }

    public void setMechanicalFailureRate(Double mechanicalFailureRate) { this.mechanicalFailureRate = mechanicalFailureRate; }

    public Double getPassengerHealthIssueRate() { return passengerHealthIssueRate; }

    public void setPassengerHealthIssueRate(Double passengerHealthIssueRate) { this.passengerHealthIssueRate = passengerHealthIssueRate; }

    public Double getRunwayInspectionRate() { return runwayInspectionRate; }

    public void setRunwayInspectionRate(Double runwayInspectionRate) { this.runwayInspectionRate = runwayInspectionRate; }

    public Double getSnowClearanceRate() { return snowClearanceRate; }

    public void setSnowClearanceRate(Double snowClearanceRate) { this.snowClearanceRate = snowClearanceRate; }

    public Double getEquipmentFailureRate() { return equipmentFailureRate; }

    public void setEquipmentFailureRate(Double equipmentFailureRate) { this.equipmentFailureRate = equipmentFailureRate; }
}


