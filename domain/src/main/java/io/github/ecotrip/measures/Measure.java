package io.github.ecotrip.measures;

import java.util.Objects;

public abstract class Measure {
    private final double value;

    protected Measure(final double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public boolean isGreaterEqualThan(Measure measure) {
        return measure.getValue() <= getValue();
    }

    public boolean isLessEqualThan(Measure measure) {
        return measure.getValue() >= getValue();
    }

    public boolean isGreaterThan(Measure measure) {
        return measure.getValue() < getValue();
    }

    public boolean isLessThan(Measure measure) {
        return measure.getValue() > getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Measure measure = (Measure) o;
        return Objects.equals(getValue(), measure.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
