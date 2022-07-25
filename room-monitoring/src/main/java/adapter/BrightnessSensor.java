package adapter;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import io.github.ecotrip.measures.ambient.Brightness;
import io.github.ecotrip.sensors.Address;
import io.github.ecotrip.sensors.I2cBus;
import io.github.ecotrip.sensors.DetectionFactory;
import io.github.ecotrip.sensors.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class BrightnessSensor<ID> extends Sensor<ID, Integer, Brightness> {
    private static final Logger LOG = LoggerFactory.getLogger(BrightnessSensor.class);
    private final I2C channel;

    private BrightnessSensor(final ID identifier, final DetectionFactory<ID, Brightness> detectionFactory,
                             final I2C channel) {
        super(identifier, detectionFactory);
        this.channel = channel;
        LOG.info("BrightnessSensor Connected to i2c bus={} address={}. OK.", channel.bus(), channel.device());
        init();
    }

    @Override
    protected CompletableFuture<Brightness> measure() {
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
    protected boolean isMeasureValid(final Brightness measure) {
        return measure.getValue() >= 0;
    }

    private void init() {
        channel.write((byte) 0x10);
    }

    public static class Builder<ID> extends SensorBuilder<ID, Integer, Brightness> {
        private final Context ctx;
        private I2CProvider provider;
        private I2cBus bus;
        private Address address;

        public Builder(final Context ctx) {
            this.ctx = ctx;
        }

        public Builder<ID> setBus(final I2cBus bus) {
            this.bus = bus;
            return this;
        }

        public Builder<ID> setAddress(final Address address) {
            this.address = address;
            return this;
        }

        public Builder<ID> setProvider(final Pi4jProvider provider) {
            this.provider = ctx.provider(provider.getValue());
            return this;
        }

        @Override
        public BrightnessSensor<ID> build() {
            final I2CConfig configuration = I2C.newConfigBuilder(ctx)
                    .id(getIdentifier().toString())
                    .bus(bus.getChannel())
                    .device(address.getValue())
                    .build();
            return new BrightnessSensor<>(getIdentifier(), getDetectionFactory(), provider.create(configuration));
        }
    }

}
