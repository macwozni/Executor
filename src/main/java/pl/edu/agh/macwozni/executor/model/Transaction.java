package pl.edu.agh.macwozni.executor.model;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** An assignment from CW5. Every term is an integer or a scaled variable. */
public record Transaction(String symbol, String target, List<Term> terms) {
    public Transaction {
        terms = List.copyOf(terms);
    }

    public Set<String> reads() {
        return terms.stream()
                .flatMap(term -> term.variable().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> writes() {
        return Set.of(target);
    }

    public record Term(BigInteger coefficient, Optional<String> variable) {}
}
