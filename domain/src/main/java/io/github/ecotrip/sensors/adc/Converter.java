package io.github.ecotrip.sensors.adc;

import io.github.ecotrip.measures.energy.Voltage;
import io.github.ecotrip.sensors.Address;

import java.util.concurrent.CompletableFuture;

public abstract class Converter {
    private final Gain gain;
    private final Address configRegister;
    private final Address conversionRegister;

    protected Converter(Gain gain, Address configRegister, Address conversionRegister) {
        this.gain = gain;
        this.configRegister = configRegister;
        this.conversionRegister = conversionRegister;
    }

    public CompletableFuture<Voltage> getData(final AnalogChannel channel) {
        return readIn(calculateConfig(channel.getValue()))
                .thenApply(v -> Voltage.of(v * gain.getValuePerByte()));
    }

    protected abstract CompletableFuture<Integer> readIn(int config);

    private int calculateConfig(int pinId) {
        return configRegister.getValue() | gain.getValue() | pinId;
    }

    public Address getConfigRegister() {
        return configRegister;
    }

    public Address getConversionRegister() {
        return conversionRegister;
    }
}
