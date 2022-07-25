package adapter.builder;

import adapter.Pi4jProvider;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import io.github.ecotrip.sensors.Address;
import io.github.ecotrip.sensors.channel.I2cBus;

public class I2cBuilder<ID> {
    private final Context ctx;
    private I2CProvider provider;
    private I2cBus bus;
    private Address address;
    private ID identifier;

    public I2cBuilder(final Context ctx) {
        this.ctx = ctx;
    }

    public I2cBuilder<ID> setBus(final I2cBus bus) {
        this.bus = bus;
        return this;
    }

    public I2cBuilder<ID> setAddress(final Address address) {
        this.address = address;
        return this;
    }

    public I2cBuilder<ID> setProvider(final Pi4jProvider provider) {
        this.provider = ctx.provider(provider.getValue());
        return this;
    }

    public I2cBuilder<ID> setIdentifier(final ID identifier) {
        this.identifier = identifier;
        return this;
    }

    private I2CConfig createConfiguration() {
        return I2C.newConfigBuilder(ctx)
                .id(identifier.toString())
                .bus(bus.getIdentifier())
                .device(address.getValue())
                .build();
    }

    public I2C build() {
        return provider.create(createConfiguration());
    }
}
