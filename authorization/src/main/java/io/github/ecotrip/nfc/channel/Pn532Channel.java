package io.github.ecotrip.nfc.channel;

import com.pi4j.context.Context;
import com.pi4j.io.spi.SpiBus;
import com.pi4j.io.spi.SpiChipSelect;

import io.github.ecotrip.Generated;
import io.github.ecotrip.nfc.Helpers.CommandStatus;

/**
 * PN532 channel interface
 */
@Generated
public abstract class Pn532Channel {
    public static final byte PN532_PREAMBLE = 0x00;
    public static final byte PN532_STARTCODE1 = 0x00;
    public static final byte PN532_STARTCODE2 = (byte) 0xFF;
    public static final byte PN532_POSTAMBLE = 0x00;
    public static final byte PN532_HOSTTOPN532 = (byte) 0xD4;
    public static final byte PN532_PN532TOHOST = (byte) 0xD5;

    public abstract void begin();

    public abstract void wakeup();

    public abstract CommandStatus writeCommand(byte[] header, byte[] body) throws InterruptedException;

    public abstract CommandStatus writeCommand(byte[] header) throws InterruptedException;

    public abstract int readResponse(byte[] buffer, int expectedLength, int timeout) throws InterruptedException;

    public abstract int readResponse(byte[] buffer, int expectedLength) throws InterruptedException;

    public static Pn532Channel createSpi(Context ctx, SpiBus bus, SpiChipSelect chip) {
        return new Pn532Spi(ctx, bus, chip);
    }
}
