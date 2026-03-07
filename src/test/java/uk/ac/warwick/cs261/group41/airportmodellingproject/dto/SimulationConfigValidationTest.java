package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation tests for SimulationConfig.
 *
 * Verifies field constraints: runway list size, numeric ranges, required nullability,
 * boundary values, nested RunwayConfig validation, and violation message content.
 */
class SimulationConfigValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static Set<ConstraintViolation<SimulationConfig>> validate(SimulationConfig config) {
        return validator.validate(config);
    }

    private static boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property));
    }

    private static boolean hasNestedViolationOn(Set<? extends ConstraintViolation<?>> violations, String prefix) {
        // e.g. "runwaySettings[0].runwayID"
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().startsWith(prefix));
    }

    private static String messagesFor(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals(property))
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(" | "));
    }

    private static SimulationConfig validConfig() {
        SimulationConfig config = new SimulationConfig();

        // Ensure runwaySettings satisfies @NotNull, @Size(min=1), and nested @Valid.
        List<RunwayConfig> runways = new ArrayList<>();
        runways.add(new RunwayConfig(0, RunwayStatus.AVAILABLE, RunwayMode.MIXED));
        config.setRunwaySettings(runways);

        // Ensure numeric fields satisfy declared ranges.
        config.setInboundRate(15);
        config.setOutboundRate(15);
        config.setMaxWaitTime(30);
        config.setTickTime(1000);
        config.setDuration(420);

        // Seed is required to support reproducible simulation runs.
        config.setSeed(1L);

        return config;
    }

    /**
     * Verifies that a fully valid SimulationConfig produces no validation errors.
     */
    @Test
    void validSimulationConfig_shouldHaveNoViolations() {
        SimulationConfig config = validConfig();
        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);
        assertTrue(violations.isEmpty(), "Expected no violations but got: " + violations);
    }

    /**
     * Verifies that runwaySettings cannot be empty.
     */
    @Test
    void runwaySettings_empty_shouldFailValidation() {
        SimulationConfig config = validConfig();
        config.setRunwaySettings(List.of());

        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "runwaySettings"));
    }

    /**
     * Verifies that runwaySettings cannot contain more than 10 runways.
     */
    @Test
    void runwaySettings_moreThan10_shouldFailValidation() {
        SimulationConfig config = validConfig();

        List<RunwayConfig> runways = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            // This test is specifically validating @Size(max=10) on the list.
            runways.add(new RunwayConfig(0, RunwayStatus.AVAILABLE, RunwayMode.MIXED));
        }
        config.setRunwaySettings(runways);

        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "runwaySettings"));
    }

    /**
     * Verifies that inboundRate must be within the allowed range (0–100).
     */
    @Test
    void inboundRate_outOfRange_shouldFailValidation() {
        SimulationConfig configLow = validConfig();
        configLow.setInboundRate(-1);

        Set<ConstraintViolation<SimulationConfig>> violationsLow = validate(configLow);
        assertFalse(violationsLow.isEmpty());
        assertTrue(hasViolationOn(violationsLow, "inboundRate"));

        SimulationConfig configHigh = validConfig();
        configHigh.setInboundRate(101);

        Set<ConstraintViolation<SimulationConfig>> violationsHigh = validate(configHigh);
        assertFalse(violationsHigh.isEmpty());
        assertTrue(hasViolationOn(violationsHigh, "inboundRate"));
    }

    /**
     * Verifies that outboundRate must be within the allowed range (0–100).
     */
    @Test
    void outboundRate_outOfRange_shouldFailValidation() {
        SimulationConfig configLow = validConfig();
        configLow.setOutboundRate(-1);

        Set<ConstraintViolation<SimulationConfig>> violationsLow = validate(configLow);
        assertFalse(violationsLow.isEmpty());
        assertTrue(hasViolationOn(violationsLow, "outboundRate"));

        SimulationConfig configHigh = validConfig();
        configHigh.setOutboundRate(101);

        Set<ConstraintViolation<SimulationConfig>> violationsHigh = validate(configHigh);
        assertFalse(violationsHigh.isEmpty());
        assertTrue(hasViolationOn(violationsHigh, "outboundRate"));
    }

    /**
     * Verifies that maxWaitTime must be at least 1.
     */
    @Test
    void maxWaitTime_belowMin_shouldFailValidation() {
        SimulationConfig config = validConfig();
        config.setMaxWaitTime(0);

        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "maxWaitTime"));
    }

    /**
     * Verifies that tickTime must be within the allowed range (1–10000 ms).
     */
    @Test
    void tickTime_outOfRange_shouldFailValidation() {
        SimulationConfig tooLow = validConfig();
        tooLow.setTickTime(0);

        Set<ConstraintViolation<SimulationConfig>> vLow = validate(tooLow);
        assertFalse(vLow.isEmpty());
        assertTrue(hasViolationOn(vLow, "tickTime"));

        SimulationConfig tooHigh = validConfig();
        tooHigh.setTickTime(10001);

        Set<ConstraintViolation<SimulationConfig>> vHigh = validate(tooHigh);
        assertFalse(vHigh.isEmpty());
        assertTrue(hasViolationOn(vHigh, "tickTime"));
    }

    /**
     * Verifies that duration must be within the allowed range (60–1440 minutes).
     */
    @Test
    void duration_outOfRange_shouldFailValidation() {
        SimulationConfig tooLow = validConfig();
        tooLow.setDuration(59);

        Set<ConstraintViolation<SimulationConfig>> vLow = validate(tooLow);
        assertFalse(vLow.isEmpty());
        assertTrue(hasViolationOn(vLow, "duration"));

        SimulationConfig tooHigh = validConfig();
        tooHigh.setDuration(1441);

        Set<ConstraintViolation<SimulationConfig>> vHigh = validate(tooHigh);
        assertFalse(vHigh.isEmpty());
        assertTrue(hasViolationOn(vHigh, "duration"));
    }

    /**
     * Verifies that seed is required (must not be null).
     */
    @Test
    void seed_null_shouldFailValidation() {
        SimulationConfig config = validConfig();
        config.setSeed(null);

        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "seed"));
    }

    /**
     * Verifies that required fields cannot be null.
     */
    @Test
    void requiredFields_null_shouldFailValidation() {
        SimulationConfig config = validConfig();

        config.setInboundRate(null);
        config.setOutboundRate(null);
        config.setMaxWaitTime(null);
        config.setTickTime(null);
        config.setDuration(null);
        config.setSeed(null);

        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "inboundRate"));
        assertTrue(hasViolationOn(violations, "outboundRate"));
        assertTrue(hasViolationOn(violations, "maxWaitTime"));
        assertTrue(hasViolationOn(violations, "tickTime"));
        assertTrue(hasViolationOn(violations, "duration"));
        assertTrue(hasViolationOn(violations, "seed"));
    }

    /**
     * Verifies that boundary values are accepted.
     */
    @Test
    void boundaryValues_shouldPassValidation() {
        SimulationConfig min = validConfig();
        min.setInboundRate(0);
        min.setOutboundRate(0);
        min.setMaxWaitTime(1);
        min.setTickTime(1);
        min.setDuration(60);
        min.setSeed(1L);

        SimulationConfig max = validConfig();
        max.setInboundRate(100);
        max.setOutboundRate(100);
        max.setMaxWaitTime(1);
        max.setTickTime(10000);
        max.setDuration(1440);
        max.setSeed(999L);

        assertTrue(validate(min).isEmpty(), "Expected lower boundary values to be valid, got: " + validate(min));
        assertTrue(validate(max).isEmpty(), "Expected upper boundary values to be valid, got: " + validate(max));
    }

    /**
     * Verifies that nested validation works:
     * an invalid RunwayConfig inside runwaySettings should invalidate the entire SimulationConfig.
     */
    @Test
    void nestedRunwayConfig_invalid_shouldFailSimulationConfigValidation() {
        SimulationConfig config = validConfig();

        List<RunwayConfig> runways = new ArrayList<>();
        runways.add(new RunwayConfig(-1, RunwayStatus.AVAILABLE, RunwayMode.MIXED)); // invalid runwayID
        config.setRunwaySettings(runways);

        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasNestedViolationOn(violations, "runwaySettings[0]."));
    }

    /**
     * Verifies that runwaySettings cannot be null (setter normalises null to an empty list, which should fail @Size(min=1)).
     */
    @Test
    void nullRunwaySettings_shouldFailValidation() {
        SimulationConfig config = validConfig();
        config.setRunwaySettings(null);

        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "runwaySettings"));
    }

    /**
     * Verifies that validation messages are returned for invalid fields.
     * Ensures user-facing error feedback exists for configuration inputs.
     */
    @Test
    void helpfulMessagesExample_forInboundRate() {
        SimulationConfig config = validConfig();
        config.setInboundRate(101);

        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);

        assertTrue(hasViolationOn(violations, "inboundRate"));

        String msg = messagesFor(violations, "inboundRate");
        assertFalse(msg.isBlank());
    }
}