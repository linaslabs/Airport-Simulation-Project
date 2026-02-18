package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.StatisticsSummary;

public class Statistics {
    private int maxHoldingSize = 0;
    private int maxTakeOffQueueSize = 0;

    private int totalHoldingTime = 0;
    private int maxHoldingTime = 0;
    private int totalWaitTime = 0;
    private int maxWaitTime = 0; // Added this, not on diagram.

    private int totalAircraftLanded = 0;
    private int totalArrivalDelay = 0;
    private int maxArrivalDelay = 0;

    private int totalAircraftDeparted = 0;
    private int totalTakeOffDelay = 0;
    private int maxTakeOffDelay = 0;

    private int diversionCount = 0;
    private int cancellationCount = 0;

    public void recordHoldingSize(int size) {
        maxHoldingSize = Math.max(size, maxHoldingSize);
    }

    public void recordTakeOffQueueSize(int size) {
        maxTakeOffQueueSize = Math.max(size, maxTakeOffQueueSize);
    }


    public void recordHoldingTime(int ticks) {
        totalHoldingTime += ticks;
        maxHoldingTime = Math.max(ticks, maxHoldingTime);
    }

    public void recordTakeOffWaitTime(int ticks) {
        totalWaitTime += ticks;
        maxWaitTime = Math.max(ticks, maxWaitTime);
    }


    public void recordLanding() {
        totalAircraftLanded++;
    }

    public void recordArrivalDelay(int ticks) {
        totalArrivalDelay += ticks;
        maxArrivalDelay = Math.max(ticks, maxArrivalDelay);
    }


    public void recordTakeOff() {
        totalAircraftDeparted++;
    }

    public void recordTakeOffDelay(int ticks) {
        totalTakeOffDelay += ticks;
        maxTakeOffDelay = Math.max(ticks, maxTakeOffDelay);
    }


    public void recordDiversion() {
        diversionCount++;
    }

    public void recordCancellation() {
        cancellationCount++;
    }

    // If the calculation involves division by 0, we set the answer to 0. 
    public StatisticsSummary generateSummary(int duration) {
        double avgHoldingTime = (totalAircraftLanded == 0) ? 0 : (double) totalHoldingTime / totalAircraftLanded; 
        double avgTakeOffDelay = (totalAircraftDeparted == 0) ? 0 : (double) totalTakeOffDelay / totalAircraftDeparted;
        double avgArrivalDelay = (totalAircraftLanded == 0) ? 0 : (double) totalArrivalDelay / totalAircraftLanded; 
        double avgWaitTime = (totalAircraftDeparted == 0) ? 0 : (double) totalWaitTime / totalAircraftDeparted;

        int totalOperations = totalAircraftDeparted + totalAircraftLanded;
        double hourlyThroughput = (duration == 0) ? 0 : (totalOperations / (double) duration) * 60.0;

        return new StatisticsSummary(
                avgHoldingTime,
                avgTakeOffDelay,
                maxTakeOffDelay,
                avgArrivalDelay,
                maxArrivalDelay,
                avgWaitTime,
                maxHoldingSize,
                maxTakeOffQueueSize,
                diversionCount,
                cancellationCount,
                hourlyThroughput
        );
    }
}
