package adapter.builder;

import adapter.Pi4jProvider;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.PullResistance;
import io.github.ecotrip.sensors.Address;

public abstract class Pi4JDigitalBuilder<ID, U> {
    private final Context ctx;
    private Pi4jProvider provider;
    private Address address;
    private ID identifier;
    private PullResistance pullResistance;

    public Pi4JDigitalBuilder(final Context ctx) {
        this.ctx = ctx;
    }

    public Pi4JDigitalBuilder<ID, U> setAddress(final Address address) {
        this.address = address;
        return this;
    }

    public Pi4JDigitalBuilder<ID, U> setProvider(final Pi4jProvider provider) {
        this.provider = provider;
        return this;
    }

    public Pi4JDigitalBuilder<ID, U> setIdentifier(final ID identifier) {
        this.identifier = identifier;
        return this;
    }

    public Pi4JDigitalBuilder<ID, U> setPullResistance(PullResistance pullResistance) {
        this.pullResistance = pullResistance;
        return this;
    }

    protected Context getContext() {
        return ctx;
    }

    protected Pi4jProvider getProvider() {
        return provider;
    }

    protected Address getAddress() {
        return address;
    }

    protected ID getIdentifier() {
        return identifier;
    }

    protected PullResistance getPullResistance() {
        return pullResistance;
    }

    public abstract U build();
}
