package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AircraftGenerator.
 *
 * Validates total aircraft counts per rate and duration, empty schedule behaviour before
 * initialisation, fuel values within [20.0, 60.0], correct FlightType assignment,
 * and safe empty-tick handling.
 */
class AircraftGeneratorTest {

    // Fixed seed ensures tests are deterministic across runs.
    private static final long SEED = 42L;

    /**
     * Helper that sums the number of aircraft returned across all ticks
     * from 0 to duration + 30 (a generous buffer for Gaussian-shifted entry ticks).
     *
     * @param gen      the AircraftGenerator to query
     * @param inbound  true to count inbound aircraft, false to count outbound aircraft
     * @param duration the simulation duration in ticks
     * @return the total number of aircraft generated across all ticks
     */
    private static int countAllAircraft(AircraftGenerator gen, boolean inbound, int duration) {
        int total = 0;
        for (int tick = 0; tick <= duration + 30; tick++) {
            List<Aircraft> list = inbound ? gen.getInboundForTick(tick) : gen.getOutboundForTick(tick);
            total += list.size();
        }
        return total;
    }

    // Before initialiseSchedules

    /**
     * Verifies that getInboundForTick returns an empty list (not null) before
     * initialiseSchedules has been called.
     */
    @Test
    void getInboundForTick_beforeInitialise_shouldReturnEmptyList() {
        AircraftGenerator gen = new AircraftGenerator(new Random(SEED), 1, 1, "TestAirport");

        List<Aircraft> result = gen.getInboundForTick(0);

        assertNotNull(result, "getInboundForTick should never return null.");
        assertTrue(result.isEmpty(), "No inbound aircraft should exist before initialiseSchedules is called.");
    }

    /**
     * Verifies that getOutboundForTick returns an empty list (not null) before
     * initialiseSchedules has been called.
     */
    @Test
    void getOutboundForTick_beforeInitialise_shouldReturnEmptyList() {
        AircraftGenerator gen = new AircraftGenerator(new Random(SEED), 1, 1, "TestAirport");

        List<Aircraft> result = gen.getOutboundForTick(0);

        assertNotNull(result, "getOutboundForTick should never return null.");
        assertTrue(result.isEmpty(), "No outbound aircraft should exist before initialiseSchedules is called.");
    }

    // Aircraft counts after initialiseSchedules

    /**
     * Verifies that the total number of inbound aircraft generated equals the expected count.
     * For a rate of N aircraft per hour over a 60-tick duration, exactly N aircraft should be
     * produced (one at tick 0 per interval of 60/N ticks).
     * This test covers the loop boundary condition flagged by the developer in the source.
     */
    @Test
    void initialiseSchedules_shouldGenerateCorrectTotalInboundAircraftCount() {
        int duration = 60;

        // Rate 1  → 1 aircraft  (interval=60, ticks: 0)
        AircraftGenerator gen1 = new AircraftGenerator(new Random(SEED), 1, 1, "TestAirport");
        gen1.initialiseSchedules(duration);
        assertEquals(1, countAllAircraft(gen1, true, duration),
                "Rate=1 over 60 ticks should produce exactly 1 inbound aircraft.");

        // Rate 2  → 2 aircraft  (interval=30, ticks: 0, 30)
        AircraftGenerator gen2 = new AircraftGenerator(new Random(SEED), 2, 1, "TestAirport");
        gen2.initialiseSchedules(duration);
        assertEquals(2, countAllAircraft(gen2, true, duration),
                "Rate=2 over 60 ticks should produce exactly 2 inbound aircraft.");

        // Rate 3  → 3 aircraft  (interval=20, ticks: 0, 20, 40)
        AircraftGenerator gen3 = new AircraftGenerator(new Random(SEED), 3, 1, "TestAirport");
        gen3.initialiseSchedules(duration);
        assertEquals(3, countAllAircraft(gen3, true, duration),
                "Rate=3 over 60 ticks should produce exactly 3 inbound aircraft.");
    }

