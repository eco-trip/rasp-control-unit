package io.github.ecotrip.sensors.channel;

public enum I2cBus {
    ZERO(0), ONE(0);
    private final int identifier;

    I2cBus(int identifier) {
        this.identifier = identifier;
    }

    public int getIdentifier() {
        return identifier;
    }
}
