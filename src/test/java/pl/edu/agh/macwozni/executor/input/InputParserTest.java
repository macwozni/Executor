package pl.edu.agh.macwozni.executor.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.edu.agh.macwozni.executor.model.Occurrence;
import pl.edu.agh.macwozni.executor.model.TraceInput;
import pl.edu.agh.macwozni.executor.model.Transaction;
import pl.edu.agh.macwozni.executor.model.Transaction.Term;

class InputParserTest {
    private final InputParser parser = new InputParser();

    @Test
    void parsesCw5TaskOne() throws Exception {
        try (var reader = new InputStreamReader(getClass().getResourceAsStream("/cw5-zadanie1.txt"),
                StandardCharsets.UTF_8)) {
            TraceInput input = parser.parse(reader);
            assertEquals(List.of("a", "b", "c", "d"), symbols(input));
            assertEquals(List.of("b[1]", "a[2]", "a[3]", "d[4]", "c[5]", "b[6]"), labels(input));
            assertEquals(Set.of("x", "y"), input.alphabet().getFirst().reads());
            assertEquals(Set.of("x"), input.alphabet().getFirst().writes());
            assertEquals(new Term(BigInteger.TWO, Optional.of("z")), input.alphabet().get(1).terms().get(1));
            assertEquals(new Term(BigInteger.valueOf(-1), Optional.of("z")),
                    input.alphabet().getLast().terms().get(1));
        }
    }

    @Test
    void parsesCw5TaskTwo() throws Exception {
        TraceInput input = parse("""
                A = {a, b, c, d, e, f}
                a: x ← y + z
                b: y ← x + w + y
                c: x ← x + y + v
                d: w ← v + z
                e: v ← x + v + w
                f: z ← y + z + v
                u = acdcfbbe
                """);
        assertEquals(List.of("a", "b", "c", "d", "e", "f"), symbols(input));
        assertEquals(List.of("a[1]", "c[2]", "d[3]", "c[4]", "f[5]", "b[6]", "b[7]", "e[8]"), labels(input));
        assertEquals(Set.of("y", "z"), input.alphabet().getFirst().reads());
        assertEquals(Set.of("x", "v", "w"), input.alphabet().get(4).reads());
    }

    @Test
    void parsesCw5HomeworkSecondExample() throws Exception {
        TraceInput input = parse("""
                A = {a, b, c, d, e, f}
                a: x := x + 1
                b: y := y + 2z
                c: x := 3x + z
                d: w := w + v
                e: z := y – z
                f: v = x + v
                w = acdcfbbe
                """);
        assertEquals(List.of("a[1]", "c[2]", "d[3]", "c[4]", "f[5]", "b[6]", "b[7]", "e[8]"), labels(input));
        assertEquals(new Term(BigInteger.ONE, Optional.empty()), input.alphabet().getFirst().terms().get(1));
        assertEquals(Set.of("y", "z"), input.alphabet().get(4).reads());
        assertEquals(Set.of("v"), input.alphabet().getLast().writes());
    }

    @Test
    void keepsAlphabetOrderAndAcceptsEmptyWords() throws Exception {
        TraceInput input = parse("""

                A = {b, a}
                a: x <- y
                b: y := 1

                w =
                """);
        assertEquals(List.of("b", "a"), symbols(input));
        assertTrue(input.word().isEmpty());
        assertEquals(new TraceInput(List.of(), List.of()), parse("A = {}\nw =\n"));
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void reportsInvalidInput(String source, String message) {
        var error = assertThrows(InputFormatException.class, () -> parse(source));
        assertTrue(error.getMessage().contains(message), error.getMessage());
    }

    private static List<Arguments> invalidInputs() {
        return List.of(
                Arguments.of("", "Brak deklaracji alfabetu"),
                Arguments.of("A = {a}", "Brak ostatniego wiersza"),
                Arguments.of("A = {a, a}\nw = a", "Powtórzony symbol alfabetu"),
                Arguments.of("A = {a,}\nw = a", "pojedynczą literą"),
                Arguments.of("A = {a}\na: x := 1\na: x := 2\nw = a", "Powtórzona definicja"),
                Arguments.of("A = {a}\nb: x := 1\nw = a", "Symbol spoza alfabetu: b"),
                Arguments.of("A = {a, b}\na: x := 1\nw = a", "Brak definicji transakcji: b"),
                Arguments.of("A = {a}\na: x := 1\nw = ab", "na pozycji 2: b"),
                Arguments.of("A = {a}\na: x := x y\nw = a", "prawa strona przypisania"),
                Arguments.of("A = {a}\na: x := x +\nw = a", "prawa strona przypisania"),
                Arguments.of("A = {a}\na: x :=\nw = a", "prawa strona przypisania"),
                Arguments.of("A = {a}\na: x := 1\nw = a a", "bez odstępów"));
    }

    private TraceInput parse(String source) throws Exception {
        return parser.parse(new StringReader(source));
    }

    private static List<String> symbols(TraceInput input) {
        return input.alphabet().stream().map(Transaction::symbol).toList();
    }

    private static List<String> labels(TraceInput input) {
        return input.word().stream().map(Occurrence::label).toList();
    }
}
