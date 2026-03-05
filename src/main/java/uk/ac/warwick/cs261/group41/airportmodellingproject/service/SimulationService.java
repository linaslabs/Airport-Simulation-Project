package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.*;
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

// Spring Boot automatically instantiates this Service as a Singleton upon application startup and injects it into the Controllers.
@Service
public class SimulationService {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    private SimulationEngine engine;
    private int currentTickDelay; // Milliseconds between each tick
    private volatile boolean isPaused = false;
    private volatile boolean isFinished = false;

    // The background worker
    private ScheduledExecutorService executor;
    // The task the worker will be doing (a controller for it, so we can pause or stop it)
    private ScheduledFuture<?> simulationTask;

    // Inject the messaging template
    private final SimpMessagingTemplate messagingTemplate;

    public SimulationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void startSimulation(SimulationConfig config) {
        // Stop any simulations previously
        stopSimulation();

        this.isFinished = false;
        this.engine = new SimulationEngine(config);

        printConfigurationSummary(config);

        this.engine.initialiseSimulation();

        // If the executor of the single (performTick) thread is shut down or doesn't exist, initialise it again
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }

        // Set the configuration tick time (the real-delay in ms between each tick)
        this.currentTickDelay = config.getTickTime();
        this.isPaused = false;

        scheduleNextTick();
    }

    // This method is only called ONCE to initialise the scheduler to start the regular call the runTick function at intervals
    // However, this method is called AGAIN only when we want to SHORTEN THE INTERVAL between calling runTick to run a tick (i.e. increase the speed)
    private void scheduleNextTick(){
        // If there is a task already running, send a cancel signal to gracefully shut down the task once its complete, so we can make a new one (with a shorter interval)
        if (simulationTask != null) {
            simulationTask.cancel(false);
        }

        // Schedule the runTick method (which will call performTick) to execute at the currentTickDelay rate
        simulationTask = executor.scheduleWithFixedDelay(
                this::runTick,
                0, // Start immediately
                currentTickDelay,
                TimeUnit.MILLISECONDS
        );
    }

    private void runTick() {
        // If the user has paused, skip this tick process
        if (isPaused) {
            return;
        }

        // The performTick function will return false if we should not continue after the tick i.e. we've reached the end
        boolean continueSimulation = this.engine.performTick();

        if (!continueSimulation){
            this.isFinished = true;
            stopSimulation();
            log.info("Simulation ended.");

            // Final statistics summary are sent to the channel "/simulation/summary" which the front end is subscribed to (the web socket)
            messagingTemplate.convertAndSend("/simulation/summary", this.engine.getStatistics());
        } else{

            // Simulation snapshot is sent to the channel "/simulation/snapshot" which the front end is subscribed to (the web socket)
            messagingTemplate.convertAndSend("/simulation/snapshot", this.engine.getSimulationSnapshot());
        }

    }

    public void pauseSimulation() {
        this.isPaused = true;
    }

    public void resumeSimulation() {
        this.isPaused = false;
        scheduleNextTick();
    }

    // Pass in 1 for 1x, 5 for 5x, 20 for 20x
    public void setSpeedMultiplier(int multiplier) {
        if (multiplier <= 0) return;

        // Calculate the new delay by dividing the tick time set by the multiplier
        this.currentTickDelay = Math.max(1, this.engine.getConfig().getTickTime() / multiplier);

        // Reschedule the task with the new tick delay
        if (!isPaused) {
            scheduleNextTick();
        }
    }

    public void fastForwardToEnd() {
        // Cancel the current schedule gracefully
        if (simulationTask != null) {
            simulationTask.cancel(false);
        }

        // Store the current engine as a variable, in case this.engine is overwritten with a new engine while this function works
        final SimulationEngine currentEngine = this.engine;

        if (currentEngine == null) {
            // No simulation engine to fast-forward
            return;
        }

        // Submit a task to performTick as fast as possible (note, the user cannot stop the simulation while this runs)
        executor.submit(() -> {
            while (currentEngine.performTick()) {
                // Engine runs to the end without delays
            }
            stopSimulation();

            // Send the final summary to the frontend
            messagingTemplate.convertAndSend("/simulation/summary", this.engine.getStatistics());
            log.info("Simulation ended.");
        });
    }

    public void stopSimulation() {
        // Let the simulation shut down gracefully
        if (simulationTask != null) {
            simulationTask.cancel(false);
        }

        // executor.shutdown() can be called to shut it down completely, but leaving it alive means the user can start a new simulation later
    }

    public SimulationProgress getSimulationProgress() {
        // Return percentage of time through duration the simulation is.
        if (this.engine == null) return new SimulationProgress(0.0);
        return this.engine.getSimulationProgress();
    }

    public boolean isPaused() { return this.isPaused; }

    public boolean isRunning() { return this.engine != null && this.simulationTask != null && !this.simulationTask.isCancelled() && !this.isPaused; }



    // The following functions are called by the ConfigurationController when saving/loading configuration JSONs.
    // Business logic checks are performed here, such as you cannot save a configuration template with the same
    // name as one that already exists.
    // However, physical logic checks are deferred to the JsonFileHandler, such as a configuration template with
    // a given name must exist if it is to be returned or deleted.

    public List<ConfigurationTemplateSummary> listSavedConfigTemplateSummaries() {
        try {
            return JsonFileHandler.listSavedConfigTemplateSummaries();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file.");
        }
    }

    public ConfigurationTemplate getConfigurationTemplate(String name) {
        try {
            return JsonFileHandler.getConfigTemplate(name);
        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find template with name: " + name);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file.");
        }
    }

    // Deletes the specified saved configuration template.
    public void deleteConfigurationTemplate(String name) {
        try {
            JsonFileHandler.deleteConfigTemplate(name);
        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find template with name: " + name);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting the file.");
        }
    }

    // Saves the user's current configuration.
    // Note that we perform the check for a template with an existing name here rather than in JsonFileHandler.
    // This is because this is business logic.
    // In getConfigurationTemplate, we do a similar check to ensure the file actually exists with a given name,
    // however checking the file actually exists is physical logic, so it should be in JsonFileHandler and not here.
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

    // This returns the result of the last simulation that was started.
    // If a simulation is still being run, or none was ever started, this function returns an error.
    public SimulationResult getLastResult() {
        // First check if a simulation was ever started since the app was opened.
        if (engine == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No simulation has been started.");
        }

        // Check that no simulation is currently running.
        if (!isFinished) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Simulation is still in progress.");
        }

        // Otherwise, a simulation must have been run, and it is finished so return its result.
        return new SimulationResult(engine.getConfig(), engine.getStatistics(), engine.getEventLog());
    }

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

    public List<SimulationResultSummary> listResultSummaries() {
        try {
            return JsonFileHandler.listResultSummaries();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file.");
        }
    }

    public SimulationResultSaved getSimulationResult(String name) {
        try {
            return JsonFileHandler.getSimulationResult(name);
        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find result with name: " + name);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file.");
        }
    }

    public void deleteSimulationResult(String name) {
        try {
            JsonFileHandler.deleteSimulationResult(name);
        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find result with name: " + name);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting the file.");
        }
    }


    // Helper method to print out the full configuration received:
    private void printConfigurationSummary(SimulationConfig config) {
        log.info("\n==================================================");
        log.info("===     SIMULATION CONFIGURATION RECEIVED      ===");
        log.info("==================================================");
        log.info("-> Simulation ID: {}", config.getSimulationID());
        log.info("-> Seed:          {}", config.getSeed());
        log.info("-> Duration:      {} ticks", config.getDuration());
        log.info("-> Tick Time:     {} ms", config.getTickTime());
        log.info("-> Traffic Rates: Inbound: {}/hr | Outbound: {}/hr", config.getInboundRate(), config.getOutboundRate());
        log.info("-> Max Wait Time: {} ticks", config.getMaxWaitTime());

        log.info("\n--- RUNWAY SETTINGS ---");
        if (config.getRunwaySettings() != null) {
            config.getRunwaySettings().forEach(r ->
                    log.info("  - ID: {} | Mode: {} | Status: {}", r.getRunwayID(), r.getMode(), r.getStatus())
            );
        }

        log.info("\n--- STATISTICAL MODELLING ---");
        log.info("  - Enabled: {}", config.getAutomaticGenerationEnabled());
        if (Boolean.TRUE.equals(config.getAutomaticGenerationEnabled())) {
            log.info("  - Inspection: {} | Snow: {} | Equip Fail: {}", config.getRunwayInspectionRate(), config.getSnowClearanceRate(), config.getEquipmentFailureRate());
            log.info("  - Mech Fail:  {} | Passenger Health: {}", config.getMechanicalFailureRate(), config.getPassengerHealthIssueRate());
        }

        log.info("\n--- SCHEDULED RUNWAY EVENTS ---");
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

        log.info("\n--- SCHEDULED AIRCRAFT EVENTS ---");
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
        log.info("==================================================\n");
    }
}