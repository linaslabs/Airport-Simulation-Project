package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import java.util.List;

/**
 * Data transfer object representing a complete snapshot of simulation state at a given tick.
 * Contains all real-time metrics, queue sizes, and lists of aircraft and runway states.
 * Used for WebSocket updates to the UI during simulation execution.
 */
public class SimulationSnapshot {

    /** Current simulation tick */
    private final int currentTick;
    
    /** Simulation progress percentage (0-100) */
    private final double progressPercent;
    
    /** Average holding time for landed aircraft */
    private final double avgHoldingTime;
    
    /** Average wait time for departed aircraft */
    private final double avgWaitTime;
    
    /** Maximum wait time recorded */
    private final int maxWaitTime;
    
    /** Maximum takeoff delay recorded */
    private final int maxTakeOffDelay;
    
    /** Average takeoff delay */
    private final double avgTakeOffDelay;
    
    /** Configured cancellation threshold */
    private final int cancellationThreshold;
    
    /** Maximum holding pattern size recorded */
    private final int maxHoldingSize;
    
    /** Maximum holding time recorded */
    private final int maxHoldingTime;
    
    /** Maximum arrival delay recorded */
    private final int maxArrivalDelay;
    
    /** Average arrival delay */
    private final double avgArrivalDelay;
    
    /** Maximum takeoff queue size recorded */
    private final int maxTakeOffQueueSize;
    
    /** Current number of aircraft in holding pattern */
    private final int holdingPatternSize;
    
    /** Current number of aircraft in takeoff queue */
    private final int takeoffQueueSize;
    
    /** Total aircraft landed so far */
    private final int totalLanded;
    
    /** Total aircraft departed so far */
    private final int totalDeparted;
    
    /** Total diversions so far */
    private final int diversionCount;
    
    /** Total cancellations so far */
    private final int cancellationCount;
    
    /** Current state of all runways */
    private final List<RunwayDTO> runways;
    
    /** Aircraft currently in holding pattern */
    private final List<HoldingAircraftDTO> holdingAircraft;
    
    /** Aircraft currently in takeoff queue */
    private final List<TakeOffAircraftDTO> takeoffAircraft;

    /**
     * Constructs a SimulationSnapshot with all state data.
     *
     * @param currentTick the current tick
     * @param progressPercent the progress percentage
     * @param avgHoldingTime average holding time
     * @param avgWaitTime average wait time
     * @param maxWaitTime maximum wait time
     * @param maxTakeOffDelay maximum takeoff delay
     * @param avgTakeOffDelay average takeoff delay
     * @param cancellationThreshold the cancellation threshold
     * @param maxHoldingSize maximum holding size
     * @param maxHoldingTime maximum holding time
     * @param maxArrivalDelay maximum arrival delay
     * @param avgArrivalDelay average arrival delay
     * @param maxTakeOffQueueSize maximum takeoff queue size
     * @param holdingPatternSize current holding pattern size
     * @param takeoffQueueSize current takeoff queue size
     * @param totalLanded total landed count
     * @param totalDeparted total departed count
     * @param diversionCount diversion count
     * @param cancellationCount cancellation count
     * @param runways list of runway states
     * @param holdingAircraft list of holding aircraft
     * @param takeoffAircraft list of takeoff aircraft
     */
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

    /** Gets the current tick. @return the current tick */
    public int getCurrentTick() { return currentTick; }

    /** Gets the progress percentage. @return the progress percentage */
    public double getProgressPercent() { return progressPercent; }

    /** Gets the average holding time. @return the average holding time */
    public double getAvgHoldingTime() { return avgHoldingTime; }

    /** Gets the average wait time. @return the average wait time */
    public double getAvgWaitTime() { return avgWaitTime; }

    /** Gets the maximum wait time. @return the maximum wait time */
    public int getMaxWaitTime() { return maxWaitTime; }

    /** Gets the maximum takeoff delay. @return the maximum takeoff delay */
    public int getMaxTakeOffDelay() { return maxTakeOffDelay; }

    /** Gets the average takeoff delay. @return the average takeoff delay */
    public double getAvgTakeOffDelay() { return avgTakeOffDelay; }

    /** Gets the cancellation threshold. @return the cancellation threshold */
    public int getCancellationThreshold() { return cancellationThreshold; }

    /** Gets the maximum holding size. @return the maximum holding size */
    public int getMaxHoldingSize() { return maxHoldingSize; }

    /** Gets the maximum holding time. @return the maximum holding time */
    public int getMaxHoldingTime() { return maxHoldingTime; }

    /** Gets the maximum arrival delay. @return the maximum arrival delay */
    public int getMaxArrivalDelay() { return maxArrivalDelay; }

    /** Gets the average arrival delay. @return the average arrival delay */
    public double getAvgArrivalDelay() { return avgArrivalDelay; }

    /** Gets the maximum takeoff queue size. @return the maximum takeoff queue size */
    public int getMaxTakeOffQueueSize() { return maxTakeOffQueueSize; }

    /** Gets the holding pattern size. @return the holding pattern size */
    public int getHoldingPatternSize() { return holdingPatternSize; }

    /** Gets the takeoff queue size. @return the takeoff queue size */
    public int getTakeoffQueueSize() { return takeoffQueueSize; }

    /** Gets the total landed count. @return the total landed */
    public int getTotalLanded() { return totalLanded; }

    /** Gets the total departed count. @return the total departed */
    public int getTotalDeparted() { return totalDeparted; }

    /** Gets the diversion count. @return the diversion count */
    public int getDiversionCount() { return diversionCount; }

    /** Gets the cancellation count. @return the cancellation count */
    public int getCancellationCount() { return cancellationCount; }

    /** Gets the runway states. @return the list of runways */
    public List<RunwayDTO> getRunways() { return runways; }

    /** Gets the holding aircraft. @return the list of holding aircraft */
    public List<HoldingAircraftDTO> getHoldingAircraft() { return holdingAircraft; }

    /** Gets the takeoff aircraft. @return the list of takeoff aircraft */
    public List<TakeOffAircraftDTO> getTakeoffAircraft() { return takeoffAircraft; }
}
