package com.aicommitguard.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadmeServiceTest {

    @TempDir
    Path repo;

    private final ReadmeService readmeService = new ReadmeService();

    @Test
    void createsReadmeWhenMissingAndAddsChangelogEntry() throws Exception {
        AIService ai = mock(AIService.class);
        when(ai.generateReadme(anyString(), any())).thenReturn("# Demo\n\n## Features / Changelog\n- placeholder");

        readmeService.ensureReadme(repo.toString(), "feat: first feature\n\n- details", "+x", ai);

        List<String> lines = Files.readAllLines(repo.resolve("README.md"));
        int header = lines.indexOf("## Features / Changelog");
        assertTrue(header >= 0);
        assertTrue(lines.get(header + 1).endsWith(": feat: first feature"));
    }

    @Test
    void updatesExistingReadmeAndAppendsChangelogSectionIfAbsent() throws Exception {
        Files.write(repo.resolve("README.md"), "# Old\n".getBytes(StandardCharsets.UTF_8));
        AIService ai = mock(AIService.class);
        when(ai.updateReadme(anyString(), anyString(), anyString())).thenReturn("# Updated");

        readmeService.ensureReadme(repo.toString(), "fix: bug", "+x", ai);

        List<String> lines = Files.readAllLines(repo.resolve("README.md"));
        assertEquals("# Updated", lines.get(0));
        assertEquals("## Features / Changelog", lines.get(2));
        assertTrue(lines.get(3).endsWith(": fix: bug"));
    }
}
