package io.github.ecotrip.measure;

/**
 * Custom exception used to catch manageable runtime exceptions
 */
public class IncompatibleMeasuresException extends RuntimeException {
    public IncompatibleMeasuresException(String message) {
        super(message);
    }
}
