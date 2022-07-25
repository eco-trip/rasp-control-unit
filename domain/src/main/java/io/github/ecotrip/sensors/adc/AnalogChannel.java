package io.github.ecotrip.sensors.adc;

public enum AnalogChannel {
    A0_IN(0b0100000000000000),
    A1_IN(0b0101000000000000),
    A2_IN(0b0110000000000000),
    A3_IN(0b0111000000000000);

    private final int value;
    AnalogChannel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
