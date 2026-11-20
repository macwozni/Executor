package pl.edu.agh.macwozni.executor.operation;

import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractOperation implements Operation {
    private final String label;

    protected AbstractOperation(String label) {
        this.label = label;
    }

    @Override
    public final void run() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 20));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Przerwano operację " + label, exception);
        }

        execute();
        System.out.println("Wykonano " + label);
    }
}
