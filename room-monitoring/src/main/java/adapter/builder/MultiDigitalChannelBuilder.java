package adapter.builder;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalMode;
import com.pi4j.io.gpio.digital.DigitalMultipurpose;
import com.pi4j.io.gpio.digital.DigitalMultipurposeConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import io.github.ecotrip.sensors.channel.MultiDigitalChannel;

public class MultiDigitalChannelBuilder<ID> extends Pi4JDigitalBuilder<ID, MultiDigitalChannel<ID>> {
    private DigitalMode mode;
    private MultiDigitalChannel.State initialState;

    public MultiDigitalChannelBuilder(Context ctx) {
        super(ctx);
    }

    @Override
    public MultiDigitalChannel<ID> build() {
        var digital = getContext().create(loadConfiguration());
        return new MultiDigitalChannel<>(getIdentifier()) {
            @Override
            public void initialize() {
                digital.initialize(getContext());
            }

            @Override
            public void shutdown() {
                digital.shutdown(getContext());
            }

            @Override
            public void sendHigh() {
                digital.high();
            }

            @Override
            public void sendLow() {
                digital.low();
            }

            @Override
            public void switchToInputMode() {
                digital.mode(DigitalMode.INPUT);
            }

            @Override
            public void switchToOutputMode() {
                digital.mode(DigitalMode.OUTPUT);
            }

            @Override
            public State getState() {
                return digital.state().equals(DigitalState.HIGH) ? State.HIGH : State.LOW;
            }
        };
    }

    public MultiDigitalChannelBuilder<ID> setMode(DigitalMode mode) {
        this.mode = mode;
        return this;
    }

    public MultiDigitalChannelBuilder<ID> setInitialState(MultiDigitalChannel.State initialState) {
        this.initialState = initialState;
        return this;
    }

    private DigitalMultipurposeConfigBuilder loadConfiguration() {
        return DigitalMultipurpose.newConfigBuilder(getContext())
                .id(getIdentifier().toString())
                .name(getIdentifier().toString())
                .address(getAddress().getValue())
                .mode(mode)
                .initial(initialState.equals(MultiDigitalChannel.State.HIGH)
                        ? DigitalState.HIGH : DigitalState.LOW)
                .provider(getProvider().getValue());
    }
}
