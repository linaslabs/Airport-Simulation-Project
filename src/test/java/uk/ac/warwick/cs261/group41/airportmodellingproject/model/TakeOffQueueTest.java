package uk.ac.warwick.cs261.group41.airportmodellingproject.model;

import org.junit.jupiter.api.Test;
import uk.ac.warwick.cs261.group41.airportmodellingproject.enums.AircraftState;
import uk.ac.warwick.cs261.group41.airportmodellingproject.service.EventManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TakeOffQueue.
 *
 * Validates FIFO ordering of departures, cancellation when wait time reaches maxWaitTime,
 * and that each cancellation is reported to EventManager.
 */
class TakeOffQueueTest {

    private static Aircraft mockAircraft(String callsign, int entryTick) {
        Aircraft a = mock(Aircraft.class);
        when(a.getCallsign()).thenReturn(callsign);
        when(a.getEntryTick()).thenReturn(entryTick);
        return a;
    }

    /**
     * Verifies FIFO ordering:
     * - peekNextAircraft() returns the first aircraft added
     * - getNextAircraft() removes aircraft in the order they were added
     */
    @Test
    void addPeekGet_shouldBehaveAsFifoQueue() {
        TakeOffQueue queue = new TakeOffQueue(10);
        EventManager em = mock(EventManager.class);
        queue.setEventManager(em);

        Aircraft a1 = mockAircraft("A1", 0);
        Aircraft a2 = mockAircraft("A2", 1);

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

        verifyNoInteractions(em);
    }

    /**
     * Verifies aircraft are not cancelled if wait time is strictly below maxWaitTime.
     */
    @Test
    void update_whenWaitTimeBelowMax_shouldNotCancelOrRemove() {
        TakeOffQueue queue = new TakeOffQueue(5);
        EventManager em = mock(EventManager.class);
        queue.setEventManager(em);

        Aircraft a = mockAircraft("A1", 0);
        queue.addAircraft(a);

        queue.update(4); // waitTime = 4 < 5

        assertEquals(1, queue.getSize(), "Aircraft should remain in queue when below maxWaitTime.");
        verify(a, never()).setState(AircraftState.CANCELLED);
        verify(em, never()).reportCancellation(anyString(), anyInt());
    }

    /**
     * Verifies the cancellation threshold is inclusive:
     * aircraft are cancelled when wait time >= maxWaitTime.
     */
    @Test
    void update_whenWaitTimeEqualsMax_shouldCancelRemoveAndReport() {
        TakeOffQueue queue = new TakeOffQueue(5);
        EventManager em = mock(EventManager.class);
        queue.setEventManager(em);

        Aircraft a = mockAircraft("A1", 0);
        queue.addAircraft(a);

        queue.update(5); // waitTime = 5 == maxWaitTime => cancel

        assertTrue(queue.isEmpty(), "Aircraft should be removed when waitTime >= maxWaitTime.");
        verify(a).setState(AircraftState.CANCELLED);
        verify(em, times(1)).reportCancellation("A1", 5);
    }

    /**
     * Verifies only expired aircraft are cancelled, while remaining aircraft keep FIFO order.
     */
    @Test
    void update_shouldCancelOnlyExpiredAircraft_andPreserveOrderOfRemaining() {
        TakeOffQueue queue = new TakeOffQueue(5);
        EventManager em = mock(EventManager.class);
        queue.setEventManager(em);

        Aircraft expired = mockAircraft("EXPIRED", 0); // wait at tick 5 = 5 => cancel
        Aircraft ok1 = mockAircraft("OK1", 3);         // wait at tick 5 = 2 => keep
        Aircraft ok2 = mockAircraft("OK2", 4);         // wait at tick 5 = 1 => keep

        queue.addAircraft(expired);
        queue.addAircraft(ok1);
        queue.addAircraft(ok2);

        queue.update(5);

        assertEquals(2, queue.getSize(), "Only expired aircraft should be removed.");
        assertTrue(queue.peekNextAircraft().isPresent());
        assertSame(ok1, queue.peekNextAircraft().get(), "FIFO should advance to the next remaining aircraft.");

        verify(expired).setState(AircraftState.CANCELLED);
        verify(ok1, never()).setState(AircraftState.CANCELLED);
        verify(ok2, never()).setState(AircraftState.CANCELLED);

        verify(em, times(1)).reportCancellation("EXPIRED", 5);
    }

