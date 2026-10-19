# ECM2414 Software Development - Card Game Simulation

This repository contains a multithreaded card game simulation and its associated test suite, developed for the Continuous Assessment (CA) of the ECM2414 module.

## 1. System Requirements
* **Java Version:** OpenJDK 17 (or compatible Java SE 17+ environment).
* **Testing Framework:** JUnit 5.
* **Dependencies:** The JUnit 5 Platform Console Standalone JAR is required to run the test suite (expected to be located in the `lib/` directory).
* **Environment:** Tested on a standard POSIX Bash terminal (Ubuntu/Linux).

## 2. Directory Structure
* `src/cardgame/` - Contains the production source code (`.java`).
* `src/test/` - Contains the JUnit test suite (`.java`).
* `bin/` - Contains the compiled bytecode (`.class`).
* `lib/` - Contains the external dependencies (JUnit standalone JAR).
* `cards.jar` - The executable Java Archive containing both bytecode and source files.

## 3. Compilation Instructions

From the root directory of the project, execute the following commands to compile both the production code and the test suite into the `bin/` directory:

```bash
# Clean existing binaries (optional)
rm -rf bin/*
mkdir -p bin

# Compile production code and tests
javac -cp "lib/*:bin" -d bin src/cardgame/*.java src/test/*.java
```

## 4. Execution Instructions

### Running the Game (Executable JAR)
To run the main simulation directly using the compiled JAR file:
```bash
java -jar cards.jar
```
*Note: The program will interactively prompt for the number of players ($n$) and the file path to a valid deck/pack. The pack must contain exactly $8n$ non-negative integers.*

### Running the Game (From Binaries)
Alternatively, you can run the game using the compiled `.class` files:
```bash
java -cp bin cardgame.CardGame
```

## 5. Running the Test Suite

The test suite validates thread-safety, OO invariants, and concurrent I/O operations. To execute all tests using the JUnit console launcher, run:

```bash
java -jar lib/junit-platform-console-standalone.jar --class-path bin --scan-class-path
```

## 6. System Outputs
Upon successful execution, the simulation generates output files (`player<ID>_output.txt` and `deck<ID>_output.txt`) in the root directory. 
*Note: The test suite strictly adheres to test isolation principles; all mock files generated during testing are unconditionally cleaned up via `@AfterEach` teardown methods.*