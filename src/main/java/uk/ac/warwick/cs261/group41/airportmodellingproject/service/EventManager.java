package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.AircraftEvent;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.RunwayEvent;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftEventType;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Airport;
import uk.ac.warwick.cs261.group41.airportmodellingproject.model.Statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventManager {
    private EventLogger logger;
    private Map<Integer, List<RunwayEvent>> scheduledRunwayEvents;
    private Map<Integer, List<AircraftEvent>> scheduledAircraftEvents;

    private Random random;
    private Airport airport;
    private Statistics statistics;

    public EventManager(EventLogger logger, Map<Integer, List<RunwayEvent>> scheduledRunwayEvents, Map<Integer, List<AircraftEvent>> scheduledAircraftEvents, Random random, Airport airport, Statistics statistics) {
        this.logger = logger;
        this.scheduledRunwayEvents = scheduledRunwayEvents;
        this.scheduledAircraftEvents = scheduledAircraftEvents;
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
            runwayEvents.add(runwayEvent);
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

    /*
        Triggers the runway event from either the user or the schedule, if it's the user, duration is infinite (-1), otherwise duration is set
        If a duration is set, then the reverse of that change is scheduled for the future at the end of the duration
        Updates the runway and logs the event as a runway event
         */
    public void triggerRunwayEvent(int runwayID, RunwayStatus status,  RunwayMode mode, int currentTick, int duration) {
        // If duration is valid, and non-zero, set a scheduled event in the future to reverse the changes
        // NOTE: this will currently override any user set runway events for that runway when a reversion happens
        if (duration > 0){
            RunwayConfig runwayPrevSnapshot = this.airport.getRunwaySnapshot(runwayID);

            int endTick = currentTick + duration;
            RunwayEvent runwayEvent = new RunwayEvent(runwayID, runwayPrevSnapshot.getStatus(), runwayPrevSnapshot.getMode(), endTick, -1);

            addScheduledRunwayEvent(runwayEvent);
        }

        this.airport.updateRunway(runwayID, status, mode);
        this.logger.addEvent(new RunwayEvent(runwayID, status, mode, currentTick, duration));
    }

    /*
    Triggers an aircraft emergency from either the user or the schedule, if it's the user, callsign is present, otherwise callsign is null and a random is chosen
    Updates the aircraft status and logs the event respective to whether it was a scheduled emergency or a manual one
     */
    public void triggerAircraftEmergency(String callsign, EmergencyStatus status, int currentTick){
        if (callsign == null){
            String randomCallsign = this.airport.getRandomHoldingAircraft(this.random);
            this.airport.updateAircraftStatus(randomCallsign, status);
            this.logger.addEvent(new AircraftEvent(randomCallsign, AircraftEventType.SCHEDULED_EMERGENCY, status, currentTick));
        } else {
            this.airport.updateAircraftStatus(callsign, status);
            this.logger.addEvent(new AircraftEvent(callsign, AircraftEventType.MANUAL_EMERGENCY, status, currentTick));
        }
    }

    public void reportDiversion(String callsign, int currentTick){
        this.logger.addEvent(new AircraftEvent(callsign, AircraftEventType.DIVERSION, EmergencyStatus.FUEL, currentTick));
        this.statistics.recordDiversion();
    }

    public void reportCancellation(String callsign, int currentTick){
        this.logger.addEvent(new AircraftEvent(callsign, AircraftEventType.CANCELLATION, EmergencyStatus.NONE, currentTick));
        this.statistics.recordCancellation();
    }

    /*
    For each event scheduled for the current tick, this method triggers them
     */
    public void processScheduledEvents(int currentTick){
        List<RunwayEvent> runwayEvents =  scheduledRunwayEvents.get(currentTick);
        if (runwayEvents != null) {
            for (RunwayEvent runwayEvent : runwayEvents) {
                triggerRunwayEvent(runwayEvent.getRunwayId(), runwayEvent.getRunwayStatus(), runwayEvent.getRunwayMode(), currentTick, runwayEvent.getDuration());
            }
        }

        List<AircraftEvent> aircraftEvents =  scheduledAircraftEvents.get(currentTick);
        if (aircraftEvents != null) {
            for (AircraftEvent aircraftEvent : aircraftEvents) {
                triggerAircraftEmergency(null, aircraftEvent.getStatus(),  currentTick);
            }
        }
    }

    public Map<Integer, List<RunwayEvent>> getScheduledRunwayEvents() {
        return this.scheduledRunwayEvents;
    }

    public Map<Integer, List<AircraftEvent>> getScheduledAircraftEvents() {
        return this.scheduledAircraftEvents;
    }

    public EventLogger getLogger() {
        return this.logger;
    }
}
