package pl.edu.agh.macwozni.executor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import pl.edu.agh.macwozni.executor.input.InputFormatException;
import pl.edu.agh.macwozni.executor.input.InputParser;
import pl.edu.agh.macwozni.executor.model.Occurrence;
import pl.edu.agh.macwozni.executor.model.TraceInput;
import pl.edu.agh.macwozni.executor.model.Transaction;
import pl.edu.agh.macwozni.executor.parallelism.ConcurrentBlockRunner;

public final class Application {
    private Application() {}

    public static void main(String[] args) {
        TraceInput input;
        try {
            input = new InputParser().parse(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        } catch (InputFormatException exception) {
            System.err.println("Input error. " + exception.getMessage());
            System.exit(1);
            return;
        } catch (IOException exception) {
            System.err.println("Could not read input. " + exception.getMessage());
            System.exit(1);
            return;
        }

        String alphabet = input.alphabet().stream().map(Transaction::symbol).collect(Collectors.joining(" "));
        String word = input.word().stream().map(Occurrence::label).collect(Collectors.joining(" "));
        System.out.println("Alphabet: " + alphabet);
        System.out.println("Word: " + word);
        new Executor(new ConcurrentBlockRunner(), input).run();
    }
}
