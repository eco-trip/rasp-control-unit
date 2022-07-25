package io.github.ecotrip.measures.energy;

import io.github.ecotrip.measures.Measure;

/**
 * Measured in Volt (V)
 */
public class Voltage extends Measure {

    private Voltage(double value) {
        super(value);
    }

    @Override
    public String toString() {
        return "Voltage{" +
                "value=" + getValue() + " volts" +
                '}';
    }

    /**
     * Factory method
     * @param volts voltage in volts
     * @return a new Voltage value object
     */
    public static Voltage of(double volts) {
        return new Voltage(volts);
    }

    public static Voltage of(Current current, Resistance resistance) {
        return Voltage.of(current.getValue() * resistance.getValue());
    }
}

