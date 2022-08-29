package io.github.ecotrip.exception;

/**
 * It's thrown when a use case accesses a sensor which is null.
 */
public class UnassignedSensorException extends RuntimeException {
    public UnassignedSensorException(String sensorType) {
        super(sensorType.concat(" sensor must be assigned!"));
    }
}
