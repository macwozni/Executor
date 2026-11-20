# Executor

`Executor` is a small Java teaching project for trace theory and concurrent
execution of transactions. It provides a starter for grouping occurrences
into Foata classes and running each class as a block.

## Requirements

- JDK 17, for example OpenJDK 17
- Maven 3.9 or newer for the Maven build

The repository includes the Gradle Wrapper. Check the installed Java version.

```bash
java -version
javac -version
```

## Quick Start With Gradle

Build the project and run its tests with the wrapper.

```bash
./gradlew clean build
```

Run the application with the sample input.

```bash
./gradlew --quiet run < src/test/resources/dane1.txt
```

Run the generated JAR directly with `java`.

```bash
java -jar build/libs/executor-1.0-SNAPSHOT.jar < src/test/resources/dane1.txt
```

On Windows, run the examples from Command Prompt and use `gradlew.bat` for
the wrapper commands.

## Quick Start With Maven

Build the project and run its tests.

```bash
mvn clean verify
```

Run the generated JAR with the supplied sample input.

```bash
java -jar target/executor-1.0-SNAPSHOT.jar < src/test/resources/dane1.txt
```

## What The Program Does

The starter reads an alphabet, a set of transactions, and a word from standard
input. It prints the alphabet and labels each occurrence with its position in
the word, starting at 1. It also prints the classes returned by `foataClasses`,
which starts out returning an empty list. The two `TODO` messages mark class
computation and the call that executes the computed classes.

## Example Input

Use UTF-8 text. The sample file contains the following data.

```text
A = {a, b, c, d}
a: x := x + y
b: y := y + 2z
c: x := 3x + z
d: z := y - z
w = baadcb
```

- The first nonblank line declares the alphabet. Each symbol is a single
  ASCII letter and appears once in the declaration. Use `A = {}` for an empty
  alphabet.
- The following lines define one transaction per symbol, in any order. Each
  transaction assigns a value to one variable. Variable names start with an
  ASCII letter and can contain only ASCII letters, digits, and underscores.
  The right-hand side can add or subtract integers and variables with integer
  coefficients such as `2z`. The assignment operator can be `:=`, `←`, `<-`, or `=`.
- The last nonblank line starts with `w =` or `u =` and contains the word.
  Use symbols from the alphabet with no spaces between them. A bare `w =` or
  `u =` represents the empty word.

### Parsed Input

`InputParser.parse(...)` returns a `TraceInput` record.

- `alphabet()` returns a `List<Transaction>` in declaration order, including
  transactions whose symbols do not appear in the word.
- `word()` returns a `List<Occurrence>` in word order. Each occurrence has a
  one-based `position()` and a `transaction()`. In the sample, `a[2]` and `a[3]`
  refer to separate occurrences of the same transaction.

In `Executor.foataClasses`, use `input.alphabet()` for the declared alphabet.
The `operations` argument contains one `TransactionOperation` per occurrence
in word order.
Use `operation.occurrence().transaction()` to get its `Transaction`. The
methods `symbol()`, `reads()`, and `writes()` give its alphabet symbol and the
sets of variables it reads and writes.

## Example Output

Running the starter with the sample input produces this output.

```text
Alphabet: a b c d
Word: b[1] a[2] a[3] d[4] c[5] b[6]
TODO: Compute Foata classes
FNF: []
TODO: Pass the computed classes to runClasses
```

## Project Structure

```text
.
|-- build.gradle
|-- gradle/wrapper
|-- gradlew
|-- gradlew.bat
|-- pom.xml
|-- settings.gradle
`-- src
    |-- main/java/pl/edu/agh/macwozni/executor
    |   |-- Application.java
    |   |-- Executor.java
    |   |-- input
    |   |-- model
    |   |-- operation
    |   `-- parallelism
    `-- test/resources/dane1.txt
```

- `pl.edu.agh.macwozni.executor` contains the application entry point and the
  code that coordinates execution.
- `input` parses and validates the input.
- `model` represents transactions, the declared alphabet, and the occurrences
  in the word.
- `operation` implements runnable operations and variable assignments.
- `parallelism` contains the block runner.

## Execution Model

`BlockRunner.runBlock` receives a block of independent operations.
`ConcurrentBlockRunner` creates an `ExecutorService` with up to eight platform
threads per block. Additional operations wait in the queue. It collects
completed tasks through `ExecutorCompletionService` and waits for the whole
block to finish. If an operation fails or the caller is interrupted, the runner
cancels outstanding work and waits for the pool to terminate before reporting
the failure or interruption.
The supplied `Executor.runClasses` calls the runner once per class and waits
for each call to return before starting the next class.

Transactions use shared integer variables with an initial value of `0`.
`AbstractOperation` adds a delay of 10–19 ms and prints a label such as `a[2]`
after the operation succeeds.

## Build Artifacts

- Gradle creates `build/libs/executor-1.0-SNAPSHOT.jar`.
- Maven creates `target/executor-1.0-SNAPSHOT.jar`.

## Cleaning

Remove build outputs with `./gradlew clean` or `mvn clean`.

## Troubleshooting

If a build reports a Java version error, set `JAVA_HOME` to the JDK 17
installation and check `./gradlew --version` or `mvn -version`.

## Usage

Complete the two `TODO`s in `Executor` during the 90-minute lab.

1. Implement `foataClasses` to compute the Foata classes from the declared
   alphabet and the occurrences in the input word.
2. In `run()`, pass the computed classes to the supplied `runClasses` method.
   Each occurrence must execute exactly once.

The project layout and block runner are based on [1DMeshParallel](https://github.com/macwozni/1DMeshParallel/tree/359d963a935e97240d1454c30870dd8c47fffaaf).
