package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StatisticsSummary {

    // Make the fields final so they are immutable. We don't want the results of a simulation to be edited.
    private final double avgHoldingTime;
    private final int maxHoldingTime;
    private final double avgTakeOffDelay;
    private final double maxTakeOffDelay;
    private final double avgArrivalDelay;
    private final double maxArrivalDelay;
    private final double avgWaitTime;
    private final int maxWaitTime;
    private final int maxHoldingSize;
    private final int maxTakeOffQueueSize;
    private final int diversionCount;
    private final int cancellationCount;
    private final double hourlyThroughput;
    private String simulationID; // Copied from the configuration during results generation.

    // JsonCreator annotation required for Jackson to make JSON from class with finals.
    @JsonCreator
    public StatisticsSummary(
            @JsonProperty("avgHoldingTime") double avgHoldingTime,
            @JsonProperty("maxHoldingTime") int maxHoldingTime,
            @JsonProperty("avgTakeOffDelay") double avgTakeOffDelay,
            @JsonProperty("maxTakeOffDelay") double maxTakeOffDelay,
            @JsonProperty("avgArrivalDelay") double avgArrivalDelay,
            @JsonProperty("maxArrivalDelay") double maxArrivalDelay,
            @JsonProperty("avgWaitTime") double avgWaitTime,
            @JsonProperty("maxWaitTime") int maxWaitTime,
            @JsonProperty("maxHoldingSize") int maxHoldingSize,
            @JsonProperty("maxTakeOffQueueSize") int maxTakeOffQueueSize,
            @JsonProperty("diversionCount") int diversionCount,
            @JsonProperty("cancellationCount") int cancellationCount,
            @JsonProperty("hourlyThroughput") double hourlyThroughput) {

        this.avgHoldingTime = avgHoldingTime;
        this.maxHoldingTime = maxHoldingTime;
        this.avgTakeOffDelay = avgTakeOffDelay;
        this.maxTakeOffDelay = maxTakeOffDelay;
        this.avgArrivalDelay = avgArrivalDelay;
        this.maxArrivalDelay = maxArrivalDelay;
        this.avgWaitTime = avgWaitTime;
        this.maxWaitTime = maxWaitTime;
        this.maxHoldingSize = maxHoldingSize;
        this.maxTakeOffQueueSize = maxTakeOffQueueSize;
        this.diversionCount = diversionCount;
        this.cancellationCount = cancellationCount;
        this.hourlyThroughput = hourlyThroughput;
    }

    // Getters
    public double getAvgHoldingTime() { return avgHoldingTime; }
    public int getMaxHoldingTime() { return maxHoldingTime; }
    public double getAvgTakeOffDelay() { return avgTakeOffDelay; }
    public double getMaxTakeOffDelay() { return maxTakeOffDelay; }
    public double getAvgArrivalDelay() { return avgArrivalDelay; }
    public double getMaxArrivalDelay() { return maxArrivalDelay; }
    public double getAvgWaitTime() { return avgWaitTime; }
    public int getMaxWaitTime() { return maxWaitTime; }
    public int getMaxHoldingSize() { return maxHoldingSize; }
    public int getMaxTakeOffQueueSize() { return maxTakeOffQueueSize; }
    public int getDiversionCount() { return diversionCount; }
    public int getCancellationCount() { return cancellationCount; }
    public double getHourlyThroughput() { return hourlyThroughput; }
    public String getSimulationID() { return simulationID; }
    public void setSimulationID(String simulationID) { this.simulationID = simulationID; }
}
