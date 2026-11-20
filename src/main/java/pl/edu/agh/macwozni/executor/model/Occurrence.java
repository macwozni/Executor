package pl.edu.agh.macwozni.executor.model;

/** An occurrence of a transaction at a one-based position in the input word. */
public record Occurrence(int position, Transaction transaction) {
    public String label() {
        return transaction.symbol() + "[" + position + "]";
    }
}
