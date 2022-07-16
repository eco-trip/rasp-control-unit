package com.pi4j.example;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BH1750 {

    private static final Logger LOG = LoggerFactory.getLogger(BH1750.class);

    public static final int ADDRESS = 0x23;

    private final int address;
    private final Context context;
    private final int i2cBus;
    private final I2C bh1750;
    private final String deviceId;

    public BH1750(Context pi4j, String deviceId) {
        this(pi4j, ADDRESS, 1, deviceId);
    }

    public BH1750(Context pi4j, int address, int i2cBus, String deviceId) {
        this.address = address;
        this.context = pi4j;
        this.i2cBus = i2cBus;
        this.deviceId = deviceId;
        I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
        I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j).id(deviceId).bus(i2cBus).device(address).build();
        bh1750 = i2CProvider.create(i2cConfig);
        LOG.info("BH1750 Connected to i2c bus={} address={}. OK.", i2cBus, address);
        init();
    }

    private void init() {
        bh1750.write((byte) 0x10);
    }

    public int getLightIntensity() {
        byte[] p = new byte[2];
        bh1750.read(p, 0, 2);
        int msb = p[0] & 0xff;
        int lsb = p[1] & 0xff;
        LOG.debug("Raw data: msb={} lsb={} p0={} p1={}", msb, lsb, p[0], p[1]);
        return (msb << 8) + lsb;
    }

    public int getAddress() {
        return address;
    }

    public Context getContext() {
        return context;
    }

    public int getI2CBus() {
        return i2cBus;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void close() throws Exception {
        bh1750.close();
    }

}