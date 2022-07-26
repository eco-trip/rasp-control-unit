package adapter;

import adapter.builder.SensorBuilder;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.measures.water.FlowRate;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WaterFlowSensor<ID> extends Sensor<ID> {
    private final int frequency;
    private final AtomicInteger pulses = new AtomicInteger();
    private final AtomicBoolean enabledForReading = new AtomicBoolean();

    protected WaterFlowSensor(ID identifier, DetectionFactory<ID> detectionFactory, int frequency,
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
    protected CompletableFuture<Measure> measure() {
        return CompletableFuture.supplyAsync(() -> {
            double start = java.lang.System.nanoTime();
            pulses.set(0);
            enabledForReading.set(true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            enabledForReading.set(false);
            double end = java.lang.System.nanoTime();
            return computeFlowRateUsingHallEffect((end - start) / 1000000);
        });
    }

    @Override
    protected boolean isMeasureValid(Measure measure) {
        return measure.getValue() >= 0;
    }

    private FlowRate computeFlowRateUsingHallEffect(final double time) {
        double effectivePulses = (pulses.get() * 1000 / time);
        return FlowRate.of(effectivePulses / frequency);
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
