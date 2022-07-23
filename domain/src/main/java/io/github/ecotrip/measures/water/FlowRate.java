package io.github.ecotrip.measures.water;

import io.github.ecotrip.measures.Measure;


public class FlowRate extends Measure<Liter> {

    private FlowRate(final Liter value) {
        super(value);
    }

    @Override
    public FlowRate increase(Measure<Liter> measure) {
        return null;
    }

    @Override
    public FlowRate decrease(Measure<Liter> measure) {
        return null;
    }

    /**
     * Factory method
     * @param value of liters
     * @return the flow rate measured in liters per minute
     */
    public static FlowRate of(Liter value) {
        return new FlowRate(value);
    }

    @Override
    public String toString() {
        return "FlowRate{" +
                "value=" + getValue() + " liter/min" +
                '}';
    }
}
