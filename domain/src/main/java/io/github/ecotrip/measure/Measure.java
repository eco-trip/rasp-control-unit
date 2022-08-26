package io.github.ecotrip.measure;

import java.util.Objects;

/**
 * Represents a measure detected by a specific sensor
 */
public abstract class Measure {
    private final double value;
    private final MeasureType type;

    protected Measure(final double value, final MeasureType type) {
        this.value = value;
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public MeasureType getType() {
        return type;
    }

    /**
     * Compare the value of @this with the one of another measure,
     * @param measure to be compared
     * @return true if the value of the second one is less than the value of @this
     */
    public boolean isGreaterEqualThan(Measure measure) {
        checkIfMeasuresAreComparable(measure, this);
        return measure.getValue() <= getValue();
    }

    /**
     * Compare the value of @this with the one of another measure,
     * @param measure to be compared
     * @return true if the value of the second one is greater than the value of @this
     */
    public boolean isLessEqualThan(Measure measure) {
        checkIfMeasuresAreComparable(measure, this);
        return measure.getValue() >= getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Measure measure = (Measure) o;
        return Double.compare(measure.getValue(), getValue()) == 0 && getType() == measure.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getType());
    }

    /**
     * Check if measures can be compared (e.g. hot_water with cold_water)
     * @param m1 the first measure
     * @param m2 the second measure
     */
    public static void checkIfMeasuresAreComparable(final Measure m1, final Measure m2) {
        if (m1.getType() != m2.getType()) {
            throw new IncompatibleMeasuresException("Cannot compare different types of measures");
        }
    }
}
