package io.github.ecotrip.measures.energy;

import io.github.ecotrip.measures.Measure;

public class Resistance extends Measure {

    private Resistance(final double value) {
        super(value);
    }

    /**
     * Factory method
     * @param ohms resistance value in Ohms
     * @return a new Resistance value object
     */
    public static Resistance of(double ohms) {
        return new Resistance(ohms);
    }

    public static Resistance of(final Current current, final Voltage voltage) {
        return Resistance.of(current.getValue() / voltage.getValue());
    }

    @Override
    public String toString() {
        return "Resistance{" +
                "value=" + getValue() + " ohm" +
                '}';
    }
}
