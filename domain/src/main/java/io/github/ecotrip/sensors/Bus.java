package io.github.ecotrip.sensors;

import java.util.Objects;

public class Bus {
    private final int channel;

    private Bus(final int value) {
        this.channel = value;
    }

    public static Bus of(final int value) {
        return new Bus(value);
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
        Bus bus = (Bus) o;
        return getChannel() == bus.getChannel();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getChannel());
    }
}
