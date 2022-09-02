package io.github.ecotrip.adapter.sensor;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.pi4j.io.i2c.I2C;

import io.github.ecotrip.adapter.builder.SensorBuilder;
import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.measure.ambient.Brightness;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.sensor.Sensor;

/**
 * Ambient light sensor, implementation for BH1750 model.
 * @param <ID>
 */
public class BH1750Sensor<ID> extends Sensor<ID> {
    private final I2C channel;

    private BH1750Sensor(final ID identifier, final DetectionFactory<ID> detectionFactory,
                         final I2C channel) {
        super(identifier, detectionFactory);
        this.channel = channel;
        init();
    }

    @Override
    protected CompletableFuture<List<Measure>> measure() {
        return CompletableFuture.supplyAsync(() -> {
            byte[] p = new byte[2];
            channel.read(p, 0, 2);
            System.out.println(Arrays.toString(Base64.getEncoder().encode(p)));
            int msb = p[0] & 0xff;
            int lsb = p[1] & 0xff;
            return Brightness.of((msb << 8) + lsb);
        }).thenApply(List::of);
    }

    @Override
    protected boolean isMeasureValid(final Measure measure) {
        System.out.println(measure);
        return measure.getValue() >= 0;
    }

    private void init() {
        channel.write((byte) 0x10);
    }

    /**
     * Public entrypoint for {@link BH1750Sensor} construction.
     * @param <ID>
     */
    public static class Builder<ID> extends SensorBuilder<ID> {
        private I2C i2c;

        public Builder<ID> setI2C(I2C i2c) {
            this.i2c = i2c;
            return this;
        }

        @Override
        public BH1750Sensor<ID> build() {
            return new BH1750Sensor<>(getIdentifier(), getDetectionFactory(), i2c);
        }
    }
}
