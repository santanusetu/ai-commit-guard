package com.aigitassist.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReadmeService {

    private static final String README_FILE = "README.md";
    private static final String CHANGELOG_HEADER = "## Features / Changelog";

    /**
     * Ensures README.md exists and updates it with AI-generated content and changelog
     * @param repositoryPath Path to the git repository
     * @param commitMessage The commit message for changelog entry
     * @param diff Git diff of staged changes
     * @param aiService AI service for generating/updating README content
     */
    public void ensureReadme(String repositoryPath, String commitMessage, String diff,
                            AIService aiService) throws IOException {
        Path readmePath = Paths.get(repositoryPath, README_FILE);
        String projectName = Paths.get(repositoryPath).getFileName().toString();
        String[] envKeys = {"OPENAI_API_KEY", "SLACK_WEBHOOK_URL", "OPENAI_MODEL"};

        String readmeContent;
        if (!Files.exists(readmePath)) {
            // Generate new README
            readmeContent = aiService.generateReadme(projectName, envKeys);
            Files.write(readmePath, readmeContent.getBytes());
        } else {
            // Update existing README
            String currentReadme = new String(Files.readAllBytes(readmePath));
            readmeContent = aiService.updateReadme(currentReadme, commitMessage, diff);
            Files.write(readmePath, readmeContent.getBytes());
        }

        // Append changelog entry
        appendChangelog(readmePath, commitMessage);
    }

    /**
     * Appends a changelog entry to README.md with timestamp and commit summary
     * @param readmePath Path to README.md file
     * @param commitMessage Commit message to extract summary from
     */
    private void appendChangelog(Path readmePath, String commitMessage) throws IOException {
        List<String> lines = Files.readAllLines(readmePath);
        String summary = commitMessage.split("\n")[0];
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String changelogEntry = String.format("- **%s**: %s", timestamp, summary);

        boolean inserted = false;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).strip().equals(CHANGELOG_HEADER) && !inserted) {
                lines.add(i + 1, changelogEntry);
                inserted = true;
                break;
            }
        }

        if (!inserted) {
            lines.add("");
            lines.add(CHANGELOG_HEADER);
            lines.add(changelogEntry);
        }

        Files.write(readmePath, lines);
    }
}

