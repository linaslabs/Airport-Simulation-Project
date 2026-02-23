package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TakeOffQueue.
 *
 * Focus: FIFO behaviour, max-wait-time cancellation rule, and ensuring cancellations
 * are recorded via Statistics when aircraft exceed the configured maxWaitTime.
 */
class TakeOffQueueTest {

    private static Aircraft mockAircraftWithEntryTick(int entryTick) {
        Aircraft a = mock(Aircraft.class);
        when(a.getEntryTick()).thenReturn(entryTick);
        return a;
    }

    @Test
    void addPeekGet_shouldBehaveAsFifoQueue() {
        // Verifies FIFO ordering: peek returns the first added aircraft,
        // getNextAircraft removes and returns in the same order.
        Statistics stats = mock(Statistics.class);
        TakeOffQueue queue = new TakeOffQueue(10, stats);

        Aircraft a1 = mockAircraftWithEntryTick(0);
        Aircraft a2 = mockAircraftWithEntryTick(1);

        queue.addAircraft(a1);
        queue.addAircraft(a2);

        Optional<Aircraft> peeked = queue.peekNextAircraft();
        assertTrue(peeked.isPresent());
        assertSame(a1, peeked.get(), "peekNextAircraft() should return the first aircraft (FIFO).");

        Optional<Aircraft> next1 = queue.getNextAircraft();
        assertTrue(next1.isPresent());
        assertSame(a1, next1.get(), "First getNextAircraft() should return the first aircraft added.");

        Optional<Aircraft> next2 = queue.getNextAircraft();
        assertTrue(next2.isPresent());
        assertSame(a2, next2.get(), "Second getNextAircraft() should return the second aircraft added.");

        assertTrue(queue.isEmpty(), "Queue should be empty after polling all aircraft.");
        verifyNoInteractions(stats);
    }

    @Test
    void update_whenWaitTimeBelowMax_shouldNotCancelOrRemove() {
        // Verifies aircraft are NOT cancelled if waitTime < maxWaitTime.
        Statistics stats = mock(Statistics.class);
        TakeOffQueue queue = new TakeOffQueue(5, stats);

        Aircraft a = mockAircraftWithEntryTick(0);
        queue.addAircraft(a);

        queue.update(4); // waitTime = 4 < 5

        assertEquals(1, queue.getSize(), "Aircraft should remain in queue when below maxWaitTime.");
        verify(a, never()).setState(AircraftState.CANCELLED);
        verify(stats, never()).recordCancellation();
    }

    @Test
    void update_whenWaitTimeEqualsMax_shouldCancelRemoveAndRecord() {
        // Verifies the cancellation threshold is inclusive: waitTime >= maxWaitTime cancels.
        Statistics stats = mock(Statistics.class);
        TakeOffQueue queue = new TakeOffQueue(5, stats);

        Aircraft a = mockAircraftWithEntryTick(0);
        queue.addAircraft(a);

        queue.update(5); // waitTime = 5 == maxWaitTime => cancel

        assertTrue(queue.isEmpty(), "Aircraft should be removed when waitTime >= maxWaitTime.");
        verify(a).setState(AircraftState.CANCELLED);
        verify(stats, times(1)).recordCancellation();
    }

    @Test
    void update_shouldCancelOnlyExpiredAircraft_andPreserveOrderOfRemaining() {
        // Verifies only aircraft meeting the cancellation rule are removed,
        // and remaining aircraft keep FIFO order.
        Statistics stats = mock(Statistics.class);
        TakeOffQueue queue = new TakeOffQueue(5, stats);

        Aircraft expired = mockAircraftWithEntryTick(0);   // waitTime at tick 5 => 5 (cancel)
        Aircraft ok1 = mockAircraftWithEntryTick(3);       // waitTime at tick 5 => 2 (keep)
        Aircraft ok2 = mockAircraftWithEntryTick(4);       // waitTime at tick 5 => 1 (keep)

        queue.addAircraft(expired);
        queue.addAircraft(ok1);
        queue.addAircraft(ok2);

        queue.update(5);

        assertEquals(2, queue.getSize(), "Only the expired aircraft should be removed.");
        assertTrue(queue.peekNextAircraft().isPresent());
        assertSame(ok1, queue.peekNextAircraft().get(), "After removing the head, FIFO should advance to the next.");

        verify(expired).setState(AircraftState.CANCELLED);
        verify(ok1, never()).setState(AircraftState.CANCELLED);
        verify(ok2, never()).setState(AircraftState.CANCELLED);
        verify(stats, times(1)).recordCancellation();
    }

