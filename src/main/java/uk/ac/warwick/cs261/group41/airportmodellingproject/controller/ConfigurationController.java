package uk.ac.warwick.cs261.group41.airportmodellingproject.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.SimulationService;

@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final SimulationService simulationService;

    // Spring calls this constructor itself, called Constructor Injection.
    public ConfigurationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validate(@Valid @RequestBody SimulationConfig config) {
        // If the body of the function runs, then the validation from the annotations in the SimulationConfig class passed.
        return ResponseEntity.ok("Configuration is valid");
    }
}
