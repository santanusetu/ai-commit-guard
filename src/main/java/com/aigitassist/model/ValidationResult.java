package com.aigitassist.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of security validation containing safety status and detected violations.
 */
public class ValidationResult {
    private final boolean isSafe;
    private final List<String> violations;

    public ValidationResult(boolean isSafe, List<String> violations) {
        this.isSafe = isSafe;
        this.violations = violations != null ? new ArrayList<>(violations) : Collections.emptyList();
    }

    public boolean isSafe() {
        return isSafe;
    }

    public List<String> violations() {
        return violations;
    }
}

