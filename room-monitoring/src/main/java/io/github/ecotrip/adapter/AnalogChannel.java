package io.github.ecotrip.adapter;

import io.github.ecotrip.Generated;

/**
 * Analog Channels available
 */
@Generated
public enum AnalogChannel {
    A0_IN(0b0100000000000000),
    A1_IN(0b0101000000000000),
    A2_IN(0b0110000000000000),
    A3_IN(0b0111000000000000);

    private final int id;

    AnalogChannel(int value) {
        this.id = value;
    }

    public int getId() {
        return id;
    }
}
