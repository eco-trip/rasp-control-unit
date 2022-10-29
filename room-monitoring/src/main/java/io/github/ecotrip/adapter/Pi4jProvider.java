package io.github.ecotrip.adapter;

import io.github.ecotrip.Generated;

/**
 * Enum based on Pi4jProvider strings.
 */
@Generated
public enum Pi4jProvider {
    LINUX_FS_I2C("linuxfs-i2c"),
    PIGPIO_DI("pigpio-digital-input"),
    PIGPIO_MD("pigpio-digital-multi");
    private final String value;

    Pi4jProvider(final String value) {
        this.value = value;
    }

    /**
     * @return the enum value.
     */
    public String getValue() {
        return value;
    }
}
