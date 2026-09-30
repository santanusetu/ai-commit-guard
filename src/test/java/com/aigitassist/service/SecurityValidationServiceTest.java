package com.aigitassist.service;

import com.aigitassist.model.ValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityValidationServiceTest {

    private final SecurityValidationService service = new SecurityValidationService();

    private static String diffAdding(String line) {
        return "diff --git a/App.java b/App.java\n"
                + "--- a/App.java\n"
                + "+++ b/App.java\n"
                + "@@ -1,1 +1,2 @@\n"
                + " class App {}\n"
                + "+" + line + "\n";
    }

    @Test
    void cleanDiffIsSafe() {
        ValidationResult result = service.validateDiff(diffAdding("int total = price * quantity;"));
        assertTrue(result.isSafe());
        assertTrue(result.violations().isEmpty());
    }

    @Test
    void emptyOrNullDiffIsSafe() {
        assertTrue(service.validateDiff("").isSafe());
        assertTrue(service.validateDiff(null).isSafe());
    }

    @Test
    void detectsAwsAccessKey() {
        ValidationResult result = service.validateDiff(diffAdding("String key = \"AKIAABCDEFGHIJKLMNOP\";"));
        assertFalse(result.isSafe());
        assertTrue(result.violations().get(0).startsWith("AWS Access Key detected"));
    }

    @Test
    void detectsClassicAndFineGrainedGithubTokens() {
        assertFalse(service.validateDiff(diffAdding("ghp_abcdefghijklmnopqrstuvwxyz0123456789")).isSafe());
        assertFalse(service.validateDiff(diffAdding("github_pat_11ABCDEFG0abcdefghijklmnopqrstuv")).isSafe());
    }

    @Test
    void detectsSlackAndAiProviderKeys() {
        assertFalse(service.validateDiff(diffAdding("token: xoxb-1234567890-abcdefghij")).isSafe());
        assertFalse(service.validateDiff(diffAdding("OPENAI=sk-proj-abcdefghijklmnopqrstuvwx")).isSafe());
        assertFalse(service.validateDiff(diffAdding("ANTHROPIC=sk-ant-abcdefghijklmnopqrstuvwx")).isSafe());
    }

    @Test
    void detectsPrivateKeys() {
        assertFalse(service.validateDiff(diffAdding("-----BEGIN RSA PRIVATE KEY-----")).isSafe());
        assertFalse(service.validateDiff(diffAdding("-----BEGIN OPENSSH PRIVATE KEY-----")).isSafe());
    }

    @Test
    void detectsGenericPasswordAssignment() {
        ValidationResult result = service.validateDiff(diffAdding("password = \"SuperSecretValue123\""));
        assertFalse(result.isSafe());
    }

    @Test
    void detectsAddedEnvFile() {
        String diff = "diff --git a/.env b/.env\n"
                + "--- /dev/null\n"
                + "+++ b/.env\n"
                + "@@ -0,0 +1 @@\n"
                + "+DEBUG=true\n";
        ValidationResult result = service.validateDiff(diff);
        assertFalse(result.isSafe());
        assertEquals(".env file detected: .env", result.violations().get(0));
    }

    @Test
    void ignoresSecretsOnRemovedLines() {
        // Deleting a leaked key is the fix, not a new leak
        String diff = "diff --git a/App.java b/App.java\n"
                + "--- a/App.java\n"
                + "+++ b/App.java\n"
                + "@@ -1,2 +1,1 @@\n"
                + "-String key = \"AKIAABCDEFGHIJKLMNOP\";\n"
                + " class App {}\n";
        assertTrue(service.validateDiff(diff).isSafe());
    }

    @Test
    void ignoresSecretsOnUnchangedContextLines() {
        String diff = "diff --git a/App.java b/App.java\n"
                + "--- a/App.java\n"
                + "+++ b/App.java\n"
                + "@@ -1,1 +1,2 @@\n"
                + " String key = \"AKIAABCDEFGHIJKLMNOP\";\n"
                + "+int x = 1;\n";
        assertTrue(service.validateDiff(diff).isSafe());
    }

    @Test
    void violationMessageMasksTheSecret() {
        ValidationResult result = service.validateDiff(diffAdding("String key = \"AKIAABCDEFGHIJKLMNOP\";"));
        String message = result.violations().get(0);
        assertFalse(message.contains("AKIAABCDEFGHIJKLMNOP"));
        assertTrue(message.contains("***MNOP***"));
    }

    @Test
    void redactRemovesEverySecretButKeepsTheRestOfTheDiff() {
        String diff = diffAdding("String key = \"AKIAABCDEFGHIJKLMNOP\"; // aws")
                + "+String gh = \"ghp_abcdefghijklmnopqrstuvwxyz0123456789\";\n";
        String redacted = service.redact(diff);
        assertFalse(redacted.contains("AKIAABCDEFGHIJKLMNOP"));
        assertFalse(redacted.contains("ghp_abcdefghijklmnopqrstuvwxyz0123456789"));
        assertTrue(redacted.contains(SecurityValidationService.REDACTED));
        assertTrue(redacted.contains("// aws"));
        assertTrue(redacted.contains("class App {}"));
    }

    @Test
    void redactLeavesCleanDiffUnchanged() {
        String diff = diffAdding("int total = price * quantity;");
        assertEquals(diff, service.redact(diff));
    }
}
