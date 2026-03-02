package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.utility.JsonFileHandler;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;

// Spring Boot automatically instantiates this Service as a Singleton upon application startup and injects it into the Controllers.
@Service
public class SimulationService {

    private SimulationEngine engine;
    private int currentTickDelay; // Milliseconds between each tick
    private volatile boolean isPaused = false;

    // The background worker
    private ScheduledExecutorService executor;
    // The task the worker will be doing (a controller for it, so we can pause or stop it)
    private ScheduledFuture<?> simulationTask;

    public void startSimulation(SimulationConfig config) {
        // Stop any simulations previously
        stopSimulation();

        this.engine = new SimulationEngine(config);

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
            stopSimulation();
            System.out.println("Simulation ended.");

            // HERE WE PREPARE THE FINAL STATS TO SEND BACK
            // TODO We can send back final simulation completion progress here (via websockets)
        } else{
            // SEND BACK SIMULATION SNAPSHOT TO THE UI
            // TODO In this case, we will send back the simulation progress (via websockets)
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
            System.out.println("Simulation ended.");
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

    public StatisticsSummary getStatisticsSummary() {
        // It is the role of the SimulationService to create the DTOs from the SimulationEngine data.
        return engine.getFinalSummary();
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

        // First check that the user hasn't made an error and given the template an existing name.
        if (JsonFileHandler.templateNameExists(configTemplate.getTemplateName())) {
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


}
