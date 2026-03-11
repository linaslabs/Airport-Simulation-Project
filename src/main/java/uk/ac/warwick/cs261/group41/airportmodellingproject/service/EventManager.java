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

public class EventManager {

    private static final Logger log = LoggerFactory.getLogger(EventManager.class);

    private final EventLogger logger;
    private final Map<Integer, List<RunwayEvent>> scheduledRunwayEvents;
    private final Map<Integer, List<AircraftEvent>> scheduledAircraftEvents;

    private final Random random;
    private final Airport airport;
    private final Statistics statistics;

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

    public void addScheduledRunwayEvent(RunwayEvent runwayEvent) {
        List<RunwayEvent> runwayEvents = scheduledRunwayEvents.get(runwayEvent.getTick());
        if (runwayEvents == null) {
            List<RunwayEvent> newRunwayEvents = new ArrayList<>();
            newRunwayEvents.add(runwayEvent);
            scheduledRunwayEvents.put(runwayEvent.getTick(), newRunwayEvents);
        } else  {
            // If the runway event is of duration 0, it is a reversion event, always call this first
            // i.e. place this reversion event as the first event in the runway event list for this tick
            if (runwayEvent.getDuration() == 0) {
                runwayEvents.add(0, runwayEvent);
            } else {
                // Otherwise append like normal
                runwayEvents.add(runwayEvent);
            }
        }
    }

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
    public void triggerRunwayEvent(int runwayID, RunwayStatus status,  RunwayMode mode, int currentTick, int duration, boolean isRandomEvent, boolean isManual) {

        // Because the "No Change" option in the menu sets status or mode to null in the event, when we log this event we must replace this null with the current status/mode.
        RunwayConfig runway = airport.getRunwaySnapshot(runwayID);
        RunwayStatus nonNullStatus = (status != null) ? status : runway.getStatus();
        RunwayMode nonNullMode = (mode != null) ? mode : runway.getMode();

        log.info("[TICK {}] [RUNWAY] ID: {} | Status: {} | Mode: {} | Duration: {} | Random: {}", currentTick, runwayID, nonNullStatus, nonNullMode, duration, isRandomEvent);

        // Create a reversion if duration is set
        if (duration > 0){
            RunwayConfig runwayPrevSnapshot = this.airport.getRunwaySnapshot(runwayID);

            int endTick = currentTick + duration;
            log.info("            -> Scheduled Reversion created for Tick: {}", endTick);

            // Create the new runway reversion (that is of duration 0) to reverse this event when it finishes
            RunwayEvent reversionEvent = new RunwayEvent(endTick, runwayID, runwayPrevSnapshot.getStatus(), runwayPrevSnapshot.getMode(), RunwayEventType.REVERSION, 0);

            addScheduledRunwayEvent(reversionEvent);
        }

        // The event is a reversion if it isn't a random event, isn't manual and the duration is 0
        boolean isReversion = (!isRandomEvent && !isManual && duration == 0);

        // Both the source (for the lock) and the type (for the log) are declared initially
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


    // Generates the random aircraft emergencies and the random runway status changes for statistical modelling based on the configured rates (only called if the user permits them during the simulation)
    public void generateRandomEventsForTick(int currentTick, double inspectionRate, double snowRate, double failureRate, double mechanicalRate, double healthRate) {

        // --- Generating AIRCRAFT EMERGENCIES
        List<Aircraft> holdingAircraft = this.airport.getHoldingPattern().getAircraftInQueue();

        // Roll the rate per aircraft in the holding pattern
        for (Aircraft aircraft : holdingAircraft) {
            if (aircraft.getStatus() == EmergencyStatus.NONE){
                double emergencyRoll = this.random.nextDouble();

                if (emergencyRoll < mechanicalRate) {
                    log.info("[TICK {}] [RANDOM] Rolled Aircraft Emergency: MECHANICAL", currentTick);
                    triggerAircraftEmergency(aircraft.getCallsign(), EmergencyStatus.MECHANICAL, currentTick, true);
                } else if (emergencyRoll < (mechanicalRate + healthRate)) {
                    System.out.println("EMERGENCY RANDOM");
                    log.info("[TICK {}] [RANDOM] Rolled Aircraft Emergency: PASSENGER", currentTick);
                    triggerAircraftEmergency(aircraft.getCallsign(), EmergencyStatus.PASSENGER, currentTick, true);
                }
            }
        }


        // --- Generating RUNWAY CLOSURES
        double tickInspectionRate =  inspectionRate / 60;
        double tickSnowRate =  snowRate / 60;
        double tickFailureRate = failureRate / 60;

        Collection<Runway> runways = this.airport.getRunways();

        for (Runway runway : runways) {
            // Check if the status of the runway is available (failures can still happen if runways are occupied)
            if(runway.getStatus() == RunwayStatus.AVAILABLE) {
                double runwayRoll = this.random.nextDouble();
                RunwayStatus newRunwayStatus = null;

                if (runwayRoll < tickInspectionRate) {
                    newRunwayStatus = RunwayStatus.INSPECTION;
                } else if (runwayRoll < (tickInspectionRate + tickSnowRate)) {
                    newRunwayStatus = RunwayStatus.SNOW_CLEARANCE;
                } else if (runwayRoll < (tickInspectionRate + tickSnowRate + tickFailureRate)) {
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

                    // Trigger the event
                    triggerRunwayEvent(runway.getRunwayID(),  newRunwayStatus, runway.getMode(), currentTick, duration, true, false);
                }
            }
        }
    }

    // Function to return the ticks until the next scheduled event for the runway
    private int ticksUntilNextScheduledRunwayEvent(int runwayID, int currentTick) {
        // Track the starting tick of the soonest event (will be updated every time we find a sooner event)
        int soonestEventTick = -1;

        // Loop through all scheduled ticks in the map (note: each entry will not be in order)
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

        // If we found a future event, return the gap in minutes
        // If we didn't, return -1 (meaning infinite free time)
        if (soonestEventTick == -1) {
            return -1;
        } else {
            return soonestEventTick - currentTick;
        }
    }

    // Triggers and logs an aircraft emergency event, events can be scheduled, random or manual
    public void triggerAircraftEmergency(String callsign, EmergencyStatus status, int currentTick, boolean isRandomEvent){

        log.info("[TICK {}] [AIRCRAFT] Triggering {} Emergency: {}", currentTick, (isRandomEvent ? "RANDOM" : "SCHEDULED/MANUAL"), status);

        if (callsign == null){
            // If callsign is null, then the ONLY option is that this was a scheduled emergency (so we need to randomly pick an aircraft)
            String randomCallsign = this.airport.getRandomHoldingAircraft(this.random);

            if (randomCallsign != null) {
                log.info("            -> Random callsign selected: {}", randomCallsign);
                this.airport.updateAircraftStatus(randomCallsign, status, EventSource.SCHEDULED);
                this.logger.addEvent(new AircraftEvent(currentTick, randomCallsign, AircraftEventType.SCHEDULED_EMERGENCY, status));
            } else {
                log.info("            -> WARNING: scheduled emergency skipped (holding pattern is empty).");
            }
        } else {
            // Callsign is not null, so could be an emergency on a chosen aircraft (each aircraft has a chance of an emergency per tick), or a manual emergency
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
    public void reportAircraftEmergency(String callsign, EmergencyStatus status, int currentTick){
        log.info("[TICK {}] [REPORT] Escalating Natural Emergency for {} (Status: {})", currentTick, callsign, status);
        this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.NATURAL_FUEL_EMERGENCY, status)); // Added new enum for better differentiation
    }

    public void reportDiversion(String callsign, int currentTick){
        log.info("[TICK {}] [REPORT] Aircraft Diverted due to low fuel: {}", currentTick, callsign);
        this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.DIVERSION, EmergencyStatus.FUEL));
        this.statistics.recordDiversion();
    }

    public void reportCancellation(String callsign, int currentTick){
        log.info("[TICK {}] [REPORT] Aircraft Cancelled: {}", currentTick, callsign);
        this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.CANCELLATION, EmergencyStatus.NONE));
        this.statistics.recordCancellation();
    }

    // For each event scheduled for the current tick, this method triggers them
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

    public Map<Integer, List<RunwayEvent>> getScheduledRunwayEvents() { return Collections.unmodifiableMap(this.scheduledRunwayEvents); }

    public Map<Integer, List<AircraftEvent>> getScheduledAircraftEvents() { return Collections.unmodifiableMap(this.scheduledAircraftEvents); }

    public EventLogger getLogger() {
        return this.logger;
    }
}