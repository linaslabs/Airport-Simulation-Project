package uk.ac.warwick.cs261.group41.airportmodellingproject.utility;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.*;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventLogger;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.SimulationMode;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JsonFileHandler.
 *
 * Exercises the real filesystem under /data/configtemplates and /data/results.
 * Every test cleans up the files it creates in @AfterEach so the data directory is left
 * in the same state it was found.
 *
 * No mocking is used because JsonFileHandler is a static utility whose entire purpose
 * is disk I/O — verifying the actual file is more valuable than mocking it away.
 */
class JsonFileHandlerTest {

    // Unique suffix shared by every artifact this test run creates, keeping parallel runs isolated.
    private static final String SUFFIX = String.valueOf(System.currentTimeMillis());
    private static final String TEST_TEMPLATE_NAME  = "TEST_TEMPLATE_"  + SUFFIX;
    private static final String TEST_TEMPLATE_NAME2 = "TEST_TEMPLATE2_" + SUFFIX;
    private static final String TEST_RESULT_NAME    = "TEST_RESULT_"    + SUFFIX;
    private static final String TEST_RESULT_NAME2   = "TEST_RESULT2_"   + SUFFIX;

    // Helpers

    /**
     * Builds a minimal but fully-populated SimulationConfig for use in tests.
     */
    private SimulationConfig buildConfig() {
        return new SimulationConfig(
                List.of(new RunwayConfig(0, RunwayStatus.AVAILABLE, RunwayMode.MIXED)),
                15, 15, 30, 420, 1000,
                false,
                0.0, 0.0, 0.0, 0.0, 0.0,
                42L,
                new HashMap<>(), new HashMap<>(),
                SimulationMode.QUICK_SIM
        );
    }

    /**
     * Builds a StatisticsSummary with known, assertable field values.
     */
    private StatisticsSummary buildStats() {
        return new StatisticsSummary(
                1.0, 2,
                3.0, 4.0,
                5.0, 6.0,
                7.0, 8,
                9, 10,
                1, 2,
                15.0
        );
    }

    /**
     * Builds a ConfigurationTemplate wrapping the given config.
     */
    private ConfigurationTemplate buildTemplate(String name, SimulationConfig config) {
        return new ConfigurationTemplate(name, new Date(), config);
    }

    /**
     * Builds a SimulationResultSaved with the given name, config, and stats.
     */
    private SimulationResultSaved buildResult(String name, SimulationConfig config, StatisticsSummary stats) {
        return new SimulationResultSaved(config, stats, new EventLogger(), name, new Date());
    }

    // Cleanup

    @AfterEach
    void cleanup() throws Exception {
        // Remove any template files this test created.
        for (String name : List.of(TEST_TEMPLATE_NAME, TEST_TEMPLATE_NAME2)) {
            Files.deleteIfExists(Paths.get(System.getProperty("user.dir"), "data", "configtemplates", name + ".json"));
        }
        // Remove any result files this test created.
        for (String name : List.of(TEST_RESULT_NAME, TEST_RESULT_NAME2)) {
            Files.deleteIfExists(Paths.get(System.getProperty("user.dir"), "data", "results", name + ".json"));
        }
    }

    // Configuration Template tests

    /**
     * Verifies that saving a ConfigurationTemplate creates a JSON file on disk.
     */
    @Test
    void saveConfigTemplate_shouldCreateJsonFileOnDisk() throws Exception {
        ConfigurationTemplate template = buildTemplate(TEST_TEMPLATE_NAME, buildConfig());

        JsonFileHandler.saveConfigTemplate(template);

        Path expectedPath = Paths.get(System.getProperty("user.dir"), "data", "configtemplates", TEST_TEMPLATE_NAME + ".json");
        assertTrue(Files.exists(expectedPath), "ConfigurationTemplate JSON file should exist after saving.");
    }

