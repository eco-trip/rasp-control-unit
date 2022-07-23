package io.github.ecotrip.measures;

public class InvalidMeasureException extends Exception {
    public <T> InvalidMeasureException(T value) {
        super("Measured value " + value + " is out of range");
    }
}
