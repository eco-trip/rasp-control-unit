package adapter;

import adapter.builder.SensorBuilder;
import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.measures.ambient.Humidity;
import io.github.ecotrip.measures.ambient.Temperature;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;
import io.github.ecotrip.sensors.channel.MultiDigitalChannel;
import utils.Execution;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class TemperatureAndHumiditySensor<ID> extends Sensor<ID> {
    private final MultiDigitalChannel channel;
    private static final long ONE_SECOND_IN_MILLIS = 1000;
    private static final long SEVEN_MILLIS_IN_NANOS = 7000000;
    private static final int MAX_HUMIDITY_VALUE = 100;
    private static final int MIN_HUMIDITY_VALUE = 0;
    private static final int MAX_ROOM_TEMPERATURE_VALUE = 40;
    private static final int MIN_ROOM_TEMPERATURE_VALUE = 15;

    protected TemperatureAndHumiditySensor(ID identifier, DetectionFactory<ID> detectionFactory,
                                           MultiDigitalChannel channel) {
        super(identifier, detectionFactory);
        this.channel = channel;
    }

    @Override
    protected CompletableFuture<List<Measure>> measure() {
        return CompletableFuture.supplyAsync(() -> {
            channel.initialize();
            synchronizeWithTheProtocol();
            return detectEdgesAndConvertToMeasures((r, v) -> {
                channel.shutdown();
                return checkReadBitsAndGetMeasures(r, v);
            });
        });
    }

    @Override
    protected boolean isMeasureValid(Measure measure) {
        return measure instanceof Temperature ?
                isTemperatureValid(measure) : isHumidityValid(measure);
    }

    private boolean isTemperatureValid(Measure measure) {
        return measure.getValue() >= MIN_ROOM_TEMPERATURE_VALUE
                && measure.getValue() <= MAX_ROOM_TEMPERATURE_VALUE;
    }

    private boolean isHumidityValid(Measure measure) {
        return measure.getValue() >= MIN_HUMIDITY_VALUE
                && measure.getValue() <= MAX_HUMIDITY_VALUE;
    }

    private void synchronizeWithTheProtocol() {
        channel.sendHigh();
        Execution.safeSleep(16);
        channel.sendHigh();
        channel.switchToInputMode();
    }

    private List<Measure> detectEdgesAndConvertToMeasures(BiFunction<Integer, Long, List<Measure>> afterRead) {
        long now = System.nanoTime();
        MultiDigitalChannel.State state = channel.getState();
        long value = 0;
        long lastHighSignal = now;
        int read = 0;
        while (System.nanoTime() - now < SEVEN_MILLIS_IN_NANOS) {
            MultiDigitalChannel.State next = channel.getState();
            if (state != next) {
                if (next == MultiDigitalChannel.State.HIGH) { //if this is the beginning of a high interval
                    lastHighSignal = System.nanoTime();
                } else { //otherwise end of interval so measure length and store bit
                    boolean isBitOne = (System.nanoTime() - lastHighSignal) / ONE_SECOND_IN_MILLIS > 55;
                    value = (value << 1);
                    if (isBitOne) {
                        value++;
                    }
                    read++;
                    if (read >= 41) {
                        break;
                    }
                }
                state = next;
            }
        }
        return afterRead.apply(read, value);
    }

    private List<Measure> checkReadBitsAndGetMeasures(int read, long value) {
        Optional<List<Measure>> measures = Optional.empty();
        if (read >= 38) {
            int hi = (int) ((value & 0xff00000000L) >> 32), hd = (int) ((value & 0xff000000L) >> 24),
                    ti = (int) ((value & 0xff0000) >> 16), td = (int) ((value & 0xff00) >> 8),
                    cs = (int) (value & 0xff);
            measures = checksumAndGetMeasures(hi, hd, ti, td, cs);
        }
        return measures.orElseGet(List::of);
    }

    private Optional<List<Measure>> checksumAndGetMeasures(int hi, int hd, int ti, int td, int cs) {
        if (cs == ((hi + hd + ti + td) & 0xff)) {
            var temperature = Temperature.of(((((ti & 0x7f) << 8) + td) / 10.) * ((ti & 0x80) != 0 ? -1 : 1));
            var humidity = Humidity.of(((hi << 8) + hd) / 10.);
            return Optional.of(List.of(temperature, humidity));
        }
        return Optional.empty();
    }

    public static class Builder<ID> extends SensorBuilder<ID> {
        private MultiDigitalChannel channel;

        public Builder<ID> setChannel(final MultiDigitalChannel channel) {
            this.channel = channel;
            return this;
        }

        @Override
        public Sensor<ID> build() {
            return new TemperatureAndHumiditySensor<>(getIdentifier(), getDetectionFactory(), channel);
        }
    }
}