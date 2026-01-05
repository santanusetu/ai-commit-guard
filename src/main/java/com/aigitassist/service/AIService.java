package com.aigitassist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    private final WebClient webClient;
    private final String model;
    private final ObjectMapper objectMapper;

    public AIService(@Value("${openai.api.key:}") String apiKey,
                    @Value("${openai.model:gpt-4o-mini}") String model) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "OPENAI_API_KEY environment variable is not set.\n" +
                "Please set it using: export OPENAI_API_KEY=your-api-key"
            );
        }
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Generates a Conventional Commit message from git diff using AI
     * @param diff The git diff of staged changes
     * @return Generated commit message following Conventional Commits specification
     */
    public String generateCommitMessage(String diff) {
        String prompt = String.format(
                "You are an expert software developer.\n" +
                "Here is a git diff of staged changes:\n\n%s\n\n" +
                "Write a Conventional Commit message:\n" +
                "- One of: feat:, fix:, docs:, refactor:, test:, chore:, perf:\n" +
                "- Summary line <= 72 chars\n" +
                "- Optional short body with bullet points\n" +
                "- Do NOT include markdown code fences or backticks",
                diff
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.2);

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You write excellent, concise conventional commits.");

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        requestBody.put("messages", new Object[]{systemMessage, userMessage});

        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            String content = jsonNode.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

            return content.trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate commit message: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a new README.md file for a project using AI
     * @param projectName Name of the project
     * @param envKeys Array of environment variable keys to document
     * @return Generated README.md content
     */
    public String generateReadme(String projectName, String[] envKeys) {
        String envList = envKeys.length > 0 
                ? String.join("\n", java.util.Arrays.stream(envKeys)
                    .map(k -> "- `" + k + "`")
                    .toArray(String[]::new))
                : "- (none)";

        String prompt = String.format(
                "Write a professional GitHub README.md for a software project called \"%s\".\n" +
                "Include these sections in this order:\n\n" +
                "# <Title>\n" +
                "One-paragraph description of what the project does.\n\n" +
                "## Features\n" +
                "Short bullet list of key capabilities.\n\n" +
                "## Installation\n" +
                "Exact steps to install the project based on its technology stack.\n\n" +
                "## Usage\n" +
                "How to run the project with primary commands.\n\n" +
                "## Configuration\n" +
                "Environment variables if needed:\n%s\n\n" +
                "## Development\n" +
                "- How to run locally\n" +
                "- How to run tests (if any)\n" +
                "- Coding style standards\n\n" +
                "## Features / Changelog\n" +
                "Add a single placeholder bullet here.",
                projectName, envList
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.2);

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You write excellent, practical READMEs for real projects.");

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        requestBody.put("messages", new Object[]{systemMessage, userMessage});

        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText()
                    .trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate README: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing README.md file based on new changes using AI
     * @param currentReadme Current README.md content
     * @param commitMessage The commit message describing the changes
     * @param diff The git diff of changes
     * @return Updated README.md content
     */
    public String updateReadme(String currentReadme, String commitMessage, String diff) {
        String prompt = String.format(
                "Here is the current README.md:\n\n%s\n\n" +
                "Here is the new commit message:\n%s\n\n" +
                "Here is the git diff:\n%s\n\n" +
                "Update the README.md to reflect the new changes.\n" +
                "- Add new features to the Features section if relevant.\n" +
                "- Update Usage or Configuration if needed.\n" +
                "- Do NOT remove existing information.\n" +
                "- Keep all original sections.\n" +
                "- Do NOT wrap the file in code fences.\n" +
                "Return the full updated README.md.",
                currentReadme, commitMessage, diff
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.2);

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You update README.md files professionally for software projects.");

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        requestBody.put("messages", new Object[]{systemMessage, userMessage});

        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText()
                    .trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update README: " + e.getMessage(), e);
        }
    }

    /**
     * Generates test cases for changed code using AI
     * @param diff The git diff showing code changes
     * @param changedFile Path to the file that was changed
     * @return Generated test code (1-2 test cases)
     */
    public String generateTestCases(String diff, String changedFile) {
        String prompt = String.format(
                "You are an expert software tester.\n" +
                "Here is a git diff showing code changes:\n\n%s\n\n" +
                "Generate 1-2 concise test cases for the changed functionality in: %s\n" +
                "- Use appropriate testing framework for the file's language\n" +
                "- Focus on main functionality\n" +
                "- Return only test code, no explanations\n" +
                "- Do NOT wrap in code fences",
                diff, changedFile
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.3);

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You write excellent, practical test cases.");

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        requestBody.put("messages", new Object[]{systemMessage, userMessage});

        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText()
                    .trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate test cases: " + e.getMessage(), e);
        }
    }

}

