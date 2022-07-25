package io.github.ecotrip.measures.energy;

import io.github.ecotrip.measures.CombinableMeasure;

public class Current extends CombinableMeasure {

    private Current(final double value) {
        super(value);
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
        return new Current(voltage.getValue() * resistance.getValue());
    }

    @Override
    public String toString() {
        return "Current{" +
                "value=" + getValue() + " amps" +
                '}';
    }

    @Override
    public CombinableMeasure combine(CombinableMeasure with) {
        return Current.of(getValue() + with.getValue());
    }
}