    /**
     * Verifies that the total number of outbound aircraft generated equals the expected count
     * using the same boundary logic as the inbound schedule.
     */
    @Test
    void initialiseSchedules_shouldGenerateCorrectTotalOutboundAircraftCount() {
        int duration = 60;

        AircraftGenerator gen1 = new AircraftGenerator(new Random(SEED), 1, 1, "TestAirport");
        gen1.initialiseSchedules(duration);
        assertEquals(1, countAllAircraft(gen1, false, duration),
                "Rate=1 over 60 ticks should produce exactly 1 outbound aircraft.");

        AircraftGenerator gen2 = new AircraftGenerator(new Random(SEED), 1, 2, "TestAirport");
        gen2.initialiseSchedules(duration);
        assertEquals(2, countAllAircraft(gen2, false, duration),
                "Rate=2 over 60 ticks should produce exactly 2 outbound aircraft.");

        AircraftGenerator gen3 = new AircraftGenerator(new Random(SEED), 1, 3, "TestAirport");
        gen3.initialiseSchedules(duration);
        assertEquals(3, countAllAircraft(gen3, false, duration),
                "Rate=3 over 60 ticks should produce exactly 3 outbound aircraft.");
    }

    // Fuel range

    /**
     * Verifies that every generated aircraft has a fuel value in the range [20.0, 60.0]
     * as documented by the generateFuelValue implementation.
     */
    @Test
    void initialiseSchedules_allGeneratedAircraftFuelShouldBeInValidRange() {
        int duration = 60;
        AircraftGenerator gen = new AircraftGenerator(new Random(SEED), 5, 5, "TestAirport");
        gen.initialiseSchedules(duration);

        for (int tick = 0; tick <= duration + 30; tick++) {
            for (Aircraft aircraft : gen.getInboundForTick(tick)) {
                double fuel = aircraft.getFuel();
                assertTrue(fuel >= 20.0 && fuel <= 60.0,
                        "Inbound aircraft fuel " + fuel + " at tick " + tick + " is outside [20.0, 60.0].");
            }
            for (Aircraft aircraft : gen.getOutboundForTick(tick)) {
                double fuel = aircraft.getFuel();
                assertTrue(fuel >= 20.0 && fuel <= 60.0,
                        "Outbound aircraft fuel " + fuel + " at tick " + tick + " is outside [20.0, 60.0].");
            }
        }
    }

    // FlightType correctness

    /**
     * Verifies that aircraft returned by getInboundForTick have FlightType.ARRIVAL,
     * and aircraft returned by getOutboundForTick have FlightType.DEPARTURE.
     */
    @Test
    void initialiseSchedules_generatedAircraftShouldHaveCorrectFlightType() {
        int duration = 60;
        AircraftGenerator gen = new AircraftGenerator(new Random(SEED), 3, 3, "TestAirport");
        gen.initialiseSchedules(duration);

        for (int tick = 0; tick <= duration + 30; tick++) {
            for (Aircraft aircraft : gen.getInboundForTick(tick)) {
                assertEquals(FlightType.ARRIVAL, aircraft.getFlightType(),
                        "Aircraft from getInboundForTick should have FlightType.ARRIVAL.");
            }
            for (Aircraft aircraft : gen.getOutboundForTick(tick)) {
                assertEquals(FlightType.DEPARTURE, aircraft.getFlightType(),
                        "Aircraft from getOutboundForTick should have FlightType.DEPARTURE.");
            }
        }
    }

    // Empty-tick safety

    /**
     * Verifies that querying a tick that has no scheduled aircraft returns an empty list,
     * not null (guarded by getOrDefault in the implementation).
     */
    @Test
    void getInboundForTick_onEmptyTick_shouldReturnEmptyListNotNull() {
        AircraftGenerator gen = new AircraftGenerator(new Random(SEED), 1, 1, "TestAirport");
        gen.initialiseSchedules(60);

        // Tick 9999 will never have aircraft; should return empty, not throw or return null.
        List<Aircraft> result = gen.getInboundForTick(9999);
        assertNotNull(result, "getInboundForTick should never return null for an empty tick.");
        assertTrue(result.isEmpty(), "getInboundForTick should return an empty list for a tick with no scheduled aircraft.");
    }
}
