package adapter.builder;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;

public class DigitalInputBuilder<ID> extends Pi4JDigitalBuilder<ID, DigitalInput> {
    public DigitalInputBuilder(Context ctx) {
        super(ctx);
    }

    private DigitalInputConfig loadConfiguration() {
        return DigitalInput.newConfigBuilder(getContext())
                .id(getIdentifier().toString())
                .pull(getPullResistance())
                .address(getAddress().getValue())
                .provider(getProvider().getValue())
                .build();
    }

    public DigitalInput build() {
        return getContext().create(loadConfiguration());
    }
}