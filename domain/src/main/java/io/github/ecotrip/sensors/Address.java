package io.github.ecotrip.sensors;

import java.util.Objects;

public class Address {
    private final int value;

    private Address(final int value) {
        this.value = value;
    }

    public static Address of(final int value) {
        return new Address(value);
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Address{" +
                "value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return getValue() == address.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
