package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class OperatorLocationsData {

    // This data is again based on London Heathrow.
    // The destinations are derived from a table found here: https://en.wikipedia.org/wiki/Heathrow_Airport
    // For airlines with <= 5 destinations, only these were selected.
    // For airlines with > 5 destinations, 5 of these were randomly selected.
    private final Map<String, List<String>> operatorLocations = new HashMap<>();

    private final Random random;

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

    // Function to pick a location given an operator, throws an IllegalArgumentException if the operator doesn't exist.
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
