package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.EmergencyStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.SimulationMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.utility.JsonFileHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;

/**
 * Spring Boot service that manages the airport simulation lifecycle.
 * Automatically instantiated as a singleton on application startup and injected into controllers.
 */
@Service
public class SimulationService {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    private SimulationEngine engine;
    private int currentTickDelay;

    /** Tracks whether the simulation is currently paused. Volatile for cross-thread visibility. */
    private volatile boolean isPaused = false;

    /** Tracks whether the simulation has completed naturally. Volatile for cross-thread visibility. */
    private volatile boolean isFinished = false;

    /** Tracks whether the simulation was abruptly stopped, in order to better differentiate between natural finishing and abrupt stopping. Volatile for cross-thread visibility. */
    private volatile boolean isAborted = false; // In order to better differentiate between natural finishing and abrupt stopping

    /** The background worker thread that executes simulation ticks. */
    private ScheduledExecutorService executor;

    /** A handle to the scheduled tick task, allowing it to be paused, cancelled, or rescheduled. */
    private ScheduledFuture<?> simulationTask;

    /** The WebSocket messaging template used to push simulation updates to the frontend. */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructor to initialise the simulation service with the messaging template
     * @param messagingTemplate a key "wrapper" for sending a message from the backend to the frontend
     */
    public SimulationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Helper method to cancel tasks and clean up threads
     * @param forceInterrupt if this is true, the thread is killed forcefully, otherwise it is shut down gracefully
     */
    private void cancelCurrentTask(boolean forceInterrupt) {
        if (simulationTask != null) {
            simulationTask.cancel(forceInterrupt);
        }
    }

    /**
     * Method to set up the simulation and initialise the simulation engine to trigger it to initialise its own dependencies
     * @param config a simulation configuration object containing all the user's simulations settings
     */
    public void initialiseSimulation(SimulationConfig config) {
        // Cancel any tasks previously
        cancelCurrentTask(true);

        this.isAborted = false;
        this.isFinished = false;
        this.isPaused = false;
        this.engine = new SimulationEngine(config);
        // For debugging purposes, log the configuration to the console
        printConfigurationSummary(config);
        // Instruct the engine to initialise its simulation through its dependencies
        this.engine.initialiseSimulation();

        // If the executor of the single (performTick) thread is shut down or doesn't exist, initialise it again
        if (executor == null || executor.isShutdown()) {
            // We use single threads to make sure that one tick executes fully before the next one begins
            executor = Executors.newSingleThreadScheduledExecutor();
        }

        // Set the configuration tick time (the real-delay in ms between each tick)
        this.currentTickDelay = config.getTickTime();
    }

    /**
     * Method that begins the simulation, the frontend calls this function once the websockets are set up for communication
     */
    public void startSimulation() {
        if (isFinished || isAborted || engine == null) {
            log.warn("Ignoring ready signal - no simulation is waiting to start, the user likely clicked back in their browser.");
            return;
        }
        scheduleNextTicks();
    }

