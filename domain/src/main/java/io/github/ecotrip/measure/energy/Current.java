package io.github.ecotrip.measure.energy;

import io.github.ecotrip.measure.CombinableMeasure;
import io.github.ecotrip.measure.MeasureType;

/**
 * Current measure in amps
 */
public class Current extends CombinableMeasure {

    private Current(final double value) {
        super(value, MeasureType.CURRENT);
    }

    /**
     * Factory method
     * @param amps current in amps
     * @return a new Current value object
     */
    public static Current of(double amps) {
        return new Current(amps);
    }

    public static Current of(Voltage voltage, Resistance resistance) {
        return new Current(voltage.getValue() / resistance.getValue());
    }

    @Override
    public String toString() {
        return "Current{"
                + "value=" + getValue() + " amps"
                + '}';
    }

    @Override
    protected CombinableMeasure combine(CombinableMeasure with) {
        return Current.of(getValue() + with.getValue());
    }
}
