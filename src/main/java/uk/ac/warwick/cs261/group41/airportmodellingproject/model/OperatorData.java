package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import java.util.*;

// This class holds the data used to populate the callsign, operator, origin and destination fields of aircraft.
// In future this could be moved to a file.
public class OperatorData {

    // Maps Operator Name (e.g. British Airways) to its Prefix, the ICAO Code for the operator (e.g. BAW)
    private final Map<String, String> prefixes = new HashMap<>();

    // Maps the Operator Name to its current aircraft counter.
    // Allows callsigns to consist of Prefix + count for that operator.
    private final Map<String, Integer> counters = new HashMap<>();

    // Map of Operator Name to probability to allow representative variation of different airlines (based on Heathrow).
    // Note we use a LinkedHashMap to ensure the order of the iterator is the same each simulation,
    // ensuring the same operator will be generated for a given random seed.
    private final Map<String, Double> probabilities = new LinkedHashMap<>();

    private final Random random;

    // These operators are based on the 20 airlines with the most operations at London Heathrow:
    // https://simpleflying.com/london-heathrow-largest-airlines-guide/
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

    // Helper function to make adding operators and their prefix easier.
    private void addOperator(String name, String prefix, double probability) {
        prefixes.put(name, prefix);
        counters.put(name, 1);
        probabilities.put(name, probability);
    }

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

    // Ensure you pass valid operator names, otherwise it will throw an IllegalArgumentException.
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
