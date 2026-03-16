package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OperatorData.
 *
 * Covers: callsign format and counter incrementing, unknown operator rejection,
 * and random operator selection always returning a valid known operator.
 */
class OperatorDataTest {

    /**
     * Verifies that getNextCallsign() returns the operator prefix concatenated with
     * a counter starting at 1, and that each consecutive call increments the counter.
     */
    @Test
    void getNextCallsign_shouldReturnPrefixPlusCounter_andIncrement() {
        OperatorData data = new OperatorData(new Random(1));

        String first  = data.getNextCallsign("British Airways");
        String second = data.getNextCallsign("British Airways");
        String third  = data.getNextCallsign("British Airways");

        assertEquals("BAW1", first,  "First callsign should be BAW1.");
        assertEquals("BAW2", second, "Second callsign should be BAW2.");
        assertEquals("BAW3", third,  "Third callsign should be BAW3.");
    }

    /**
     * Verifies that counters are tracked independently per operator —
     * calling getNextCallsign() for one operator does not affect another.
     */
    @Test
    void getNextCallsign_shouldTrackCounters_independentlyPerOperator() {
        OperatorData data = new OperatorData(new Random(1));

        data.getNextCallsign("British Airways"); // BAW1
        data.getNextCallsign("British Airways"); // BAW2

        // Virgin Atlantic counter should still start at 1.
        String virFirst = data.getNextCallsign("Virgin Atlantic");
        assertEquals("VIR1", virFirst,
                "Virgin Atlantic counter should be independent of British Airways counter.");
    }

    /**
     * Verifies that getNextCallsign() throws IllegalArgumentException for an operator
     * name that does not exist in the data.
     */
    @Test
    void getNextCallsign_shouldThrow_forUnknownOperator() {
        OperatorData data = new OperatorData(new Random(1));

        assertThrows(IllegalArgumentException.class,
                () -> data.getNextCallsign("Unknown Airline"),
                "Expected IllegalArgumentException for an unknown operator name.");
    }

    /**
     * Verifies that getRandomOperatorName() always returns a non-null operator name
     * that is one of the known operators across many calls with different seeds.
     */
    @Test
    void getRandomOperatorName_shouldAlwaysReturnKnownNonNullOperator() {
        // Known operators that exist in OperatorData
        Set<String> known = new HashSet<>(Set.of(
                "British Airways", "Virgin Atlantic", "Lufthansa", "Aer Lingus",
                "United Airlines", "American Airlines", "Scandinavian Airlines",
                "Swiss International Air Lines", "Delta Air Lines", "Eurowings",
                "KLM Royal Dutch Airlines", "Air Canada", "Iberia", "Air France",
                "Qatar Airways", "Turkish Airlines", "Emirates", "TAP Air Portugal",
                "Cathay Pacific", "Etihad Airways"
        ));

        OperatorData data = new OperatorData(new Random(42));

        for (int i = 0; i < 200; i++) {
            String operator = data.getRandomOperatorName();
            assertNotNull(operator, "getRandomOperatorName() should never return null.");
            assertTrue(known.contains(operator),
                    "Returned operator '" + operator + "' is not in the known operators list.");
        }
    }

    /**
     * Verifies that British Airways is the most commonly selected operator,
     * consistent with its probability of 0.601 (highest share).
     * Over a large sample, it should appear in more than 50% of selections.
     */
    @Test
    void getRandomOperatorName_shouldSelectBritishAirways_mostFrequently() {
        OperatorData data = new OperatorData(new Random(0));
        int baCount = 0;
        int total = 1000;

        for (int i = 0; i < total; i++) {
            if ("British Airways".equals(data.getRandomOperatorName())) {
                baCount++;
            }
        }

        assertTrue(baCount > total / 2,
                "British Airways (probability 0.601) should be selected in over 50% of calls. Got: " + baCount + "/" + total);
    }
}
