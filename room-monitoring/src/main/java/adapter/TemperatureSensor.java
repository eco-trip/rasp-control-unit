package adapter;

import adapter.builder.SensorBuilder;
import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.measures.ambient.Temperature;
import io.github.ecotrip.measures.energy.Current;
import io.github.ecotrip.measures.energy.Resistance;
import io.github.ecotrip.measures.energy.Voltage;
import io.github.ecotrip.sensors.channel.DataChannel;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.concurrent.CompletableFuture;

public class TemperatureSensor <ID> extends Sensor<ID> {
    private static final double KELVIN = 273.15;
    private final DataChannel<Voltage> channel;
    private final Configuration configuration;

    private TemperatureSensor(final ID identifier, final DetectionFactory<ID> detectionFactory,
                              DataChannel<Voltage> channel, final Configuration configuration) {
        super(identifier, detectionFactory);
        this.channel = channel;
        this.configuration = configuration;
    }

    @Override
    protected CompletableFuture<Measure> measure() {
        return channel.getRawData()
                .thenApply(voltage -> computeSteinhartFormula(
                        voltage,
                        configuration.vcc,
                        configuration.boardResistance,
                        configuration.sensorResistance,
                        configuration.bValue,
                        configuration.nominalTemperature));
    }

    @Override
    protected boolean isMeasureValid(Measure measure) {
        return measure.isGreaterEqualThan(configuration.minValue) && measure.isLessEqualThan(configuration.maxValue);
    }

    private static Temperature computeSteinhartFormula(Voltage voltage, Voltage vcc, Resistance r1, Resistance r2,
                                                       int bValue, Temperature nominalTemperature) {
        final Resistance r3 = Resistance.of(Current.of(voltage, r1),
                Voltage.of(vcc.getValue() - voltage.getValue()));
        final double value = 1 / ((Math.log(r3.getValue() / r2.getValue()) / bValue) +
                1 / (nominalTemperature.getValue() + KELVIN)) - KELVIN;
        return Temperature.of(value);
    }

    public static class Builder<ID> extends SensorBuilder<ID> {
        private DataChannel<Voltage> channel;
        private Configuration configuration;

        public Builder<ID> setChannel(DataChannel<Voltage> channel) {
            this.channel = channel;
            return this;
        }

        public Builder<ID> setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        @Override
        public TemperatureSensor<ID> build() {
            return new TemperatureSensor<>(getIdentifier(), getDetectionFactory(), channel, configuration);
        }
    }

    public static class Configuration {
        private final Temperature maxValue;
        private final Temperature minValue;
        private final Resistance boardResistance;
        private final Voltage vcc;
        private final int bValue;
        private final Temperature nominalTemperature;
        private final Resistance sensorResistance;

        public Configuration(Temperature maxValue, Temperature minValue, Temperature nominalTemperature,
                             Resistance boardResistance, Resistance sensorResistance, Voltage vcc, int bValue) {
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.boardResistance = boardResistance;
            this.vcc = vcc;
            this.bValue = bValue;
            this.nominalTemperature = nominalTemperature;
            this.sensorResistance = sensorResistance;
        }

        public static class Builder {
            private Temperature maxValue;
            private Temperature minValue;
            private Resistance boardResistance;
            private Voltage vcc;
            private int bValue;
            private Temperature nominalTemperature;
            private Resistance sensorResistance;

            public Builder setMaxValue(Temperature maxValue) {
                this.maxValue = maxValue;
                return this;
            }

            public Builder setMinValue(Temperature minValue) {
                this.minValue = minValue;
                return this;
            }

            public Builder setBoardResistance(Resistance boardResistance) {
                this.boardResistance = boardResistance;
                return this;
            }

            public Builder setVcc(Voltage vcc) {
                this.vcc = vcc;
                return this;
            }

            public Builder setBvalue(int bValue) {
                this.bValue = bValue;
                return this;
            }

            public Builder setNominalTemperature(Temperature nominalTemperature) {
                this.nominalTemperature = nominalTemperature;
                return this;
            }

            public Builder setSensorResistance(Resistance sensorResistance) {
                this.sensorResistance = sensorResistance;
                return this;
            }

            public Configuration build() {
                return new Configuration(maxValue, minValue, nominalTemperature, boardResistance, sensorResistance,
                        vcc, bValue);
            }
        }
    }
}
