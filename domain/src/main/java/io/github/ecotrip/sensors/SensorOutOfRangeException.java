package io.github.ecotrip.sensors;

import io.github.ecotrip.measures.InvalidMeasureException;

public class SensorOutOfRangeException extends InvalidMeasureException {

    public <ID, T> SensorOutOfRangeException(ID identifier, T value) {
        super("Sensor " + identifier + " has measured value " + value + "which is out of range!");
    }
}