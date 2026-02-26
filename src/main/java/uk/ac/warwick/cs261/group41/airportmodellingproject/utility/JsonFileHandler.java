package uk.ac.warwick.cs261.group41.airportmodellingproject.utility;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.SimulationConfig;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.StatisticsSummary;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to handle saving and loading simulation data in JSON format.
 * This class is static and does not need to be instantiated.
 */
public class JsonFileHandler {

    // Use the builder pattern to allow us to configure the mapper on the same line as initialisation.
    // This allows us to enable pretty printing to make the resulting JSON files more human-readable.
    private static final ObjectMapper mapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    // Defines the root storage location.
    // System.getProperty("user.dir") ensures we save to the project root.
    // File.separator ensures compatibility between Windows (\) and Mac/Linux (/).
    private static final String dataDirectory = System.getProperty("user.dir") + File.separator + "data";

    /**
     * Saves a simulation's input parameters to the /data/configs folder.
     * @param config The configuration object containing the simulation settings.
     */
    public static void saveConfig(SimulationConfig config) {
        saveToFile(config, "configs", config.getSimulationID());
    }

    /**
     * Saves the final simulation results to the /data/results folder.
     * @param stats The statistics object containing the statistical results of the simulation.
     */
    public static void saveResults(StatisticsSummary stats) {
        saveToFile(stats, "results", stats.getSimulationID());
    }

    /**
     * Generic private helper to handle the physical writing of objects to disk.
     * @param data The Java object to be converted to JSON.
     * @param subFolder The specific subdirectory (configs or results).
     * @param id The unique ID of the simulation, used as the filename.
     */
    private static void saveToFile(Object data, String subFolder, String id) {
        try {
            // Construct the logical path to the destination folder.
            Path directory = Paths.get(dataDirectory, subFolder);

            // Ensure the directory exists, if not create it and any missing parent folders.
            Files.createDirectories(directory);

            // Combine the directory path with the filename and convert to a File object.
            File outputFile = directory.resolve(id + ".json").toFile();

            // Transform the Java object into JSON text and write it to the file.
            mapper.writeValue(outputFile, data);
        } catch (IOException e) {
            // Log the error to the terminal is disk access fails.
            System.err.println("Error saving JSON: " + e.getMessage());
        }
    }
}
