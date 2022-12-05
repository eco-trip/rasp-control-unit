package io.github.ecotrip.adapter.sensor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;

import io.github.ecotrip.Generated;
import io.github.ecotrip.adapter.builder.SensorBuilder;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.water.FlowRate;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.sensor.Sensor;

/**
 * Generic water flow hall effect sensor implementation
 *
 * @param <ID> uniquely identifies the sensor instance.
 */
@Generated
public class WaterFlowHallSensor<ID> extends Sensor<ID> {
    private static final long ONE_SECOND_IN_MILLIS = 1000;
    private final int frequency;
    private final AtomicInteger pulses = new AtomicInteger();
    private final AtomicBoolean enabledForReading = new AtomicBoolean();
    private final FlowRate.FlowRateType flowRateType;

    private WaterFlowHallSensor(ID identifier, DetectionFactory<ID> detectionFactory, int frequency,
                                DigitalInput digitalInput, FlowRate.FlowRateType flowRateType) {
        super(identifier, detectionFactory);
        this.frequency = frequency;
        this.flowRateType = flowRateType;
        digitalInput.addListener(e -> {
            if (e.state() == DigitalState.HIGH && enabledForReading.get()) {
                pulses.incrementAndGet();
            }
        });
    }

    @Override
    protected CompletableFuture<List<Measure>> measure() {
        return CompletableFuture.supplyAsync(() -> {
            pulses.set(0);
            double actualTime = Execution.getComputationalTimeInMillis(this::receivePulsesWithinOneSecond);
            return computeFlowRateUsingHallEffect(actualTime);
        }).thenApply(List::of);
    }

    @Override
    protected boolean isMeasureValid(Measure measure) {
        return measure.getValue() >= 0;
    }

    private FlowRate computeFlowRateUsingHallEffect(final double actualTime) {
        double actualPulses = ((pulses.get() * actualTime) / ONE_SECOND_IN_MILLIS);
        return FlowRate.of(actualPulses / frequency, flowRateType);
    }

    private void receivePulsesWithinOneSecond() {
        enabledForReading.set(true);
        Execution.safeSleep(ONE_SECOND_IN_MILLIS);
        enabledForReading.set(false);
    }

    /**
     * Public entrypoint for {@link WaterFlowHallSensor} construction.
     * @param <ID> uniquely identifies the sensor instance.
     */
    @Generated
    public static class Builder<ID> extends SensorBuilder<ID> {
        private final FlowRate.FlowRateType flowRateType;
        private int frequency;
        private DigitalInput digitalInput;

        public Builder(final FlowRate.FlowRateType flowRateType) {
            this.flowRateType = flowRateType;
        }

        public Builder<ID> setFrequency(int frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder<ID> setDigitalInput(DigitalInput digitalInput) {
            this.digitalInput = digitalInput;
            return this;
        }

        @Override
        public WaterFlowHallSensor<ID> build() {
            return new WaterFlowHallSensor<>(
                getIdentifier(), getDetectionFactory(), frequency, digitalInput, flowRateType);
        }
    }
}
