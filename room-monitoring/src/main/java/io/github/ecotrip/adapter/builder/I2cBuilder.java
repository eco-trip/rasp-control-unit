package io.github.ecotrip.adapter.builder;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

import io.github.ecotrip.Generated;
import io.github.ecotrip.adapter.Pi4jProvider;

/**
 * Builder for I2C communication interface
 *
 * @param <ID> uniquely identifies the sensor instance.
 */
@Generated
public class I2cBuilder<ID> {
    private final Context ctx;
    private I2CProvider provider;
    private int bus;
    private int address;
    private ID identifier;

    public I2cBuilder(final Context ctx) {
        this.ctx = ctx;
    }

    public I2cBuilder<ID> setBus(final int bus) {
        this.bus = bus;
        return this;
    }

    public I2cBuilder<ID> setPin(final int address) {
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
                .bus(bus)
                .device(address)
                .build();
    }

    public I2C build() {
        return provider.create(createConfiguration());
    }
}
