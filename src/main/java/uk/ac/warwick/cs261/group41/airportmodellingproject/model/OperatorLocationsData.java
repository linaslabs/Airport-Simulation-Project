package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/**
 * Holds destination data for airline operators based on London Heathrow routes.
 * Maps each operator to their typical destinations, used by AircraftGenerator
 * to create realistic origin/destination pairs for flights.
 */
public class OperatorLocationsData {

    /** Maps operator name to list of their typical destinations */
    private final Map<String, List<String>> operatorLocations = new HashMap<>();

    /** Random number generator for destination selection */
    private final Random random;

    /**
     * Constructs OperatorLocationsData with destination data based on London Heathrow routes.
     * Destinations are derived from actual airline route data.
     *
     * @param random the random number generator for destination selection
     */
    public OperatorLocationsData(Random random) {
        operatorLocations.put("British Airways", List.of("Amsterdam", "Berlin", "Istanbul", "Riyadh", "Valencia"));
        operatorLocations.put("Virgin Atlantic", List.of("Atlanta", "Boston", "Miami", "Las Vegas", "New York-JFK"));
        operatorLocations.put("Lufthansa", List.of("Frankfurt", "Munich", "Salzburg"));
        operatorLocations.put("Aer Lingus", List.of("Cork", "Dublin", "Knock", "Shannon"));
        operatorLocations.put("United Airlines", List.of("Chicago-O'Hare", "Denver", "Los Angeles", "Newark", "Washington-Dulles"));
        operatorLocations.put("American Airlines", List.of("Charlotte", "Dallas/Fort Worth", "Los Angeles", "New York-JFK", "Philadelphia"));
        operatorLocations.put("Scandinavian Airlines", List.of("Copenhagen", "Oslo", "Stockholm-Arlanda", "Sälen-Trysil"));
        operatorLocations.put("Swiss International Air Lines", List.of("Geneva", "Zurich"));
        operatorLocations.put("Delta Air Lines", List.of("Detroit", "Minneapolis/St. Paul", "New York-JFK", "Salt Lake City", "Seattle/Tacoma"));
        operatorLocations.put("Eurowings", List.of("Berlin", "Cologne/Bonn", "Düsseldorf", "Hamburg", "Stuttgart"));
        operatorLocations.put("KLM Royal Dutch Airlines", List.of("Amsterdam"));
        operatorLocations.put("Air Canada", List.of("Calgary", "Halifax", "Montréal–Trudeau", "Ottawa", "Vancouver"));
        operatorLocations.put("Iberia", List.of("Madrid"));
        operatorLocations.put("Air France", List.of("Paris-Charles de Gaulle"));
        operatorLocations.put("Qatar Airways", List.of("Doha"));
        operatorLocations.put("Turkish Airlines", List.of("Istanbul"));
        operatorLocations.put("Emirates", List.of("Dubai-International"));
        operatorLocations.put("TAP Air Portugal", List.of("Lisbon"));
        operatorLocations.put("Cathay Pacific", List.of("Hong Kong"));
        operatorLocations.put("Etihad Airways", List.of("Abu Dhabi"));
        this.random = random;
    }

    /**
     * Selects a random destination for the specified operator.
     *
     * @param operator the operator name
     * @return a random destination from the operator's route network
     * @throws IllegalArgumentException if the operator is unknown
     */
    public String getRandomLocationForOperator(String operator) {
        List<String> locations = operatorLocations.get(operator);

        if (locations == null) {
            throw new IllegalArgumentException("Unknown operator name: " + operator);
        }

        // Could consider what happens if locations.isEmpty(), but this shouldn't be needed as
        // the locations are all hardcoded and clearly all populated.

        return locations.get(random.nextInt(locations.size()));
    }
}