    /**
     * Verifies that a saved ConfigurationTemplate can be reloaded with all fields intact.
     */
    @Test
    void saveConfigTemplate_andGetConfigTemplate_shouldRoundTripCorrectly() throws Exception {
        SimulationConfig originalConfig = buildConfig();
        ConfigurationTemplate template = buildTemplate(TEST_TEMPLATE_NAME, originalConfig);

        JsonFileHandler.saveConfigTemplate(template);
        ConfigurationTemplate loaded = JsonFileHandler.getConfigTemplate(TEST_TEMPLATE_NAME);

        assertNotNull(loaded, "Loaded template should not be null.");
        assertEquals(TEST_TEMPLATE_NAME, loaded.getTemplateName());
        assertEquals(originalConfig.getInboundRate(),  loaded.getInboundRate());
        assertEquals(originalConfig.getOutboundRate(), loaded.getOutboundRate());
        assertEquals(originalConfig.getDuration(),     loaded.getDuration());
        assertEquals(originalConfig.getSeed(),         loaded.getSeed());
        assertEquals(1, loaded.getRunwaySettings().size(), "Runway count should be preserved.");
        assertEquals(RunwayMode.MIXED,       loaded.getRunwaySettings().get(0).getMode());
        assertEquals(RunwayStatus.AVAILABLE, loaded.getRunwaySettings().get(0).getStatus());
    }

    /**
     * Verifies that loading a template that does not exist throws NoSuchFileException.
     */
    @Test
    void getConfigTemplate_whenFileDoesNotExist_shouldThrowNoSuchFileException() {
        assertThrows(NoSuchFileException.class, () ->
                JsonFileHandler.getConfigTemplate("NON_EXISTENT_TEMPLATE_" + SUFFIX)
        );
    }

    /**
     * Verifies that templateNameExists returns true after the template has been saved.
     */
    @Test
    void templateNameExists_whenTemplateHasBeenSaved_shouldReturnTrue() throws Exception {
        JsonFileHandler.saveConfigTemplate(buildTemplate(TEST_TEMPLATE_NAME, buildConfig()));

        assertTrue(JsonFileHandler.templateNameExists(TEST_TEMPLATE_NAME),
                "templateNameExists should return true after the template is saved.");
    }

    /**
     * Verifies that templateNameExists returns false for a template that was never saved.
     */
    @Test
    void templateNameExists_whenTemplateHasNotBeenSaved_shouldReturnFalse() {
        assertFalse(JsonFileHandler.templateNameExists("NEVER_SAVED_TEMPLATE_" + SUFFIX),
                "templateNameExists should return false for a template that was never saved.");
    }

    /**
     * Verifies that deleting a ConfigurationTemplate removes its file from disk.
     */
    @Test
    void deleteConfigTemplate_shouldRemoveFileFromDisk() throws Exception {
        JsonFileHandler.saveConfigTemplate(buildTemplate(TEST_TEMPLATE_NAME, buildConfig()));

        JsonFileHandler.deleteConfigTemplate(TEST_TEMPLATE_NAME);

        Path deletedPath = Paths.get(System.getProperty("user.dir"), "data", "configtemplates", TEST_TEMPLATE_NAME + ".json");
        assertFalse(Files.exists(deletedPath), "File should no longer exist after deletion.");
    }

    /**
     * Verifies that deleting a template that does not exist throws NoSuchFileException.
     */
    @Test
    void deleteConfigTemplate_whenFileDoesNotExist_shouldThrowNoSuchFileException() {
        assertThrows(NoSuchFileException.class, () ->
                JsonFileHandler.deleteConfigTemplate("NEVER_SAVED_TEMPLATE_" + SUFFIX)
        );
    }

