package io.github.ecotrip.nfc.channel;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiBus;
import com.pi4j.io.spi.SpiChipSelect;
import com.pi4j.io.spi.SpiMode;

import io.github.ecotrip.Generated;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.nfc.Helpers.CommandStatus;

/**
 * PN532 SPI channel
 */
@Generated
public class Pn532Spi extends Pn532Channel {
    private static final int ACK_TIMEOUT = 5000;
    private static final int SPICHANNEL = 1;
    private static final int SPISPEED = 1000000;
    private static final byte PN532_SPI_READY = 0x01;
    private static final byte PN532_SPI_STATREAD = 0x02;
    private static final byte PN532_SPI_DATAWRITE = 0x01;
    private static final byte PN532_SPI_DATAREAD = 0x03;
    private static final int OUTPUT = 1;
    private static final int LOW = 0;
    private static final int HIGH = 1;
    private static final int _cs = 8;
    private static final int _rst = 0;

    private byte command;

    private Spi spi;
    private DigitalOutput cs;
    private DigitalOutput rst;
    private final Context context;
    private final SpiBus spiBus;
    private final SpiChipSelect chipSelect;

    Pn532Spi(Context pi4j, SpiBus spiBus, SpiChipSelect chipSelect) {
        this.context = pi4j;
        this.chipSelect = chipSelect;
        this.spiBus = spiBus;
    }

    @Override
    public void begin() {
        /*

        log("Beginning SPI.");

        int j = Gpio.wiringPiSetup();
        log("Wiringpisetup is " + j);
        int fd = Spi.wiringPiSPISetup(SPICHANNEL, SPISPEED);
        log("Wiringpispisetup is " + fd);

        if (fd <= -1) {
            log("SPI Setup failed!");
            throw new RuntimeException("SPI Setup failed!");
        }
        Gpio.pinMode(_cs, OUTPUT);
        */

        var spiConfig = Spi.newConfigBuilder(context)
                .id("SPI" + spiBus + " " + chipSelect)
                .name("pn532_spi")
                .bus(spiBus)
                .chipSelect(chipSelect)
                .channel(1)
                .baud(SPISPEED)
                .mode(SpiMode.MODE_0)
                .provider("pigpio-spi")
                .build();

        spi = context.create(spiConfig);

        var csConfig = DigitalOutput.newConfigBuilder(context)
            .address(_cs)
            .initial(DigitalState.HIGH)
            .provider("pigpio-digital-output");

        cs = context.create(csConfig);

        DigitalOutputConfigBuilder rstConfig = DigitalOutput.newConfigBuilder(context)
            .address(_rst)
            .provider("pigpio-digital-output");

        rst = context.create(rstConfig);
        Execution.logsInfo("PN532 Connected to spi");
    }

    @Override
    public void wakeup() {
        Execution.logsInfo("Waking SPI.");
        /*
        Gpio.digitalWrite(_cs, HIGH);
        Gpio.digitalWrite(rst, HIGH);
        Gpio.digitalWrite(_cs, LOW);
        */
        cs.low();
        rst.high();
        Execution.safeSleep(2);
        cs.high();
    }

    @Override
    public CommandStatus writeCommand(byte[] header, byte[] body) throws InterruptedException {
        Execution.logsInfo("Medium.writeCommand(" + getByteString(header) + " "
            + (body != null ? getByteString(body) : "") + ")");

        command = header[0];

        byte checksum;
        byte cmdlen1;
        byte i;
        byte checksum1;

        byte cmdLen = (byte) header.length;

        cmdLen++;

        cs.low();
        Execution.safeSleep(2);

        writeByte(PN532_SPI_DATAWRITE);

        checksum = (byte) (PN532_PREAMBLE + PN532_STARTCODE1 + PN532_STARTCODE2);
        writeByte(PN532_PREAMBLE);
        writeByte(PN532_STARTCODE1);
        writeByte(PN532_STARTCODE2);

        writeByte(cmdLen);
        cmdlen1 = (byte) (~cmdLen + 1);
        writeByte(cmdlen1);

        writeByte(PN532_HOSTTOPN532);
        checksum += PN532_HOSTTOPN532;

        for (i = 0; i < cmdLen - 1; i++) {
            writeByte(header[i]);
            checksum += header[i];
        }

        checksum1 = (byte) ~checksum;
        writeByte(checksum1);
        writeByte(PN532_POSTAMBLE);
        cs.high();

        return waitForAck(ACK_TIMEOUT);
    }

    @Override
    public CommandStatus writeCommand(byte[] header) throws InterruptedException {
        return writeCommand(header, null);
    }

