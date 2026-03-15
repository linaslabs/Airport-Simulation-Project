package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import java.util.Date;

/**
 * Data transfer object containing a summary of a configuration template.
 * Used for displaying template lists without loading full configuration details.
 * Contains key metadata like name, date, and basic configuration parameters.
 */
public class ConfigurationTemplateSummary {

    /** The name of the configuration template */
    private String templateName;
    
    /** The date when the template was created */
    private Date dateCreated;

    /** The number of runways in this configuration */
    private Integer runwayCount;
    
    /** The total number of scheduled events */
    private Integer scheduledEventsCount;
    
    /** The inbound aircraft rate per hour */
    private Integer inboundRate;
    
    /** The outbound aircraft rate per hour */
    private Integer outboundRate;

    /**
     * Constructs a ConfigurationTemplateSummary with all fields.
     *
     * @param templateName the name of the template
     * @param dateCreated the creation date
     * @param runwayCount the number of runways
     * @param scheduledEventsCount the number of scheduled events
     * @param inboundRate the inbound rate per hour
     * @param outboundRate the outbound rate per hour
     */
    public ConfigurationTemplateSummary(String templateName, Date dateCreated, Integer runwayCount, Integer scheduledEventsCount, Integer inboundRate, Integer outboundRate) {
        this.templateName = templateName;
        this.dateCreated = dateCreated;
        this.runwayCount = runwayCount;
        this.scheduledEventsCount = scheduledEventsCount;
        this.inboundRate = inboundRate;
        this.outboundRate = outboundRate;
    }

    /**
     * Gets the template name.
     *
     * @return the template name
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Gets the creation date.
     *
     * @return the date created
     */
    public Date getDateCreated() {
        return dateCreated;
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
     * Sets the template name.
     *
     * @param templateName the template name
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Sets the creation date.
     *
     * @param dateCreated the date created
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
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
}
