package pl.edu.agh.macwozni.executor.model;

import java.util.List;

public record TraceInput(List<Transaction> alphabet, List<Occurrence> word) {
    public TraceInput {
        alphabet = List.copyOf(alphabet);
        word = List.copyOf(word);
    }
}
