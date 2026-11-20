package pl.edu.agh.macwozni.executor;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import pl.edu.agh.macwozni.executor.model.TraceInput;
import pl.edu.agh.macwozni.executor.operation.TransactionOperation;
import pl.edu.agh.macwozni.executor.parallelism.BlockRunner;

public final class Executor implements Runnable {
    private final BlockRunner runner;
    private final TraceInput input;

    public Executor(BlockRunner runner, TraceInput input) {
        this.runner = Objects.requireNonNull(runner, "runner");
        this.input = Objects.requireNonNull(input, "input");
    }

    @Override
    public void run() {
        var variables = new ConcurrentHashMap<String, BigInteger>();
        var operations = input.word().stream()
                .map(occurrence -> new TransactionOperation(occurrence, variables))
                .toList();
        runClasses(foataClasses(operations), runner);
    }

    private List<List<TransactionOperation>> foataClasses(List<TransactionOperation> operations) {
        // TODO: Compute the Foata classes from the operations in input word order.
        System.out.println("TODO: Compute Foata classes");
        return List.of();
    }

    private void runClasses(List<List<TransactionOperation>> classes, BlockRunner runner) {
        // TODO: Pass the classes to runner.runBlock in order.
        System.out.println("TODO: Run the classes in order");
    }
}
