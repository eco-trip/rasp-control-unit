package io.github.ecotrip.measures;

public abstract class CombinableMeasure extends Measure {
    protected CombinableMeasure(double value) {
        super(value);
    }

    public abstract CombinableMeasure combine(CombinableMeasure with);
}
