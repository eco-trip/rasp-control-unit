package io.github.ecotrip.measure.energy;

import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.MeasureType;

/**
 * Resistance in ohms
 */
public class Resistance extends Measure {

    private Resistance(final double value) {
        super(value, MeasureType.RESISTANCE);
    }

    /**
     * Factory method
     * @param ohms resistance value in Ohms
     * @return a new Resistance value object
     */
    public static Resistance of(double ohms) {
        return new Resistance(ohms);
    }

    @Override
    public String toString() {
        return "Resistance{"
                + "value=" + getValue() + " ohm"
                + '}';
    }
}
