package io.github.ecotrip.measures.ambient;

import io.github.ecotrip.measures.InvalidMeasureException;
import io.github.ecotrip.measures.Measure;

/**
 * Humidity in %
 */
public class Humidity extends Measure {

    private Humidity(final double value) throws InvalidMeasureException {
        super(value);
        if(value > 100 || value < 0) {
            throw new InvalidMeasureException(value);
        }
    }

    /**
     * Factory method
     * @param percentage value of humidity
     * @return
     */
    public static Humidity of(final double percentage) throws InvalidMeasureException {
        return new Humidity(percentage);
    }

    @Override
    public String toString() {
        return "Humidity{" +
                "value=" + getValue() + "%" +
                '}';
    }
}
