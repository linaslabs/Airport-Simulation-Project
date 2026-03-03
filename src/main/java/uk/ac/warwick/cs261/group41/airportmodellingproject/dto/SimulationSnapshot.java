package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import java.util.List;

public class SimulationSnapshot {

    private final int currentTick;
    private final double progressPercent;
    private final int holdingPatternSize;
    private final int takeoffQueueSize;
    private final int totalLanded;
    private final int totalDeparted;
    private final int diversionCount;
    private final int cancellationCount;
    private final List<RunwayDTO> runways;
    private final List<HoldingAircraftDTO> holdingAircraft;
    private final List<TakeOffAircraftDTO> takeoffAircraft;

    public SimulationSnapshot(int currentTick, double progressPercent, int holdingPatternSize, int takeoffQueueSize,
                              int totalLanded, int totalDeparted, int diversionCount, int cancellationCount,
                              List<RunwayDTO> runways, List<HoldingAircraftDTO> holdingAircraft, List<TakeOffAircraftDTO> takeoffAircraft) {
        this.currentTick = currentTick;
        this.progressPercent = progressPercent;
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
