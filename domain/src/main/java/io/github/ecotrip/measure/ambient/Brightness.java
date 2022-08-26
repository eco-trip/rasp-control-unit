package io.github.ecotrip.measure.ambient;

import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.MeasureType;

/**
 * Brightness measure in Lux
 */
public class Brightness extends Measure {

    private Brightness(final double value) {
        super(value, MeasureType.BRIGHTNESS);
    }

    public static Brightness of(final double value) {
        return new Brightness(value);
    }

    @Override
    public String toString() {
        return "Brightness{"
                + "value="
                + getValue()
                + " lux"
                + '}';
    }
}
