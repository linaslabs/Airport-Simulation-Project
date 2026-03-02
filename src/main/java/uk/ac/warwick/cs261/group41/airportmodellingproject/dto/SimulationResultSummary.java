package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import java.util.Date;

public class SimulationResultSummary {
    private String simulationName;
    private Date dateExecuted;

    private Integer runwayCount;
    private Integer scheduledEventsCount;
    private Integer inboundRate;
    private Integer outboundRate;
    private Double throughput;

    public SimulationResultSummary(String simulationName, Date dateExecuted, Integer runwayCount, Integer scheduledEventsCount, Integer inboundRate, Integer outboundRate, Double throughput) {
        this.simulationName = simulationName;
        this.dateExecuted = dateExecuted;
        this.runwayCount = runwayCount;
        this.scheduledEventsCount = scheduledEventsCount;
        this.inboundRate = inboundRate;
        this.outboundRate = outboundRate;
        this.throughput = throughput;
    }

    public String getSimulationName() {
        return simulationName;
    }

    public Date getDateExecuted() {
        return dateExecuted;
    }

    public Integer getRunwayCount() {
        return runwayCount;
    }

    public Integer getScheduledEventsCount() {
        return scheduledEventsCount;
    }

    public Integer getInboundRate() {
        return inboundRate;
    }

    public Integer getOutboundRate() {
        return outboundRate;
    }

    public Double getThroughput() {
        return throughput;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    public void setDateExecuted(Date dateExecuted) {
        this.dateExecuted = dateExecuted;
    }

    public void setRunwayCount(Integer runwayCount) {
        this.runwayCount = runwayCount;
    }

    public void setScheduledEventsCount(Integer scheduledEventsCount) {
        this.scheduledEventsCount = scheduledEventsCount;
    }

    public void setInboundRate(Integer inboundRate) {
        this.inboundRate = inboundRate;
    }

    public void setOutboundRate(Integer outboundRate) {
        this.outboundRate = outboundRate;
    }

    public void setThroughput(Double throughput) {
        this.throughput = throughput;
    }
}
