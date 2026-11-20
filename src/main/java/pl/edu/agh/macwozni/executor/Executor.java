package pl.edu.agh.macwozni.executor;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
        var classes = foataClasses(operations);
        String foata = classes.stream()
                .map(block -> block.stream()
                        .map(operation -> operation.occurrence().label())
                        .collect(Collectors.joining(" ", "(", ")")))
                .collect(Collectors.joining(" ", "[", "]"));
        System.out.println("FNF: " + foata);

        // TODO: Pass the computed classes to runClasses.
        System.out.println("TODO: Pass the computed classes to runClasses");
    }

    private List<List<TransactionOperation>> foataClasses(List<TransactionOperation> operations) {
        // TODO: Compute the Foata classes from the operations in input word order.
        System.out.println("TODO: Compute Foata classes");
        return List.of();
    }

    private void runClasses(List<List<TransactionOperation>> classes, BlockRunner runner) {
        for (var block : classes) {
            runner.runBlock(block);
        }
    }
}
