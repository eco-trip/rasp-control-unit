package io.github.ecotrip.measure.energy;

import io.github.ecotrip.measure.CombinableMeasure;
import io.github.ecotrip.measure.MeasureType;

/**
 * Voltage measure in Volt (V)
 */
public class Voltage extends CombinableMeasure {
    private Voltage(double value) {
        super(value, MeasureType.VOLTAGE);
    }

    @Override
    public String toString() {
        return "Voltage{"
                + "value=" + getValue() + " volts"
                + '}';
    }

    /**
     * Factory method
     * @param volts voltage in volts
     * @return a new Voltage value object
     */
    public static Voltage of(double volts) {
        return new Voltage(volts);
    }

    @Override
    protected CombinableMeasure combine(CombinableMeasure with) {
        return Voltage.of(getValue() + with.getValue());
    }
}
