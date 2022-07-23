package io.github.ecotrip.measures.ambient;

import io.github.ecotrip.measures.Measure;

public class Temperature extends Measure<Double> {

    private Temperature(double value) {
        super(value);
    }

    public static Temperature of(final double value) {
        return new Temperature(value);
    }

    @Override
    public String toString() {
        return "Temperature{" +
                "value=" + getValue() + " °C" +
                '}';
    }

    @Override
    public Measure<Double> increase(Measure<Double> measure) {
        return Temperature.of(getValue() + measure.getValue());
    }

    @Override
    public Measure<Double> decrease(Measure<Double> measure) {
        return Temperature.of(getValue() - measure.getValue());
    }
}
