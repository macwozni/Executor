package pl.edu.agh.macwozni.executor.parallelism;

import java.util.List;
import java.util.Objects;
import pl.edu.agh.macwozni.executor.operation.Operation;

public abstract class AbstractBlockRunner implements BlockRunner {
    @Override
    public final void runBlock(List<? extends Operation> operations) {
        Objects.requireNonNull(operations, "operations");

        if (operations.isEmpty()) {
            return;
        }

        runNonEmptyBlock(List.copyOf(operations));
    }

    protected abstract void runNonEmptyBlock(List<Operation> operations);
}
