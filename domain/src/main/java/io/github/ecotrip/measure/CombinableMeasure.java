package io.github.ecotrip.measure;

/**
 * Represents a Measure which can be combined
 */
public abstract class CombinableMeasure extends Measure {
    protected CombinableMeasure(double value, MeasureType type) {
        super(value, type);
    }

    /**
     * Helper method used to combine the measures after checking if they're comparable
     * @param with the second measured to be combined
     * @return a new measure with the combined value.
     */
    public CombinableMeasure checkAndCombine(CombinableMeasure with) {
        checkIfMeasuresAreComparable(this, with);
        return combine(with);
    }

    protected abstract CombinableMeasure combine(CombinableMeasure with);
}