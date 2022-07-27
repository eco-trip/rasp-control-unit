package adapter;

public enum Pi4jProvider {
    LINUX_FS_I2C("linuxfs-i2c"),
    PIGPIO_DI("pigpio-digital-input"),
    PIGPIO_MD("pigpio-digital-multi");
    private final String value;

    Pi4jProvider(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
