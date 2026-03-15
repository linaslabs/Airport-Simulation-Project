package uk.ac.warwick.cs261.group41.airportmodellingproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationProgress;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.SimulationService;

/**
 * REST controller exposing the simulation lifecycle and manual override endpoints.
 * All endpoints are prefixed with /api/simulation.
 */
@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationService simulationService;

    /**
     * Creates a SimulationController with the given SimulationService.
     * Spring injects the dependency automatically via constructor injection.
     *
     * @param simulationService the service handling simulation lifecycle and state
     */
    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /**
     * Initialises a new simulation with the provided configuration.
     *
     * @param config the simulation configuration supplied in the request body
     * @return a 200 OK response confirming the simulation was initialised
     */
    @PostMapping("/initialise")
    public ResponseEntity<String> initialise(@RequestBody SimulationConfig config) {
        simulationService.initialiseSimulation(config);
        return ResponseEntity.ok("Simulation initialised");
    }

    /**
     * Starts the previously initialised simulation.
     *
     * @return a 200 OK response confirming the simulation was started
     */
    @PostMapping("/start")
    public ResponseEntity<String> start() {
        simulationService.startSimulation();
        return ResponseEntity.ok("Simulation started");
    }

    /**
     * Manually updates the operating mode of the specified runway.
     *
     * @param id   the ID of the runway to update
     * @param mode the new runway mode as a string
     * @return a 200 OK response confirming the mode was updated
     */
    @PostMapping("/runway/{id}/mode")
    public ResponseEntity<String> updateRunwayMode(@PathVariable int id, @RequestParam String mode) {
        simulationService.manualRunwayModeChange(id, mode);
        return ResponseEntity.ok("Runway mode updated");
    }

    /**
     * Manually updates the status of the specified runway.
     *
     * @param id     the ID of the runway to update
     * @param status the new runway status as a string
     * @return a 200 OK response confirming the status was updated
     */
    @PostMapping("/runway/{id}/status")
    public ResponseEntity<String> updateRunwayStatus(@PathVariable int id, @RequestParam String status) {
        simulationService.manualRunwayStatusChange(id, status);
        return ResponseEntity.ok("Runway status updated");
    }

    /**
     * Manually sets the emergency status of the specified aircraft.
     *
     * @param callsign the callsign identifying the aircraft
     * @param status   the new emergency status as a string
     * @return a 200 OK response confirming the emergency status was updated
     */
    @PostMapping("/aircraft/{callsign}/emergency")
    public ResponseEntity<String> updateAircraftEmergency(@PathVariable String callsign, @RequestParam String status) {
        simulationService.manualAircraftEmergencyChange(callsign, status);
        return ResponseEntity.ok("Aircraft emergency status updated");
    }

    /**
     * Pauses the currently running simulation.
     *
     * @return a 200 OK response confirming the simulation was paused
     */
    @PostMapping("/pause")
    public ResponseEntity<String> pause() {
        simulationService.pauseSimulation();
        return ResponseEntity.ok("Simulation paused");
    }

    /**
     * Resumes a previously paused simulation.
     *
     * @return a 200 OK response confirming the simulation was resumed
     */
    @PostMapping("/resume")
    public ResponseEntity<String> resume() {
        simulationService.resumeSimulation();
        return ResponseEntity.ok("Simulation resumed");
    }

    /**
     * Sets the simulation speed to the given multiplier.
     *
     * @param multiplier the speed multiplier to apply
     * @return a 200 OK response confirming the speed was set
     */
    @PostMapping("/speed/{multiplier}")
    public ResponseEntity<String> setSpeed(@PathVariable int multiplier) {
        simulationService.setSpeedMultiplier(multiplier);
        return ResponseEntity.ok("Speed set to " + multiplier + "x");
    }

    /**
     * Fast-forwards the simulation to its end, processing all remaining ticks immediately.
     *
     * @return a 200 OK response confirming the fast-forward was triggered
     */
    @PostMapping("/fastforward")
    public ResponseEntity<String> fastForward() {
        simulationService.fastForwardToEnd();
        return ResponseEntity.ok("Fast forwarding to end");
    }

    /**
     * Returns the current progress snapshot of the running simulation.
     *
     * @return a 200 OK response containing the current SimulationProgress
     */
    @GetMapping("/progress")
    public ResponseEntity<SimulationProgress> getProgress() {
        return ResponseEntity.ok(simulationService.getSimulationProgress());
    }

    /**
     * Stops the current simulation and finalises results.
     *
     * @return a 200 OK response confirming the simulation was stopped
     */
    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        simulationService.stopSimulation();
        return ResponseEntity.ok("Simulation stopped");
    }
}
