package com.aigitassist.service;

import com.aigitassist.model.SensitivePattern;
import com.aigitassist.model.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SecurityValidationService {

    /**
     * Globally recognized patterns for sensitive information detection.
     * Based on industry standards (gitleaks, GitGuardian, SonarQube).
     */
    private static final List<SensitivePattern> SENSITIVE_PATTERNS = Arrays.asList(
        new SensitivePattern(
            Pattern.compile("AKIA[0-9A-Z]{16}"),
            "AWS Access Key detected"
        ),
        new SensitivePattern(
            Pattern.compile("ghp_[a-zA-Z0-9]{36}"),
            "GitHub Personal Access Token detected"
        ),
        new SensitivePattern(
            Pattern.compile("-----BEGIN\\s+(RSA\\s+)?PRIVATE\\s+KEY-----"),
            "Private key detected"
        ),
        new SensitivePattern(
            Pattern.compile("(?i)(api[_-]?key|password|secret|token)\\s*[=:]\\s*['\"]?([a-zA-Z0-9_-]{16,})['\"]?"),
            "API key, password, or secret detected"
        ),
        new SensitivePattern(
            Pattern.compile("(?i)[/\\\\]\\.env(\\s|$|/|\\\\|\\+)"),
            ".env file detected"
        )
    );

    /**
     * Validates git diff for sensitive information using industry-standard patterns.
     * @param diff The git diff content to validate
     * @return ValidationResult indicating if diff is safe and list of violations
     */
    public ValidationResult validateDiff(String diff) {
        if (diff == null || diff.trim().isEmpty()) {
            return new ValidationResult(true, Collections.emptyList());
        }

        List<String> violations = new ArrayList<>();
        String[] lines = diff.split("\n");

        for (SensitivePattern pattern : SENSITIVE_PATTERNS) {
            for (String line : lines) {
                Matcher matcher = pattern.pattern.matcher(line);
                if (matcher.find()) {
                    String maskedLine = maskSensitiveValue(line, matcher);
                    violations.add(pattern.description + ": " + maskedLine);
                    break; // Report once per pattern type
                }
            }
        }

        return new ValidationResult(violations.isEmpty(), violations);
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

