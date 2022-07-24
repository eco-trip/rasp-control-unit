package io.github.ecotrip.measures.water;

import io.github.ecotrip.measures.CombinableMeasure;

public class FlowRate extends CombinableMeasure<Liter> {

    private FlowRate(final Liter value) {
        super(value);
    }

    @Override
    public CombinableMeasure<Liter> combine(CombinableMeasure<Liter> with) {
        return FlowRate.of(getValue().add(with.getValue()));
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
