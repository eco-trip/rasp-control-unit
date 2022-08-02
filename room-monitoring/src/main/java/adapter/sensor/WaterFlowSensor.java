package adapter.sensor;

import adapter.builder.SensorBuilder;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;
import execution.Execution;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WaterFlowSensor<ID> extends Sensor<ID> {
    private static final long ONE_SECOND_IN_MILLIS = 1000;
    private final int frequency;
    private final AtomicInteger pulses = new AtomicInteger();
    private final AtomicBoolean enabledForReading = new AtomicBoolean();

    private WaterFlowSensor(ID identifier, DetectionFactory<ID> detectionFactory, int frequency,
                              DigitalInput digitalInput) {
        super(identifier, detectionFactory);
        this.frequency = frequency;
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
            double actualTime = Execution.calculateExecutionTimeInMillis(this::receivePulsesWithinOneSecond);
            return computeFlowRateUsingHallEffect(actualTime);
        }).thenApply(List::of);
    }

    @Override
    protected boolean isMeasureValid(Measure measure) {
        return measure.getValue() >= 0;
    }

    private FlowRate computeFlowRateUsingHallEffect(final double actualTime) {
        double actualPulses = ((pulses.get() * actualTime) / ONE_SECOND_IN_MILLIS);
        return FlowRate.of(actualPulses / frequency);
    }

    private void receivePulsesWithinOneSecond() {
        enabledForReading.set(true);
        Execution.safeSleep(ONE_SECOND_IN_MILLIS);
        enabledForReading.set(false);
    }

    public static class Builder<ID> extends SensorBuilder<ID> {
        private int frequency;
        private DigitalInput digitalInput;

        public Builder<ID> setFrequency(int frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder<ID> setDigitalInput(DigitalInput digitalInput) {
            this.digitalInput = digitalInput;
            return this;
        }

        @Override
        public WaterFlowSensor<ID> build() {
            return new WaterFlowSensor<>(getIdentifier(), getDetectionFactory(), frequency, digitalInput);
        }
    }
}
