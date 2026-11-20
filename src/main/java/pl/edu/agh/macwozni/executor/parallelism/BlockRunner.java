package pl.edu.agh.macwozni.executor.parallelism;

import java.util.List;
import pl.edu.agh.macwozni.executor.operation.Operation;

/** Runs independent operations from one block and waits for the whole block. */
public interface BlockRunner {
    void runBlock(List<? extends Operation> operations);
}
