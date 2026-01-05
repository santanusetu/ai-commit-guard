# AI Git Assist

AI Git Assist is an intelligent command-line Java application that automates your entire Git commit workflow using OpenAI. It analyzes staged changes and provides AI-powered commit messages, automatic test generation, security validation, and README maintenance—all in one streamlined process.

## Features

- **AI-Powered Commit Messages**: Generates conventional commit messages (feat:, fix:, docs:, etc.) following industry best practices
- **AI-Powered Test Generation**: Automatically generates test cases for functionality changes, supporting Java, Python, JavaScript/TypeScript, and more
- **Security Validation**: Scans staged changes for sensitive information (API keys, passwords, tokens, secrets) and warns before committing
- **README Auto-Update**: Intelligently generates or updates README.md files based on project changes
- **Slack Integration**: Optional webhook notifications for commit tracking


## Installation & Setup

1. **Ensure Java 11+ is installed**

2. **Set environment variables:**
```bash
export OPENAI_API_KEY=your-openai-api-key
export SLACK_WEBHOOK_URL=https://hooks.slack.com/services/xxx/yyy/zzz  # Optional
export OPENAI_MODEL=gpt-4o-mini  # Optional, defaults to gpt-4o-mini
```

3. **Build the project (if running from source):**
```bash
mvn clean install
```

## How to Run

**Using pre-built JAR:**
```bash
java -jar ai-git-assist.jar
```

**Build from source:**
```bash
mvn clean install
java -jar target/ai-git-assist.jar
```

**Run in a different repository:**
```bash
java -jar ai-git-assist.jar /path/to/repository
```

## How to Use

1. **Stage your changes:**
```bash
git add .
```

2. **Run the application:**
```bash
java -jar ai-git-assist.jar
```

3. **Follow the interactive prompts** to review commit message, edit if needed, and confirm actions.

### Workflow Diagram

```
Start
  │
  ▼
Validate Repository & Staged Changes
  │
  ▼
Security Validation ──► ⚠️ Warning (optional) ──► Continue/Cancel
  │
  ▼
Generate Test Cases? (y/n) ──► [Optional: Generate & Save Tests]
  │
  ▼
Generate Commit Message (AI) ──► Edit Message? (y/n) ──► Commit with Message? (y/n)
                                        │                        │
                                        ▼                        ▼
                                  [Optional: Edit]          [Cancel if No]
  │
  ▼
Update README? (y/n) ──► [Optional: Update]
  │
  ▼
Commit Changes ──► Push to Remote ──► ⚠️ Warning if fails
  │
  ▼
Send Slack Notification (if configured)
  │
  ▼
✅ Success
```

### Example Workflow

```bash
$ git add .
$ java -jar ai-git-assist.jar

Validating changes for sensitive information...
✅ Security validation passed.

Generate test cases for functionality changes? (y/n): y

Generating test cases for functionality changes...

═══════════════════════════════════════════════════════════
                    GENERATED TEST CASES
═══════════════════════════════════════════════════════════

[Generated test code]

═══════════════════════════════════════════════════════════

Save test file? (y/n): y
✅ Test saved: src/test/java/com/example/UserServiceTest.java

Generating commit message...
✅ Done.

═══════════════════════════════════════════════════════════
                    COMMIT MESSAGE
═══════════════════════════════════════════════════════════

feat: implement user authentication service

- Add UserService with login and registration
- Fix DatabaseConnection timeout handling
- Update API documentation

═══════════════════════════════════════════════════════════

Edit message? (y/n): n
Commit with this message? (y/n): y

Update README? (y/n): y
✅ README updated.

Committing changes...
✅ Changes committed.

Pushing to remote...
✅ Changes pushed to remote.

═══════════════════════════════════════════════════════════
                        SUCCESS
═══════════════════════════════════════════════════════════

✅ All changes have been committed successfully.
```

### Security Validation

Automatically scans staged changes for sensitive information using industry-standard patterns:
- AWS Access Keys, GitHub Tokens, Private Keys
- API Keys/Passwords/Secrets (common patterns)
- Environment files (`.env`)

