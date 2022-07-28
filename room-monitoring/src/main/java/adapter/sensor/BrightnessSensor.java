package adapter.sensor;

import adapter.builder.SensorBuilder;
import com.pi4j.io.i2c.I2C;
import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.measures.ambient.Brightness;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BrightnessSensor<ID> extends Sensor<ID> {
    private final I2C channel;

    private BrightnessSensor(final ID identifier, final DetectionFactory<ID> detectionFactory,
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
            int msb = p[0] & 0xff;
            int lsb = p[1] & 0xff;
            return Brightness.of((msb << 8) + lsb);
        }).thenApply(List::of);
    }

    @Override
    protected boolean isMeasureValid(final Measure measure) {
        return measure.getValue() >= 0;
    }

    private void init() {
        channel.write((byte) 0x10);
    }

    public static class Builder<ID> extends SensorBuilder<ID> {
        private I2C i2c;

        public Builder<ID> setI2C(I2C i2c) {
            this.i2c = i2c;
            return this;
        }

        @Override
        public BrightnessSensor<ID> build() {
            return new BrightnessSensor<>(getIdentifier(), getDetectionFactory(), i2c);
        }
    }
}