    /**
     * Method to schedule the threads to perform the next ticks of the simulation with a set interval
     * This method is only called ONCE to initialise the scheduler for the rest of the simulation
     * It is called again only when we want to change the intervals between running the ticks (i.e. the user wants to change the speed)
     */
    private void scheduleNextTicks(){
        // Cancel any tasks currently running
        cancelCurrentTask(false);

        // Call the fast-forward to the end function immediately if the user has set the configuration of the simulation to a quick simulation
        if (engine.getConfig().getSimulationMode() == SimulationMode.QUICK_SIM) {
            fastForwardToEnd();
            return;
        }

        // Schedule the runTick method (which will call performTick) to execute at currentTickDelay intervals
        simulationTask = executor.scheduleWithFixedDelay(
                this::runTick,
                0, // Start immediately
                currentTickDelay,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Method to run the simulation (perform the tick in the simulation engine) and send the snapshot for each tick to the frontend
     */
    private void runTick() {
        // If the user has paused, skip this tick process
        if (isPaused || isAborted) {
            return;
        }

        // The performTick function will return false if we should not continue after the tick i.e. we've reached the end
        boolean continueSimulation = this.engine.performTick();

        if (!continueSimulation){
            this.isFinished = true;
            cancelCurrentTask(false);
            log.info("Simulation ended naturally.");

            // Notification that the simulation has completed is sent to the channel "/simulation/complete" which the front end is listening to (the web-socket)
            // Sending it here is an indication the simulation has completed
            messagingTemplate.convertAndSend("/simulation/complete", "done");
        } else{

            // Simulation snapshot is sent to the channel "/simulation/snapshot" which the front end is listening to (the web-socket)
            // Sending it here is an indication that the simulation is still running
            messagingTemplate.convertAndSend("/simulation/snapshot", this.engine.getSimulationSnapshot());
        }

    }

    /**
     * Pauses the simulation.
     */
    public void pauseSimulation() {
        this.isPaused = true;
    }

    /**
     * Resumes a paused simulation by clearing the paused flag and rescheduling ticks,
     * provided the simulation has not already finished or been aborted.
     */
    public void resumeSimulation() {
        this.isPaused = false;
        if (!isFinished && !isAborted) scheduleNextTicks(); // Safety checks
    }

    /**
     * Method to set the speed of the simulation by changing the time between tick operations
     * @param multiplier the speed the user wants to set the simulation to (1 for 1x, 5 for 5x, 20 for 20x)
     */
    public void setSpeedMultiplier(int multiplier) {
        if (multiplier <= 0) return; // Safety checks

        // Calculate the new delay by dividing the tick time set by the multiplier
        this.currentTickDelay = Math.max(1, this.engine.getConfig().getTickTime() / multiplier);

        // Reschedule the task with the new tick delay
        if (!isPaused && !isFinished && !isAborted) {
            scheduleNextTicks();
        }
    }

    /**
     * Method to loop the threads consecutively with as little tick time as possible to fast-forward the simulation to the end
     */
    public void fastForwardToEnd() {
        // Cancel the current schedule
        cancelCurrentTask(false);

        // Store the current engine as a variable, in case this.engine is overwritten with a new engine while this function works
        final SimulationEngine currentEngine = this.engine;

        if (currentEngine == null) {
            // No simulation engine to fast-forward
            return;
        }

        // Submit a task to performTick as fast as possible
        executor.submit(() -> {
            int tickCount = 0;
            // In case user managed to stop simulation midway through the fast-forward, this loop breaks
            while (!this.isFinished && !this.isAborted && currentEngine.performTick()) {
                // Tracks the number of ticks and every 100 ticks to update the progress percentage shown in the progress screen.
                tickCount++;
                if (tickCount % 100 == 0) {
                    messagingTemplate.convertAndSend("/simulation/snapshot",
                            new SimulationProgress(
                                    currentEngine.getCurrentTick(),
                                    (double) currentEngine.getCurrentTick() / currentEngine.getDurationTicks()
                            )
                    );
                }
            }

            // Notify the frontend that the simulation has completed. ONLY if the user hasn't aborted
            if (!this.isAborted) {
                this.isFinished = true;
                messagingTemplate.convertAndSend("/simulation/complete", "done");
                log.info("Simulation ended via fast-forward.");
            }
        });
    }

    /**
     * Method called by the simulation controllers that receive requests from the frontend to manually change a runway mode
     */
    public void manualRunwayModeChange(int runwayId, String mode) {
        if (this.engine != null && !isFinished && !isAborted) {
            try {
                // Try to match enum with the passed value as best as possible
                RunwayMode parsedMode = RunwayMode.valueOf(mode.trim().toUpperCase());
                this.engine.triggerRunwayEvent(runwayId, null, parsedMode);
            } catch (IllegalArgumentException e) { // Unlikely to happen, but a log will be made if an incorrect enum is sent
                log.error("Received invalid runway mode from UI: {}", mode);
            }
        }
    }

    /**
     * Method called by the simulation controllers that receive requests from the frontend to manually change a runway status
     */
    public void manualRunwayStatusChange(int runwayId, String status) {
        if (this.engine != null && !isFinished && !isAborted) {
            try {
                RunwayStatus parsedStatus = RunwayStatus.valueOf(status.trim().toUpperCase());
                this.engine.triggerRunwayEvent(runwayId, parsedStatus, null);
            } catch (IllegalArgumentException e) {
                log.error("Received invalid runway status from UI: {}", status);
            }
        }
    }

    /**
     * Method called by the simulation controllers that receive requests from the frontend to manually change an aircraft to an emergency
     */
    public void manualAircraftEmergencyChange(String callsign, String status) {
        if (this.engine != null && !isFinished && !isAborted) {
            try {
                EmergencyStatus parsedStatus = EmergencyStatus.valueOf(status.trim().toUpperCase());
                this.engine.triggerAircraftEmergency(callsign, parsedStatus);
            } catch (IllegalArgumentException e) {
                log.error("Received invalid emergency status from UI: {}", status);
            }
        }
    }

    /**
     * Method to HARD stop the simulation, called by the simulation controllers when a stop simulation request is received from the frontend
     */
    public void stopSimulation() {
        log.info("Simulation triggered to stop.");

        this.isAborted = true;
        this.isPaused = false;

        // Shut down the simulation immediately
        cancelCurrentTask(true);

        // executor.shutdown() can be also called to shut it down completely, but leaving it alive means the user can start a new simulation later
    }

    /**
     * Function to return a simulation progress object detailing the current simulation progress
     * Previously used in testing, is redundant now that progress is sent within the simulation snapshot but kept for future testing
     * @return simulation progress object with current tick and the simulation progress as a percentage
     */
    public SimulationProgress getSimulationProgress() {
        // Return percentage of time through duration the simulation is.
        if (this.engine == null) return new SimulationProgress(0, 0.0);
        return this.engine.getSimulationProgress();
    }

    /**
     * Returns whether the simulation is currently paused.
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        return this.isPaused;
    }

    /**
     * Returns whether the simulation is actively running (not paused, finished, or aborted).
     * @return true if a tick task is scheduled and executing, false otherwise
     */
    public boolean isRunning() {
        return this.engine != null && this.simulationTask != null && !this.simulationTask.isCancelled() && !this.isPaused && !this.isAborted;
    }

    // The following functions are called by the ConfigurationController when saving/loading configuration JSONs.
    // Business logic checks are performed here, such as you cannot save a configuration template with the same
    // name as one that already exists.
    // However, physical logic checks are deferred to the JsonFileHandler, such as a configuration template with
    // a given name must exist if it is to be returned or deleted.

    /**
     * Returns a list of summaries for all saved configuration templates.
     * @return a list of ConfigurationTemplateSummary objects
     * @throws ResponseStatusException with HTTP 500 if an I/O error occurs while reading the files
     */
    public List<ConfigurationTemplateSummary> listSavedConfigTemplateSummaries() {
        try {
            return JsonFileHandler.listSavedConfigTemplateSummaries();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file.");
        }
    }

    /**
     * Retrieves a saved configuration template by name.
     * @param name the name of the configuration template to retrieve
     * @return the matching ConfigurationTemplate
     * @throws ResponseStatusException with HTTP 404 if no template with the given name exists,
     *         or HTTP 500 if an I/O error occurs
     */
    public ConfigurationTemplate getConfigurationTemplate(String name) {
        try {
            return JsonFileHandler.getConfigTemplate(name);
        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find template with name: " + name);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file.");
        }
    }

    /**
     * Deletes the specified saved configuration template.
     * @param name the name of the configuration template to delete
     * @throws ResponseStatusException with HTTP 404 if no template with the given name exists,
     *         or HTTP 500 if an I/O error occurs during deletion
     */
    public void deleteConfigurationTemplate(String name) {
        try {
            JsonFileHandler.deleteConfigTemplate(name);
        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find template with name: " + name);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting the file.");
        }
    }

