package pl.edu.agh.macwozni.executor.parallelism;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.StructuredTaskScope;
import pl.edu.agh.macwozni.executor.operation.Operation;

/** Runs each operation in a structured subtask on a virtual thread. */
public class ConcurrentBlockRunner extends AbstractBlockRunner {
    @Override
    protected void runNonEmptyBlock(List<Operation> operations) {
        var startGate = new CountDownLatch(1);

        try (var scope = StructuredTaskScope.open()) {
            for (var operation : operations) {
                scope.fork(() -> {
                    startGate.await();
                    operation.run();
                    return null;
                });
            }

            startGate.countDown();
            scope.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Przerwano wykonanie bloku", exception);
        } catch (StructuredTaskScope.FailedException exception) {
            throw new IllegalStateException("Operacja w bloku zakończyła się błędem", exception.getCause());
        }
    }
}
