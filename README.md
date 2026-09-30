# AI Git Assist

[![build](https://github.com/santanusetu/Ai-Git-Assist/actions/workflows/build.yml/badge.svg)](https://github.com/santanusetu/Ai-Git-Assist/actions/workflows/build.yml)
![Java](https://img.shields.io/badge/Java-11%2B-ED8B00?logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

An interactive command-line assistant for `git commit`. It checks your staged changes for leaked secrets, writes a [Conventional Commits](https://www.conventionalcommits.org/) message with an LLM, can draft tests for the code you changed, and keeps your README up to date. Nothing is committed or pushed until you say yes.

```
$ git add src/main/java/com/example/UserService.java
$ java -jar ai-git-assist.jar

Validating changes for sensitive information...
✅ Security validation passed.

Generating commit message...
═══════════════════════════════════════════════════════════
                    COMMIT MESSAGE
═══════════════════════════════════════════════════════════

feat: implement user authentication service

- Add UserService with login and registration
- Fix DatabaseConnection timeout handling

Edit message? (y/n): n
Commit with this message? (y/n): y
Push to origin/main? (y/n): y
✅ Changes pushed to remote.
```

## Features

- **Secret scanning before anything leaves your machine**: AWS keys, GitHub tokens (classic and fine-grained), Slack tokens, OpenAI/Anthropic keys, private keys, `password=`/`token=` assignments, and `.env` files.
- **Commit messages that follow Conventional Commits**: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`, `perf:`, with a summary line of 72 characters or less.
- **Test drafts for the file you changed**: JUnit for Java, pytest for Python, `*.test.js/ts` for JavaScript and TypeScript, placed where each ecosystem expects them.
- **README upkeep**: updates the relevant sections and adds a dated changelog entry.
- **You stay in control**: every step (tests, message, commit, README, push) asks first.
- **Works with any OpenAI-compatible API**: OpenAI by default; point `OPENAI_BASE_URL` at Azure OpenAI, a gateway, or a local server such as Ollama.
- **Optional Slack notification** after each commit.

## How your code and secrets are handled

This tool sends your diff to an LLM, so it is careful about what goes out and what goes in:

| Guarantee | How |
|---|---|
| Only the lines you **add** are scanned | Removing a leaked key is the fix, so deleted and unchanged lines never raise a warning. |
| Secrets are **never sent to the AI provider** | Every detected secret is replaced with `[REDACTED]` before the diff is sent, even if you choose to continue past the warning. |
| Only what you **staged** is committed | Unstaged and untracked files are left alone, because they were never scanned. The only extra files committed are the ones this tool wrote for you (a saved test, an updated README). |
| Nothing is **pushed** without asking | Push is a separate yes/no prompt after the commit. |

Pattern matching catches the common credential formats, but it is not a full secret scanner. For CI-grade coverage, pair it with a tool like [gitleaks](https://github.com/gitleaks/gitleaks).

## Getting started

**Requirements:** Java 11 or newer, Maven 3.6+, and an API key for an OpenAI-compatible endpoint.

```bash
git clone https://github.com/santanusetu/Ai-Git-Assist.git
cd Ai-Git-Assist
mvn clean package
```

That produces `target/ai-git-assist.jar`. Then, from any Git repository:

```bash
export OPENAI_API_KEY=your-api-key
git add <files>
java -jar /path/to/ai-git-assist.jar            # uses the current directory
java -jar /path/to/ai-git-assist.jar ~/code/app # or pass a repository path
```

Tip: add an alias such as `alias aic='java -jar ~/tools/ai-git-assist.jar'`.

## Configuration

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `OPENAI_API_KEY` | yes | | API key for the endpoint below (any non-empty value for a local server that needs no key) |
| `OPENAI_MODEL` | no | `gpt-4o-mini` | Model name |
| `OPENAI_BASE_URL` | no | `https://api.openai.com/v1` | Any OpenAI-compatible endpoint, e.g. `http://localhost:11434/v1` for Ollama |
| `SLACK_WEBHOOK_URL` | no | | Posts each commit message to a Slack channel |

## How it works

```
Validate repository and staged changes
  │
  ▼
Scan added lines for secrets ──► warning ──► continue or cancel
  │
  ▼
Redact secrets from the diff (this is the only version the AI sees)
  │
  ▼
Draft tests? (y/n) ──► save test file? (y/n)
  │
  ▼
Generate commit message ──► edit? (y/n) ──► commit? (y/n)
  │
  ▼
Update README? (y/n)
  │
  ▼
Commit staged changes (+ files this tool wrote)
  │
  ▼
Push? (y/n) ──► Slack notification (if configured)
```

| Component | Responsibility |
|---|---|
| `GitService` | Reads the staged diff (HEAD vs index) with JGit, commits, pushes |
| `SecurityValidationService` | Scans added lines for secrets and redacts them |
| `AIService` | Calls the chat-completions endpoint for messages, tests and README text |
| `ReadmeService` | Creates or updates `README.md` and adds changelog entries |
| `SlackService` | Optional webhook notification |

## Development

```bash
mvn test      # run the test suite
mvn verify    # full build, as run in CI on Java 11, 17 and 21
```

The tests run offline: Git operations use temporary repositories, and the AI client is tested against a local stub server, so no API key is needed.

## License

[MIT](LICENSE)
