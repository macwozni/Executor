package pl.edu.agh.macwozni.executor.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import pl.edu.agh.macwozni.executor.model.Occurrence;
import pl.edu.agh.macwozni.executor.model.TraceInput;
import pl.edu.agh.macwozni.executor.model.Transaction;
import pl.edu.agh.macwozni.executor.model.Transaction.Term;

/** Parses an alphabet, assignments, and a word from text input. */
public final class InputParser {
    private static final String IDENTIFIER = "[A-Za-z][A-Za-z0-9_]*";
    private static final Pattern SYMBOL = Pattern.compile("[A-Za-z]");
    private static final Pattern ALPHABET = Pattern.compile("A\\s*=\\s*\\{(.*)}");
    private static final Pattern ASSIGNMENT = Pattern.compile(
            "([A-Za-z])\\s*:\\s*(" + IDENTIFIER + ")\\s*(?::=|←|<-|=)\\s*(.*)");
    private static final Pattern WORD = Pattern.compile("[wu]\\s*=\\s*([A-Za-z]*)");
    private static final Pattern TERM = Pattern.compile(
            "(?<sign>[+-]?)\\s*(?<coefficient>[0-9]*)\\s*(?<variable>" + IDENTIFIER + ")?");

    public TraceInput parse(Reader reader) throws IOException, InputFormatException {
        List<Line> lines = readNonBlankLines(reader);
        if (lines.isEmpty()) {
            throw new InputFormatException("Missing alphabet declaration A.");
        }
        Set<String> symbols = parseAlphabet(lines.get(0));
        if (lines.size() == 1) {
            throw new InputFormatException("Missing final line declaring the word w or u.");
        }

        var transactions = new HashMap<String, Transaction>();
        for (Line line : lines.subList(1, lines.size() - 1)) {
            Transaction transaction = parseAssignment(line);
            String symbol = transaction.symbol();
            if (!symbols.contains(symbol)) {
                throw line.error("Symbol outside the alphabet: " + symbol);
            }
            if (transactions.putIfAbsent(symbol, transaction) != null) {
                throw line.error("Duplicate transaction definition: " + symbol);
            }
        }

        Line wordLine = lines.get(lines.size() - 1);
        var word = WORD.matcher(wordLine.text());
        if (!word.matches()) {
            throw wordLine.error("Expected w = ... or u = ... with no spaces between letters.");
        }
        List<String> missing = symbols.stream().filter(symbol -> !transactions.containsKey(symbol)).toList();
        if (!missing.isEmpty()) {
            throw wordLine.error("Missing transaction definitions: " + String.join(", ", missing));
        }

        String letters = word.group(1);
        var occurrences = new ArrayList<Occurrence>();
        for (int index = 0; index < letters.length(); index++) {
            String symbol = String.valueOf(letters.charAt(index));
            Transaction transaction = transactions.get(symbol);
            if (transaction == null) {
                throw wordLine.error("Symbol outside the alphabet at position " + (index + 1) + ": " + symbol);
            }
            occurrences.add(new Occurrence(index + 1, transaction));
        }
        return new TraceInput(symbols.stream().map(transactions::get).toList(), occurrences);
    }

    private static List<Line> readNonBlankLines(Reader reader) throws IOException {
        var buffered = new BufferedReader(reader);
        var lines = new ArrayList<Line>();
        int number = 0;
        String text;
        while ((text = buffered.readLine()) != null) {
            number++;
            if (!text.isBlank()) {
                lines.add(new Line(number, text.strip()));
            }
        }
        return lines;
    }

    private static Set<String> parseAlphabet(Line line) throws InputFormatException {
        var alphabet = ALPHABET.matcher(line.text());
        if (!alphabet.matches()) {
            throw line.error("Expected an alphabet declaration in the form A = {a, b, ...}.");
        }
        var symbols = new LinkedHashSet<String>();
        if (!alphabet.group(1).isBlank()) {
            for (String entry : alphabet.group(1).split(",", -1)) {
                String symbol = entry.strip();
                if (!SYMBOL.matcher(symbol).matches()) {
                    throw line.error("An alphabet symbol must be a single letter from A to Z or a to z.");
                }
                if (!symbols.add(symbol)) {
                    throw line.error("Duplicate alphabet symbol: " + symbol);
                }
            }
        }
        return symbols;
    }

    private static Transaction parseAssignment(Line line) throws InputFormatException {
        var assignment = ASSIGNMENT.matcher(line.text());
        if (!assignment.matches()) {
            throw line.error("Expected a transaction definition such as a: x := x + y.");
        }

        String expression = assignment.group(3).replace('−', '-').replace('–', '-');
        var terms = new ArrayList<Term>();
        for (String part : expression.split("(?=[+-])")) {
            var term = TERM.matcher(part.strip());
            if (!term.matches() || (term.group("coefficient").isEmpty() && term.group("variable") == null)) {
                throw line.error("Invalid right-hand side of assignment: \"" + assignment.group(3) + "\".");
            }
            String digits = term.group("coefficient");
            BigInteger coefficient = digits.isEmpty() ? BigInteger.ONE : new BigInteger(digits);
            if (term.group("sign").equals("-")) {
                coefficient = coefficient.negate();
            }
            terms.add(new Term(coefficient, Optional.ofNullable(term.group("variable"))));
        }
        return new Transaction(assignment.group(1), assignment.group(2), terms);
    }

    private record Line(int number, String text) {
        private InputFormatException error(String message) {
            return new InputFormatException("Line " + number + ". " + message);
        }
    }
}
