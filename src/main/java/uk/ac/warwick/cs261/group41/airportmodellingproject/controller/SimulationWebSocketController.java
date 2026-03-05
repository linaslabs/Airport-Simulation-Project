package uk.ac.warwick.cs261.group41.airportmodellingproject.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.SimulationService;

// This is the controller for websocket stuff.
// If the frontend needs to send stuff to the backend using websockets, then it should be done via a /app/... endpoint defined in this controller.

@Controller
@MessageMapping("/simulation")
public class SimulationWebSocketController {

    private final SimulationService simulationService;

    public SimulationWebSocketController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @MessageMapping("/ready")
    public void frontendReady() {
        simulationService.startSimulation();
    }
}
