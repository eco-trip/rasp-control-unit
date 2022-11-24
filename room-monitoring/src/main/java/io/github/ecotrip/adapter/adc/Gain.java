package io.github.ecotrip.adapter.adc;

import io.github.ecotrip.Generated;

/**
 * ADC Gain
 */
@Generated
public enum Gain {
    GAIN_6_144V(0b0000000000000000, 187.5 / 1_000_000),
    GAIN_4_096V(0b0000001000000000, 125.0 / 1_000_000),
    GAIN_2_048V(0b0000010000000000, 62.5 / 1_000_000),
    GAIN_1_024V(0b0000011000000000, 31.25 / 1_000_000),
    GAIN_0_512V(0b0000100000000000, 15.625 / 1_000_000),
    GAIN_0_256V(0b0000101000000000, 7.8125 / 1_000_000);

    private final int value;
    private final double valuePerByte;

    Gain(int gain, double gainPerByte) {
        this.value = gain;
        this.valuePerByte = gainPerByte;
    }

    public int getValue() {
        return value;
    }

    public double getValuePerByte() {
        return valuePerByte;
    }
}
