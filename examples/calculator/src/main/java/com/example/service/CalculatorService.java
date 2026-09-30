package com.example.service;

/**
 * Service class for performing calculator operations
 */
public class CalculatorService {

    /**
     * Performs the specified calculation operation
     *
     * @param operation The operation to perform (add, subtract, multiply, divide, power)
     * @param num1      First number
     * @param num2      Second number
     * @return The result of the calculation
     * @throws ArithmeticException if division by zero is attempted
     * @throws IllegalArgumentException if operation is not supported
     */
    public double calculate(String operation, double num1, double num2) {
        switch (operation.toLowerCase()) {
            case "add":
                return add(num1, num2);
            case "subtract":
                return subtract(num1, num2);
            case "multiply":
                return multiply(num1, num2);
            case "divide":
                return divide(num1, num2);
            case "power":
                return power(num1, num2);
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
    }

    public double add(double a, double b) {
        return a + b;
    }

    public double subtract(double a, double b) {
        return a - b;
    }

    public double multiply(double a, double b) {
        return a * b;
    }

    public double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero is not allowed");
        }
        return a / b;
    }

    /**
     * Calculates the power of a number (base raised to exponent)
     *
     * @param base     The base number
     * @param exponent The exponent
     * @return The result of base raised to the power of exponent
     */
    public double power(double base, double exponent) {
        return Math.pow(base, exponent);
    }
}