    /**
     * Verifies multiple expired aircraft are cancelled in a single update() call,
     * and each cancellation is reported.
     */
    @Test
    void update_whenMultipleExpired_shouldCancelAllExpiredAndReportEach() {
        TakeOffQueue queue = new TakeOffQueue(5);
        EventManager em = mock(EventManager.class);
        queue.setEventManager(em);

        Aircraft a1 = mockAircraft("A1", 0); // wait at tick 10 = 10 => cancel
        Aircraft a2 = mockAircraft("A2", 1); // wait at tick 10 = 9  => cancel

        queue.addAircraft(a1);
        queue.addAircraft(a2);

        queue.update(10);

        assertTrue(queue.isEmpty(), "All expired aircraft should be removed.");
        verify(a1).setState(AircraftState.CANCELLED);
        verify(a2).setState(AircraftState.CANCELLED);

        verify(em, times(1)).reportCancellation("A1", 10);
        verify(em, times(1)).reportCancellation("A2", 10);
    }

    /**
     * Defensive test: if currentTick is earlier than entryTick, wait time is negative and must not cancel.
     */
    @Test
    void update_whenCurrentTickBeforeEntryTick_shouldNotCancel() {
        TakeOffQueue queue = new TakeOffQueue(5);
        EventManager em = mock(EventManager.class);
        queue.setEventManager(em);

        Aircraft future = mockAircraft("FUTURE", 10);
        queue.addAircraft(future);

        queue.update(5); // waitTime = -5

        assertEquals(1, queue.getSize(), "Aircraft with future entryTick should not be cancelled.");
        verify(future, never()).setState(AircraftState.CANCELLED);
        verify(em, never()).reportCancellation(anyString(), anyInt());
    }

    /**
     * Verifies changing maxWaitTime affects the cancellation rule.
     */
    @Test
    void setMaxWaitTime_shouldAffectCancellationRule() {
        TakeOffQueue queue = new TakeOffQueue(10);
        EventManager em = mock(EventManager.class);
        queue.setEventManager(em);

        Aircraft a = mockAircraft("A1", 0);
        queue.addAircraft(a);

        queue.update(9); // waitTime 9 < 10 => keep
        assertEquals(1, queue.getSize());
        verify(em, never()).reportCancellation(anyString(), anyInt());

        queue.setMaxWaitTime(9);
        queue.update(9); // waitTime 9 >= 9 => cancel

        assertTrue(queue.isEmpty(), "After lowering maxWaitTime, the aircraft should be cancelled.");
        verify(a).setState(AircraftState.CANCELLED);
        verify(em, times(1)).reportCancellation("A1", 9);
    }

    /**
     * Verifies removeAircraft() removes the provided aircraft from the queue.
     */
    @Test
    void removeAircraft_shouldRemoveSpecificAircraft() {
        TakeOffQueue queue = new TakeOffQueue(10);
        EventManager em = mock(EventManager.class);
        queue.setEventManager(em);

        Aircraft a1 = mockAircraft("A1", 0);
        Aircraft a2 = mockAircraft("A2", 0);

        queue.addAircraft(a1);
        queue.addAircraft(a2);

        queue.removeAircraft(a1);

        assertEquals(1, queue.getSize(), "Queue size should decrease after removal.");
        assertTrue(queue.peekNextAircraft().isPresent());
        assertSame(a2, queue.peekNextAircraft().get(), "Remaining aircraft should now be at the head.");

        verifyNoInteractions(em);
    }
}