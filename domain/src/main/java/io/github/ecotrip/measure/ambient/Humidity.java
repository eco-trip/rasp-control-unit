package io.github.ecotrip.measure.ambient;

import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.MeasureType;

/**
 * Humidity measure in %
 */
public class Humidity extends Measure {

    private Humidity(final double value) {
        super(value, MeasureType.HUMIDITY);
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
        return "Humidity{"
                + "value="
                + getValue()
                + "%"
                + '}';
    }
}
