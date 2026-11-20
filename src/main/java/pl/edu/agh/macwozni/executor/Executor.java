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
        // TODO: Wyznacz klasy Foaty dla operacji w kolejności słowa wejściowego.
        System.out.println("TODO: wyznaczenie klas Foaty");
        return List.of();
    }

    private void runClasses(List<List<TransactionOperation>> classes, BlockRunner runner) {
        // TODO: Przekaż kolejne klasy do runner.runBlock, zachowując ich kolejność.
        System.out.println("TODO: wykonanie kolejnych klas");
    }
}
