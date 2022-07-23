package io.github.ecotrip.measures.water;

import io.github.ecotrip.measures.InvalidMeasureException;

import java.util.Objects;

public class Liter {
    private static final double ZERO_LITER  = 0.0;
    private final double value;

    private Liter(final double value) {
        this.value = value < 0 ? ZERO_LITER : value;
    }

    public static Liter of(double value) {
        return new Liter(value);
    }

    /**
     *
     * @return current value in amps
     */
    public Double getValue() {
        return value;
    }

    public Liter add(final Liter liter) {
        return Liter.of(getValue() + liter.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Liter liter = (Liter) o;
        return Double.compare(liter.getValue(), getValue()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    @Override
    public String toString() {
        return "Liter{" +
                "value=" + value + " liter" +
                '}';
    }
}
