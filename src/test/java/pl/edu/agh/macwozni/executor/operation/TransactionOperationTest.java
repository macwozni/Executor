package pl.edu.agh.macwozni.executor.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import pl.edu.agh.macwozni.executor.input.InputParser;

class TransactionOperationTest {
    @Test
    void executesAnAssignmentAndPrintsItsOccurrence() throws Exception {
        var variables = new ConcurrentHashMap<String, BigInteger>();
        variables.put("x", BigInteger.valueOf(2));
        variables.put("y", BigInteger.valueOf(3));
        var operation = operation(variables);
        var output = new ByteArrayOutputStream();
        var originalOut = System.out;

        try (var capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            operation.run();
        } finally {
            System.setOut(originalOut);
        }

        assertEquals(BigInteger.valueOf(5), variables.get("x"));
        assertEquals(List.of("Executed a[2]"), output.toString(StandardCharsets.UTF_8).lines().toList());
    }

    @Test
    void restoresInterruptAndSkipsAssignmentWhenSleepIsInterrupted() throws Exception {
        var variables = new ConcurrentHashMap<String, BigInteger>();
        var operation = operation(variables);

        Thread.currentThread().interrupt();
        try {
            var error = assertThrows(IllegalStateException.class, operation::run);
            assertInstanceOf(InterruptedException.class, error.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        assertTrue(variables.isEmpty());
    }

    private static TransactionOperation operation(ConcurrentHashMap<String, BigInteger> variables) throws Exception {
        var input = new InputParser().parse(new StringReader("""
                A = {a}
                a: x := 3x - y + 2
                w = aa
                """));
        return new TransactionOperation(input.word().getLast(), variables);
    }
}