    @Override
    public int readResponse(byte[] buffer, int expectedLength, int timeout) throws InterruptedException {
        Execution.logsInfo("Medium.readResponse(..., " + expectedLength + ", " + timeout + ")");

        cs.low();
        Execution.safeSleep(2);
        writeByte(PN532_SPI_DATAREAD);

        byte[] first = new byte[] {readByte(), readByte(), readByte()};
        byte length;
        byte comLength;

        if (PN532_PREAMBLE != first[0] || PN532_STARTCODE1 != first[1] || PN532_STARTCODE2 != first[2]) {

            Execution.logsInfo("pn532spi.readResponse bad starting bytes found");

            if (first[0] == PN532_STARTCODE2) {
                length = first[1];
                comLength = length;
                comLength += first[2];

                Execution.logsInfo("pn532spi.readResponse but hey it's ok!");
            } else if (first[0] == PN532_STARTCODE1 && first[1] == PN532_STARTCODE2) {
                length = first[2];
                comLength = length;
                comLength += readByte();

                Execution.logsInfo("pn532spi.readResponse but hey it's ok!");
            } else {
                return -1;
            }
        } else {
            length = readByte();
            comLength = length;
            comLength += readByte();
        }

        if (comLength != 0) {
            Execution.logsInfo("pn532spi.readResponse bad length checksum: " + comLength);
            return -1;
        }

        byte cmd = 1;
        cmd += command;

        if (PN532_PN532TOHOST != readByte() || (cmd) != readByte()) {
            Execution.logsInfo("pn532spi.readResponse bad command check.");
            return -1;
        }

        length -= 2;
        if (length > expectedLength) {
            Execution.logsInfo("pn532spi.readResponse not enough space");
            readByte();
            readByte();
            return -1;
        }

        byte sum = PN532_PN532TOHOST;
        sum += cmd;

        for (int i = 0; i < length; i++) {
            buffer[i] = readByte();
            sum += buffer[i];
        }

        byte checksum = readByte();
        checksum += sum;
        if (0 != checksum) {
            Execution.logsInfo("pn532spi.readResponse bad checksum");
            return -1;
        }

        readByte(); // POSTAMBLE

        cs.high();
        return length;
    }

    @Override
    public int readResponse(byte[] buffer, int expectedLength) throws InterruptedException {
        return readResponse(buffer, expectedLength, 1000);
    }

    private CommandStatus waitForAck(int timeout) throws InterruptedException {
        Execution.logsInfo("Medium.waitForAck()");
        int timer = 0;
        while (readSpiStatus() != PN532_SPI_READY) {
            if (timeout != 0) {
                timer += 10;
                if (timer > timeout) {
                    return CommandStatus.TIMEOUT;
                }
            }
            Execution.safeSleep(10);
        }
        if (!checkSpiAck(timeout)) {
            return CommandStatus.INVALID_ACK;
        }

        timer = 0;

        while (readSpiStatus() != PN532_SPI_READY) {
            if (timeout != 0) {
                timer += 10;
                if (timer > timeout) {
                    return CommandStatus.TIMEOUT;
                }
            }
            Execution.safeSleep(10);
        }
        return CommandStatus.OK;
    }
    //
    //  @Override
    //  public int getOffsetBytes() {
    //      return 7;
    //  }

    private byte readSpiStatus() throws InterruptedException {
        // LOG.info("Medium.readSpiStatus()");
        byte status;

        cs.low();
        Execution.safeSleep(2);
        writeByte(PN532_SPI_STATREAD);
        status = readByte();
        cs.high();
        return status;
    }

    private boolean checkSpiAck(int timeout) {
        Execution.logsInfo("Medium.checkSpiAck()");
        var ackbuff = new byte[6];
        var pn532Ack = new byte[] { 0, 0, (byte) 0xFF, 0, (byte) 0xFF, 0 };

        cs.low();
        Execution.safeSleep(2);
        writeByte(PN532_SPI_DATAREAD);

        int read = spi.read(ackbuff, 0, 6);
        if (read > 0) {
            Execution.logsInfo("pn532i2c.waitForAck Read " + read + " bytes.");
        }

        for (int i = 0; i < ackbuff.length; i++) {
            if (ackbuff[i] != pn532Ack[i]) {
                Execution.logsInfo("pn532i2c.waitForAck Invalid Ack.");
                return false;
            }

            // else
            //  LOG.info("pn532i2c.waitForAck ok.");
        }
        return true;
    }

    private void writeByte(byte byteToWrite) {
        // System.out.println("Medium.write(" + Integer.toHexString(_data) +
        // ")");
        byte[] dataToSend = new byte[1];
        dataToSend[0] = reverseByte(byteToWrite);

        spi.write(dataToSend, 1);
    }

    private byte readByte() {
        Execution.safeSleep(1);
        byte[] data = new byte[1];
        //data[0] = 0;
        spi.read(data, 1);
        //LOG.info("Medium.read dritto() = "+Integer.toHexString((int)(data[0] & 0xff)));
        data[0] = reverseByte(data[0]);
        // LOG.info("Medium.read reversed() = "+Integer.toHexString((int)(data[0] & 0xff)));

        return data[0];
    }

    private String getByteString(byte[] arr) {
        StringBuilder output = new StringBuilder("[");
        for (byte b : arr) {
            output.append(Integer.toHexString(b)).append(" ");
        }
        return output.toString().trim() + "]";
    }

    private byte reverseByte(byte inputByte) {
        byte input = inputByte;
        byte output = 0;
        for (int p = 0; p < 8; p++) {
            if ((input & 0x01) > 0) {
                output |= 1 << (7 - p);
            }
            input = (byte) (input >> 1);
        }
        return output;
    }
}
