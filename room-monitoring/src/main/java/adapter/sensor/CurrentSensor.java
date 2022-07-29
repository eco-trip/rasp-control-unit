package adapter.sensor;

import adapter.builder.SensorBuilder;
import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.measures.energy.Current;
import io.github.ecotrip.measures.energy.Voltage;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;
import io.github.ecotrip.sensors.channel.DataChannel;
import utils.Execution;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class CurrentSensor<ID> extends Sensor<ID> {
    private static final int AVG_ITERATIONS = 100;
    private final DataChannel<Voltage> channel;

    private final Configuration configuration;

    public enum ScaleFactor {
        ACS_30A(66),
        ACS_20A(100),
        ACS_5A(185);

        private final int value;

        ScaleFactor(final int value) {
            this.value = value;
        }
    }

    private CurrentSensor(ID identifier, DetectionFactory<ID> detectionFactory, DataChannel<Voltage> channel,
                          Configuration configuration) {
        super(identifier, detectionFactory);
        this.channel = channel;
        this.configuration = configuration;
    }

    @Override
    protected CompletableFuture<List<Measure>> measure() {
        return computeOutputVoltage()
                .thenApply(this::computeCurrentInA)
                .thenApply(List::of);
    }

    @Override
    protected boolean isMeasureValid(Measure measure) {
        return measure.getValue() >= 0;
    }

    private Current computeCurrentInA(final Voltage outVoltage) {
        return Current.of((outVoltage.getValue() - configuration.voltageAtZeroCurrent.getValue())
                / configuration.scaleFactor.value);
    }

    private CompletableFuture<Voltage> computeOutputVoltage() {
        return computeAvgOfRawData()
                .thenApply(v -> (v.getValue() / configuration.adcMaxValue) * configuration.referenceVoltage.getValue())
                .thenApply(Voltage::of);
    }

    private CompletableFuture<Voltage> computeAvgOfRawData() {
        return IntStream.of(AVG_ITERATIONS)
                .peek((x) -> Execution.safeSleep(1))
                .mapToObj((x) -> channel.getRawData())
                .reduce((c1, c2) -> c1.thenCombine(c2, (v1, v2) -> Voltage.of(v1.getValue() + v2.getValue())))
                .orElseThrow()
                .thenApply((x) -> x.getValue()/AVG_ITERATIONS)
                .thenApply(Voltage::of);
    }

    public static class Configuration {
        private final ScaleFactor scaleFactor;
        private final Voltage voltageAtZeroCurrent;
        private final Voltage referenceVoltage;
        private final int adcMaxValue;

        public Configuration(ScaleFactor scaleFactor, Voltage voltageAtZeroCurrent, Voltage referenceVoltage,
                             int adcMaxValue) {
            this.scaleFactor = scaleFactor;
            this.voltageAtZeroCurrent = voltageAtZeroCurrent;
            this.referenceVoltage = referenceVoltage;
            this.adcMaxValue = adcMaxValue;
        }

        public static class Builder {
            private ScaleFactor scaleFactor;
            private Voltage voltageAtZeroCurrent;
            private Voltage referenceVoltage;
            private int adcMaxValue;

            public Builder setScaleFactor(ScaleFactor scaleFactor) {
                this.scaleFactor = scaleFactor;
                return this;
            }

            public Builder setVoltageAtZeroCurrent(Voltage voltageAtZeroCurrent) {
                this.voltageAtZeroCurrent = Voltage.of(voltageAtZeroCurrent.getValue() * 1000);
                return this;
            }

            public Builder setReferenceVoltage(Voltage referenceVoltage) {
                this.referenceVoltage = Voltage.of(referenceVoltage.getValue() * 1000);
                return this;
            }

            public Builder setAdcMaxValue(int adcMaxValue) {
                this.adcMaxValue = adcMaxValue;
                return this;
            }

            public Configuration build() {
                return new Configuration(scaleFactor, voltageAtZeroCurrent, referenceVoltage, adcMaxValue);
            }
        }
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
        public Sensor<ID> build() {
            return new CurrentSensor<>(getIdentifier(), getDetectionFactory(), channel, configuration);
        }
    }
}