    /**
     * Verifies that a saved template appears in the list returned by listSavedConfigTemplateSummaries.
     */
    @Test
    void listSavedConfigTemplateSummaries_shouldContainSavedTemplate() throws Exception {
        ConfigurationTemplate template = buildTemplate(TEST_TEMPLATE_NAME, buildConfig());
        JsonFileHandler.saveConfigTemplate(template);

        List<ConfigurationTemplateSummary> summaries = JsonFileHandler.listSavedConfigTemplateSummaries();

        boolean found = summaries.stream()
                .anyMatch(s -> TEST_TEMPLATE_NAME.equals(s.getTemplateName()));
        assertTrue(found, "Saved template should appear in the summary list.");
    }

    /**
     * Verifies that each ConfigurationTemplateSummary carries the correct rates, runway count, and event count.
     */
    @Test
    void listSavedConfigTemplateSummaries_summaryShouldCarryCorrectMetadata() throws Exception {
        SimulationConfig config = buildConfig();
        JsonFileHandler.saveConfigTemplate(buildTemplate(TEST_TEMPLATE_NAME, config));

        List<ConfigurationTemplateSummary> summaries = JsonFileHandler.listSavedConfigTemplateSummaries();

        ConfigurationTemplateSummary summary = summaries.stream()
                .filter(s -> TEST_TEMPLATE_NAME.equals(s.getTemplateName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected summary not found"));

        assertEquals(config.getInboundRate(),  summary.getInboundRate());
        assertEquals(config.getOutboundRate(), summary.getOutboundRate());
        assertEquals(1, summary.getRunwayCount(),          "Summary runway count should match the config.");
        assertEquals(0, summary.getScheduledEventsCount(), "No events were added, so count should be 0.");
    }

    /**
     * Verifies that listSavedConfigTemplateSummaries returns templates sorted newest-first by dateCreated.
     */
    @Test
    void listSavedConfigTemplateSummaries_shouldBeSortedNewestFirst() throws Exception {
        // Use fixed epoch millis so the ordering is deterministic regardless of wall-clock timing.
        ConfigurationTemplate older = new ConfigurationTemplate(TEST_TEMPLATE_NAME,  new Date(1_000_000L), buildConfig());
        ConfigurationTemplate newer = new ConfigurationTemplate(TEST_TEMPLATE_NAME2, new Date(2_000_000L), buildConfig());

        JsonFileHandler.saveConfigTemplate(older);
        JsonFileHandler.saveConfigTemplate(newer);

        List<ConfigurationTemplateSummary> summaries = JsonFileHandler.listSavedConfigTemplateSummaries();

        // Filter to only the two we just created so other files on disk don't influence the order assertion.
        List<ConfigurationTemplateSummary> testSummaries = summaries.stream()
                .filter(s -> s.getTemplateName().equals(TEST_TEMPLATE_NAME)
                          || s.getTemplateName().equals(TEST_TEMPLATE_NAME2))
                .toList();

        assertEquals(2, testSummaries.size());
        assertEquals(TEST_TEMPLATE_NAME2, testSummaries.get(0).getTemplateName(),
                "The template with the newer date should be listed first.");
    }

    // Simulation Result tests

    /**
     * Verifies that saving a SimulationResultSaved creates a JSON file on disk.
     */
    @Test
    void saveResults_shouldCreateJsonFileOnDisk() throws Exception {
        SimulationResultSaved result = buildResult(TEST_RESULT_NAME, buildConfig(), buildStats());

        JsonFileHandler.saveResults(result);

        Path expectedPath = Paths.get(System.getProperty("user.dir"), "data", "results", TEST_RESULT_NAME + ".json");
        assertTrue(Files.exists(expectedPath), "Result JSON file should exist after saving.");
    }

    /**
     * Verifies that a saved SimulationResultSaved can be reloaded with both its embedded
     * SimulationConfig and StatisticsSummary fields intact.
     */
    @Test
    void saveResults_andGetSimulationResult_shouldRoundTripCorrectly() throws Exception {
        SimulationConfig originalConfig = buildConfig();
        StatisticsSummary originalStats = buildStats();
        SimulationResultSaved result = buildResult(TEST_RESULT_NAME, originalConfig, originalStats);

        JsonFileHandler.saveResults(result);
        SimulationResultSaved loaded = JsonFileHandler.getSimulationResult(TEST_RESULT_NAME);

        assertNotNull(loaded, "Loaded result should not be null.");
        assertEquals(TEST_RESULT_NAME, loaded.getSimulationName());

        // Verify the embedded SimulationConfig round-trips correctly.
        assertNotNull(loaded.getConfig(), "Embedded config should not be null.");
        assertEquals(originalConfig.getInboundRate(),  loaded.getConfig().getInboundRate());
        assertEquals(originalConfig.getOutboundRate(), loaded.getConfig().getOutboundRate());
        assertEquals(originalConfig.getDuration(),     loaded.getConfig().getDuration());
        assertEquals(originalConfig.getSeed(),         loaded.getConfig().getSeed());
        assertEquals(1, loaded.getConfig().getRunwaySettings().size());

        // Verify the embedded StatisticsSummary round-trips correctly.
        assertNotNull(loaded.getStats(), "Embedded stats should not be null.");
        assertEquals(originalStats.getAvgHoldingTime(),    loaded.getStats().getAvgHoldingTime(),    0.0001);
        assertEquals(originalStats.getHourlyThroughput(),  loaded.getStats().getHourlyThroughput(),  0.0001);
        assertEquals(originalStats.getDiversionCount(),    loaded.getStats().getDiversionCount());
        assertEquals(originalStats.getCancellationCount(), loaded.getStats().getCancellationCount());
    }

    /**
     * Verifies that loading a result that does not exist throws NoSuchFileException.
     */
    @Test
    void getSimulationResult_whenFileDoesNotExist_shouldThrowNoSuchFileException() {
        assertThrows(NoSuchFileException.class, () ->
                JsonFileHandler.getSimulationResult("NON_EXISTENT_RESULT_" + SUFFIX)
        );
    }

    /**
     * Verifies that resultNameExists returns true after the result has been saved.
     */
    @Test
    void resultNameExists_whenResultHasBeenSaved_shouldReturnTrue() throws Exception {
        JsonFileHandler.saveResults(buildResult(TEST_RESULT_NAME, buildConfig(), buildStats()));

        assertTrue(JsonFileHandler.resultNameExists(TEST_RESULT_NAME),
                "resultNameExists should return true after the result is saved.");
    }

    /**
     * Verifies that resultNameExists returns false for a result that was never saved.
     */
    @Test
    void resultNameExists_whenResultHasNotBeenSaved_shouldReturnFalse() {
        assertFalse(JsonFileHandler.resultNameExists("NEVER_SAVED_RESULT_" + SUFFIX),
                "resultNameExists should return false for a result that was never saved.");
    }

    /**
     * Verifies that deleting a SimulationResultSaved removes its file from disk.
     */
    @Test
    void deleteSimulationResult_shouldRemoveFileFromDisk() throws Exception {
        JsonFileHandler.saveResults(buildResult(TEST_RESULT_NAME, buildConfig(), buildStats()));

        JsonFileHandler.deleteSimulationResult(TEST_RESULT_NAME);

        Path deletedPath = Paths.get(System.getProperty("user.dir"), "data", "results", TEST_RESULT_NAME + ".json");
        assertFalse(Files.exists(deletedPath), "File should no longer exist after deletion.");
    }

    /**
     * Verifies that deleting a result that does not exist throws NoSuchFileException.
     */
    @Test
    void deleteSimulationResult_whenFileDoesNotExist_shouldThrowNoSuchFileException() {
        assertThrows(NoSuchFileException.class, () ->
                JsonFileHandler.deleteSimulationResult("NEVER_SAVED_RESULT_" + SUFFIX)
        );
    }

    /**
     * Verifies that a saved result appears in the list returned by listResultSummaries.
     */
    @Test
    void listResultSummaries_shouldContainSavedResult() throws Exception {
        JsonFileHandler.saveResults(buildResult(TEST_RESULT_NAME, buildConfig(), buildStats()));

        List<SimulationResultSummary> summaries = JsonFileHandler.listResultSummaries();

        boolean found = summaries.stream()
                .anyMatch(s -> TEST_RESULT_NAME.equals(s.getSimulationName()));
        assertTrue(found, "Saved result should appear in the summary list.");
    }

    /**
     * Verifies that each SimulationResultSummary carries the correct rates, runway count,
     * event count, and throughput.
     */
    @Test
    void listResultSummaries_summaryShouldCarryCorrectMetadata() throws Exception {
        SimulationConfig config = buildConfig();
        StatisticsSummary stats = buildStats();
        JsonFileHandler.saveResults(buildResult(TEST_RESULT_NAME, config, stats));

        List<SimulationResultSummary> summaries = JsonFileHandler.listResultSummaries();

        SimulationResultSummary summary = summaries.stream()
                .filter(s -> TEST_RESULT_NAME.equals(s.getSimulationName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected summary not found"));

        assertEquals(config.getInboundRate(),        summary.getInboundRate());
        assertEquals(config.getOutboundRate(),        summary.getOutboundRate());
        assertEquals(1,                               summary.getRunwayCount());
        assertEquals(0,                               summary.getScheduledEventsCount());
        assertEquals(stats.getHourlyThroughput(),     summary.getThroughput(), 0.0001);
    }

    /**
     * Verifies that listResultSummaries returns results sorted newest-first by dateExecuted.
     */
    @Test
    void listResultSummaries_shouldBeSortedNewestFirst() throws Exception {
        // Use fixed epoch millis so the ordering is deterministic regardless of wall-clock timing.
        SimulationResultSaved older = buildResult(TEST_RESULT_NAME,  buildConfig(), buildStats());
        older.setDateExecuted(new Date(1_000_000L));

        SimulationResultSaved newer = buildResult(TEST_RESULT_NAME2, buildConfig(), buildStats());
        newer.setDateExecuted(new Date(2_000_000L));

        JsonFileHandler.saveResults(older);
        JsonFileHandler.saveResults(newer);

        List<SimulationResultSummary> summaries = JsonFileHandler.listResultSummaries();

        // Filter to only the two we just created so other files on disk don't influence the order assertion.
        List<SimulationResultSummary> testSummaries = summaries.stream()
                .filter(s -> s.getSimulationName().equals(TEST_RESULT_NAME)
                          || s.getSimulationName().equals(TEST_RESULT_NAME2))
                .toList();

        assertEquals(2, testSummaries.size());
        assertEquals(TEST_RESULT_NAME2, testSummaries.get(0).getSimulationName(),
                "The result with the newer date should be listed first.");
    }

    /**
     * Verifies the core architectural guarantee of the new design: a single result file is
     * self-contained, embedding both the SimulationConfig and StatisticsSummary
     * without requiring a separate config template file.
     */
    @Test
    void saveResults_resultIsSelfContained_configAndStatsBothPersistedTogether() throws Exception {
        SimulationResultSaved result = buildResult(TEST_RESULT_NAME, buildConfig(), buildStats());
        JsonFileHandler.saveResults(result);

        SimulationResultSaved loaded = JsonFileHandler.getSimulationResult(TEST_RESULT_NAME);

        assertNotNull(loaded.getConfig(), "Self-contained result must embed the SimulationConfig.");
        assertNotNull(loaded.getStats(),  "Self-contained result must embed the StatisticsSummary.");
        // The result file is sufficient on its own — no separate config template file should exist.
        assertFalse(JsonFileHandler.templateNameExists(TEST_RESULT_NAME),
                "No separate config template file should be created when saving a result.");
    }
}