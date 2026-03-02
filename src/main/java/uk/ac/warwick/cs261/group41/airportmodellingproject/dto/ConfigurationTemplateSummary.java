package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import java.util.Date;

public class ConfigurationTemplateSummary {

    private String templateName;
    private Date dateCreated;

    private Integer runwayCount;
    private Integer scheduledEventsCount;
    private Integer inboundRate;
    private Integer outboundRate;

    public ConfigurationTemplateSummary(String templateName, Date dateCreated, Integer runwayCount, Integer scheduledEventsCount, Integer inboundRate, Integer outboundRate) {
        this.templateName = templateName;
        this.dateCreated = dateCreated;
        this.runwayCount = runwayCount;
        this.scheduledEventsCount = scheduledEventsCount;
        this.inboundRate = inboundRate;
        this.outboundRate = outboundRate;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Date getDateCreated() {
        return dateCreated;
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

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
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
}
