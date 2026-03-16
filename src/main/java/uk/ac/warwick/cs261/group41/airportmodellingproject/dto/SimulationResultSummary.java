package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import java.util.Date;

/**
 * Data transfer object containing a summary of a saved simulation result.
 * Used for displaying result lists without loading full simulation data.
 * Contains key metadata and the throughput metric for quick comparison.
 */
public class SimulationResultSummary {
    /** The name of the saved simulation */
    private String simulationName;
    
    /** The date when the simulation was executed */
    private Date dateExecuted;

    /** The number of runways in the configuration */
    private Integer runwayCount;
    
    /** The total number of scheduled events */
    private Integer scheduledEventsCount;
    
    /** The inbound aircraft rate per hour */
    private Integer inboundRate;
    
    /** The outbound aircraft rate per hour */
    private Integer outboundRate;
    
    /** The hourly throughput achieved */
    private Double throughput;

    /**
     * Constructs a SimulationResultSummary with all fields.
     *
     * @param simulationName the simulation name
     * @param dateExecuted the execution date
     * @param runwayCount the number of runways
     * @param scheduledEventsCount the number of scheduled events
     * @param inboundRate the inbound rate per hour
     * @param outboundRate the outbound rate per hour
     * @param throughput the hourly throughput
     */
    public SimulationResultSummary(String simulationName, Date dateExecuted, Integer runwayCount, Integer scheduledEventsCount, Integer inboundRate, Integer outboundRate, Double throughput) {
        this.simulationName = simulationName;
        this.dateExecuted = dateExecuted;
        this.runwayCount = runwayCount;
        this.scheduledEventsCount = scheduledEventsCount;
        this.inboundRate = inboundRate;
        this.outboundRate = outboundRate;
        this.throughput = throughput;
    }

    /**
     * Gets the simulation name.
     *
     * @return the simulation name
     */
    public String getSimulationName() {
        return simulationName;
    }

    /**
     * Gets the execution date.
     *
     * @return the date executed
     */
    public Date getDateExecuted() {
        return dateExecuted;
    }

    /**
     * Gets the runway count.
     *
     * @return the number of runways
     */
    public Integer getRunwayCount() {
        return runwayCount;
    }

    /**
     * Gets the scheduled events count.
     *
     * @return the number of scheduled events
     */
    public Integer getScheduledEventsCount() {
        return scheduledEventsCount;
    }

    /**
     * Gets the inbound rate.
     *
     * @return the inbound rate per hour
     */
    public Integer getInboundRate() {
        return inboundRate;
    }

    /**
     * Gets the outbound rate.
     *
     * @return the outbound rate per hour
     */
    public Integer getOutboundRate() {
        return outboundRate;
    }

    /**
     * Gets the throughput.
     *
     * @return the hourly throughput
     */
    public Double getThroughput() {
        return throughput;
    }

    /**
     * Sets the simulation name.
     *
     * @param simulationName the simulation name
     */
    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    /**
     * Sets the execution date.
     *
     * @param dateExecuted the date executed
     */
    public void setDateExecuted(Date dateExecuted) {
        this.dateExecuted = dateExecuted;
    }

    /**
     * Sets the runway count.
     *
     * @param runwayCount the number of runways
     */
    public void setRunwayCount(Integer runwayCount) {
        this.runwayCount = runwayCount;
    }

    /**
     * Sets the scheduled events count.
     *
     * @param scheduledEventsCount the number of scheduled events
     */
    public void setScheduledEventsCount(Integer scheduledEventsCount) {
        this.scheduledEventsCount = scheduledEventsCount;
    }

    /**
     * Sets the inbound rate.
     *
     * @param inboundRate the inbound rate per hour
     */
    public void setInboundRate(Integer inboundRate) {
        this.inboundRate = inboundRate;
    }

    /**
     * Sets the outbound rate.
     *
     * @param outboundRate the outbound rate per hour
     */
    public void setOutboundRate(Integer outboundRate) {
        this.outboundRate = outboundRate;
    }

    /**
     * Sets the throughput.
     *
     * @param throughput the hourly throughput
     */
    public void setThroughput(Double throughput) {
        this.throughput = throughput;
    }
}
