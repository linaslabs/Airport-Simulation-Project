package uk.ac.warwick.cs261.group41.airportmodellingproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationResult;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.SimulationService;

@RestController
@RequestMapping("/api/results")
public class ResultsController {

    private final SimulationService simulationService;

    // Spring calls this constructor itself, called Constructor Injection.
    public ResultsController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    // Gets the result of the last simulation that was started.
    // A simulation must have been run, and no simulation can be currently running, or an error is returned.
    @GetMapping("/lastresult")
    public ResponseEntity<SimulationResult> getSummary() {
        return ResponseEntity.ok(simulationService.getLastResult());
    }

    // Saves the simulation result, taking in the name the user inputs in the save simulation result popup.
    @PostMapping("/save")
    public ResponseEntity<String> saveSimulationResult(@RequestBody String name) {
        simulationService.saveSimulationResult(name);
        return ResponseEntity.ok("Simulation result saved.");
    }
}
