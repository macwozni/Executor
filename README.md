# Executor

`Executor` is a small Java teaching project for trace theory and concurrent
execution of transactions. It provides a starter for grouping occurrences
into Foata classes and running each class as a block.

## Requirements

- JDK 26, for example OpenJDK 26
- Maven 3.9 or newer

The block runner uses virtual threads and `StructuredTaskScope`, a preview
API in JDK 26. Maven enables preview features for compilation and tests.

Check the installed Java and Maven versions.

```bash
java -version
javac -version
mvn -version
```

## Quick Start With Maven

Build the project and run its tests.

```bash
mvn clean verify
```

Run the generated JAR with the supplied sample input.

```bash
java --enable-preview -jar target/executor-1.0-SNAPSHOT.jar < src/test/resources/dane1.txt
```

## What The Program Does

The starter reads an alphabet, a set of transactions, and a word from standard
input. It prints the alphabet and labels each occurrence with its position in
the word, starting at 1. The two `TODO` messages mark the methods to complete.

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

## Example Output

Running the starter with the sample input produces this output.

```text
Alphabet: a b c d
Word: b[1] a[2] a[3] d[4] c[5] b[6]
TODO: Compute Foata classes
TODO: Run the classes in order
```

## Project Structure

```text
.
|-- pom.xml
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
`ConcurrentBlockRunner` starts one virtual thread per operation through
`StructuredTaskScope`. A `CountDownLatch` holds the tasks until the entire
block has been submitted. The runner waits for the block to finish and
propagates failures or interruptions to the caller after the scope closes.

Transactions use shared integer variables with an initial value of `0`.
`AbstractOperation` adds a delay of 10–19 ms and prints a label such as `a[2]`
after the operation succeeds.

## Build Artifacts

Maven creates the runnable JAR at `target/executor-1.0-SNAPSHOT.jar`.

## Cleaning

Remove Maven build outputs with `mvn clean`.

## Troubleshooting

- If Maven reports a Java version error, set `JAVA_HOME` to the JDK 26
  installation and check `mvn -version`.
- If the JVM reports a preview feature error, pass `--enable-preview` before
  `-jar` as shown in the quick start.

## Usage

Complete the two methods in `Executor` during the 90-minute lab.

1. `foataClasses` computes the Foata classes from the declared alphabet and
   the occurrences in the input word. Use `operation.occurrence().transaction()`
   to access the transaction. Its `reads()` and `writes()` methods return the
   sets of variables it reads and writes.
2. `runClasses` passes each class to `BlockRunner.runBlock` in order. Execute
   each occurrence exactly once. Start the next class after all operations in
   the previous class have finished.

The project layout and block runner are based on [1DMeshParallel](https://github.com/macwozni/1DMeshParallel/tree/359d963a935e97240d1454c30870dd8c47fffaaf).
