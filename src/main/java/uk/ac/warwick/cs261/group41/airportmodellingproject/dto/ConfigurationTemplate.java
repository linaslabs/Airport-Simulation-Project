package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

/**
 * This class is an extension of the SimulationConfig class.
 * It adds fields to assign a name and creation date to a configuration.
 * These are not required by the simulation when using the SimulationConfig, but they are needed when storing them as JSON templates.
 * The separation of the two creates separation between the pure data which the simulation needs, SimulationConfig, and the
 * additional data required when storing the configuration permanently.
 */
public class ConfigurationTemplate extends SimulationConfig {

    /** The name of the configuration template */
    @NotBlank(message = "Template name is required.")
    private String templateName;

    /** The date when the template was created */
    private Date dateCreated;

    /**
     * Default constructor required for Jackson deserialization.
     */
    public ConfigurationTemplate() { super(); }

    /**
     * Constructs a ConfigurationTemplate from an existing SimulationConfig.
     * Used for testing and programmatic template creation.
     *
     * @param templateName the name for this template
     * @param dateCreated the creation date
     * @param config the simulation configuration to copy
     */
    @JsonIgnore
    public ConfigurationTemplate(String templateName, Date dateCreated, SimulationConfig config) {
        // Pass the data up to the parent's parameterised constructor.
        super(
                config.getRunwaySettings(),
                config.getInboundRate(),
                config.getOutboundRate(),
                config.getMaxWaitTime(),
                config.getDuration(),
                config.getTickTime(),
                config.getAutomaticGenerationEnabled(),
                config.getMechanicalFailureMultiplier(),
                config.getPassengerHealthIssueMultiplier(),
                config.getRunwayInspectionMultiplier(),
                config.getSnowClearanceMultiplier(),
                config.getEquipmentFailureMultiplier(),
                config.getSeed(),
                config.getScheduledRunwayEvents(),
                config.getScheduledAircraftEvents(),
                config.getSimulationMode()
        );
        this.templateName = templateName;
        this.dateCreated = dateCreated;
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
     * Sets the template name.
     *
     * @param templateName the template name
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
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
     * Sets the creation date.
     *
     * @param dateCreated the date created
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
