package uk.ac.warwick.cs261.group41.airportmodellingproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationResult;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationResultSaved;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationResultSummary;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.SimulationService;

import java.util.List;

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


    // These 3 endpoints are used for the window to select which result to view/compare/delete.

    @GetMapping("/summaries")
    public ResponseEntity<List<SimulationResultSummary>> listSimulationResultSummaries() {

    }

    // Similar to the summary window for configuration selection, view and compare can use the same logic.
    @GetMapping("/vieworcompare/{name}")
    public ResponseEntity<SimulationResultSaved> viewOrCompareSimulationResult(@PathVariable String name) {

    }

    @DeleteMapping("/delete/{name}")
    public ResponseEntity<String> deleteSavedResult(@PathVariable String name) {
        simulationService.deleteSimulationResult(name);
        return ResponseEntity.ok("Simulation result deleted.");
    }
}