    /**
     * Saves the user's current configuration as a named template.
     * The template name is sanitised to remove special characters, and the current date is
     * stamped automatically. Business logic (duplicate name check) is enforced here rather
     * than in JsonFileHandler, as this is a business concern.
     * @param configTemplate the configuration template to save, including its chosen name
     * @throws ResponseStatusException with HTTP 409 if a template with the same name already exists,
     *         or HTTP 500 if a disk error prevents saving
     */
    public void saveConfigurationTemplate(ConfigurationTemplate configTemplate) {
        // Sanitise the name to remove spaces and special characters.
        String safeName = configTemplate.getTemplateName().replaceAll("[^a-zA-Z0-9-_\\s]", "");
        configTemplate.setTemplateName(safeName);

        // Check that the user hasn't made an error and given the template an existing name.
        if (JsonFileHandler.templateNameExists(safeName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Template name already exists.");
        }

        // Set the date created to the current date and time.
        configTemplate.setDateCreated(new Date());

        // Use try catch in case of disk errors.
        try {
            JsonFileHandler.saveConfigTemplate(configTemplate);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Disk error, could not save file.");
        }
    }


    // The following functions are called by the ResultsController.

    /**
     * Returns the result of the last completed simulation.
     * @return a SimulationResult containing the config, statistics, and event log
     * @throws ResponseStatusException with HTTP 404 if no simulation has been started,
     *         HTTP 400 if the simulation was manually aborted and has no final results,
     *         or HTTP 400 if the simulation is still in progress
     */
    public SimulationResult getLastResult() {
        // First check if a simulation was ever started since the app was opened.
        if (engine == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No simulation has been started.");
        }

        // If user requests for results but simulation was killed, request is rejected instead of sending incomplete data from stopped simulation
        if (isAborted) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Simulation was aborted manually and has no final results.");
        }

        // Check that no simulation is currently running.
        if (!isFinished) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Simulation is still in progress.");
        }

