package adapter;

import adapter.builder.SensorBuilder;
import com.pi4j.io.i2c.I2C;
import io.github.ecotrip.measures.Measure;
import io.github.ecotrip.measures.ambient.Brightness;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class BrightnessSensor<ID> extends Sensor<ID> {
    private static final Logger LOG = LoggerFactory.getLogger(BrightnessSensor.class);
    private final I2C channel;

    private BrightnessSensor(final ID identifier, final DetectionFactory<ID> detectionFactory,
                             final I2C channel) {
        super(identifier, detectionFactory);
        this.channel = channel;
        LOG.info("BrightnessSensor Connected to i2c bus={} address={}. OK.", channel.bus(), channel.device());
        init();
    }

    @Override
    protected CompletableFuture<Measure> measure() {
        return CompletableFuture.supplyAsync(() -> {
            byte[] p = new byte[2];
            channel.read(p, 0, 2);
            int msb = p[0] & 0xff;
            int lsb = p[1] & 0xff;
            LOG.debug("Raw data: msb={} lsb={} p0={} p1={}", msb, lsb, p[0], p[1]);
            return Brightness.of((msb << 8) + lsb);
        });
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
