package io.github.ecotrip.measure.water;

import io.github.ecotrip.measure.CombinableMeasure;
import io.github.ecotrip.measure.MeasureType;

/**
 * Flow rate in liters/min
 */
public class FlowRate extends CombinableMeasure {

    /**
     * Allows to filter flow rate based on the water pipe
     */
    public enum FlowRateType {
        COLD, HOT;
    }

    private FlowRate(final double value, final MeasureType type) {
        super(value, type);
    }

    @Override
    protected CombinableMeasure combine(CombinableMeasure with) {
        return FlowRate.of((getValue() + with.getValue()), toFlowRateType(getType()));
    }

    /**
     * Factory method
     * @param value of liters
     * @return the flow rate measured in liters per minute
     */
    public static FlowRate of(double value, FlowRateType type) {
        return new FlowRate(value, toMeasureType(type));
    }

    private static MeasureType toMeasureType(final FlowRateType type) {
        return type == FlowRateType.HOT ? MeasureType.HOT_FLOW_RATE : MeasureType.COLD_FLOW_RATE;
    }

    private static FlowRateType toFlowRateType(final MeasureType type) {
        return type == MeasureType.COLD_FLOW_RATE ? FlowRateType.COLD : FlowRateType.HOT;
    }

    @Override
    public String toString() {
        return "FlowRate{"
                + "value=" + getValue() + " liters/min"
                + '}';
    }
}
