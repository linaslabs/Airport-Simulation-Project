package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.FlightType;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/**
 * Generates aircraft for the airport simulation based on configured inbound and outbound rates.
 * Creates schedules of arrivals and departures with randomised entry times, fuel levels,
 * and realistic operator/destination data based on London Heathrow statistics.
 */
public class AircraftGenerator {
    /** Random number generator for stochastic elements */
    private final Random random;
    
    /** Number of inbound aircraft per hour */
    private final int inboundRate;
    
    /** Number of outbound aircraft per hour */
    private final int outboundRate;
    
    /** Schedule mapping tick numbers to lists of arriving aircraft */
    private final Map<Integer, List<Aircraft>> inboundSchedule;
    
    /** Schedule mapping tick numbers to lists of departing aircraft */
    private final Map<Integer, List<Aircraft>> outboundSchedule;
    
    /** Data provider for airline operators and callsigns */
    private final OperatorData operatorData;
    
    /** Data provider for operator route destinations */
    private final OperatorLocationsData operatorLocationsData;
    
    /** Name of the airport being simulated */
    private final String airportName;

    /**
     * Constructs a new AircraftGenerator with the specified parameters.
     *
     * @param random the random number generator for stochastic elements
     * @param inboundRate number of arriving aircraft per hour
     * @param outboundRate number of departing aircraft per hour
     * @param airportName name of the airport being simulated
     */
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

    /**
     * Generates a single aircraft with randomised attributes.
     *
     * @param scheduledTick the tick when the aircraft is scheduled
     * @param type whether this is an ARRIVAL or DEPARTURE
     * @return a new Aircraft instance with generated attributes
     */
    private Aircraft generateAircraft(int scheduledTick, FlightType type) {
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

        return new Aircraft(callsign, operator, origin, destination, fuel, scheduledTick, entryTick, type);
    }

    /**
     * Generates a random fuel level uniformly distributed between 20 and 60 minutes.
     *
     * @return the generated fuel value
     */
    private double generateFuelValue() {
        double min = 20.0;
        double max = 60.0;

        return min + (random.nextDouble() * (max - min));
    }

    /**
     * Generates an entry tick with Gaussian variation from the scheduled tick.
     * Uses a standard deviation of 5 minutes to simulate realistic arrival variance.
     *
     * @param scheduledTick the originally scheduled tick
     * @return the actual entry tick (minimum 0)
     */
    private int generateEntryTick(int scheduledTick) {
        double standardDeviation = 5.0;

        double generatedEntryTick = (random.nextGaussian() * standardDeviation) + scheduledTick;

        int roundedEntryTick = (int) Math.round(generatedEntryTick);

        // Ensure roundedEntryTick is not less than 0.
        return Math.max(0, roundedEntryTick);
    }

    /**
     * Initialises the complete aircraft schedules for the simulation duration.
     * Generates all inbound and outbound aircraft based on configured rates.
     * One tick equals one minute; time acceleration is handled by advancing multiple ticks.
     *
     * @param duration the total simulation duration in ticks (minutes)
     */
    public void initialiseSchedules(int duration) {
        double inboundInterval = 60.0 / inboundRate; // This gives the interval between each scheduled tick in minutes.

        for (double inboundUnroundedTick = 0.0; inboundUnroundedTick < duration; inboundUnroundedTick += inboundInterval) {
            // Round to the nearest integer as all scheduled ticks are integers.
            int scheduledTick = (int) Math.round(inboundUnroundedTick);

            // Generate the actual aircraft for the simulation so we can access its randomized entryTick
            // autoEnabled is a boolean value that is true if the user wants random emergency aircraft generation
            Aircraft newAircraft = generateAircraft(scheduledTick, FlightType.ARRIVAL);

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
            Aircraft newAircraft = generateAircraft(scheduledTick, FlightType.DEPARTURE);

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

    /**
     * Gets the list of inbound aircraft scheduled to arrive at the given tick.
     * Returns an empty list if no aircraft are scheduled for that tick.
     *
     * @param currentTick the current simulation tick
     * @return list of arriving aircraft, or empty list if none
     */
    public List<Aircraft> getInboundForTick(int currentTick) {
        // getOrDefault returns an empty list if the tick isn't in the map
        return inboundSchedule.getOrDefault(currentTick, List.of());
    }

    /**
     * Gets the list of outbound aircraft scheduled to depart at the given tick.
     * Returns an empty list if no aircraft are scheduled for that tick.
     *
     * @param currentTick the current simulation tick
     * @return list of departing aircraft, or empty list if none
     */
    public List<Aircraft> getOutboundForTick(int currentTick) {
        // getOrDefault returns an empty list if the tick isn't in the map
        return outboundSchedule.getOrDefault(currentTick, List.of());
    }
}
