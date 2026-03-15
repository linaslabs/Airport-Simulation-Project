package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.StatisticsSummary;

/**
 * Tracks and accumulates statistics throughout the airport simulation.
 * Records metrics for landings, takeoffs, delays, diversions, and cancellations.
 * Provides both real-time rolling averages and final summary generation.
 */
public class Statistics {
    /** Maximum number of aircraft in holding pattern at any point */
    private int maxHoldingSize = 0;
    
    /** Maximum number of aircraft in takeoff queue at any point */
    private int maxTakeOffQueueSize = 0;

    /** Total holding time accumulated across all landed aircraft */
    private int totalHoldingTime = 0;
    
    /** Maximum holding time for any single aircraft */
    private int maxHoldingTime = 0;
    
    /** Total wait time accumulated across all departed aircraft */
    private int totalWaitTime = 0;
    
    /** Maximum wait time for any single aircraft */
    private int maxWaitTime = 0;

    /** Total number of aircraft that have landed */
    private int totalAircraftLanded = 0;
    
    /** Total arrival delay accumulated across all landed aircraft */
    private int totalArrivalDelay = 0;
    
    /** Maximum arrival delay for any single aircraft */
    private int maxArrivalDelay = 0;

    /** Total number of aircraft that have departed */
    private int totalAircraftDeparted = 0;
    
    /** Total takeoff delay accumulated across all departed aircraft */
    private int totalTakeOffDelay = 0;
    
    /** Maximum takeoff delay for any single aircraft */
    private int maxTakeOffDelay = 0;

    /** Number of aircraft that diverted due to low fuel */
    private int diversionCount = 0;
    
    /** Number of aircraft that were cancelled due to excessive wait time */
    private int cancellationCount = 0;

    /**
     * Generates a summary of all statistics for the completed simulation.
     * Calculates averages and throughput based on accumulated data.
     *
     * @param duration the total simulation duration in ticks
     * @return a StatisticsSummary containing all computed metrics
     */
    public StatisticsSummary generateSummary(int duration) {
        double avgHoldingTime = (totalAircraftLanded == 0) ? 0 : (double) totalHoldingTime / totalAircraftLanded;
        double avgTakeOffDelay = (totalAircraftDeparted == 0) ? 0 : (double) totalTakeOffDelay / totalAircraftDeparted;
        double avgArrivalDelay = (totalAircraftLanded == 0) ? 0 : (double) totalArrivalDelay / totalAircraftLanded;
        double avgWaitTime = (totalAircraftDeparted == 0) ? 0 : (double) totalWaitTime / totalAircraftDeparted;

        int totalOperations = totalAircraftDeparted + totalAircraftLanded;
        double hourlyThroughput = (duration == 0) ? 0 : (totalOperations / (double) duration) * 60.0;

        return new StatisticsSummary(
                avgHoldingTime,
                maxHoldingTime,
                avgTakeOffDelay,
                maxTakeOffDelay,
                avgArrivalDelay,
                maxArrivalDelay,
                avgWaitTime,
                maxWaitTime,
                maxHoldingSize,
                maxTakeOffQueueSize,
                diversionCount,
                cancellationCount,
                hourlyThroughput
        );
    }

    /**
     * Records the current holding pattern size and updates maximum if needed.
     *
     * @param size the current number of aircraft in holding pattern
     */
    public void recordHoldingSize(int size) {
        maxHoldingSize = Math.max(size, maxHoldingSize);
    }

    /**
     * Records the current takeoff queue size and updates maximum if needed.
     *
     * @param size the current number of aircraft in takeoff queue
     */
    public void recordTakeOffQueueSize(int size) {
        maxTakeOffQueueSize = Math.max(size, maxTakeOffQueueSize);
    }


    /**
     * Records the holding time for a landed aircraft.
     *
     * @param ticks the number of ticks the aircraft spent in holding pattern
     */
    public void recordHoldingTime(int ticks) {
        totalHoldingTime += ticks;
        maxHoldingTime = Math.max(ticks, maxHoldingTime);
    }

    /**
     * Records the wait time for a departed aircraft.
     *
     * @param ticks the number of ticks the aircraft spent in takeoff queue
     */
    public void recordTakeOffWaitTime(int ticks) {
        totalWaitTime += ticks;
        maxWaitTime = Math.max(ticks, maxWaitTime);
    }


