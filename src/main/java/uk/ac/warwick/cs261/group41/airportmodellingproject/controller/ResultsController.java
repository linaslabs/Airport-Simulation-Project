package uk.ac.warwick.cs261.group41.airportmodellingproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationResult;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationResultSaved;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationResultSummary;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.SimulationService;

import java.util.List;

/**
 * REST controller exposing endpoints for retrieving, saving, listing, and deleting
 * simulation results. All endpoints are prefixed with /api/results.
 */
@RestController
@RequestMapping("/api/results")
public class ResultsController {

    private final SimulationService simulationService;

    /**
     * Creates a ResultsController with the given SimulationService.
     * Spring injects the dependency automatically via constructor injection.
     *
     * @param simulationService the service used to access simulation result data
     */
    public ResultsController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /**
     * Returns the result of the most recently completed simulation.
     * A simulation must have finished running before this endpoint can be called successfully.
     *
     * @return a 200 OK response containing the last SimulationResult
     */
    @GetMapping("/lastresult")
    public ResponseEntity<SimulationResult> getSummary() {
        return ResponseEntity.ok(simulationService.getLastResult());
    }

    /**
     * Saves the most recent simulation result under the given name.
     *
     * @param name the name chosen by the user for this saved result
     * @return a 200 OK response confirming the result was saved
     */
    @PostMapping("/save")
    public ResponseEntity<String> saveSimulationResult(@RequestBody String name) {
        simulationService.saveSimulationResult(name);
        return ResponseEntity.ok("Simulation result saved.");
    }

    /**
     * Returns a summary list of all saved simulation results, used to populate the
     * result selection window for viewing, comparing, or deleting results.
     *
     * @return a 200 OK response containing a list of SimulationResultSummary objects
     */
    @GetMapping("/summaries")
    public ResponseEntity<List<SimulationResultSummary>> listSimulationResultSummaries() {
        return ResponseEntity.ok(simulationService.listResultSummaries());
    }

    /**
     * Retrieves the full saved simulation result with the given name.
     * The same endpoint serves both the view and compare workflows.
     *
     * @param name the name of the saved simulation result to retrieve
     * @return a 200 OK response containing the SimulationResultSaved object
     */
    @GetMapping("/vieworcompare/{name}")
    public ResponseEntity<SimulationResultSaved> viewOrCompareSimulationResult(@PathVariable String name) {
        return ResponseEntity.ok(simulationService.getSimulationResult(name));
    }

    /**
     * Deletes the saved simulation result with the given name.
     *
     * @param name the name of the saved simulation result to delete
     * @return a 200 OK response confirming the result was deleted
     */
    @DeleteMapping("/delete/{name}")
    public ResponseEntity<String> deleteSavedResult(@PathVariable String name) {
        simulationService.deleteSimulationResult(name);
        return ResponseEntity.ok("Simulation result deleted.");
    }
}
