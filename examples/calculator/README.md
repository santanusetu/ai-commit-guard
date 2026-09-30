# Test Java Committer Project

A simple calculator application designed for testing the AI Git Assist JAR tool. This project provides a clean, structured codebase with multiple files that can be modified to test various commit scenarios (features, fixes, docs, refactoring, tests).

## Project Structure

```
test-java-commiter-proj/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── example/
│   │               ├── Calculator.java          # Main application entry point
│   │               ├── service/
│   │               │   └── CalculatorService.java  # Business logic for calculations
│   │               └── util/
│   │                   └── InputValidator.java     # Input validation utilities
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   ├── service/
│                   │   └── CalculatorServiceTest.java
│                   └── util/
│                       └── InputValidatorTest.java
├── pom.xml
└── README.md
```

## Features

- **Basic Calculator Operations**: Add, subtract, multiply, divide, and power
- **Input Validation**: Validates user input for numbers and operations
- **Error Handling**: Handles division by zero, invalid operations, and unsupported operations
- **Unit Tests**: Comprehensive test coverage for services and utilities
- **Maven Build**: Standard Maven project structure

## Building and Running

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Build the Project

```bash
mvn clean compile
```

### Run the Application

```bash
mvn exec:java -Dexec.mainClass="com.example.Calculator"
```

Or compile and run manually:

```bash
mvn clean compile
java -cp target/classes com.example.Calculator
```

### Run Tests

```bash
mvn test
```

## Usage

The calculator is an interactive command-line application:

```
=== Simple Calculator ===
Enter 'quit' to exit

Enter operation (add, subtract, multiply, divide, power): add
Enter first number: 5
Enter second number: 3
Result: 8.0

Enter operation (add, subtract, multiply, divide, power): power
Enter first number: 2
Enter second number: 3
Result: 8.0

Enter operation (add, subtract, multiply, divide, power): quit
Goodbye!
```

## Testing AI Git Assist

This project is designed to test various commit scenarios:

### 1. Feature Commits
- Add new operations (power, square root, etc.)
- Add new utility classes
- Add configuration support

### 2. Fix Commits
- Fix bugs in calculation logic
- Fix input validation issues
- Fix error handling

### 3. Documentation Commits
- Update README
- Add JavaDoc comments
- Add usage examples

### 4. Refactoring Commits
- Improve code structure
- Extract methods
- Rename variables/classes

### 5. Test Commits
- Add new test cases
- Improve test coverage
- Add integration tests

### Example Testing Workflow

1. **Make a change** (e.g., add a new feature):
   ```bash
   # Edit a file or add a new file
   ```

2. **Stage changes**:
   ```bash
   git add .
   ```

3. **Run AI Git Assist**:
   ```bash
   java -jar /path/to/ai-git-assist.jar
   ```

4. **Review and commit** following the AI Git Assist prompts

## Project Status

This is a test project for validating AI Git Assist functionality. The codebase is intentionally simple to make it easy to understand and modify for testing purposes.


## Features / Changelog
- **2026-01-04 17:13**: feat: add power operation to calculator functionality
