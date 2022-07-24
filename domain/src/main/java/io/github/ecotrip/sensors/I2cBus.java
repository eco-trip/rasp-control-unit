package io.github.ecotrip.sensors;

import java.util.Objects;

public class I2cBus {
    private final int channel;

    private I2cBus(final int value) {
        this.channel = value;
    }

    public static I2cBus of(final int value) {
        return new I2cBus(value);
    }

    public static I2cBus zero() {
        return new I2cBus(0);
    }

    public static I2cBus one() {
        return new I2cBus(1);
    }


    public int getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "Bus{" +
                "value='" + channel + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        I2cBus bus = (I2cBus) o;
        return getChannel() == bus.getChannel();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getChannel());
    }
}
