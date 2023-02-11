package io.github.ecotrip.adapter.sensor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.ecotrip.Generated;
import io.github.ecotrip.adapter.builder.SensorBuilder;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.energy.Current;
import io.github.ecotrip.measure.energy.Voltage;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.sensor.Sensor;

/**
 * Current sensor, implementation for ACSxxx model.
 * @param <ID> uniquely identifies the sensor.
 */
@Generated
public class AcsSensor<ID> extends Sensor<ID> {
    private static final double CURRENT_THRESHOLD = 0; // 0.4
    private static final double CURRENT_OFFSET = 0;
    private static final int CALIBRATION_PERIODS = 25;
    private static final int AC_FREQUENCY = 50;
    private static final Voltage VOLTAGE_FALLBACK = Voltage.of(0);
    private static final int MIN_SAMPLES = 5;

    private final Supplier<Voltage> channel;
    private final Configuration configuration;
    private Voltage voltageAtZero;

    /**
     * Scale factor identifing different ACS sensor versions
     */
    public enum ScaleFactor {
        ACS_30A(0.066),
        ACS_20A(0.1),
        ACS_5A(0.185);

        private final double value;

        ScaleFactor(final double value) {
            this.value = value;
        }
    }

    protected AcsSensor(ID identifier, DetectionFactory<ID> detectionFactory, Supplier<Voltage> channel,
        Configuration configuration) {
        super(identifier, detectionFactory);
        this.channel = channel;
        this.configuration = configuration;
        calibrate();
    }

    @Override
    protected CompletableFuture<List<Measure>> measure() {
        return CompletableFuture.supplyAsync(() -> computeCurrentAC())
                .thenApply(List::of);
    }

    @Override
    protected boolean isMeasureValid(Measure measure) {
        return measure.getValue() >= 0;
    }

    /**
     * Determines and set the voltage value at 0A current
     */
    private void calibrate() {
        Double middle = configuration.referenceVoltage.getValue() / 2;

        channel.get(); //First read used to clean the channel
        Optional<Voltage> avg = Optional.empty();
        while (avg.isEmpty()) {
            avg = getAvgVoltage(Voltage.of(middle), CALIBRATION_PERIODS);
        }
        voltageAtZero = Voltage.of(avg.get().getValue() + middle);
    }

    /**
     * @return current in amphs
     */
    private Current computeCurrentAC() {
        var avg = getAvgVoltage(voltageAtZero, 1).orElse(VOLTAGE_FALLBACK).getValue();

        Double currentValue = avg
                    / configuration.scaleFactor.value - CURRENT_OFFSET;

        return Current.of(currentValue > CURRENT_THRESHOLD ? currentValue : 0.0);
    }

    private Optional<Voltage> getAvgVoltage(Voltage offset, int periods) {
        var period = Execution.SECOND_IN_MICRO / AC_FREQUENCY;
        var start = Execution.instantInMicros();

        // var totalVoltage = 0.0;
        var numberOfMeasurements = 0;
        var maxVoltage = 0.0;
        // var previousValue = 0.0;

        /*
         * 2 versioni:
         * 1. faccio la media dei valori
         * 2. cerco il picco ... usiamo questa adesso
         */

        while (Execution.instantInMicros() - start < period * periods) {
            var currentValue = channel.get().getValue();

            // if (Math.abs(currentValue - previousValue) > Double.MIN_VALUE) {
            var voltage = Math.abs(currentValue - offset.getValue());
            // System.out.println(currentValue + " / " + offset.getValue() + " / "  + voltage);
            if (voltage > maxVoltage) {
                maxVoltage = voltage;
            }

            //totalVoltage += Math.abs(voltage); // * voltage;
            numberOfMeasurements++;
            // previousValue = currentValue;
            // }
        }

        return numberOfMeasurements < MIN_SAMPLES * periods
            ? Optional.empty()
            : Optional.of(Voltage.of(maxVoltage)); // totalVoltage / numberOfMeasurements
    }

    /**
     * ACS configuration.
     */
    public static class Configuration {
        private final ScaleFactor scaleFactor;
        private final Voltage referenceVoltage;

        protected Configuration(ScaleFactor scaleFactor, Voltage referenceVoltage) {
            this.scaleFactor = scaleFactor;
            this.referenceVoltage = referenceVoltage;
        }

        /**
         * Builder used to construct a {@link Configuration} instance.
         */
        public static class Builder {
            private ScaleFactor scaleFactor;
            private Voltage referenceVoltage;

            public Builder setScaleFactor(ScaleFactor scaleFactor) {
                this.scaleFactor = scaleFactor;
                return this;
            }

            public Builder setReferenceVoltage(Voltage referenceVoltage) {
                this.referenceVoltage = Voltage.of(referenceVoltage.getValue());
                return this;
            }

            public Configuration build() {
                return new Configuration(scaleFactor, referenceVoltage);
            }
        }
    }

    /**
     * Public entrypoint for {@link AcsSensor} construction.
     * @param <ID> uniquely identifies the sensor instance.
     */
    @Generated
    public static class Builder<ID> extends SensorBuilder<ID> {
        private Supplier<Voltage> channel;

        private Configuration configuration;

        public Builder<ID> setChannel(Supplier<Voltage> channel) {
            this.channel = channel;
            return this;
        }

        public Builder<ID> setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        @Override
        public Sensor<ID> build() {
            return new AcsSensor<>(getIdentifier(), getDetectionFactory(), channel, configuration);
        }
    }
}
