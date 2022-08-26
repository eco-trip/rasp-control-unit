package io.github.ecotrip.measure.ambient;

import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.MeasureType;

/**
 * Temperature measure in °C
 */
public class Temperature extends Measure {
    /**
     * Represents the environment where the measure took place
     */
    public enum Environment {
        ROOM, HOT_WATER_PIPE, COLD_WATER_PIPE;
    }

    private Temperature(double value, MeasureType type) {
        super(value, type);
    }

    public static Temperature of(final double value, final Environment environment) {
        return new Temperature(value, toMeasureType(environment));
    }

    private static MeasureType toMeasureType(Environment environment) {
        if (environment == Environment.ROOM) {
            return MeasureType.ROOM_TEMPERATURE;
        } else if (environment == Environment.COLD_WATER_PIPE) {
            return MeasureType.COLD_WATER_TEMPERATURE;
        }
        return MeasureType.HOT_WATER_TEMPERATURE;
    }

    @Override
    public String toString() {
        return "Temperature{"
                + "value=" + getValue() + "°C"
                + '}';
    }
}
