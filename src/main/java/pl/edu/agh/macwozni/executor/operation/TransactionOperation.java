package pl.edu.agh.macwozni.executor.operation;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentMap;
import pl.edu.agh.macwozni.executor.model.Occurrence;

public final class TransactionOperation extends AbstractOperation {
    private final Occurrence occurrence;
    private final ConcurrentMap<String, BigInteger> variables;

    public TransactionOperation(Occurrence occurrence, ConcurrentMap<String, BigInteger> variables) {
        super(occurrence.label());
        this.occurrence = occurrence;
        this.variables = variables;
    }

    public Occurrence occurrence() {
        return occurrence;
    }

    @Override
    public void execute() {
        var transaction = occurrence.transaction();
        var result = BigInteger.ZERO;
        for (var term : transaction.terms()) {
            BigInteger factor = term.variable()
                    .map(variable -> variables.getOrDefault(variable, BigInteger.ZERO))
                    .orElse(BigInteger.ONE);
            result = result.add(term.coefficient().multiply(factor));
        }
        variables.put(transaction.target(), result);
    }
}
