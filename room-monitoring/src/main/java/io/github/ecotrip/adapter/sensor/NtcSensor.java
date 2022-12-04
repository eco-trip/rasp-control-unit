package io.github.ecotrip.adapter.sensor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.ecotrip.Generated;
import io.github.ecotrip.adapter.builder.SensorBuilder;
import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.ambient.Temperature;
import io.github.ecotrip.measure.energy.Resistance;
import io.github.ecotrip.measure.energy.Voltage;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.sensor.Sensor;

/**
 * NTC sensor implementation
 *
 * @param <ID> uniquely identifies the sensor instance.
 */
@Generated
public class NtcSensor<ID> extends Sensor<ID> {
    private static final double KELVIN = 273.15;
    private final Supplier<Voltage> channel;
    private final Configuration configuration;

    private NtcSensor(final ID identifier, final DetectionFactory<ID> detectionFactory,
                      final Supplier<Voltage> channel, final Configuration configuration) {
        super(identifier, detectionFactory);
        this.channel = channel;
        this.configuration = configuration;
    }

    @Override
    protected CompletableFuture<List<Measure>> measure() {
        return CompletableFuture.supplyAsync(() -> {
            var voltage = channel.get();
            return computeSteinhartFormula(
                    voltage,
                    configuration.vcc,
                    configuration.boardResistance,
                    configuration.sensorResistance,
                    configuration.bValue,
                    configuration.nominalTemperature);
        }).thenApply(List::of);
    }

    @Override
    protected boolean isMeasureValid(Measure measure) {
        return measure.isGreaterEqualThan(configuration.minValue)
                && measure.isLessEqualThan(configuration.maxValue);
    }

    private Temperature computeSteinhartFormula(Voltage inputVoltage, Voltage vcc,
                                                Resistance r1, Resistance r2, int bValue,
                                                Temperature nominalTemperature) {
        var r3 = inputVoltage.getValue() * r1.getValue()
                / (vcc.getValue() - inputVoltage.getValue());
        final double value = 1 / ((Math.log(r3 / r2.getValue()) / bValue)
                + 1 / (nominalTemperature.getValue() + KELVIN)) - KELVIN;
        return Temperature.of(value, configuration.environment);
    }

    /**
     * Sensor Builder
     * @param <ID> uniquely identifies the sensor instance.
     */
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
        public NtcSensor<ID> build() {
            return new NtcSensor<>(getIdentifier(), getDetectionFactory(), channel, configuration);
        }
    }

    /**
     * Configuration data and builder
     */
    @Generated
    public static class Configuration {
        private final Temperature maxValue;
        private final Temperature minValue;
        private final Resistance boardResistance;
        private final Voltage vcc;
        private final int bValue;
        private final Temperature nominalTemperature;
        private final Resistance sensorResistance;
        private final Temperature.Environment environment;

        /**
         * @param maxValue
         * @param minValue
         * @param nominalTemperature
         * @param boardResistance
         * @param sensorResistance
         * @param vcc
         * @param bValue
         * @param environment
         */
        public Configuration(Temperature maxValue, Temperature minValue, Temperature nominalTemperature,
                             Resistance boardResistance, Resistance sensorResistance, Voltage vcc, int bValue,
                             Temperature.Environment environment) {
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.boardResistance = boardResistance;
            this.vcc = vcc;
            this.bValue = bValue;
            this.nominalTemperature = nominalTemperature;
            this.sensorResistance = sensorResistance;
            this.environment = environment;
        }

        /**
         * Configuration builder
         */
        @Generated
        public static class Builder {
            private final Temperature.Environment environment;
            private Temperature maxValue;
            private Temperature minValue;
            private Resistance boardResistance;
            private Voltage vcc;
            private int bValue;
            private Temperature nominalTemperature;
            private Resistance sensorResistance;

            public Builder(Temperature.Environment environment) {
                this.environment = environment;
            }

            public Builder setMaxValue(double maxValue) {
                this.maxValue = Temperature.of(maxValue, environment);
                return this;
            }

            public Builder setMinValue(double minValue) {
                this.minValue = Temperature.of(minValue, environment);
                return this;
            }

            public Builder setBoardResistance(double boardResistance) {
                this.boardResistance = Resistance.of(boardResistance);
                return this;
            }

            public Builder setVcc(double vcc) {
                this.vcc = Voltage.of(vcc);
                return this;
            }

            public Builder setBvalue(int bValue) {
                this.bValue = bValue;
                return this;
            }

            public Builder setNominalTemperature(double nominalTemperature) {
                this.nominalTemperature = Temperature.of(nominalTemperature, environment);
                return this;
            }

            public Builder setSensorResistance(double sensorResistance) {
                this.sensorResistance = Resistance.of(sensorResistance);
                return this;
            }

            /**
             * build method
             */
            public Configuration build() {
                return new Configuration(maxValue, minValue, nominalTemperature, boardResistance, sensorResistance,
                        vcc, bValue, environment);
            }
        }
    }
}
