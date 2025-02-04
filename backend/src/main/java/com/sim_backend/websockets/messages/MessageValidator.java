package com.sim_backend.websockets.messages;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for validating objects using Jakarta Bean Validation (JSR-380).
 * It provides methods to check whether an object is valid and retrieve validation messages.
 */
public class MessageValidator{

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private static final Validator validator = factory.getValidator();

    /**
     * Validates the given object and returns validation messages if any constraint violations occur.
     *
     * @param object The object to be validated.
     * @param <T>    The type of the object.
     * @return A string containing validation messages if invalid, or "Valid" if there are no violations.
     */
    public static <T> String validate(T object) {
        // Get the set of constraint violations
        Set<ConstraintViolation<T>> violations = validator.validate(object);

        // If there are no violations, return "Valid"
        if (violations.isEmpty()) {
            return "Valid";
        }

        // Convert validation errors into a readable string format
        return violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage()) // Example: "transactionId must not be null"
                .collect(Collectors.joining(", "));
    }

    /**
     * Checks if the given object is valid.
     *
     * @param object The object to be validated.
     * @param <T>    The type of the object.
     * @return true if the object is valid, false otherwise.
     */
    public static <T> boolean isValid(T object) {
        return validator.validate(object).isEmpty(); // Returns true if no violations
    }
}
