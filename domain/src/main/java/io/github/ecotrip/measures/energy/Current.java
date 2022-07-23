package io.github.ecotrip.measures.energy;

import io.github.ecotrip.measures.Measure;

public class Current extends Measure<Double> {

    private Current(final double value) {
        super(value);
    }

    /**
     * Factory method
     * @param amps current in amps
     * @return a new Current value object
     */
    public static Current of(double amps) {
        return new Current(amps);
    }

    @Override
    public String toString() {
        return "Current{" +
                "value=" + getValue() + " amps" +
                '}';
    }

    @Override
    public Current increase(Measure<Double> measure) {
        return Current.of(getValue() + measure.getValue());
    }

    @Override
    public Current decrease(Measure<Double> measure) {
        return Current.of(getValue() + measure.getValue());
    }
}
