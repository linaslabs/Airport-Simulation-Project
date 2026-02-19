package uk.ac.warwick.cs261.group41.airportmodellingproject.service;

import org.springframework.stereotype.Service;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationProgress;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.StatisticsSummary;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
            // HERE WE PREPARE THE FINAL STATS TO SEND BACK
            // TODO We can send back final simulation completion progress here
        } else{
            // SEND BACK SIMULATION SNAPSHOT TO THE UI
            // TODO In this case, we will send back the simulation progress (via websockets?)
        }

    }

    public void pauseSimulation() {
        this.isPaused = true;
    }

    public void resumeSimulation() {
        this.isPaused = false;
    }

    // Pass in 1 for 1x, 5 for 5x, 20 for 20x
    public void setSpeedMultiplier(int multiplier) {
        if (multiplier <= 0) return;

        // Calculate the new delay by dividing the tick time set by the multiplier
        this.currentTickDelay = this.engine.getConfig().getTickTime() / multiplier;

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

        // Submit a task to performTick as fast as possible (note, the user cannot stop the simulation while this runs)
        executor.submit(() -> {
            while (this.engine.performTick()) {
                // Engine runs to the end without delays
            }
            stopSimulation();
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
        return null;
    }

    public boolean getIsPaused() { return this.isPaused; }

    public boolean isRunning() { return this.engine != null && this.simulationTask != null && !this.simulationTask.isCancelled() && !this.isPaused; }
}
