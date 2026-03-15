package uk.ac.warwick.cs261.group41.airportmodellingproject.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayMode;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.RunwayStatus;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation tests for RunwayConfig.
 *
 * Verifies that runwayID must be within 0–9, status and mode cannot be null,
 * boundary values are accepted, and multiple invalid fields each produce a violation.
 */
class RunwayConfigValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Builds a fully valid RunwayConfig that satisfies all validation constraints.
     *
     * @return a RunwayConfig that should produce zero constraint violations
     */
    private static RunwayConfig validConfig() {
        return new RunwayConfig(
                0,
                RunwayStatus.AVAILABLE,
                RunwayMode.MIXED
        );
    }

    /**
     * Runs Jakarta Bean Validation on the given RunwayConfig.
     *
     * @param config the RunwayConfig to validate
     * @return the set of constraint violations found, or an empty set if the config is valid
     */
    private static Set<ConstraintViolation<RunwayConfig>> validate(RunwayConfig config) {
        return validator.validate(config);
    }

    /**
     * Checks whether any of the given violations target the specified property path.
     *
     * @param violations the set of violations to search
     * @param property   the exact property path to look for (e.g. "runwayID")
     * @return true if at least one violation matches the property path
     */
    private static boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property));
    }

    /**
     * Concatenates the violation messages for all violations targeting the specified property.
     *
     * @param violations the set of violations to filter
     * @param property   the exact property path to collect messages for
     * @return a single string of all matching violation messages joined by " | "
     */
    private static String messagesFor(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals(property))
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(" | "));
    }

    /**
     * Verifies that a fully valid RunwayConfig produces no validation errors.
     * Confirms the "happy path" works correctly.
     */
    @Test
    void validRunwayConfig_shouldHaveNoViolations() {
        RunwayConfig config = validConfig();
        Set<ConstraintViolation<RunwayConfig>> violations = validate(config);
        assertTrue(violations.isEmpty(), "Expected no violations but got: " + violations);
    }

    /**
     * Verifies that runwayID below the allowed range (0–9) fails validation.
     */
    @Test
    void runwayId_belowRange_shouldFailValidation() {
        RunwayConfig config = validConfig();
        config.setRunwayID(-1);

        Set<ConstraintViolation<RunwayConfig>> violations = validate(config);

        assertFalse(violations.isEmpty(), "Expected violations but got none.");
        assertTrue(hasViolationOn(violations, "runwayID"));
        assertTrue(messagesFor(violations, "runwayID").contains("between 0 and 9"));
    }

    /**
     * Verifies that runwayID above the allowed range (0–9) fails validation.
     */
    @Test
    void runwayId_aboveRange_shouldFailValidation() {
        RunwayConfig config = validConfig();
        config.setRunwayID(10);

        Set<ConstraintViolation<RunwayConfig>> violations = validate(config);

        assertFalse(violations.isEmpty(), "Expected violations but got none.");
        assertTrue(hasViolationOn(violations, "runwayID"));
        assertTrue(messagesFor(violations, "runwayID").contains("between 0 and 9"));
    }

    /**
     * Verifies that boundary values (0 and 9) are accepted.
     * Ensures inclusive range validation is correct.
     */
    @Test
    void runwayId_boundaryValues_shouldPassValidation() {
        RunwayConfig low = validConfig();
        low.setRunwayID(0);

        RunwayConfig high = validConfig();
        high.setRunwayID(9);

        assertTrue(validate(low).isEmpty());
        assertTrue(validate(high).isEmpty());
    }

    /**
     * Verifies that runway status cannot be null.
     */
    @Test
    void nullStatus_shouldFailValidation() {
        RunwayConfig config = validConfig();
        config.setStatus(null);

        Set<ConstraintViolation<RunwayConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "status"));
    }

    /**
     * Verifies that runway mode cannot be null.
     */
    @Test
    void nullMode_shouldFailValidation() {
        RunwayConfig config = validConfig();
        config.setMode(null);

        Set<ConstraintViolation<RunwayConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "mode"));
    }

    /**
     * Verifies that multiple invalid fields produce multiple validation errors.
     * Ensures validation does not stop at the first failure.
     */
    @Test
    void multipleInvalidFields_shouldReportMultipleViolations() {
        RunwayConfig config = validConfig();
        config.setRunwayID(-1);
        config.setStatus(null);
        config.setMode(null);

        Set<ConstraintViolation<RunwayConfig>> violations = validate(config);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOn(violations, "runwayID"));
        assertTrue(hasViolationOn(violations, "status"));
        assertTrue(hasViolationOn(violations, "mode"));
    }
}
