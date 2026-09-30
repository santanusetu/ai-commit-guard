package com.example.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating user input
 */
public class InputValidator {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * Validates if the input is a valid number
     *
     * @param input The input string to validate
     * @return true if input is a valid number, false otherwise
     */
    public boolean isValidNumber(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return NUMBER_PATTERN.matcher(input.trim()).matches();
    }

    /**
     * Validates if the input is a valid operation
     *
     * @param operation The operation string to validate
     * @return true if operation is valid, false otherwise
     */
    public boolean isValidOperation(String operation) {
        if (operation == null || operation.trim().isEmpty()) {
            return false;
        }
        String op = operation.trim().toLowerCase();
        return op.equals("add") || op.equals("subtract") || 
               op.equals("multiply") || op.equals("divide") || op.equals("power");
    }
}