    /**
     * Records that an aircraft has landed.
     */
    public void recordLanding() {
        totalAircraftLanded++;
    }

    /**
     * Records the arrival delay for a landed aircraft.
     *
     * @param ticks the delay in ticks from scheduled to actual landing
     */
    public void recordArrivalDelay(int ticks) {
        totalArrivalDelay += ticks;
        maxArrivalDelay = Math.max(ticks, maxArrivalDelay);
    }


    /**
     * Records that an aircraft has taken off.
     */
    public void recordTakeOff() {
        totalAircraftDeparted++;
    }

    /**
     * Records the takeoff delay for a departed aircraft.
     *
     * @param ticks the delay in ticks from scheduled to actual takeoff
     */
    public void recordTakeOffDelay(int ticks) {
        totalTakeOffDelay += ticks;
        maxTakeOffDelay = Math.max(ticks, maxTakeOffDelay);
    }


    /**
     * Records that an aircraft has diverted.
     */
    public void recordDiversion() {
        diversionCount++;
    }

    /**
     * Records that an aircraft has been cancelled.
     */
    public void recordCancellation() {
        cancellationCount++;
    }

    /**
     * Gets the total number of aircraft that have landed.
     *
     * @return the total landed count
     */
    public int getTotalAircraftLanded() { return totalAircraftLanded; }

    /**
     * Gets the total number of aircraft that have departed.
     *
     * @return the total departed count
     */
    public int getTotalAircraftDeparted() { return totalAircraftDeparted; }

    /**
     * Gets the total number of diversions.
     *
     * @return the diversion count
     */
    public int getDiversionCount() { return diversionCount; }

    /**
     * Gets the total number of cancellations.
     *
     * @return the cancellation count
     */
    public int getCancellationCount() { return cancellationCount; }

    /**
     * Calculates the rolling average holding time.
     *
     * @return the average holding time, or 0 if no aircraft have landed
     */
    public double getRollingAvgHoldingTime() {
        return (totalAircraftLanded == 0) ? 0 : (double) totalHoldingTime / totalAircraftLanded;
    }

    /**
     * Calculates the rolling average wait time.
     *
     * @return the average wait time, or 0 if no aircraft have departed
     */
    public double getRollingAvgWaitTime() {
        return (totalAircraftDeparted == 0) ? 0 : (double) totalWaitTime / totalAircraftDeparted;
    }

    /**
     * Calculates the rolling average arrival delay.
     *
     * @return the average arrival delay, or 0 if no aircraft have landed
     */
    public double getRollingAvgArrivalDelay() {
        return (totalAircraftLanded == 0) ? 0 : (double) totalArrivalDelay / totalAircraftLanded;
    }

    /**
     * Calculates the rolling average takeoff delay.
     *
     * @return the average takeoff delay, or 0 if no aircraft have departed
     */
    public double getRollingAvgTakeOffDelay() {
        return (totalAircraftDeparted == 0) ? 0 : (double) totalTakeOffDelay / totalAircraftDeparted;
    }

    /**
     * Gets the maximum holding pattern size recorded.
     *
     * @return the maximum holding size
     */
    public int getMaxHoldingSize() { return maxHoldingSize; }

    /**
     * Gets the maximum takeoff queue size recorded.
     *
     * @return the maximum takeoff queue size
     */
    public int getMaxTakeOffQueueSize() { return maxTakeOffQueueSize; }

    /**
     * Gets the maximum holding time recorded.
     *
     * @return the maximum holding time in ticks
     */
    public int getMaxHoldingTime() { return maxHoldingTime; }

    /**
     * Gets the maximum wait time recorded.
     *
     * @return the maximum wait time in ticks
     */
    public int getMaxWaitTime() { return maxWaitTime; }

    /**
     * Gets the maximum arrival delay recorded.
     *
     * @return the maximum arrival delay in ticks
     */
    public int getMaxArrivalDelay() { return maxArrivalDelay; }

    /**
     * Gets the maximum takeoff delay recorded.
     *
     * @return the maximum takeoff delay in ticks
     */
    public int getMaxTakeOffDelay() { return maxTakeOffDelay; }
}
