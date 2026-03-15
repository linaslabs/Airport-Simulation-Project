package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.AircraftEvent;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayEvent;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Aircraft;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Airport;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Runway;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Statistics;

import java.util.*;

/**
 * Represents an event manager to handles simulation events (runway events and aircraft emergencies)
 *
 * Responsible for initiating manual, scheduled and random events
 * Handles reporting of events and logging to an event logger object to ensure simulation traceability
 * Acts as a central mediator for events to be processed, ensuring single responsibility is maintained
 */
public class EventManager {

    private static final Logger log = LoggerFactory.getLogger(EventManager.class);

    private final EventLogger logger;
    private final Map<Integer, List<RunwayEvent>> scheduledRunwayEvents;
    private final Map<Integer, List<AircraftEvent>> scheduledAircraftEvents;

    private final Random random;
    private final Airport airport;
    private final Statistics statistics;

    /**
     * Constructor to initialise the event manager object and generate the scheduled events for the simulation
     * Scheduled events are generated before the simulation in order to distribute arrival times according to the specification
     *
     * @param logger the event logger object for the simulatoion
     * @param scheduledRunwayEvents a mapping of a tick in the simulation to a runway event
     * @param scheduledAircraftEvents a mapping of a tick in the simulation to an aircraft event
     * @param random the random seed used in the simulation, added for reproducibility
     * @param airport the airport object of the simulation
     * @param statistics the statistics object of the simulation
     */
    public EventManager(EventLogger logger, Map<Integer, List<RunwayEvent>> scheduledRunwayEvents, Map<Integer, List<AircraftEvent>> scheduledAircraftEvents, Random random, Airport airport, Statistics statistics) {
        this.logger = logger;

        // Deep copy for runway events
        this.scheduledRunwayEvents = new HashMap<>();
        if (scheduledRunwayEvents != null) {
            for (Map.Entry<Integer, List<RunwayEvent>> entry : scheduledRunwayEvents.entrySet()) {
                this.scheduledRunwayEvents.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }

        // Deep copy for aircraft events
        this.scheduledAircraftEvents = new HashMap<>();
        if (scheduledAircraftEvents != null) {
            for (Map.Entry<Integer, List<AircraftEvent>> entry : scheduledAircraftEvents.entrySet()) {
                this.scheduledAircraftEvents.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }

        this.random = random;
        this.airport = airport;
        this.statistics = statistics;
    }

    /**
     * Method to manually add a new scheduled runway event to the scheduled map
     * Primarily used for creating reversion events for runway events with durations
     * Can be used as a general method to add events to the simulation after initialisation
     */
    public void addScheduledRunwayEvent(RunwayEvent runwayEvent) {
        List<RunwayEvent> runwayEvents = scheduledRunwayEvents.get(runwayEvent.getTick());
        if (runwayEvents == null) {
            List<RunwayEvent> newRunwayEvents = new ArrayList<>();
            newRunwayEvents.add(runwayEvent);
            scheduledRunwayEvents.put(runwayEvent.getTick(), newRunwayEvents);
        } else  {
            // If the runway event is of duration 0, it is a reversion event
            if (runwayEvent.getDuration() == 0) {
                // We place this reversion event as the first event in the runway event list for this tick (so reversions happen before anything else)
                runwayEvents.add(0, runwayEvent);
            } else {
                // Otherwise append like normal
                runwayEvents.add(runwayEvent);
            }
        }
    }

    /**
     * Method to perform a similar operation as "addScheduledRunwayEvent"
     * Not currently used in the simulation processes, but implemented with consideration of future extension of the program
     */
    public void addScheduledAircraftEvent(AircraftEvent aircraftEvent) {
        List<AircraftEvent> aircraftEvents = scheduledAircraftEvents.get(aircraftEvent.getTick());
        if (aircraftEvents == null) {
            List<AircraftEvent> newAircraftEvents = new ArrayList<>();
            newAircraftEvents.add(aircraftEvent);
            scheduledAircraftEvents.put(aircraftEvent.getTick(), newAircraftEvents);
        } else {
            aircraftEvents.add(aircraftEvent);
        }
    }

    // Triggers and logs a runway event, events can be scheduled, random or manual
    // If a duration is set, then the reverse of that change is scheduled for the future at the end of the duration

    /**
     * Method to trigger and log a runway event
     * Events can be scheduled, random or manual, described through the arguments passed into the method
     * If a duration is set for the event, the reversion event is scheduled for the future at the end of the duration
     */
    public synchronized void triggerRunwayEvent(int runwayID, RunwayStatus status,  RunwayMode mode, int currentTick, int duration, boolean isRandomEvent, boolean isManual) {

        // Frontend can set status or mode to "null", in this case we use its current status or mode when logging the runway event with null
        RunwayConfig runwaySnapshot = this.airport.getRunwaySnapshot(runwayID);
        RunwayStatus nonNullStatus = (status != null) ? status : runwaySnapshot.getStatus();
        RunwayMode nonNullMode = (mode != null) ? mode : runwaySnapshot.getMode();

        log.info("[TICK {}] [RUNWAY] ID: {} | Status: {} | Mode: {} | Duration: {} | Random: {}", currentTick, runwayID, nonNullStatus, nonNullMode, duration, isRandomEvent);

        // Create a reversion if duration is set
        if (duration > 0){
            int endTick = currentTick + duration;
            log.info("            -> Scheduled Reversion created for Tick: {}", endTick);
            // Create the new runway reversion (that is of duration 0) to reverse this event when it finishes
            RunwayEvent reversionEvent = new RunwayEvent(endTick, runwayID, runwaySnapshot.getStatus(), runwaySnapshot.getMode(), RunwayEventType.REVERSION, 0);
            addScheduledRunwayEvent(reversionEvent);
        }

        // The event is a reversion if it isn't a random event, isn't manual and the duration is 0
        boolean isReversion = (!isRandomEvent && !isManual && duration == 0);

        // Both the source (to describe the event) and the type (for the log) are declared initially
        EventSource source;
        RunwayEventType eventType;

        if (isRandomEvent) {
            source = EventSource.RANDOM;
            eventType = RunwayEventType.RANDOMLY_GENERATED_CLOSURE;
        } else if (isManual) {
            source = EventSource.MANUAL;
            eventType = RunwayEventType.MANUAL_CHANGE;
        } else if (isReversion) {
            source = EventSource.NONE;
            eventType = RunwayEventType.REVERSION;
        } else {
            source = EventSource.SCHEDULED;
            eventType = RunwayEventType.SCHEDULED_CHANGE;
        }

        this.airport.updateRunway(runwayID, status, mode, source);

        this.logger.addEvent(new RunwayEvent(currentTick, runwayID, nonNullStatus, nonNullMode, eventType, duration));
    }

    /**
     * Method to generate the random aircraft emergencies and the random runway status changes for statistical modelling based on the configured rates
     * It is only called if the user permits random events in the simulation
     */
    public void generateRandomEventsForTick(int currentTick, double inspectionRate, double snowRate, double failureRate, double mechanicalRate, double healthRate) {

        // Aircraft emergencies
        List<Aircraft> holdingAircraft = this.airport.getHoldingPattern().getAircraftInQueue();

        // Roll with the defined rates for each aircraft in the holding pattern
        for (Aircraft aircraft : holdingAircraft) {
            if (aircraft.getStatus() == EmergencyStatus.NONE){
                double emergencyRoll = this.random.nextDouble();

                if (emergencyRoll < mechanicalRate) {
                    log.info("[TICK {}] [RANDOM] Rolled Aircraft Emergency: MECHANICAL", currentTick);
                    triggerAircraftEmergency(aircraft.getCallsign(), EmergencyStatus.MECHANICAL, currentTick, true);
                } else if (emergencyRoll < (mechanicalRate + healthRate)) {
                    log.info("[TICK {}] [RANDOM] Rolled Aircraft Emergency: PASSENGER", currentTick);
                    triggerAircraftEmergency(aircraft.getCallsign(), EmergencyStatus.PASSENGER, currentTick, true);
                }
            }
        }


        // Runway closures
        Collection<Runway> runways = this.airport.getRunways();

        // Roll with the defined rates for each runway
        for (Runway runway : runways) {
            // Check if the status of the runway is available (failures can still happen if runways are occupied)
            if(runway.getStatus() == RunwayStatus.AVAILABLE) {
                double runwayRoll = this.random.nextDouble();
                RunwayStatus newRunwayStatus = null;

                if (runwayRoll < inspectionRate) {
                    newRunwayStatus = RunwayStatus.INSPECTION;
                } else if (runwayRoll < (inspectionRate + snowRate)) {
                    newRunwayStatus = RunwayStatus.SNOW_CLEARANCE;
                } else if (runwayRoll < (inspectionRate + snowRate + failureRate)) {
                    newRunwayStatus = RunwayStatus.EQUIPMENT_FAILURE;
                }

                // Checking if the roll landed to satisfy one of the rates
                if (newRunwayStatus != null) {
                    log.info("[TICK {}] [RANDOM] Rolled Runway Closure: {} on ID: {}", currentTick, newRunwayStatus, runway.getRunwayID());

                    // Generating a random number between 10 and 60 ticks
                    int duration = 10 + this.random.nextInt(51);

                    // Find the ticks until the next scheduled event for this runway
                    int ticksUntilNextEvent = ticksUntilNextScheduledRunwayEvent(runway.getRunwayID(), currentTick);

                    // Make sure that the duration of this random event is CAPPED so it finishes before the next scheduled runway event
                    if (ticksUntilNextEvent > 0 && duration > ticksUntilNextEvent) {
                        log.info("            -> TRUNCATED duration from {} to {} to prevent overlap with user schedule.", duration, ticksUntilNextEvent);
                        duration = ticksUntilNextEvent;
                    }

                    triggerRunwayEvent(runway.getRunwayID(),  newRunwayStatus, runway.getMode(), currentTick, duration, true, false);
                }
            }
        }
    }

    /**
     * Function that calculates the time until the next scheduled event to happen on the specified runway
     * @return the number of ticks until the next scheduled event for the runway
     */
    private int ticksUntilNextScheduledRunwayEvent(int runwayID, int currentTick) {
        // Track the starting tick of the soonest event (will be updated every time we find a sooner event)
        int soonestEventTick = -1;

        // Loop through all scheduled ticks in the map (since each entry will not be in order)
        for (Map.Entry<Integer, List<RunwayEvent>> entry : this.scheduledRunwayEvents.entrySet()) {
            // Get the tick linked to this list of runway events
            int scheduledTick = entry.getKey();

            // Check if this tick linked to the runway events is scheduled for the future
            if (scheduledTick > currentTick) {
                // Check all events scheduled for that specific tick
                for (RunwayEvent event : entry.getValue()) {
                    // Check if the runway event is an event on the current runway ID
                    if (event.getRunwayID() == runwayID) {
                        // If this is the first one we found, or it's sooner than the last one found, save it
                        if (soonestEventTick == -1 || scheduledTick < soonestEventTick) {
                            soonestEventTick = scheduledTick;
                        }
                    }
                }
            }
        }

        // If we found a future event, return the ticks until the event
        // If we didn't, return -1 (meaning infinite free time)
        if (soonestEventTick == -1) {
            return -1;
        } else {
            return soonestEventTick - currentTick;
        }
    }

    /**
     * Triggers and logs an aircraft emergency event
     * Events can be scheduled, random or manual, described through the value of the arguments passed into the method
     *
     * @param callsign can be a null value to signify that the event is a scheduled emergency (we don't know the call signs ahead of time)
     * @param status the emergency status the aircraft should be triggered with
     */
    public synchronized void triggerAircraftEmergency(String callsign, EmergencyStatus status, int currentTick, boolean isRandomEvent){

        log.info("[TICK {}] [AIRCRAFT] Triggering {} Emergency: {}", currentTick, (isRandomEvent ? "RANDOM" : "SCHEDULED/MANUAL"), status);

        if (callsign == null){
            // If callsign is null, then the ONLY option is that this was a scheduled emergency (so we need to randomly pick an aircraft)
            String randomCallsign = this.airport.getRandomHoldingAircraft(this.random);

            // If randomCallsign is null, then there is no aircraft in the holding pattern, so skip
            if (randomCallsign != null) {
                log.info("            -> Random callsign selected: {}", randomCallsign);
                this.airport.updateAircraftStatus(randomCallsign, status, EventSource.SCHEDULED);
                this.logger.addEvent(new AircraftEvent(currentTick, randomCallsign, AircraftEventType.SCHEDULED_EMERGENCY, status));
            } else {
                log.info("            -> WARNING: scheduled emergency skipped (holding pattern is empty).");
            }
        } else {
            // If callsign is not null, this is an emergency on an aircraft chosen by random events, or a manual emergency
            log.info("            -> Applying to specific callsign: {}", callsign);
            if (isRandomEvent) {
                // Random emergency on a chosen aircraft in holding pattern
                this.airport.updateAircraftStatus(callsign, status, EventSource.RANDOM);
                this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.RANDOMLY_GENERATED_EMERGENCY, status));
            } else {
                // Manual emergency from UI
                this.airport.updateAircraftStatus(callsign, status, EventSource.MANUAL);
                this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.MANUAL_EMERGENCY, status));
            }
        }
    }

    // Used only when holding pattern triggers an emergency because of fuel

    /**
     * Method to log a natural aircraft emergency
     * Currently only called by holding pattern when it finds an aircraft is low on fuel
     */
    public void reportAircraftEmergency(String callsign, EmergencyStatus status, int currentTick){
        log.info("[TICK {}] [REPORT] Escalating Natural Emergency for {} (Status: {})", currentTick, callsign, status);
        this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.NATURAL_FUEL_EMERGENCY, status));
    }

    /**
     * Method to log an arrival aircraft diversion (low fuel)
     */
    public void reportDiversion(String callsign, int currentTick){
        log.info("[TICK {}] [REPORT] Aircraft Diverted due to low fuel: {}", currentTick, callsign);
        this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.DIVERSION, EmergencyStatus.FUEL));
        this.statistics.recordDiversion();
    }

    /**
     * Method to log a departure aircraft cancellation (exceeded wait time)
     */
    public void reportCancellation(String callsign, int currentTick){
        log.info("[TICK {}] [REPORT] Aircraft Cancelled: {}", currentTick, callsign);
        this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.CANCELLATION, EmergencyStatus.NONE));
        this.statistics.recordCancellation();
    }

    /**
     * Method to trigger the events scheduled for the current tick (using the event maps)
     */
    public void processScheduledEvents(int currentTick){
        List<RunwayEvent> runwayEvents =  scheduledRunwayEvents.get(currentTick);
        if (runwayEvents != null && !runwayEvents.isEmpty()) {
            log.info("[TICK {}] [SCHEDULE] Processing {} scheduled Runway Event(s).", currentTick, runwayEvents.size());
            for (RunwayEvent runwayEvent : runwayEvents) {
                log.info("            -> Processing {} event", runwayEvent.getType() == RunwayEventType.REVERSION ? "REVERSION" : "SCHEDULED");
                triggerRunwayEvent(runwayEvent.getRunwayID(), runwayEvent.getRunwayStatus(), runwayEvent.getRunwayMode(), currentTick, runwayEvent.getDuration(), false, false);
            }
        }

        List<AircraftEvent> aircraftEvents =  scheduledAircraftEvents.get(currentTick);
        if (aircraftEvents != null && !aircraftEvents.isEmpty()) {
            log.info("[TICK {}] [SCHEDULE] Processing {} scheduled Aircraft Event(s).", currentTick, aircraftEvents.size());
            for (AircraftEvent aircraftEvent : aircraftEvents) {
                triggerAircraftEmergency(null, aircraftEvent.getStatus(),  currentTick, false);
            }
        }
    }

    /**
     * Function to return the scheduled runway events as an unmodifiable map (to make sure the map is read-only)
     * @return an unmodifiable, read-only version of the scheduled map for runway events
     */
    public Map<Integer, List<RunwayEvent>> getScheduledRunwayEvents() { return Collections.unmodifiableMap(this.scheduledRunwayEvents); }

    /**
     * Function to return the scheduled aircraft events as an unmodifiable map (to make sure the map is read-only)
     * @return an unmodifiable, read-only version of the scheduled map for aircraft events
     */
    public Map<Integer, List<AircraftEvent>> getScheduledAircraftEvents() { return Collections.unmodifiableMap(this.scheduledAircraftEvents); }

    public EventLogger getLogger() { return this.logger; }
}