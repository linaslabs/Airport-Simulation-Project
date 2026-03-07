package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.dto.StatisticsSummary;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Statistics.
 *
 * Validates accumulation of holding times, wait times, delays, diversions and cancellations,
 * queue size maximums, and correct throughput calculation in the generated summary.
 */
class StatisticsTest {

    /**
     * Verifies that holding and takeoff queue size maximums are tracked correctly.
     */
    @Test
    void recordSizes_shouldTrackMaximums() {
        Statistics stats = new Statistics();

        stats.recordHoldingSize(3);
        stats.recordHoldingSize(7);
        stats.recordHoldingSize(5);

        stats.recordTakeOffQueueSize(2);
        stats.recordTakeOffQueueSize(9);
        stats.recordTakeOffQueueSize(4);

        StatisticsSummary summary = stats.generateSummary(60);

        assertEquals(7, summary.getMaxHoldingSize());
        assertEquals(9, summary.getMaxTakeOffQueueSize());
    }

    /**
     * Verifies that holding time and wait time are accumulated and their maximums are tracked.
     */
    @Test
    void recordHoldingAndWaitTimes_shouldAccumulateAndTrackMax() {
        Statistics stats = new Statistics();

        stats.recordLanding();
        stats.recordLanding();
        stats.recordHoldingTime(10);
        stats.recordHoldingTime(20);

        stats.recordTakeOff();
        stats.recordTakeOff();
        stats.recordTakeOffWaitTime(5);
        stats.recordTakeOffWaitTime(15);

        StatisticsSummary summary = stats.generateSummary(60);

        assertEquals(15.0, summary.getAvgHoldingTime());
        assertEquals(20, summary.getMaxHoldingTime());

        assertEquals(10.0, summary.getAvgWaitTime());
        assertEquals(15, summary.getMaxWaitTime());
    }

    /**
     * Verifies that arrival and takeoff delays are accumulated and their maximums are tracked.
     */
    @Test
    void recordDelays_shouldAccumulateAndTrackMax() {
        Statistics stats = new Statistics();

        stats.recordLanding();
        stats.recordLanding();
        stats.recordArrivalDelay(3);
        stats.recordArrivalDelay(7);

        stats.recordTakeOff();
        stats.recordTakeOff();
        stats.recordTakeOffDelay(4);
        stats.recordTakeOffDelay(8);

        StatisticsSummary summary = stats.generateSummary(60);

        assertEquals(5.0, summary.getAvgArrivalDelay());
        assertEquals(7, summary.getMaxArrivalDelay());

        assertEquals(6.0, summary.getAvgTakeOffDelay());
        assertEquals(8, summary.getMaxTakeOffDelay());
    }

    /**
     * Verifies that diversions and cancellations increment their respective counters.
     */
    @Test
    void recordDiversionsAndCancellations_shouldIncrementCounters() {
        Statistics stats = new Statistics();

        stats.recordDiversion();
        stats.recordDiversion();
        stats.recordCancellation();

        StatisticsSummary summary = stats.generateSummary(60);

        assertEquals(2, summary.getDiversionCount());
        assertEquals(1, summary.getCancellationCount());
    }

    /**
     * Verifies that generateSummary handles zero landings and takeoffs without division by zero.
     */
    @Test
    void generateSummary_shouldHandleZeroDivisionSafely() {
        Statistics stats = new Statistics();

        StatisticsSummary summary = stats.generateSummary(60);

        assertEquals(0.0, summary.getAvgHoldingTime());
        assertEquals(0.0, summary.getAvgWaitTime());
        assertEquals(0.0, summary.getAvgArrivalDelay());
        assertEquals(0.0, summary.getAvgTakeOffDelay());
        assertEquals(0.0, summary.getHourlyThroughput());
    }

    /**
     * Verifies that hourly throughput is calculated correctly from total operations and duration.
     */
    @Test
    void generateSummary_shouldCalculateThroughputCorrectly() {
        Statistics stats = new Statistics();

        stats.recordLanding();
        stats.recordLanding();
        stats.recordTakeOff();
        stats.recordTakeOff();

        // 4 operations in 120 ticks (2 minutes)
        StatisticsSummary summary = stats.generateSummary(120);

        // throughput = (4 / 120) * 60 = 2 per hour
        assertEquals(2.0, summary.getHourlyThroughput());
    }
}