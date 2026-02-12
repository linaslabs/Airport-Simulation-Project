package uk.ac.warwick.cs261.group41.airportmodellingproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.StatisticsSummary;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.SimulationService;

@RestController
@RequestMapping("/api/results")
public class ResultsController {

    private final SimulationService simulationService;

    // Spring calls this constructor itself, called Constructor Injection.
    public ResultsController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/summary")
    public ResponseEntity<StatisticsSummary> getSummary() {
        return ResponseEntity.ok(simulationService.getStatisticsSummary());
    }
}
