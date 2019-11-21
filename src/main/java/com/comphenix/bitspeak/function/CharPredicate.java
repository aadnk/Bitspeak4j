package com.comphenix.bitspeak.function;

import java.util.Objects;

/**
 * Represents a char-specialization of {@link java.util.function.Predicate}.
 */
@FunctionalInterface
public interface CharPredicate {
    /**
     * Compose the current predicate and the given predicate using the logical AND operator.
     * @param other the other predicate.
     * @return The composed predicate.
     */
    default CharPredicate and(CharPredicate other) {
        Objects.requireNonNull(other);
        return (value) -> test(value) && other.test(value);
    }

    /**
     * Compose the current predicate and the given predicate using the logical OR operator.
     * @param other the other predicate.
     * @return The composed predicate.
     */
    default CharPredicate or(CharPredicate other) {
        Objects.requireNonNull(other);
        return (value) -> test(value) || other.test(value);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     * @return The logical negation of this predicate.
     */
    default CharPredicate negate() {
        return value -> !test(value);
    }

    /**
     * Evaluate this predicate on the given argument-
     * @param value the argument.
     * @return TRUE if the input matches the predicate, FALSE otherwise.
     */
    public boolean test(char value);
}
