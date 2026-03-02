package uk.ac.warwick.cs261.group41.airportmodellingproject.utility;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class to handle saving and loading simulation data in JSON format.
 * This class is static and does not need to be instantiated.
 *
 * One of the key ideas with this class is that the errors from disk access are not handled here.
 * Instead, they are propagated to the function which called it, so they can be handled in the SimulationService.
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
     * Generic private helper to handle the physical writing of objects to disk.
     * @param data The Java object to be converted to JSON.
     * @param subFolder The specific subdirectory (configs or results).
     * @param id The unique ID of the simulation, used as the filename.
     * @throws IOException Error thrown if disk access fails.
     */
    private static void saveToFile(Object data, String subFolder, String id) throws IOException {
        // Construct the logical path to the destination folder.
        Path directory = Paths.get(dataDirectory, subFolder);

        // Ensure the directory exists, if not create it and any missing parent folders.
        Files.createDirectories(directory);

        // Combine the directory path with the filename and convert to a File object.
        File outputFile = directory.resolve(id + ".json").toFile();

        // Transform the Java object into JSON text and write it to the file.
        mapper.writeValue(outputFile, data);
    }


    // The following functions are for the configuration templates file handling logic.

    /**
     * Lists summaries of all the configuration templates stored in the /data/configtemplates folder.
     * Note that for the function which is mapped to each file, we do catch that error so we can continue
     * reading the rest of the files after one read failed.
     * @return List of ConfigurationTemplateSummary objects, sorted by data, newest-first.
     * @throws IOException Error thrown if disk access fails.
     */
    public static List<ConfigurationTemplateSummary> listSavedConfigTemplateSummaries() throws IOException {
        Path configsDir = Paths.get(dataDirectory, "configtemplates");

        if (!Files.exists(configsDir)) {
            return Collections.emptyList();
        }

        try (Stream<Path> files = Files.list(configsDir)) {
            return files
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> {

                        try {
                            // Load the full template.
                            ConfigurationTemplate temp = mapper.readValue(path.toFile(), ConfigurationTemplate.class);

                            // Calculate the event count (summing nested lists).
                            int eventCount = 0;
                            // Sum the aircraft events.
                            for (List<AircraftEvent> list : temp.getScheduledAircraftEvents().values()) {
                                eventCount += list.size();
                            }
                            // Sum the runway events.
                            for (List<RunwayEvent> list : temp.getScheduledRunwayEvents().values()) {
                                eventCount += list.size();
                            }

                            // Return the lightweight summary.
                            return new ConfigurationTemplateSummary(
                                    temp.getTemplateName(),
                                    temp.getDateCreated(),
                                    temp.getRunwaySettings().size(),
                                    eventCount,
                                    temp.getInboundRate(),
                                    temp.getOutboundRate()
                            );
                        } catch (Exception e) {
                            // If one of the templates fails to read, log the error and handle by returning null.
                            System.err.println("Skipping invalid template file: " + path.getFileName());
                            return null;
                        }

                    })
                    .filter(Objects::nonNull) // Remove the nulls from failed reads.
                    .sorted((a, b) -> b.getDateCreated().compareTo(a.getDateCreated())) // Sort by newest date
                    .collect(Collectors.toList());
        }
    }

    /**
     * Loads a ConfigurationTemplate from a JSON file in the /data/configtemplates folder.
     * @param name The name of the configuration template to load.
     * @return The loaded ConfigurationTemplate object.
     * @throws IOException If the file does not exist or contains invalid JSON.
     */
    public static ConfigurationTemplate getConfigTemplate(String name) throws IOException {
        Path configtemplateFilePath = Paths.get(dataDirectory, "configtemplates", name + ".json");

        // Check if it exists first to throw a more specific error.
        if (!Files.exists(configtemplateFilePath)) {
            throw new NoSuchFileException(configtemplateFilePath.toString());
        }

        return mapper.readValue(configtemplateFilePath.toFile(), ConfigurationTemplate.class);
    }

    public static void deleteConfigTemplate(String name) throws IOException {
        Path configtemplateFilePath = Paths.get(dataDirectory, "configtemplates", name + ".json");

        // Check if it exists first to throw a more specific error.
        if (!Files.exists(configtemplateFilePath)) {
            throw new NoSuchFileException(configtemplateFilePath.toString());
        }

        Files.delete(configtemplateFilePath);
    }

    public static boolean templateNameExists(String name) {
        Path configtemplateFilePath = Paths.get(dataDirectory, "configtemplates", name + ".json");
        return Files.exists(configtemplateFilePath);
    }

    /**
     * Saves a simulation's input parameters to the /data/configs folder.
     * @param configTemplate The ConfigurationTemplate object containing the simulation settings.
     */
    public static void saveConfigTemplate(ConfigurationTemplate configTemplate) throws IOException {
        saveToFile(configTemplate, "configtemplates", configTemplate.getTemplateName());
    }


    // The following functions are for the simulation results file handling logic.

    public static boolean resultNameExists(String name) {
        Path resultPath = Paths.get(dataDirectory, "results", name + ".json");
        return Files.exists(resultPath);
    }

    /**
     * Saves the simulation results to the /data/results folder.
     * @param results The results object, containing the name and date of the results, and the statistics and configuration.
     */
    public static void saveResults(SimulationResultSaved results) throws IOException {
        saveToFile(results, "results", results.getSimulationName());
    }

    /**
     * Loads a SimulationResultSaved object from a JSON file in the /data/results folder.
     * @param name The name of the result to load.
     * @return The loaded SimulationResultSaved object.
     * @throws IOException If the file does not exist or contains invalid JSON.
     */
    public static SimulationResultSaved getSimulationResult(String name) throws IOException {
        Path resultPath = Paths.get(dataDirectory, "results", name + ".json");

        // Check if it exists first to throw a more specific error.
        if (!Files.exists(resultPath)) {
            throw new NoSuchFileException(resultPath.toString());
        }

        return mapper.readValue(resultPath.toFile(), SimulationResultSaved.class);
    }

    public static void deleteSimulationResult(String name) throws IOException {
        Path resultPath = Paths.get(dataDirectory, "results", name + ".json");

        // Check if it exists first to throw a more specific error.
        if (!Files.exists(resultPath)) {
            throw new NoSuchFileException(resultPath.toString());
        }

        Files.delete(resultPath);
    }
}
