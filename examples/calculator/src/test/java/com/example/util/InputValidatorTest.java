package com.example.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InputValidator
 */
public class InputValidatorTest {
    private InputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InputValidator();
    }

    @Test
    void testValidNumber() {
        assertTrue(validator.isValidNumber("123"));
    }

    @Test
    void testValidOperation() {
        assertTrue(validator.isValidOperation("add"));
    }
}

