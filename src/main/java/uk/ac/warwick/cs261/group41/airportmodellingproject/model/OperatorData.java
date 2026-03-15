package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import java.util.*;

/**
 * Holds data for airline operators used to generate realistic aircraft callsigns and identities.
 * Contains operator names, ICAO prefixes, and probability distributions based on London Heathrow statistics.
 * Used by AircraftGenerator to create realistic flight data.
 */
public class OperatorData {

    /** Maps operator name to ICAO prefix code (e.g., "British Airways" -> "BAW") */
    private final Map<String, String> prefixes = new HashMap<>();

    /** Maps operator name to current flight counter for generating unique callsigns */
    private final Map<String, Integer> counters = new HashMap<>();

    /** Maps operator name to probability of selection, based on Heathrow traffic data */
    private final Map<String, Double> probabilities = new LinkedHashMap<>();

    /** Random number generator for stochastic operator selection */
    private final Random random;

    /**
     * Constructs OperatorData with the 20 largest airlines at London Heathrow.
     * Probability distributions are based on actual traffic statistics.
     *
     * @param random the random number generator for operator selection
     */
    public OperatorData(Random random) {
        addOperator("British Airways", "BAW", 0.601);
        addOperator("Virgin Atlantic", "VIR", 0.049);
        addOperator("Lufthansa", "DLH", 0.036);
        addOperator("Aer Lingus", "EIN", 0.036);
        addOperator("United Airlines", "UAL", 0.035);
        addOperator("American Airlines", "AAL", 0.034);
        addOperator("Scandinavian Airlines", "SWS", 0.027);
        addOperator("Swiss International Air Lines", "SWR", 0.021);
        addOperator("Delta Air Lines", "DAL", 0.021);
        addOperator("Eurowings", "EWG", 0.021);
        addOperator("KLM Royal Dutch Airlines", "KLM", 0.018);
        addOperator("Air Canada", "ACA", 0.014);
        addOperator("Iberia", "IBE", 0.014);
        addOperator("Air France", "AFR", 0.012);
        addOperator("Qatar Airways", "QTR", 0.011);
        addOperator("Turkish Airlines", "THY", 0.011);
        addOperator("Emirates", "UAE", 0.011);
        addOperator("TAP Air Portugal", "TAP", 0.010);
        addOperator("Cathay Pacific", "CPA", 0.009);
        addOperator("Etihad Airways", "ETD", 0.009);
        this.random = random;
    }

    /**
     * Adds an operator with its ICAO prefix and selection probability.
     *
     * @param name the full operator name
     * @param prefix the ICAO code prefix for callsigns
     * @param probability the probability of this operator being selected
     */
    private void addOperator(String name, String prefix, double probability) {
        prefixes.put(name, prefix);
        counters.put(name, 1);
        probabilities.put(name, probability);
    }

    /**
     * Selects a random operator name based on probability distribution.
     *
     * @return the selected operator name
     */
    public String getRandomOperatorName() {
        // Generate a number between 0.0 and 1.0.
        double p = random.nextDouble();

        double cumulativeProbability = 0.0;
        for(Map.Entry<String, Double> entry : probabilities.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (p <= cumulativeProbability) {
                return entry.getKey();
            }
        }

        // If something goes wrong with precision above, just return the first operator.
        System.out.println("Something went wrong with the precision in Operator Data class, getRandomOperatorName function");
        return probabilities.keySet().iterator().next();
    }

    /**
     * Generates the next unique callsign for the specified operator.
     * Callsigns consist of the ICAO prefix plus an incrementing number.
     *
     * @param operatorName the name of the operator
     * @return the generated callsign (e.g., "BAW1", "BAW2")
     * @throws IllegalArgumentException if the operator name is unknown
     */
    public String getNextCallsign(String operatorName) {
        String prefix = prefixes.get(operatorName);
        Integer count = counters.get(operatorName);

        if (prefix == null || count == null) {
            throw new IllegalArgumentException("Unknown operator name: " + operatorName);
        }

        // Increment for the next flight.
        counters.put(operatorName, count + 1);

        // Perform a simple concatenation, e.g. "BAW" + "1" = "BAW1".
        return prefix + count;
    }
}
