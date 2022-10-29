package io.github.ecotrip.adapter.sensor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalMode;
import com.pi4j.io.gpio.digital.DigitalMultipurpose;
import com.pi4j.io.gpio.digital.DigitalState;

import io.github.ecotrip.Generated;
import io.github.ecotrip.adapter.builder.SensorBuilder;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.ambient.Humidity;
import io.github.ecotrip.measure.ambient.Temperature;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.sensor.Sensor;

/**
 * Humidity and temperature sensor implementation for the DHT family.
 *
 * @param <ID>
 */
@Generated
public class DhtSensor<ID> extends Sensor<ID> {
    private static final long ONE_SECOND_IN_MILLIS = 1000;
    private static final long SEVEN_MILLIS_IN_NANOS = 7000000;
    private static final int MAX_HUMIDITY_VALUE = 100;
    private static final int MIN_HUMIDITY_VALUE = 0;
    private static final int MAX_ROOM_TEMPERATURE_VALUE = 40;
    private static final int MIN_ROOM_TEMPERATURE_VALUE = 15;

    private final DigitalMultipurpose channel;
    private final Context ctx;

    protected DhtSensor(ID identifier, DetectionFactory<ID> detectionFactory,
                        DigitalMultipurpose channel, Context ctx) {
        super(identifier, detectionFactory);
        this.channel = channel;
        this.ctx = ctx;
    }

    @Override
    protected CompletableFuture<List<Measure>> measure() {
        return CompletableFuture.runAsync(() -> channel.initialize(ctx))
            .thenCompose(u -> synchronizeWithTheProtocol())
            .thenApplyAsync(u -> detectEdgesAndConvertToMeasures((r, v) -> {
                channel.shutdown(ctx);
                return checkReadBitsAndGetMeasures(r, v);
            }))
            .thenCompose(l -> l.isEmpty() ? measure() : CompletableFuture.completedFuture(l));
    }

    @Override
    protected boolean isMeasureValid(Measure measure) {
        return measure instanceof Temperature
            ? isTemperatureValid(measure)
            : isHumidityValid(measure);
    }

    private boolean isTemperatureValid(Measure measure) {
        return measure.getValue() >= MIN_ROOM_TEMPERATURE_VALUE
            && measure.getValue() <= MAX_ROOM_TEMPERATURE_VALUE;
    }

    private boolean isHumidityValid(Measure measure) {
        return measure.getValue() >= MIN_HUMIDITY_VALUE
            && measure.getValue() <= MAX_HUMIDITY_VALUE;
    }

    private CompletableFuture<Void> synchronizeWithTheProtocol() {
        return CompletableFuture.runAsync(channel::low)
            .thenRun(() -> Execution.safeSleep(16))
            .thenRun(channel::high)
            .thenRun(() -> channel.mode(DigitalMode.INPUT));
    }

    private List<Measure> detectEdgesAndConvertToMeasures(BiFunction<Integer, Long, List<Measure>> afterRead) {
        long now = System.nanoTime();
        var state = channel.state();
        long value = 0;
        long lastHighSignal = now;
        int read = 0;
        while (System.nanoTime() - now < SEVEN_MILLIS_IN_NANOS) {
            var next = channel.state();
            if (state != next) {
                if (next == DigitalState.HIGH) { //if this is the beginning of a high interval
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
            int hi = (int) ((value & 0xff00000000L) >> 32);
            int hd = (int) ((value & 0xff000000L) >> 24);
            int ti = (int) ((value & 0xff0000) >> 16);
            int td = (int) ((value & 0xff00) >> 8);
            int cs = (int) (value & 0xff);
            measures = checksumAndGetMeasures(hi, hd, ti, td, cs);
        }
        return measures.orElseGet(List::of);
    }

    private Optional<List<Measure>> checksumAndGetMeasures(int hi, int hd, int ti, int td, int cs) {
        if (cs == ((hi + hd + ti + td) & 0xff)) {
            var value1 = ((((ti & 0x7f) << 8) + td) / 10.) * ((ti & 0x80) != 0 ? -1 : 1);
            var value2 = ((hi << 8) + hd) / 10.;
            return Optional.of(List.of(Temperature.of(value1, Temperature.Environment.ROOM), Humidity.of(value2)));
        }
        return Optional.empty();
    }

    /**
     * Public entrypoint for {@link DhtSensor} construction.
     *
     * @param <ID> uniquely identifies the sensor instance.
     */
    @Generated
    public static class Builder<ID> extends SensorBuilder<ID> {
        private final Context ctx;
        private DigitalMultipurpose channel;

        public Builder(Context ctx) {
            this.ctx = ctx;
        }

        public Builder<ID> setChannel(final DigitalMultipurpose channel) {
            this.channel = channel;
            return this;
        }

        @Override
        public Sensor<ID> build() {
            return new DhtSensor<>(getIdentifier(), getDetectionFactory(), channel, ctx);
        }
    }
}
