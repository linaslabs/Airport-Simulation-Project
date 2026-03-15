package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable data transfer object containing the final statistical summary of a simulation.
 * All fields are final to prevent modification of simulation results after completion.
 * Used for displaying results and storing in saved simulation files.
 */
public class StatisticsSummary {

    /** Average time aircraft spent in holding pattern */
    private final double avgHoldingTime;
    
    /** Maximum time any aircraft spent in holding pattern */
    private final int maxHoldingTime;
    
    /** Average delay from scheduled to actual takeoff */
    private final double avgTakeOffDelay;
    
    /** Maximum delay from scheduled to actual takeoff */
    private final double maxTakeOffDelay;
    
    /** Average delay from scheduled to actual arrival */
    private final double avgArrivalDelay;
    
    /** Maximum delay from scheduled to actual arrival */
    private final double maxArrivalDelay;
    
    /** Average time aircraft spent in takeoff queue */
    private final double avgWaitTime;
    
    /** Maximum time any aircraft spent in takeoff queue */
    private final int maxWaitTime;
    
    /** Maximum number of aircraft in holding pattern at any time */
    private final int maxHoldingSize;
    
    /** Maximum number of aircraft in takeoff queue at any time */
    private final int maxTakeOffQueueSize;
    
    /** Total number of aircraft that diverted */
    private final int diversionCount;
    
    /** Total number of flights that were cancelled */
    private final int cancellationCount;
    
    /** Average operations per hour (landings + takeoffs) */
    private final double hourlyThroughput;

    /**
     * Constructs a StatisticsSummary with all metrics.
     * JsonCreator annotation required for Jackson deserialization of final fields.
     *
     * @param avgHoldingTime average holding time
     * @param maxHoldingTime maximum holding time
     * @param avgTakeOffDelay average takeoff delay
     * @param maxTakeOffDelay maximum takeoff delay
     * @param avgArrivalDelay average arrival delay
     * @param maxArrivalDelay maximum arrival delay
     * @param avgWaitTime average wait time
     * @param maxWaitTime maximum wait time
     * @param maxHoldingSize maximum holding size
     * @param maxTakeOffQueueSize maximum takeoff queue size
     * @param diversionCount total diversions
     * @param cancellationCount total cancellations
     * @param hourlyThroughput hourly throughput
     */
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

    /** Gets the average holding time. @return the average holding time */
    public double getAvgHoldingTime() { return avgHoldingTime; }
    
    /** Gets the maximum holding time. @return the maximum holding time */
    public int getMaxHoldingTime() { return maxHoldingTime; }
    
    /** Gets the average takeoff delay. @return the average takeoff delay */
    public double getAvgTakeOffDelay() { return avgTakeOffDelay; }
    
    /** Gets the maximum takeoff delay. @return the maximum takeoff delay */
    public double getMaxTakeOffDelay() { return maxTakeOffDelay; }
    
    /** Gets the average arrival delay. @return the average arrival delay */
    public double getAvgArrivalDelay() { return avgArrivalDelay; }
    
    /** Gets the maximum arrival delay. @return the maximum arrival delay */
    public double getMaxArrivalDelay() { return maxArrivalDelay; }
    
    /** Gets the average wait time. @return the average wait time */
    public double getAvgWaitTime() { return avgWaitTime; }
    
    /** Gets the maximum wait time. @return the maximum wait time */
    public int getMaxWaitTime() { return maxWaitTime; }
    
    /** Gets the maximum holding size. @return the maximum holding size */
    public int getMaxHoldingSize() { return maxHoldingSize; }
    
    /** Gets the maximum takeoff queue size. @return the maximum takeoff queue size */
    public int getMaxTakeOffQueueSize() { return maxTakeOffQueueSize; }
    
    /** Gets the diversion count. @return the diversion count */
    public int getDiversionCount() { return diversionCount; }
    
    /** Gets the cancellation count. @return the cancellation count */
    public int getCancellationCount() { return cancellationCount; }
    
    /** Gets the hourly throughput. @return the hourly throughput */
    public double getHourlyThroughput() { return hourlyThroughput; }
}
