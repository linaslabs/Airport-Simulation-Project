package uk.ac.warwick.cs261.group41.airportmodellingproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationProgress;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.SimulationService;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationService simulationService;

    // Spring calls this constructor itself, called Constructor Injection.
    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestBody SimulationConfig config) {
        simulationService.startSimulation(config);
        return ResponseEntity.ok("Simulation started");
    }

    @GetMapping("/progress")
    public ResponseEntity<SimulationProgress> getProgress() {
        return ResponseEntity.ok(simulationService.getSimulationProgress());
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        simulationService.stopSimulation();
        return ResponseEntity.ok("Simulation stopped");
    }
}
