package io.github.ecotrip.measures.ambient;

import io.github.ecotrip.measures.Measure;

/**
 * Humidity in %
 */
public class Humidity extends Measure {

    private Humidity(final double value) {
        super(value);
    }

    /**
     * Factory method
     * @param percentage value of humidity
     * @return
     */
    public static Humidity of(final double percentage) {
        return new Humidity(percentage);
    }

    @Override
    public String toString() {
        return "Humidity{" +
                "value=" + getValue() + "%" +
                '}';
    }
}
