package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

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

    @NotBlank(message = "Template name is required.")
    private String templateName;

    private Date dateCreated;

    // Default constructor required for Jackson to turn the JSON into this object.
    public ConfigurationTemplate() { super(); }

    // Constructor used to create a ConfigurationTemplate from an existing SimulationConfig object, used for testing.
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
                config.getMechanicalFailureRate(),
                config.getPassengerHealthIssueRate(),
                config.getRunwayInspectionRate(),
                config.getSnowClearanceRate(),
                config.getEquipmentFailureRate(),
                config.getSeed(),
                config.getScheduledRunwayEvents(),
                config.getScheduledAircraftEvents(),
                config.getSimulationMode()
        );
        this.templateName = templateName;
        this.dateCreated = dateCreated;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
