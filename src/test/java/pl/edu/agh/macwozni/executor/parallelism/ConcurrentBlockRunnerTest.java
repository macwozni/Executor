package pl.edu.agh.macwozni.executor.parallelism;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import pl.edu.agh.macwozni.executor.operation.Operation;

class ConcurrentBlockRunnerTest {
    private final BlockRunner runner = new ConcurrentBlockRunner();

    @Test
    void startsOperationsConcurrentlyAndWaitsForBothToFinish() throws Exception {
        var started = new CountDownLatch(2);
        var release = new CountDownLatch(1);
        var finished = new AtomicInteger();
        Operation action = operation(() -> {
            started.countDown();
            await(release);
            finished.incrementAndGet();
        });

        var caller = Executors.newSingleThreadExecutor();
        try {
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
        } finally {
            shutdown(caller);
        }
    }

    @Test
    @Timeout(10)
    void runsEveryOperationOnceWhenTheBlockExceedsThePoolSize() {
        var completed = ConcurrentHashMap.<Integer>newKeySet();
        var block = IntStream.range(0, 24)
                .mapToObj(index -> operation(() -> assertTrue(completed.add(index))))
                .toList();

        runner.runBlock(block);

        assertEquals(block.size(), completed.size());
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

    @Test
    void interruptsRunningOperationsAndWaitsForThemToExit() throws Exception {
        var started = new CountDownLatch(1);
        var hold = new CountDownLatch(1);
        var interrupted = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var owner = new AtomicReference<Thread>();
        var workerFinished = new AtomicBoolean();
        Operation action = operation(() -> {
            started.countDown();
            try {
                assertTrue(hold.await(5, TimeUnit.SECONDS), "Operation was not interrupted");
            } catch (InterruptedException exception) {
                interrupted.countDown();
                await(release);
                Thread.currentThread().interrupt();
            } finally {
                workerFinished.set(true);
            }
        });

        var caller = Executors.newSingleThreadExecutor();
        try {
            var result = caller.submit(() -> {
                owner.set(Thread.currentThread());
                var error = assertThrows(IllegalStateException.class, () -> runner.runBlock(List.of(action)));
                assertTrue(Thread.currentThread().isInterrupted());
                assertTrue(workerFinished.get());
                return error;
            });
            await(started);
            owner.get().interrupt();
            await(interrupted);
            assertFalse(result.isDone());
            release.countDown();
            assertInstanceOf(InterruptedException.class, result.get(5, TimeUnit.SECONDS).getCause());
        } finally {
            hold.countDown();
            release.countDown();
            shutdown(caller);
        }
    }

    private static void shutdown(ExecutorService executor) throws InterruptedException {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Caller executor did not terminate");
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