        // Otherwise, a simulation must have been run, and it is finished so return its result.
        return new SimulationResult(engine.getConfig(), engine.getStatistics(), engine.getEventLog());
    }

    /**
     * Saves the result of the last completed simulation under the given name.
     * @param name the name to save the result under; special characters are stripped automatically
     * @throws ResponseStatusException with HTTP 409 if a result with the same name already exists,
     *         or HTTP 500 if a disk error prevents saving
     */
    public void saveSimulationResult(String name) {
        // Sanitise the name to remove spaces and special characters.
        String safeName = name.replaceAll("[^a-zA-Z0-9-_\\s]", "");

        // Check the name is unique.
        if (JsonFileHandler.resultNameExists(safeName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Simulation result name already exists.");
        }

        // Construct the saved results DTO
        SimulationResultSaved savedResult = new SimulationResultSaved(
                engine.getConfig(),
                engine.getStatistics(),
                engine.getEventLog(),
                safeName,
                new Date()
        );

        // Use try catch to write the file in case of disk errors.
        try {
            JsonFileHandler.saveResults(savedResult);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Disk error, could not save file.");
        }
    }

    /**
     * Returns a list of summaries for all saved simulation results.
     * @return a list of SimulationResultSummary objects
     * @throws ResponseStatusException with HTTP 500 if an I/O error occurs while reading the files
     */
    public List<SimulationResultSummary> listResultSummaries() {
        try {
            return JsonFileHandler.listResultSummaries();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file.");
        }
    }

    /**
     * Retrieves a saved simulation result by name.
     * @param name the name of the saved result to retrieve
     * @return the matching SimulationResultSaved object
     * @throws ResponseStatusException with HTTP 404 if no result with the given name exists,
     *         or HTTP 500 if an I/O error occurs
     */
    public SimulationResultSaved getSimulationResult(String name) {
        try {
            return JsonFileHandler.getSimulationResult(name);
        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find result with name: " + name);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file.");
        }
    }

    /**
     * Deletes a saved simulation result by name.
     * @param name the name of the saved result to delete
     * @throws ResponseStatusException with HTTP 404 if no result with the given name exists,
     *         or HTTP 500 if an I/O error occurs during deletion
     */
    public void deleteSimulationResult(String name) {
        try {
            JsonFileHandler.deleteSimulationResult(name);
        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find result with name: " + name);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting the file.");
        }
    }

    /**
     * Logs a human-readable summary of the simulation configuration to the console.
     * Covers seed, duration, tick time, traffic rates, runway settings, statistical
     * multipliers, and all scheduled runway and aircraft events.
     * @param config the simulation configuration to summarise
     */
    private void printConfigurationSummary(SimulationConfig config) {
        log.info("==================================================");
        log.info("===     SIMULATION CONFIGURATION RECEIVED      ===");
        log.info("==================================================");
        log.info("-> Seed:          {}", config.getSeed());
        log.info("-> Duration:      {} ticks", config.getDuration());
        log.info("-> Tick Time:     {} ms", config.getTickTime());
        log.info("-> Traffic Rates: Inbound: {}/hr | Outbound: {}/hr", config.getInboundRate(), config.getOutboundRate());
        log.info("-> Max Wait Time: {} ticks", config.getMaxWaitTime());

        log.info("--- RUNWAY SETTINGS ---");
        if (config.getRunwaySettings() != null) {
            config.getRunwaySettings().forEach(r ->
                    log.info("  - ID: {} | Mode: {} | Status: {}", r.getRunwayID(), r.getMode(), r.getStatus())
            );
        }

        log.info("--- STATISTICAL MODELLING ---");
        log.info("  - Enabled: {}", config.getAutomaticGenerationEnabled());
        if (Boolean.TRUE.equals(config.getAutomaticGenerationEnabled())) {
            log.info("  - Inspection Multiplier: {}x | Snow Multiplier: {}x | Equip Fail Multiplier: {}x",
                config.getRunwayInspectionMultiplier(),
                config.getSnowClearanceMultiplier(),
                config.getEquipmentFailureMultiplier());
            log.info("  - Mech Fail Multiplier: {}x | Passenger Health Multiplier: {}x",
                config.getMechanicalFailureMultiplier(),
                config.getPassengerHealthIssueMultiplier());
        }

        log.info("--- SCHEDULED RUNWAY EVENTS ---");
        Map<Integer, List<RunwayEvent>> scheduledRunways = config.getScheduledRunwayEvents();
        if (scheduledRunways != null && !scheduledRunways.isEmpty()) {
            // Sort the keys so they print in chronological order
            scheduledRunways.keySet().stream().sorted().forEach(tick -> {
                for (RunwayEvent event : scheduledRunways.get(tick)) {
                    log.info("  - [TICK {}] Runway ID: {} | Set Status: {} | Set Mode: {} | Duration: {}", tick, event.getRunwayID(), event.getRunwayStatus(), event.getRunwayMode(), event.getDuration());
                }
            });
        } else {
            log.info("  - No pre-scheduled runway events.");
        }

        log.info("--- SCHEDULED AIRCRAFT EVENTS ---");
        Map<Integer, List<AircraftEvent>> scheduledAircraft = config.getScheduledAircraftEvents();
        if (scheduledAircraft != null && !scheduledAircraft.isEmpty()) {
            // Sort the keys so they print in chronological order
            scheduledAircraft.keySet().stream().sorted().forEach(tick -> {
                for (AircraftEvent event : scheduledAircraft.get(tick)) {
                    log.info("  - [TICK {}] Callsign: {} | Emergency Status: {}", tick, event.getCallsign(), event.getStatus());
                }
            });
        } else {
            log.info("  - No pre-scheduled aircraft events.");
        }
        log.info("==================================================");
    }
}