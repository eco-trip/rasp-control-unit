package io.github.ecotrip.measures.water;

import io.github.ecotrip.measures.CombinableMeasure;

public class FlowRate extends CombinableMeasure {

    private FlowRate(final double value) {
        super(value);
    }

    @Override
    public CombinableMeasure combine(CombinableMeasure with) {
        return FlowRate.of(getValue() + with.getValue());
    }

    /**
     * Factory method
     * @param value of liters
     * @return the flow rate measured in liters per minute
     */
    public static FlowRate of(double value) {
        return new FlowRate(value);
    }

    @Override
    public String toString() {
        return "FlowRate{" +
                "value=" + getValue() + " liter/min" +
                '}';
    }
}
