package io.github.ecotrip.measures;

import java.util.Objects;

public abstract class Measure<T> {
    private final T value;

    protected Measure(final T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public abstract Measure<T> increase(Measure<T> measure);
    public abstract Measure<T> decrease(Measure<T> measure);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Measure<?> measure = (Measure<?>) o;
        return Objects.equals(getValue(), measure.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
