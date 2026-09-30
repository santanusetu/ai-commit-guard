package com.example;

import com.example.service.CalculatorService;
import com.example.util.InputValidator;

import java.util.Scanner;

/**
 * Main Calculator Application
 * A simple command-line calculator for testing AI Git Assist
 */
public class Calculator {
    private final CalculatorService calculatorService;
    private final InputValidator inputValidator;
    private final Scanner scanner;

    public Calculator() {
        this.calculatorService = new CalculatorService();
        this.inputValidator = new InputValidator();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("=== Simple Calculator ===");
        System.out.println("Enter 'quit' to exit\n");

        while (true) {
            System.out.print("Enter operation (add, subtract, multiply, divide, power): ");
            String operation = scanner.nextLine().trim().toLowerCase();

            if (operation.equals("quit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (!inputValidator.isValidOperation(operation)) {
                System.out.println("Invalid operation. Please try again.");
                continue;
            }

            System.out.print("Enter first number: ");
            String num1Str = scanner.nextLine().trim();
            if (!inputValidator.isValidNumber(num1Str)) {
                System.out.println("Invalid number. Please try again.");
                continue;
            }

            System.out.print("Enter second number: ");
            String num2Str = scanner.nextLine().trim();
            if (!inputValidator.isValidNumber(num2Str)) {
                System.out.println("Invalid number. Please try again.");
                continue;
            }

            try {
                double num1 = Double.parseDouble(num1Str);
                double num2 = Double.parseDouble(num2Str);

                double result = calculatorService.calculate(operation, num1, num2);
                System.out.println("Result: " + result + "\n");
            } catch (ArithmeticException e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage() + "\n");
            }
        }

        scanner.close();
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.run();
    }
}

