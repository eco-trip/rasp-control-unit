package io.github.ecotrip.sensors.channel;

import io.github.ecotrip.Entity;

public abstract class MultiDigitalChannel<ID> extends Entity<ID> {
    protected MultiDigitalChannel(ID identifier) {
        super(identifier);
    }

    public abstract void initialize();

    public abstract void shutdown();

    public abstract void sendHigh();

    public abstract void sendLow();

    public abstract void switchToInputMode();

    public abstract void switchToOutputMode();

    public abstract State getState();

    public enum State {
        HIGH, LOW;
    }
}
