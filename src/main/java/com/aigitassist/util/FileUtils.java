package com.aigitassist.util;

/**
 * Utility class for file-related operations.
 */
public class FileUtils {

    /**
     * Extracts the first changed source file from git diff (excluding test files).
     * @param diff Git diff content
     * @return Path to changed file, or null if none found
     */
    public static String extractChangedFile(String diff) {
        String[] lines = diff.split("\n");
        for (String line : lines) {
            if (line.startsWith("+++") && line.contains("src/")) {
                String file = line.replace("+++ b/", "").trim();
                // Skip test files and common non-source files
                if (!file.contains("Test") && !file.contains("test") && 
                    !file.endsWith(".md") && !file.endsWith(".txt") &&
                    !file.endsWith(".json") && !file.endsWith(".xml")) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * Generates test file path based on source file path and language conventions.
     * @param sourceFile Path to source file
     * @return Path for test file
     */
    public static String generateTestFilePath(String sourceFile) {
        // Extract file extension
        int lastDot = sourceFile.lastIndexOf('.');
        if (lastDot == -1) {
            return "tests/" + sourceFile + "_test";
        }
        
        String extension = sourceFile.substring(lastDot);
        String basePath = sourceFile.substring(0, lastDot);
        
        // Determine test directory and naming convention based on language
        if (extension.equals(".java")) {
            // Java: src/test/java/.../ClassNameTest.java
            String javaPath = basePath.replace("src/main/java/", "");
            return "src/test/java/" + javaPath + "Test.java";
        } else if (extension.equals(".py")) {
            // Python: tests/test_*.py
            String pythonPath = basePath.replace("src/", "").replace("/", "_");
            return "tests/test_" + pythonPath + ".py";
        } else if (extension.equals(".js") || extension.equals(".ts")) {
            // JavaScript/TypeScript: *.test.js or *.spec.js
            return basePath + ".test" + extension;
        } else {
            // Generic: tests/*_test.*
            String genericPath = basePath.replace("src/", "");
            return "tests/" + genericPath + "_test" + extension;
        }
    }
}

