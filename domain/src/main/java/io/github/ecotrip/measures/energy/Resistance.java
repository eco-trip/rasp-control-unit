package io.github.ecotrip.measures.energy;

import io.github.ecotrip.measures.Measure;

import java.util.Objects;

public class Resistance extends Measure<Double> {

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

    @Override
    public String toString() {
        return "Resistance{" +
                "value=" + getValue() + " ohm" +
                '}';
    }

    @Override
    public Resistance increase(Measure<Double> measure) {
        return Resistance.of(getValue() + measure.getValue());
    }

    @Override
    public Resistance decrease(Measure<Double> measure) {
        return Resistance.of(getValue() - measure.getValue());
    }
}