If detected, a warning (⚠️) is displayed. You can cancel (`n`) or proceed (`y`) after acknowledging the risk.

### AI-Powered Test Generation

When functionality changes (feat/fix) are detected, you'll be prompted to generate test cases. **Language-agnostic** support:
- **Java**: JUnit tests in `src/test/java/`
- **Python**: pytest tests in `tests/`
- **JavaScript/TypeScript**: `.test.js` or `.spec.js` files
- **Other languages**: Generic structure in `tests/` directory

The AI generates 1-2 relevant test cases using the appropriate framework. You can choose to generate tests, and then save or skip the generated file.

### What Happens Behind the Scenes

| Step | Process | Description |
|------|---------|-------------|
| 1 | **Validation** | Checks if repository exists and verifies staged changes are present |
| 2 | **Diff Extraction** | Retrieves and analyzes staged changes using JGit library |
| 3 | **Security Validation** | Scans the git diff for sensitive information (API keys, passwords, tokens, secrets, sensitive files). If detected, a warning (⚠️) is displayed with details, and you can choose to proceed or cancel. |
| 4 | **Test Generation** (optional) | Prompts user to generate test cases. If confirmed, uses OpenAI to generate test cases for the changed code. Supports multiple languages and appropriate testing frameworks. |
| 5 | **AI Processing** | Sends git diff to OpenAI API for intelligent commit message generation |
| 6 | **User Interaction** | Displays message, allows editing, and confirms actions |
| 7 | **README Update** (if confirmed) | Uses AI to update README.md by sending the current README content, commit message, and git diff to OpenAI. The AI intelligently adds new features to the Features section, updates Usage/Configuration sections as needed, and preserves existing content. A changelog entry is automatically appended with timestamp. |
| 8 | **Git Commit** | Commits all staged changes with AI-generated message |
| 9 | **Push** | Attempts to push changes to remote repository. If push fails (e.g., no remote configured, authentication issues, network problems), a warning (⚠️) is displayed but the commit remains successful. You can push manually later using `git push`. |
| 10 | **Slack Notification** (if configured) | Sends a POST request to the configured Slack webhook URL with the commit message in JSON format. The notification appears in your Slack channel as "AI Commit:" followed by the commit message. |

### Commit Message Types

Follows Conventional Commits specification: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`, `perf:`

## Configuration

Environment variables:
- `OPENAI_API_KEY` (required): Your OpenAI API key
- `SLACK_WEBHOOK_URL` (optional): Slack webhook URL for notifications
- `OPENAI_MODEL` (optional): OpenAI model to use (default: gpt-4o-mini)

### Project Structure

```
src/main/java/com/aigitassist/
├── AiGitAssistApplication.java    # Main application with CommandLineRunner
├── model/
│   ├── SensitivePattern.java     # Model for sensitive pattern detection
│   └── ValidationResult.java     # Model for validation results
├── service/
│   ├── AIService.java             # OpenAI API integration
│   ├── GitService.java            # Git operations using JGit
│   ├── ReadmeService.java         # README generation/updates
│   ├── SecurityValidationService.java  # Security validation for sensitive data
│   └── SlackService.java          # Slack notifications
└── util/
    └── FileUtils.java             # Utility methods for file operations
```

## Architecture

The application follows a simple service-oriented architecture:

1. **Main Application**: `CommandLineRunner` that orchestrates the workflow
2. **Model Layer**: Data models for validation and patterns
   - `ValidationResult`: Contains security validation results
   - `SensitivePattern`: Represents patterns for detecting sensitive information
3. **Service Layer**: Contains business logic
   - `GitService`: Manages Git operations using JGit
   - `AIService`: Communicates with OpenAI API using WebFlux for commit messages, README updates, and test generation
   - `SecurityValidationService`: Scans staged changes for sensitive information
   - `ReadmeService`: Handles README file operations
   - `SlackService`: Sends optional Slack notifications
4. **Utility Layer**: Helper utilities
   - `FileUtils`: Utility methods for file path operations and test file generation
