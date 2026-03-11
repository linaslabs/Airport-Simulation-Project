package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OperatorLocationsData.
 *
 * Covers: valid location lookup for known operators, unknown operator rejection,
 * and result always being within the expected set of locations.
 */
class OperatorLocationsDataTest {

    /**
     * Verifies that getRandomLocationForOperator() returns a non-null location
     * for a known operator.
     */
    @Test
    void getRandomLocationForOperator_shouldReturnNonNull_forKnownOperator() {
        OperatorLocationsData data = new OperatorLocationsData(new Random(1));

        String location = data.getRandomLocationForOperator("British Airways");

        assertNotNull(location, "Expected a non-null location for a known operator.");
        assertFalse(location.isBlank(), "Expected a non-blank location string.");
    }

    /**
     * Verifies that getRandomLocationForOperator() only returns locations from the
     * known set for that operator. British Airways has 5 destinations.
     */
    @Test
    void getRandomLocationForOperator_shouldOnlyReturnValidLocations_forBritishAirways() {
        Set<String> expected = new HashSet<>(Set.of(
                "Amsterdam", "Berlin", "Istanbul", "Riyadh", "Valencia"
        ));
        OperatorLocationsData data = new OperatorLocationsData(new Random(42));

        for (int i = 0; i < 50; i++) {
            String location = data.getRandomLocationForOperator("British Airways");
            assertTrue(expected.contains(location),
                    "Unexpected location '" + location + "' returned for British Airways.");
        }
    }

    /**
     * Verifies that operators with only one destination always return that destination.
     * KLM Royal Dutch Airlines only flies to Amsterdam.
     */
    @Test
    void getRandomLocationForOperator_shouldReturnOnlyDestination_forSingleDestinationOperator() {
        OperatorLocationsData data = new OperatorLocationsData(new Random(1));

        for (int i = 0; i < 10; i++) {
            String location = data.getRandomLocationForOperator("KLM Royal Dutch Airlines");
            assertEquals("Amsterdam", location,
                    "KLM Royal Dutch Airlines should always return Amsterdam as its only destination.");
        }
    }

    /**
     * Verifies that getRandomLocationForOperator() throws IllegalArgumentException
     * for an operator name that does not exist in the data.
     */
    @Test
    void getRandomLocationForOperator_shouldThrow_forUnknownOperator() {
        OperatorLocationsData data = new OperatorLocationsData(new Random(1));

        assertThrows(IllegalArgumentException.class,
                () -> data.getRandomLocationForOperator("Unknown Airline"),
                "Expected IllegalArgumentException for an unknown operator name.");
    }
}