    @Test
    void update_whenMultipleExpired_shouldCancelAllExpiredAndRecordEach() {
        // Verifies multiple expired aircraft are all cancelled in a single update()
        // and each cancellation is recorded.
        Statistics stats = mock(Statistics.class);
        TakeOffQueue queue = new TakeOffQueue(5, stats);

        Aircraft a1 = mockAircraftWithEntryTick(0); // waitTime at tick 10 => 10 (cancel)
        Aircraft a2 = mockAircraftWithEntryTick(1); // waitTime at tick 10 => 9  (cancel)

        queue.addAircraft(a1);
        queue.addAircraft(a2);

        queue.update(10);

        assertTrue(queue.isEmpty(), "All expired aircraft should be removed.");
        verify(a1).setState(AircraftState.CANCELLED);
        verify(a2).setState(AircraftState.CANCELLED);
        verify(stats, times(2)).recordCancellation();
    }

    @Test
    void update_whenCurrentTickBeforeEntryTick_shouldNotCancel() {
        // Defensive test: if entryTick is in the future, waitTime is negative and must not cancel.
        Statistics stats = mock(Statistics.class);
        TakeOffQueue queue = new TakeOffQueue(5, stats);

        Aircraft future = mockAircraftWithEntryTick(10);
        queue.addAircraft(future);

        queue.update(5); // waitTime = -5

        assertEquals(1, queue.getSize(), "Aircraft with future entryTick should not be cancelled.");
        verify(future, never()).setState(AircraftState.CANCELLED);
        verify(stats, never()).recordCancellation();
    }

    @Test
    void setMaxWaitTime_shouldAffectCancellationRule() {
        // Verifies changing maxWaitTime changes whether aircraft will be cancelled.
        Statistics stats = mock(Statistics.class);
        TakeOffQueue queue = new TakeOffQueue(10, stats);

        Aircraft a = mockAircraftWithEntryTick(0);
        queue.addAircraft(a);

        queue.update(9); // waitTime 9 < 10 => keep
        assertEquals(1, queue.getSize());
        verify(stats, never()).recordCancellation();

        queue.setMaxWaitTime(9);
        queue.update(9); // waitTime 9 >= 9 => cancel

        assertTrue(queue.isEmpty(), "After lowering maxWaitTime, the aircraft should now be cancelled.");
        verify(a).setState(AircraftState.CANCELLED);
        verify(stats, times(1)).recordCancellation();
    }

    @Test
    void removeAircraft_shouldRemoveSpecificAircraft() {
        // Verifies removeAircraft() removes the provided aircraft from the queue.
        Statistics stats = mock(Statistics.class);
        TakeOffQueue queue = new TakeOffQueue(10, stats);

        Aircraft a1 = mockAircraftWithEntryTick(0);
        Aircraft a2 = mockAircraftWithEntryTick(0);

        queue.addAircraft(a1);
        queue.addAircraft(a2);

        queue.removeAircraft(a1);

        assertEquals(1, queue.getSize(), "Queue size should decrease after removal.");
        assertTrue(queue.peekNextAircraft().isPresent());
        assertSame(a2, queue.peekNextAircraft().get(), "Remaining aircraft should now be at the head.");
        verifyNoInteractions(stats);
    }
}