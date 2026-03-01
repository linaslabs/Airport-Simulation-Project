package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class AircraftGenerator {
    private final Random random;
    private final int inboundRate;
    private final int outboundRate;
    private final Map<Integer, List<Aircraft>> inboundSchedule;
    private final Map<Integer, List<Aircraft>> outboundSchedule;
    private final OperatorData operatorData;
    private final OperatorLocationsData operatorLocationsData;
    private final String airportName;

    public AircraftGenerator(Random random, int inboundRate, int outboundRate, String airportName) {
        this.random = random;
        this.inboundRate = inboundRate;
        this.outboundRate = outboundRate;
        this.airportName = airportName;

        // Currently these schedules are HashMaps.
        // This gives O(1) lookups, however iterating through the schedule will occur in random order.
        // If we need to iterate through schedule, change to a TreeMap, however this means O(log n) lookups.
        inboundSchedule = new HashMap<>();
        outboundSchedule = new HashMap<>();

        operatorData = new OperatorData(random);
        operatorLocationsData = new OperatorLocationsData(random);
    }

    // Can add EmergencyStatus as a parameter to this function in future if needed when Events are added.
    private Aircraft generateAircraft(int scheduledTick, FlightType type, boolean autoEnabled, double mechanicalRate, double healthRate) {
        String operator = operatorData.getRandomOperatorName();
        String callsign = operatorData.getNextCallsign(operator);
        String origin;
        String destination;

        if (type == FlightType.ARRIVAL) {
            origin = operatorLocationsData.getRandomLocationForOperator(operator);
            destination = airportName;
        }
        else {
            origin = airportName;
            destination = operatorLocationsData.getRandomLocationForOperator(operator);
        }

        double fuel = generateFuelValue();
        int entryTick = generateEntryTick(scheduledTick);


        // Statistical event modelling - determining if the plane generated should generate with an emergency
        EmergencyStatus status = EmergencyStatus.NONE;

        if (autoEnabled) {
            // Generate a value between 0 and 1
            double roll = random.nextDouble();

            if (roll < mechanicalRate) {
                status = EmergencyStatus.MECHANICAL;
            } else if (roll < (mechanicalRate + healthRate)) {
                // If the roll hasn't landed in the range 0 to mechanicalRate, this "else if" checks that the roll lands mechanicalRate < x < mechanicalRate + healthRate i.e. the range for the health rate
                status = EmergencyStatus.PASSENGER;
            }
        }

        return new Aircraft(callsign, operator, origin, destination, fuel, scheduledTick, entryTick, type, status);
    }

    // Function responsible for generating a fuel level uniformly distributed between 20 and 60.
    private double generateFuelValue() {
        double min = 20.0;
        double max = 60.0;

        return min + (random.nextDouble() * (max - min));
    }



    // Function responsible for the Gaussian variation with std dev. 5 of scheduledTick.
    private int generateEntryTick(int scheduledTick) {
        double standardDeviation = 5.0;

        double generatedEntryTick = (random.nextGaussian() * standardDeviation) + scheduledTick;

        int roundedEntryTick = (int) Math.round(generatedEntryTick);

        // Ensure roundedEntryTick is not less than 0.
        return Math.max(0, roundedEntryTick);
    }

    // We initialise the entire schedule of planes up to the duration of the simulation at the start.
    // I am working off the assumption that one tick is one minute,
    // and the way we speed up time is by advancing more than one tick per iteration.
    public void initialiseSchedules(int duration, boolean autoEnabled, double mechanicalRate, double healthRate) {
        double inboundInterval = 60.0 / inboundRate; // This gives the interval between each scheduled tick in minutes.

        // Need to write a test to check that the correct number of planes are generated.
        // Say the duration is 60 minutes, due to rounding errors, the last iteration may be 60.000001, so
        // the last plane won't get spawned and the actual spawn rate will be 1 less than intended.
        // So, we need to write a test to check that the correct number of planes are generated.
        // If they are not then we could change the condition to "inboundUnroundedTick <= duration + 0.0001" for example.
        // Changed comparison to inboundUnroundedTick < duration instead of <= (since a plane is generated at tick 0)
        for (double inboundUnroundedTick = 0.0; inboundUnroundedTick < duration; inboundUnroundedTick += inboundInterval) {
            // Round to the nearest integer as all scheduled ticks are integers.
            int scheduledTick = (int) Math.round(inboundUnroundedTick);

            // Generate the actual aircraft for the simulation so we can access its randomized entryTick
            // autoEnabled is a boolean value that is true if the user wants random emergency aircraft generation
            Aircraft newAircraft = generateAircraft(scheduledTick, FlightType.ARRIVAL, autoEnabled, mechanicalRate, healthRate);

            // Use the actual entry tick as the map key
            int actualArrivalSpawnTick = newAircraft.getEntryTick();

            // Get the list at the scheduledTick
            List<Aircraft> tickAircraft = inboundSchedule.get(actualArrivalSpawnTick);

            // If it is empty, initialise an empty list first.
            if (tickAircraft == null) {
                tickAircraft = new ArrayList<>();
                inboundSchedule.put(actualArrivalSpawnTick, tickAircraft);
            }

            // Add new aircraft to this list.
            tickAircraft.add(newAircraft);
        }

        // Outbound, repeat the same proces.
        double outboundInterval = 60.0 / outboundRate; // This gives the interval between each scheduled tick in minutes.

        // Changed comparison to inboundUnroundedTick < duration instead of <= (since a plane is generated at tick 0)
        for (double outboundUnroundedTick = 0.0; outboundUnroundedTick < duration; outboundUnroundedTick += outboundInterval) {
            // Round to the nearest integer as all scheduled ticks are integers.
            int scheduledTick = (int) Math.round(outboundUnroundedTick);

            // Generate the actual aircraft for the simulation so we can access its randomized entryTick
            Aircraft newAircraft = generateAircraft(scheduledTick, FlightType.DEPARTURE, autoEnabled, mechanicalRate, healthRate);

            // Use the actual entry tick as the map key
            int actualDepartureSpawnTick = newAircraft.getEntryTick();

            // Get the list at the scheduledTick
            List<Aircraft> tickAircraft = outboundSchedule.get(actualDepartureSpawnTick);

            // If it is empty, initialise an empty list first.
            if (tickAircraft == null) {
                tickAircraft = new ArrayList<>();
                outboundSchedule.put(actualDepartureSpawnTick, tickAircraft);
            }

            // Add new aircraft to this list.
            tickAircraft.add(newAircraft);
        }
    }

    // These methods will be called for each tick of the simulation, therefore they must correctly handle when there are no aircraft for a given tick.
    // This is done by returning an empty list if the tick isn't in the schedule map.
    // The SimulationEngine will then skip over this when trying to iterate over it, but it will not cause a NullPointerException.

    // Method for returning the inbound aircraft scheduled for a given tick.
    public List<Aircraft> getInboundForTick(int currentTick) {
        // getOrDefault returns an empty list if the tick isn't in the map
        return inboundSchedule.getOrDefault(currentTick, List.of());
    }

    // Method for returning the outbound aircraft scheduled for a given tick.
    public List<Aircraft> getOutboundForTick(int currentTick) {
        // getOrDefault returns an empty list if the tick isn't in the map
        return outboundSchedule.getOrDefault(currentTick, List.of());
    }
}
