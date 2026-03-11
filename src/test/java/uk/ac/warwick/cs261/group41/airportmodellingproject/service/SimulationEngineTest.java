package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimulationEngine (SR-FR-4).
 *
 * Covers: initialisation state, discrete tick progression, progress fraction,
 * end-of-simulation signalling, end-to-end aircraft flow through the system,
 * and scheduled runway events firing at the correct tick.
 *
 */
class SimulationEngineTest {

    private static RunwayConfig rc(int id, RunwayMode mode) {
        return new RunwayConfig(id, RunwayStatus.AVAILABLE, mode);
    }

    /**
     * Builds a minimal SimulationConfig with a single LANDING runway,
     * no outbound traffic, no auto-generation, and no scheduled events.
     */
    private static SimulationConfig makeConfig(int duration) {
        return new SimulationConfig(
                List.of(rc(1, RunwayMode.LANDING)),
                20, 0,             // inboundRate=20/hr, outboundRate=0
                30, duration, 10,  // maxWaitTime=30, duration, tickTime=10ms
                false,
                0.0, 0.0, 0.0, 0.0, 0.0, // all auto-generation rates off
                42L,
                new HashMap<>(), new HashMap<>(),
                SimulationMode.QUICK_SIM
        );
    }

    /**
     * Verifies that after initialiseSimulation(), the engine starts at tick 0
     * with a progress fraction of 0.0.
     */
    @Test
    void initialiseSimulation_shouldStartAtTickZeroWithZeroProgress() {
        SimulationEngine engine = new SimulationEngine(makeConfig(10));
        engine.initialiseSimulation();

        SimulationProgress progress = engine.getSimulationProgress();
        assertEquals(0, progress.getCurrentTick(),
                "Engine should start at tick 0 after initialisation.");
        assertEquals(0.0, progress.getProgressPercent(), 0.0001,
                "Progress fraction should be 0.0 at tick 0.");
    }

    /**
     * Verifies that performTick() returns true while the simulation is still running
     * and false on the final tick, signalling the simulation is complete.
     */
    @Test
    void performTick_shouldReturnTrue_whileRunning_andFalseOnLastTick() {
        SimulationEngine engine = new SimulationEngine(makeConfig(3));
        engine.initialiseSimulation();

        assertTrue(engine.performTick(),  "Tick 1 of 3 should return true (not finished).");
        assertTrue(engine.performTick(),  "Tick 2 of 3 should return true (not finished).");
        assertFalse(engine.performTick(), "Tick 3 of 3 (last) should return false (finished).");
    }

    /**
     * Verifies that getCurrentTick() increments by exactly 1 per performTick() call.
     */
    @Test
    void performTick_shouldIncrementCurrentTick_afterEachCall() {
        SimulationEngine engine = new SimulationEngine(makeConfig(10));
        engine.initialiseSimulation();

        assertEquals(0, engine.getCurrentTick(), "Tick should be 0 before any performTick() calls.");
        engine.performTick();
        engine.performTick();
        engine.performTick();
        assertEquals(3, engine.getCurrentTick(),
                "getCurrentTick() should equal the number of ticks performed.");
    }

    /**
     * Verifies that getSimulationProgress() reports the correct current tick and
     * progress fraction after a known number of ticks.
     */
    @Test
    void getSimulationProgress_shouldReflectCurrentTick_andFraction() {
        SimulationEngine engine = new SimulationEngine(makeConfig(10));
        engine.initialiseSimulation();

        for (int i = 0; i < 4; i++) engine.performTick();

        SimulationProgress progress = engine.getSimulationProgress();
        assertEquals(4, progress.getCurrentTick(),
                "Expected tick 4 after 4 performTick() calls.");
        assertEquals(0.4, progress.getProgressPercent(), 0.0001,
                "Progress should be 4/10 = 0.4 after 4 of 10 ticks.");
    }

    /**
     * Verifies end-to-end aircraft flow (SR-FR-4): aircraft are generated, accepted
     * into the airport, assigned to the runway, and recorded as landed in Statistics.
     * A high inbound rate and fixed seed ensure aircraft are generated; with a single
     * LANDING runway over 60 ticks at least one landing must occur.
     */
    @Test
    void performTick_shouldFlowAircraftThroughSystem_andRecordLandings() {
        SimulationConfig config = new SimulationConfig(
                List.of(rc(1, RunwayMode.LANDING)),
                60, 0,            // inboundRate=60/hr (~1 per tick), outboundRate=0
                60, 60, 10,       // maxWaitTime=60 (prevents cancellations), duration=60
                false,
                0.0, 0.0, 0.0, 0.0, 0.0,
                42L,
                new HashMap<>(), new HashMap<>(),
                SimulationMode.QUICK_SIM
        );
        SimulationEngine engine = new SimulationEngine(config);
        engine.initialiseSimulation();

        while (engine.performTick()) { /* run to completion */ }

        assertTrue(engine.getStats().getTotalAircraftLanded() > 0,
                "At least one aircraft should have landed in a 60-tick simulation at 60 aircraft/hour.");
    }

    /**
     * Verifies that a scheduled runway event fires at the correct tick:
     * after running past tick 2, the runway status changes from AVAILABLE to INSPECTION.
     * processScheduledEvents(2) is called at the start of tick 2 inside performTick().
     */
    @Test
    void performTick_shouldFireScheduledRunwayEvent_atCorrectTick() {
        Map<Integer, List<RunwayEvent>> runwayEvents = new HashMap<>();
        runwayEvents.put(2, new ArrayList<>(List.of(
                new RunwayEvent(2, 1, RunwayStatus.INSPECTION, RunwayMode.LANDING,
                        RunwayEventType.SCHEDULED_CHANGE, -1) // duration=-1: no reversion
        )));

        SimulationConfig config = new SimulationConfig(
                List.of(rc(1, RunwayMode.LANDING)),
                0, 0,
                30, 10, 10,
                false,
                0.0, 0.0, 0.0, 0.0, 0.0,
                42L,
                runwayEvents, new HashMap<>(),
                SimulationMode.QUICK_SIM
        );
        SimulationEngine engine = new SimulationEngine(config);
        engine.initialiseSimulation();

        assertEquals(RunwayStatus.AVAILABLE, engine.getAirport().getRunwaySnapshot(1).getStatus(),
                "Runway should start AVAILABLE before the scheduled event fires.");

        // Run ticks 0, 1, 2 — the event fires at the start of tick 2.
        engine.performTick();
        engine.performTick();
        engine.performTick();

        assertEquals(RunwayStatus.INSPECTION, engine.getAirport().getRunwaySnapshot(1).getStatus(),
                "Runway should be INSPECTION after the scheduled event fires at tick 2.");
    }
}
