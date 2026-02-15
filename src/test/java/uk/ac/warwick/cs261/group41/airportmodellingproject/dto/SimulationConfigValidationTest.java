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

        List<RunwayConfig> runways = new ArrayList<>();
        runways.add(new RunwayConfig(0, RunwayStatus.AVAILABLE, RunwayMode.MIXED));
        config.setRunwaySettings(runways);

        config.setInboundRate(15);
        config.setOutboundRate(15);
        config.setMaxWaitTime(30);
        config.setTickTime(1000);

        return config;
    }

    /**
     * Verifies that a fully valid SimulationConfig produces no validation errors.
     * Confirms the "happy path" works correctly.
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
     * Verifies that tickTime must be within the allowed range (100–10000 ms).
     */
    @Test
    void tickTime_outOfRange_shouldFailValidation() {
        SimulationConfig tooLow = validConfig();
        tooLow.setTickTime(99);

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
     * Verifies that boundary values are accepted.
     */
    @Test
    void boundaryValues_shouldPassValidation() {
        SimulationConfig config = validConfig();

        config.setInboundRate(0);
        config.setOutboundRate(100);
        config.setMaxWaitTime(1);
        config.setTickTime(100);

        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);
        assertTrue(violations.isEmpty());
    }

    /**
     * Verifies that nested validation works:
     * an invalid RunwayConfig inside the list should invalidate the entire SimulationConfig.
     */
    @Test
    void nestedRunwayConfig_invalid_shouldFailSimulationConfigValidation() {
        SimulationConfig config = validConfig();

        List<RunwayConfig> runways = new ArrayList<>();
        runways.add(new RunwayConfig(-1, RunwayStatus.AVAILABLE, RunwayMode.MIXED));
        config.setRunwaySettings(runways);

        Set<ConstraintViolation<SimulationConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasNestedViolationOn(violations, "runwaySettings[0]."));
    }

    /**
     * Verifies that runwaySettings cannot be null.
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
     * Ensures error feedback exists (important for UI error handling).
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
