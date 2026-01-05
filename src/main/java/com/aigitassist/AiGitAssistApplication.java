package com.aigitassist;

import com.aigitassist.model.ValidationResult;
import com.aigitassist.service.AIService;
import com.aigitassist.service.GitService;
import com.aigitassist.service.ReadmeService;
import com.aigitassist.service.SecurityValidationService;
import com.aigitassist.service.SlackService;
import com.aigitassist.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.nio.file.Files;
import java.util.Scanner;

@SpringBootApplication
@ComponentScan(basePackages = "com.aigitassist")
public class AiGitAssistApplication implements CommandLineRunner {
    
    private static final String SKIP_RUNNER = "skip.runner";

    @Autowired
    private GitService gitService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ReadmeService readmeService;

    @Autowired
    private SecurityValidationService securityValidationService;

    @Autowired
    private SlackService slackService;

    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AiGitAssistApplication.class);
        app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
        app.setLogStartupInfo(false);
        app.run(args);
    }

    @Override
    public void run(String... args) {
        // Skip if running in test mode
        if (System.getProperty(SKIP_RUNNER) != null) {
            return;
        }
        
        // Clear console and show welcome message
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              AI Git Assist v1.0.0                        ║");
        System.out.println("║     AI-Powered Commit Messages & Test Generation         ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
        
        try {
            executeRun(args);
        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage() + "\n");
            System.exit(1);
        }
    }
    
    private void executeRun(String... args) throws Exception {
        // Get repository path (default to current directory)
        String repoPath = args.length > 0 ? args[0] : System.getProperty("user.dir");
        File repoDir = new File(repoPath);

        if (!repoDir.exists() || !repoDir.isDirectory()) {
            System.err.println("\n❌ ERROR: Repository path does not exist: " + repoPath + "\n");
            System.exit(1);
        }

        // Check if it's a Git repository
        File gitDir = new File(repoDir, ".git");
        if (!gitDir.exists() || !gitDir.isDirectory()) {
            System.err.println("\n❌ ERROR: Not a Git repository: " + repoPath);
            System.err.println("Please run this tool from within a Git repository, or initialize one with 'git init'\n");
            System.exit(1);
        }

        // Check for staged changes
        if (!gitService.hasStagedChanges(repoPath)) {
            System.out.println("\n❌ ERROR: No staged changes found. Run `git add` first.\n");
            System.exit(1);
        }

        // Get git diff
        String diff = gitService.getStagedDiff(repoPath);
        if (diff.isEmpty()) {
            System.out.println("\n❌ ERROR: No changes detected in staged files.\n");
            System.exit(1);
        }

        // Validate for sensitive information
        System.out.println("\nValidating changes for sensitive information...");
        ValidationResult validation = securityValidationService.validateDiff(diff);
        if (!validation.isSafe()) {
            System.out.println("⚠️  WARNING: Security issues detected!\n");
            System.err.println("SECURITY WARNING: Sensitive information detected in staged changes!");
            System.err.println("\nThe following potential security issues were found:\n");
            for (String violation : validation.violations()) {
                System.err.println("  - " + violation);
            }
            System.err.println("\nWARNING: Proceeding may expose sensitive data.\n");
            if (!askYesNo("Continue anyway? (y/n): ")) {
                System.out.println("\nCommit cancelled.\n");
                return;
            }
            System.out.println("\nProceeding with commit (user acknowledged risk)...\n");
        } else {
            System.out.println("✅ Security validation passed.\n");
        }

        // Generate test cases for functionality changes
        if (diff.contains("+") || diff.contains("-")) {
            System.out.println();
            if (askYesNo("Generate test cases for functionality changes? (y/n): ")) {
                System.out.println("\nGenerating test cases for functionality changes...\n");
                
                String changedFile = FileUtils.extractChangedFile(diff);
                if (changedFile != null) {
                    try {
                        String testCode = aiService.generateTestCases(diff, changedFile);
                        
                        System.out.println("═══════════════════════════════════════════════════════════");
                        System.out.println("                    GENERATED TEST CASES");
                        System.out.println("═══════════════════════════════════════════════════════════\n");
                        System.out.println(testCode);
                        System.out.println("\n═══════════════════════════════════════════════════════════\n");
                        
                        if (askYesNo("Save test file? (y/n): ")) {
                            String testFilePath = FileUtils.generateTestFilePath(changedFile);
                            File testFile = new File(repoPath, testFilePath);
                            testFile.getParentFile().mkdirs();
                            Files.write(testFile.toPath(), testCode.getBytes());
                            System.out.println("\n✅ Test saved: " + testFilePath + "\n");
                        } else {
                            System.out.println("\nTest file not saved.\n");
                        }
                    } catch (Exception e) {
                        System.err.println("\n❌ ERROR: Could not generate tests: " + e.getMessage() + "\n");
                    }
                } else {
                    System.out.println("\nNo suitable source file found for test generation.\n");
                }
            }
        }

        // Generate commit message using AI
        System.out.println("Generating commit message...");
        String commitMessage = aiService.generateCommitMessage(diff);
        System.out.println("✅ Done.\n");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("                    COMMIT MESSAGE");
        System.out.println("═══════════════════════════════════════════════════════════\n");
        System.out.println(commitMessage);
        System.out.println("\n═══════════════════════════════════════════════════════════\n");

        // Ask if user wants to edit the message
        if (askYesNo("Edit message? (y/n): ")) {
            commitMessage = editCommitMessage(commitMessage);
            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("                 UPDATED COMMIT MESSAGE");
            System.out.println("═══════════════════════════════════════════════════════════\n");
            System.out.println(commitMessage);
            System.out.println("\n═══════════════════════════════════════════════════════════\n");
        }

        // Ask for confirmation before committing
        System.out.println();
        if (!askYesNo("Commit with this message? (y/n): ")) {
            System.out.println("\nCommit cancelled.\n");
            return;
        }

        // Ask if user wants to update README
        System.out.println();
        if (askYesNo("Update README? (y/n): ")) {
            System.out.println("\nUpdating README...");
            readmeService.ensureReadme(repoPath, commitMessage, diff, aiService);
            System.out.println("✅ README updated.\n");
        } else {
            System.out.println("\nSkipping README update.\n");
        }

        // Commit changes
        System.out.println("Committing changes...");
        gitService.commitChanges(repoPath, commitMessage);
        System.out.println("✅ Changes committed.\n");

        // Get current branch and try to push
        String branch = gitService.getCurrentBranch(repoPath);
        System.out.println("Pushing to remote...");
        try {
            gitService.pushChanges(repoPath, branch);
            System.out.println("✅ Changes pushed to remote.\n");
        } catch (Exception e) {
            System.out.println("⚠️  WARNING: Push failed: " + e.getMessage());
            System.out.println("(Commit was successful, but push failed)\n");
        }

        // Send Slack notification
        slackService.sendNotification(commitMessage);

        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("                        SUCCESS");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("\n✅ All changes have been committed successfully.\n");
        
        // Exit application after successful completion
        System.exit(0);
    }

    /**
     * Prompts user for yes/no input
     * @param prompt The question to ask
     * @return true if user answers yes, false otherwise
     */
    private boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                return true;
            } else if (response.equals("n") || response.equals("no")) {
                return false;
            } else {
                System.out.println("Please enter 'y' or 'n'");
            }
        }
    }

    /**
     * Allows user to edit the commit message
     * @param originalMessage The original AI-generated message
     * @return The edited message, or original if user cancels
     */
    private String editCommitMessage(String originalMessage) {
        System.out.println("Enter your commit message (press Enter twice to finish, or 'cancel' to keep original):");
        System.out.println("Current message:");
        System.out.println(originalMessage);
        System.out.println("\nEnter new message:");

        StringBuilder newMessage = new StringBuilder();
        String line;
        int emptyLines = 0;

        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            
            if (line.trim().equalsIgnoreCase("cancel")) {
                return originalMessage;
            }
            
            if (line.isEmpty()) {
                emptyLines++;
                if (emptyLines >= 2) {
                    break;
                }
                newMessage.append("\n");
            } else {
                emptyLines = 0;
                if (newMessage.length() > 0) {
                    newMessage.append("\n");
                }
                newMessage.append(line);
            }
        }

        String edited = newMessage.toString().trim();
        return edited.isEmpty() ? originalMessage : edited;
    }

}

