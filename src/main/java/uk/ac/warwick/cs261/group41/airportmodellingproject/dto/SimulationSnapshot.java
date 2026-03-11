package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import java.util.List;

public class SimulationSnapshot {

    private final int currentTick;
    private final double progressPercent;
    private final double avgHoldingTime;
    private final double avgWaitTime;
    private final int maxWaitTime;
    private final int maxTakeOffDelay;
    private final double avgTakeOffDelay;
    private final int cancellationThreshold;
    private final int maxHoldingSize;
    private final int maxHoldingTime;
    private final int maxArrivalDelay;
    private final double avgArrivalDelay;
    private final int maxTakeOffQueueSize;
    private final int holdingPatternSize;
    private final int takeoffQueueSize;
    private final int totalLanded;
    private final int totalDeparted;
    private final int diversionCount;
    private final int cancellationCount;
    private final List<RunwayDTO> runways;
    private final List<HoldingAircraftDTO> holdingAircraft;
    private final List<TakeOffAircraftDTO> takeoffAircraft;

    public SimulationSnapshot(int currentTick, double progressPercent, double avgHoldingTime, double avgWaitTime, int maxWaitTime,
                              int maxTakeOffDelay, double avgTakeOffDelay, int cancellationThreshold, int maxHoldingSize, int maxHoldingTime, int maxArrivalDelay,
                              double avgArrivalDelay, int maxTakeOffQueueSize, int holdingPatternSize, int takeoffQueueSize,
                              int totalLanded, int totalDeparted, int diversionCount, int cancellationCount,
                              List<RunwayDTO> runways, List<HoldingAircraftDTO> holdingAircraft, List<TakeOffAircraftDTO> takeoffAircraft) {
        this.currentTick = currentTick;
        this.progressPercent = progressPercent;
        this.avgHoldingTime = avgHoldingTime;
        this.avgWaitTime = avgWaitTime;
        this.maxWaitTime = maxWaitTime;
        this.maxTakeOffDelay = maxTakeOffDelay;
        this.avgTakeOffDelay = avgTakeOffDelay;
        this.cancellationThreshold = cancellationThreshold;
        this.maxHoldingSize = maxHoldingSize;
        this.maxHoldingTime = maxHoldingTime;
        this.maxArrivalDelay = maxArrivalDelay;
        this.avgArrivalDelay = avgArrivalDelay;
        this.maxTakeOffQueueSize = maxTakeOffQueueSize;
        this.holdingPatternSize = holdingPatternSize;
        this.takeoffQueueSize = takeoffQueueSize;
        this.totalLanded = totalLanded;
        this.totalDeparted = totalDeparted;
        this.diversionCount = diversionCount;
        this.cancellationCount = cancellationCount;
        this.runways = runways;
        this.holdingAircraft = holdingAircraft;
        this.takeoffAircraft = takeoffAircraft;
    }

    public int getCurrentTick() { return currentTick; }

    public double getProgressPercent() { return progressPercent; }

    public double getAvgHoldingTime() { return avgHoldingTime; }

    public double getAvgWaitTime() { return avgWaitTime; }

    public int getMaxWaitTime() { return maxWaitTime; }

    public int getMaxTakeOffDelay() { return maxTakeOffDelay; }

    public double getAvgTakeOffDelay() { return avgTakeOffDelay; }

    public int getCancellationThreshold() { return cancellationThreshold; }

    public int getMaxHoldingSize() { return maxHoldingSize; }

    public int getMaxHoldingTime() { return maxHoldingTime; }

    public int getMaxArrivalDelay() { return maxArrivalDelay; }

    public double getAvgArrivalDelay() { return avgArrivalDelay; }

    public int getMaxTakeOffQueueSize() { return maxTakeOffQueueSize; }

    public int getHoldingPatternSize() { return holdingPatternSize; }

    public int getTakeoffQueueSize() { return takeoffQueueSize; }

    public int getTotalLanded() { return totalLanded; }

    public int getTotalDeparted() { return totalDeparted; }

    public int getDiversionCount() { return diversionCount; }

    public int getCancellationCount() { return cancellationCount; }

    public List<RunwayDTO> getRunways() { return runways; }

    public List<HoldingAircraftDTO> getHoldingAircraft() { return holdingAircraft; }

    public List<TakeOffAircraftDTO> getTakeoffAircraft() { return takeoffAircraft; }
}
