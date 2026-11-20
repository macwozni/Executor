package pl.edu.agh.macwozni.executor.parallelism;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import pl.edu.agh.macwozni.executor.operation.Operation;

/** Runs a block on a fixed-size pool of platform threads. */
public class ConcurrentBlockRunner extends AbstractBlockRunner {
    private static final int MAX_WORKERS = 8;

    @Override
    protected void runNonEmptyBlock(List<Operation> operations) {
        var pool = Executors.newFixedThreadPool(Math.min(MAX_WORKERS, operations.size()));
        var completed = new ExecutorCompletionService<Void>(pool);

        try {
            for (var operation : operations) {
                completed.submit(operation, null);
            }

            // Observe failures as tasks finish, even if an earlier task is still running.
            for (int index = 0; index < operations.size(); index++) {
                completed.take().get();
            }
            pool.shutdown();
            awaitTermination(pool);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Block execution interrupted", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("An operation in the block failed", exception.getCause());
        } finally {
            if (!pool.isTerminated()) {
                cancelAndAwaitTermination(pool);
            }
        }
    }

    private static void awaitTermination(ExecutorService pool) throws InterruptedException {
        while (!pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
            // Resume waiting if the timeout expires.
        }
    }

    private static void cancelAndAwaitTermination(ExecutorService pool) {
        pool.shutdownNow();
        boolean interrupted = Thread.interrupted();
        try {
            while (!pool.isTerminated()) {
                try {
                    awaitTermination(pool);
                } catch (InterruptedException exception) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
