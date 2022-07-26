package adapter.builder;

import adapter.Pi4jProvider;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.PullResistance;
import io.github.ecotrip.sensors.Address;

public class DigitalInputBuilder<ID> {
    private final Context ctx;
    private Pi4jProvider provider;
    private Address address;
    private ID identifier;
    private PullResistance pullResistance;

    public DigitalInputBuilder(final Context ctx) {
        this.ctx = ctx;
    }

    public DigitalInputBuilder<ID> setAddress(final Address address) {
        this.address = address;
        return this;
    }

    public DigitalInputBuilder<ID> setProvider(final Pi4jProvider provider) {
        this.provider = provider;
        return this;
    }

    public DigitalInputBuilder<ID> setIdentifier(final ID identifier) {
        this.identifier = identifier;
        return this;
    }

    public DigitalInputBuilder<ID> setPullResistance(PullResistance pullResistance) {
        this.pullResistance = pullResistance;
        return this;
    }

    private DigitalInputConfig createConfiguration() {
        return DigitalInput.newConfigBuilder(ctx)
                .id(identifier.toString())
                .pull(pullResistance)
                .address(address.getValue())
                .provider(provider.getValue())
                .build();
    }

    public DigitalInput build() {
        return ctx.create(createConfiguration());
    }
}