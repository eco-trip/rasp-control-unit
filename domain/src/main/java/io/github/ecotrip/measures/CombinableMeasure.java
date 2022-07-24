package io.github.ecotrip.measures;

public abstract class CombinableMeasure<T> extends Measure<T> {
    protected CombinableMeasure(T value) {
        super(value);
    }

    public abstract CombinableMeasure<T> combine(CombinableMeasure<T> with);
}
