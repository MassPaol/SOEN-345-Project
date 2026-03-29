package com.team_one.soen_345_project.acceptance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stress test for last-seat booking contention.
 *
 * Simulates two devices racing to book the same last seat and verifies:
 * - exactly one booking succeeds
 * - capacity is never exceeded
 * - available seats never becomes negative
 */
public class BookingConcurrencyStressTest {

    private static final int STRESS_ROUNDS = 200;

    @Test
    public void twoDevicesBookingLastSeat_onlyOneSucceeds_andCapacityNeverNegative() throws InterruptedException {
        for (int round = 0; round < STRESS_ROUNDS; round++) {
            RaceResult result = runSingleLastSeatRace();

            assertEquals("Exactly one booking must succeed (round " + round + ")", 1, result.successes);
            assertEquals("Exactly one booking must fail (round " + round + ")", 1, result.failures);
            assertEquals("Reservations must end at 1 (round " + round + ")", 1, result.finalReservations);
            assertTrue("Reservations cannot exceed capacity (round " + round + ")",
                    result.finalReservations <= result.capacity);
            assertTrue("Available seats cannot be negative (round " + round + ")",
                    result.availableSeats >= 0);
        }
    }

    private RaceResult runSingleLastSeatRace() throws InterruptedException {
        final SeatInventory inventory = new SeatInventory(1);

        final CountDownLatch ready = new CountDownLatch(2);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(2);

        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Runnable deviceBookingTask = () -> {
                ready.countDown();
                try {
                    start.await();
                    if (inventory.tryBook()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            };

            executor.submit(deviceBookingTask);
            executor.submit(deviceBookingTask);

            assertTrue("Both simulated devices should be ready", ready.await(2, TimeUnit.SECONDS));
            start.countDown();
            assertTrue("Both simulated devices should finish", done.await(2, TimeUnit.SECONDS));

            return new RaceResult(
                    inventory.getCapacity(),
                    successCount.get(),
                    failureCount.get(),
                    inventory.getReservations(),
                    inventory.getAvailableSeats()
            );
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    private static final class SeatInventory {
        private final int capacity;
        private final AtomicInteger reservations = new AtomicInteger(0);

        private SeatInventory(int capacity) {
            this.capacity = capacity;
        }

        private boolean tryBook() {
            while (true) {
                int current = reservations.get();
                if (current >= capacity) {
                    return false;
                }
                if (reservations.compareAndSet(current, current + 1)) {
                    return true;
                }
            }
        }

        private int getCapacity() {
            return capacity;
        }

        private int getReservations() {
            return reservations.get();
        }

        private int getAvailableSeats() {
            return capacity - reservations.get();
        }
    }

    private static final class RaceResult {
        private final int capacity;
        private final int successes;
        private final int failures;
        private final int finalReservations;
        private final int availableSeats;

        private RaceResult(int capacity, int successes, int failures, int finalReservations, int availableSeats) {
            this.capacity = capacity;
            this.successes = successes;
            this.failures = failures;
            this.finalReservations = finalReservations;
            this.availableSeats = availableSeats;
        }
    }
}
