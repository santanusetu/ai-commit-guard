# Testing Guide for AI Git Assist

This guide helps you test your AI Git Assist JAR with various commit scenarios.

## Quick Start

1. **Set up environment variables** (if not already set):
   ```bash
   export OPENAI_API_KEY=your-openai-api-key
   export SLACK_WEBHOOK_URL=https://hooks.slack.com/services/xxx/yyy/zzz  # Optional
   export OPENAI_MODEL=gpt-4o-mini  # Optional
   ```

2. **Make some changes** to the codebase (see scenarios below)

3. **Stage your changes**:
   ```bash
   git add .
   ```

4. **Run AI Git Assist**:
   ```bash
   java -jar /path/to/ai-git-assist.jar
   ```

## Test Scenarios

### Scenario 1: Feature Addition (feat:)

**Goal**: Test feature commit message generation

**Steps**:
1. Add a new operation to `CalculatorService.java` (e.g., `power` or `sqrt`)
2. Update `InputValidator.java` to support the new operation
3. Update `Calculator.java` to handle the new operation
4. Stage changes and run AI Git Assist

**Expected**: Commit message should start with `feat:`

### Scenario 2: Bug Fix (fix:)

**Goal**: Test bug fix commit message generation

**Steps**:
1. Introduce a bug (e.g., remove division by zero check)
2. Fix the bug
3. Stage changes and run AI Git Assist

**Expected**: Commit message should start with `fix:`

### Scenario 3: Documentation Update (docs:)

**Goal**: Test documentation commit message generation

**Steps**:
1. Update README.md with new information
2. Add JavaDoc comments to classes
3. Stage changes and run AI Git Assist

**Expected**: Commit message should start with `docs:`

### Scenario 4: Refactoring (refactor:)

**Goal**: Test refactoring commit message generation

**Steps**:
1. Extract methods from `CalculatorService`
2. Rename variables for clarity
3. Reorganize code structure
4. Stage changes and run AI Git Assist

**Expected**: Commit message should start with `refactor:`

### Scenario 5: Test Addition (test:)

**Goal**: Test test commit message generation

**Steps**:
1. Add new test cases to existing test files
2. Create new test files
3. Stage changes and run AI Git Assist

**Expected**: Commit message should start with `test:`

### Scenario 6: Security Validation

**Goal**: Test security validation feature

**Steps**:
1. Temporarily add a file with sensitive data (e.g., `config.properties` with `api_key=AKIA1234567890ABCDEF`)
2. Stage changes and run AI Git Assist

**Expected**: Security warning should be displayed, commit should be blocked

### Scenario 7: README Auto-Update

**Goal**: Test README auto-update feature

**Steps**:
1. Add a new feature to the code
2. Stage changes and run AI Git Assist
3. When prompted, choose "y" to update README

**Expected**: README should be automatically updated with new features

### Scenario 8: Multiple File Changes

**Goal**: Test commit message generation for complex changes

**Steps**:
1. Modify multiple files simultaneously:
   - Add feature to `CalculatorService.java`
   - Update `InputValidator.java`
   - Add tests
   - Update README
2. Stage all changes and run AI Git Assist

**Expected**: Commit message should summarize all changes appropriately

## Sample Test Changes

### Quick Feature Addition

Add this method to `CalculatorService.java`:

```java
public double power(double base, double exponent) {
    return Math.pow(base, exponent);
}
```

Update `calculate` method to handle "power" operation.

### Quick Bug Fix

Remove the division by zero check temporarily, then add it back:

```java
// Remove this check temporarily
if (b == 0) {
    throw new ArithmeticException("Division by zero is not allowed");
}
```

Then add it back to test the fix commit.

### Quick Documentation

Add JavaDoc to a method or update README with usage examples.

## Tips

- **Start fresh**: Reset to a clean state between tests if needed:
  ```bash
  git reset --hard HEAD
  ```

- **View diffs**: Before running AI Git Assist, check what will be committed:
  ```bash
  git diff --cached
  ```

- **Test interactively**: Use the edit feature in AI Git Assist to see how it handles message modifications

- **Test edge cases**: Try committing empty changes, single-line changes, large changes, etc.

## Troubleshooting

- **No staged changes**: Make sure to run `git add .` before running AI Git Assist
- **Git not initialized**: Run `git init` if you get repository errors
- **API key issues**: Verify `OPENAI_API_KEY` is set correctly
- **Push failures**: If push fails, the commit still succeeds - you can push manually later

