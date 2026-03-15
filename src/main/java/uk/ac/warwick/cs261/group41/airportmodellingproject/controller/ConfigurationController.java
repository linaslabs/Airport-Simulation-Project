package uk.ac.warwick.cs261.group41.airportmodellingproject.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.ConfigurationTemplate;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.ConfigurationTemplateSummary;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.SimulationService;

import java.util.List;

/**
 * REST controller exposing endpoints for validating, saving, loading, and deleting
 * simulation configuration templates. All endpoints are prefixed with /api/configuration.
 */
@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final SimulationService simulationService;

    /**
     * Creates a ConfigurationController with the given SimulationService.
     * Spring injects the dependency automatically via constructor injection.
     *
     * @param simulationService the service used to manage configuration templates
     */
    public ConfigurationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /**
     * Validates the provided simulation configuration against its field-level constraints.
     * If this method executes successfully, all validation annotations on SimulationConfig passed.
     *
     * @param config the simulation configuration to validate
     * @return a 200 OK response confirming the configuration is valid
     */
    @PostMapping("/validate")
    public ResponseEntity<String> validate(@Valid @RequestBody SimulationConfig config) {
        return ResponseEntity.ok("Configuration is valid");
    }



    /**
     * Returns a summary list of all saved configuration templates, used to populate the
     * template selection window for viewing, loading, or deleting templates.
     *
     * @return a 200 OK response containing a list of ConfigurationTemplateSummary objects
     */
    @GetMapping("/templates/summaries")
    public ResponseEntity<List<ConfigurationTemplateSummary>> listSavedConfigurationTemplates() {
        return ResponseEntity.ok(simulationService.listSavedConfigTemplateSummaries());
    }

    /**
     * Retrieves the full configuration template with the given name.
     * The same endpoint serves both the view and load workflows: when loading, only the
     * SimulationConfig fields need to be extracted; when viewing, all fields including
     * name and date are used.
     *
     * @param name the name of the configuration template to retrieve
     * @return a 200 OK response containing the ConfigurationTemplate object
     */
    @GetMapping("/templates/vieworload/{name}")
    public ResponseEntity<ConfigurationTemplate> viewOrLoadConfigTemplate(@PathVariable String name) {
        return ResponseEntity.ok(simulationService.getConfigurationTemplate(name));
    }

    /**
     * Deletes the saved configuration template with the given name.
     *
     * @param name the name of the configuration template to delete
     * @return a 200 OK response confirming the template was deleted
     */
    @DeleteMapping("/templates/delete/{name}")
    public ResponseEntity<String> deleteConfigTemplate(@PathVariable String name) {
        simulationService.deleteConfigurationTemplate(name);
        return ResponseEntity.ok("Configuration template deleted.");
    }



    /**
     * Saves a new configuration template with the details provided in the request body.
     *
     * @param configTemplate the configuration template to save
     * @return a 200 OK response confirming the template was saved
     */
    @PostMapping("/save")
    public ResponseEntity<String> saveConfigTemplate(@Valid @RequestBody ConfigurationTemplate configTemplate) {
        simulationService.saveConfigurationTemplate(configTemplate);
        return ResponseEntity.ok("Configuration saved.");
    }
}
