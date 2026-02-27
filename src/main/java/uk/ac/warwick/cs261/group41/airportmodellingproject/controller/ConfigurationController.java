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

    @PostMapping("/validate")
    public ResponseEntity<String> validate(@Valid @RequestBody SimulationConfig config) {
        // If the body of the function runs, then the validation from the annotations in the SimulationConfig class passed.
        return ResponseEntity.ok("Configuration is valid");
    }

    @GetMapping("/saved")
    public ResponseEntity<List<String>> listSavedConfigs() {
        List<String> configIds = JsonFileHandler.listSavedConfigs();
        return ResponseEntity.ok(configIds);
    }

    @GetMapping("/load/{id}")
    public ResponseEntity<SimulationConfig> loadConfig(@PathVariable String id) {
        try {
            SimulationConfig config = JsonFileHandler.loadConfig(id);
            return ResponseEntity.ok(config);
        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Config not found: " + id);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid config file: " + id);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveConfig(@Valid @RequestBody SimulationConfig config) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        config.setSimulationID(timestamp);
        JsonFileHandler.saveConfig(config);
        return ResponseEntity.ok(timestamp);
    }
}
