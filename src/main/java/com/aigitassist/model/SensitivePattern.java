package com.aigitassist.model;

import java.util.regex.Pattern;

/**
 * Represents a pattern for detecting sensitive information.
 */
public class SensitivePattern {
    public final Pattern pattern;
    public final String description;

    public SensitivePattern(Pattern pattern, String description) {
        this.pattern = pattern;
        this.description = description;
    }
}

