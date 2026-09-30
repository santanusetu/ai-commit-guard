package com.aicommitguard.service;

import com.aicommitguard.model.SensitivePattern;
import com.aicommitguard.model.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SecurityValidationService {

    static final String REDACTED = "[REDACTED]";

    /**
     * Patterns for secrets inside file content.
     * Based on industry standards (gitleaks, GitGuardian, SonarQube).
     */
    private static final List<SensitivePattern> CONTENT_PATTERNS = Arrays.asList(
        new SensitivePattern(
            Pattern.compile("AKIA[0-9A-Z]{16}"),
            "AWS Access Key detected"
        ),
        new SensitivePattern(
            Pattern.compile("(ghp|gho|ghu|ghs|ghr)_[a-zA-Z0-9]{36}|github_pat_[a-zA-Z0-9_]{22,}"),
            "GitHub token detected"
        ),
        new SensitivePattern(
            Pattern.compile("xox[baprs]-[a-zA-Z0-9-]{10,}"),
            "Slack token detected"
        ),
        new SensitivePattern(
            Pattern.compile("sk-(ant-|proj-)?[a-zA-Z0-9_-]{20,}"),
            "AI provider API key detected"
        ),
        new SensitivePattern(
            Pattern.compile("-----BEGIN\\s+(RSA\\s+|EC\\s+|DSA\\s+|OPENSSH\\s+)?PRIVATE\\s+KEY-----"),
            "Private key detected"
        ),
        new SensitivePattern(
            Pattern.compile("(?i)(api[_-]?key|password|secret|token)\\s*[=:]\\s*['\"]?([a-zA-Z0-9_-]{16,})['\"]?"),
            "API key, password, or secret detected"
        )
    );

    /**
     * Patterns for sensitive file names, matched against the "+++ b/path" header of a diff.
     */
    private static final List<SensitivePattern> FILE_PATTERNS = Collections.singletonList(
        new SensitivePattern(
            Pattern.compile("(?i)(^|/)\\.env(\\.[a-z0-9_-]+)?$"),
            ".env file detected"
        )
    );

    /**
     * Validates the lines a diff ADDS for sensitive information. Removed and unchanged
     * lines are ignored, so deleting a leaked secret does not raise a warning.
     * @param diff The git diff content to validate
     * @return ValidationResult indicating if diff is safe and list of violations
     */
    public ValidationResult validateDiff(String diff) {
        if (diff == null || diff.trim().isEmpty()) {
            return new ValidationResult(true, Collections.emptyList());
        }

        List<String> addedLines = new ArrayList<>();
        List<String> addedFiles = new ArrayList<>();
        for (String line : diff.split("\n")) {
            if (line.startsWith("+++ ")) {
                addedFiles.add(line.substring(4).replaceFirst("^b/", "").trim());
            } else if (line.startsWith("+")) {
                addedLines.add(line.substring(1));
            }
        }

        List<String> violations = new ArrayList<>();
        for (SensitivePattern pattern : CONTENT_PATTERNS) {
            for (String line : addedLines) {
                Matcher matcher = pattern.pattern.matcher(line);
                if (matcher.find()) {
                    violations.add(pattern.description + ": " + maskSensitiveValue(line, matcher));
                    break; // Report once per pattern type
                }
            }
        }
        for (SensitivePattern pattern : FILE_PATTERNS) {
            for (String file : addedFiles) {
                if (pattern.pattern.matcher(file).find()) {
                    violations.add(pattern.description + ": " + file);
                    break;
                }
            }
        }

        return new ValidationResult(violations.isEmpty(), violations);
    }

    /**
     * Replaces every detected secret in the diff with [REDACTED], so the diff can be sent
     * to an AI provider without leaking credentials.
     * @param diff The git diff content
     * @return The diff with secret values removed
     */
    public String redact(String diff) {
        if (diff == null) {
            return null;
        }
        String result = diff;
        for (SensitivePattern pattern : CONTENT_PATTERNS) {
            result = pattern.pattern.matcher(result).replaceAll(REDACTED);
        }
        return result;
    }

    /**
     * Masks sensitive values in a line for safe display.
     * Shows only last 4 characters of the matched value.
     * @param line The line containing sensitive data
     * @param matcher Matcher that found the sensitive pattern
     * @return Line with sensitive value masked
     */
    private String maskSensitiveValue(String line, Matcher matcher) {
        String matched = matcher.group(0);
        if (matched == null || matched.length() <= 4) {
            return line.replaceAll("\\S+", "***");
        }

        // Show only last 4 characters
        String masked = "***" + matched.substring(matched.length() - 4) + "***";
        return line.replace(matched, masked);
    }

}
