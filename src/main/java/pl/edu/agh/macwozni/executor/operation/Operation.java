package pl.edu.agh.macwozni.executor.operation;

public interface Operation extends Runnable {
    /** Performs the operation itself. The runner calls {@link #run()}. */
    void execute();
}
