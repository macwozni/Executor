package pl.edu.agh.macwozni.executor.parallelism;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import pl.edu.agh.macwozni.executor.operation.Operation;

class ConcurrentBlockRunnerTest {
    private final BlockRunner runner = new ConcurrentBlockRunner();

    @Test
    void startsOperationsConcurrentlyAndWaitsForBothToFinish() throws Exception {
        var started = new CountDownLatch(2);
        var release = new CountDownLatch(1);
        var finished = new AtomicInteger();
        Operation action = operation(() -> {
            assertTrue(Thread.currentThread().isVirtual());
            started.countDown();
            await(release);
            finished.incrementAndGet();
        });

        try (var caller = Executors.newSingleThreadExecutor()) {
            var result = caller.submit(() -> {
                runner.runBlock(List.of(action, action));
                return finished.get();
            });
            try {
                await(started);
                assertFalse(result.isDone());
            } finally {
                release.countDown();
            }
            assertEquals(2, result.get(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void propagatesFailureAndWaitsForCancelledOperationsToExit() {
        var siblingStarted = new CountDownLatch(1);
        var stop = new CountDownLatch(1);
        var siblingInterrupted = new AtomicBoolean();
        var failure = new IllegalArgumentException("failure in test operation");
        Operation sibling = operation(() -> {
            siblingStarted.countDown();
            try {
                assertTrue(stop.await(5, TimeUnit.SECONDS), "Sibling operation was not interrupted");
            } catch (InterruptedException exception) {
                siblingInterrupted.set(true);
                Thread.currentThread().interrupt();
            }
        });
        Operation failing = operation(() -> {
            await(siblingStarted);
            throw failure;
        });

        var error = assertThrows(IllegalStateException.class, () -> runner.runBlock(List.of(sibling, failing)));
        assertSame(failure, error.getCause());
        assertTrue(siblingInterrupted.get());
    }

    @Test
    void restoresInterruptOnTheCallingThread() {
        Thread.currentThread().interrupt();
        try {
            var error = assertThrows(IllegalStateException.class,
                    () -> runner.runBlock(List.of(operation(() -> {}))));
            assertInstanceOf(InterruptedException.class, error.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for the test latch");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

    private static Operation operation(Runnable action) {
        return new Operation() {
            @Override
            public void execute() {
                action.run();
            }

            @Override
            public void run() {
                execute();
            }
        };
    }
}
