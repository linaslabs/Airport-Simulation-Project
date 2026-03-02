package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

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
                runwayEvents.addFirst(runwayEvent);
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


    // Triggers the runway event from either the user or the schedule, if it's the user, duration is infinite (-1), if duration is 0, it is a reversion, otherwise it's a scheduled event
    // If a duration is set, then the reverse of that change is scheduled for the future at the end of the duration
    // Updates the runway and logs the event as a runway event

    public void triggerRunwayEvent(int runwayID, RunwayStatus status,  RunwayMode mode, int currentTick, int duration, boolean isNaturalEvent) {
        // If duration is > 0, this is a scheduled event, set another scheduled event in the future to reverse the changes
        // NOTE: this will currently override any user set runway events for that runway when a reversion happens
        if (duration > 0){
            RunwayConfig runwayPrevSnapshot = this.airport.getRunwaySnapshot(runwayID);

            int endTick = currentTick + duration;
            // Create the new runway reversion (that is of duration 0) to reverse this event when it finishes
            RunwayEvent reversionEvent = new RunwayEvent(endTick, runwayID, runwayPrevSnapshot.getStatus(), runwayPrevSnapshot.getMode(), RunwayEventType.REVERSION, 0);

            addScheduledRunwayEvent(reversionEvent);
        }

        this.airport.updateRunway(runwayID, status, mode);

        // If the event is a natural event, its event type being logged is as a natural (random) closure, else, we determine the event type by the duration
        // If the duration is less than 0, then we know it's a manual change by the user, if it's a scheduled change (since it wasn't a natural event to begin with)
        // If the duration is therefore equal to 0, we know its a reversion
        this.logger.addEvent(new RunwayEvent(currentTick, runwayID, status, mode,
                isNaturalEvent ? RunwayEventType.NATURAL_CLOSURE : (duration < 0) ? RunwayEventType.MANUAL_CHANGE :
                        (duration > 0) ? RunwayEventType.SCHEDULED_CHANGE : RunwayEventType.REVERSION, duration));
    }


    // Generates the random aircraft emergencies and the random runway status changes for statistical modelling based on the configured rates (only called if the user permits them during the simulation)
    public void generateRandomEventsForTick(int currentTick, double inspectionRate, double snowRate, double failureRate, double mechanicalRate, double healthRate) {

        // --- Generating AIRCRAFT EMERGENCIES ---
        // Assuming that the rates are per hour, so need to convert to per tick equivalents
        double tickMechanicalRate = mechanicalRate / 60;
        double tickHealthRate = healthRate / 60;

        double emergencyRoll = this.random.nextDouble();

        if (emergencyRoll < tickMechanicalRate) {
            triggerAircraftEmergency(null, EmergencyStatus.MECHANICAL, currentTick, true);
        } else if (emergencyRoll < (tickMechanicalRate + tickHealthRate)) {
            triggerAircraftEmergency(null, EmergencyStatus.PASSENGER, currentTick, true);
        }

        // --- Generating RUNWAY CLOSURES ---
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
                    newRunwayStatus = RunwayStatus.SNOWCLEARANCE;
                } else if (runwayRoll < (tickInspectionRate + tickSnowRate + tickFailureRate)) {
                    newRunwayStatus = RunwayStatus.FAILURE;
                }

                // Checking if the roll landed to satisfy one of the rates
                if (newRunwayStatus != null) {
                    // Generating a random number between 10 and 60 ticks
                    int duration = 10 + this.random.nextInt(51);

                    // Find the ticks until the next scheduled event for this runway
                    int ticksUntilNextEvent = ticksUntilNextScheduledRunwayEvent(runway.getRunwayID(), currentTick);

                    // Make sure that the duration of this random event is CAPPED so it finishes before the next scheduled runway event
                    if (ticksUntilNextEvent > 0 && duration > ticksUntilNextEvent) {
                        duration = ticksUntilNextEvent;
                    }

                    // Trigger the event
                    triggerRunwayEvent(runway.getRunwayID(),  newRunwayStatus, null, currentTick, duration, true);
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

    // Triggers an aircraft emergency from either the user or the schedule, if it's the user, callsign is present, otherwise callsign is null and a random is chosen
    // Updates the aircraft status and logs the event respective to whether it was a scheduled emergency or a manual one

    public void triggerAircraftEmergency(String callsign, EmergencyStatus status, int currentTick, boolean isNaturalEvent){
        if (callsign == null){
            String randomCallsign = this.airport.getRandomHoldingAircraft(this.random);
            // If there is no current aircraft in the holding queue, there are no aircraft to set emergencies to
            if (randomCallsign != null) {
                this.airport.updateAircraftStatus(randomCallsign, status);
                this.logger.addEvent(new AircraftEvent(currentTick, randomCallsign, isNaturalEvent ? AircraftEventType.NATURAL_EMERGENCY : AircraftEventType.SCHEDULED_EMERGENCY, status));
            } else {
                System.out.println("Tick " + currentTick + ": Scheduled emergency skipped, holding pattern is empty.");
            }
        } else {
            this.airport.updateAircraftStatus(callsign, status);
            this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.MANUAL_EMERGENCY, status));
        }
    }

    public void reportAircraftEmergency(String callsign, EmergencyStatus status, int currentTick){
        this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.NATURAL_EMERGENCY, status));
    }

    public void reportDiversion(String callsign, int currentTick){
        this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.DIVERSION, EmergencyStatus.FUEL));
        this.statistics.recordDiversion();
    }

    public void reportCancellation(String callsign, int currentTick){
        this.logger.addEvent(new AircraftEvent(currentTick, callsign, AircraftEventType.CANCELLATION, EmergencyStatus.NONE));
        this.statistics.recordCancellation();
    }

    // For each event scheduled for the current tick, this method triggers them
    public void processScheduledEvents(int currentTick){
        List<RunwayEvent> runwayEvents =  scheduledRunwayEvents.get(currentTick);
        if (runwayEvents != null) {
            for (RunwayEvent runwayEvent : runwayEvents) {
                triggerRunwayEvent(runwayEvent.getRunwayID(), runwayEvent.getRunwayStatus(), runwayEvent.getRunwayMode(), currentTick, runwayEvent.getDuration(), false);
            }
        }

        List<AircraftEvent> aircraftEvents =  scheduledAircraftEvents.get(currentTick);
        if (aircraftEvents != null) {
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
