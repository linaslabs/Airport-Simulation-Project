package uk.ac.warwick.cs261.group41.airportmodellingproject.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.ConfigurationTemplate;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.ConfigurationTemplateSummary;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.SimulationService;
import uk.ac.warwick.cs261.group41.airportmodellingproject.utility.JsonFileHandler;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final SimulationService simulationService;

    // Spring calls this constructor itself, called Constructor Injection.
    public ConfigurationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    // Used to validate the configuration before the user starts the simulation.
    @PostMapping("/validate")
    public ResponseEntity<String> validate(@Valid @RequestBody SimulationConfig config) {
        // If the body of the function runs, then the validation from the annotations in the SimulationConfig class passed.
        return ResponseEntity.ok("Configuration is valid");
    }



    // These 3 endpoints are used for the window to select a previous configuration template to
    // view/load/delete.

    @GetMapping("/templates/summaries")
    public ResponseEntity<List<ConfigurationTemplateSummary>> listSavedConfigurationTemplates() {
        return ResponseEntity.ok(simulationService.listSavedConfigTemplateSummaries());
    }

    // This function and API endpoint is "view or load" because we can use the same logic for each.
    // In the frontend, when loading a configuration into the input boxes, only the data needed from the
    // ConfigurationTemplate object should be extracted, therefore it is used in the same way as if a SimulationConfig
    // object was being sent.
    // When viewing a configuration, it should take all the fields, including name and date, and the configuration
    // itself.
    @GetMapping("/templates/vieworload/{name}")
    public ResponseEntity<ConfigurationTemplate> viewOrLoadConfigTemplate(@PathVariable String name) {
        return ResponseEntity.ok(simulationService.getConfigurationTemplate(name));
    }

    @DeleteMapping("/templates/delete/{name}")
    ResponseEntity<String> deleteConfigTemplate(@PathVariable String name) {
        simulationService.deleteConfigurationTemplate(name);
        return ResponseEntity.ok("Configuration template deleted.");
    }



    @PostMapping("/save")
    public ResponseEntity<String> saveConfigTemplate(@Valid @RequestBody ConfigurationTemplate configTemplate) {
        simulationService.saveConfigurationTemplate(configTemplate);
        return ResponseEntity.ok("Configuration saved.");
    }
}
