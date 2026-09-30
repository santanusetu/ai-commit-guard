package com.aigitassist.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FileUtilsTest {

    @Test
    void extractsFirstChangedSourceFile() {
        String diff = "+++ b/README.md\n"
                + "+++ b/src/main/java/com/example/Calculator.java\n";
        assertEquals("src/main/java/com/example/Calculator.java", FileUtils.extractChangedFile(diff));
    }

    @Test
    void skipsTestAndConfigFiles() {
        String diff = "+++ b/src/test/java/com/example/CalculatorTest.java\n"
                + "+++ b/src/main/resources/config.json\n";
        assertNull(FileUtils.extractChangedFile(diff));
    }

    @Test
    void javaTestPathFollowsMavenLayout() {
        assertEquals("src/test/java/com/example/CalculatorTest.java",
                FileUtils.generateTestFilePath("src/main/java/com/example/Calculator.java"));
    }

    @Test
    void pythonTestPathUsesTestPrefix() {
        assertEquals("tests/test_app_utils.py", FileUtils.generateTestFilePath("src/app/utils.py"));
    }

    @Test
    void javascriptTestPathSitsNextToSource() {
        assertEquals("src/lib/format.test.ts", FileUtils.generateTestFilePath("src/lib/format.ts"));
    }

    @Test
    void fileWithoutExtensionGoesUnderTests() {
        assertEquals("tests/Makefile_test", FileUtils.generateTestFilePath("Makefile"));
    }
}
