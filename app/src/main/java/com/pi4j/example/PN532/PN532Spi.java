package com.pi4j.example.PN532;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.spi.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PN532Spi implements IPN532Interface {

	private static final Logger LOG = LoggerFactory.getLogger(PN532Spi.class);

	static final int SPICHANNEL = 1;
	static final int SPISPEED = 1000000;

	static final byte PN532_SPI_READY = 0x01;
	static final byte PN532_SPI_STATREAD = 0x02;
	static final byte PN532_SPI_DATAWRITE = 0x01;
	static final byte PN532_SPI_DATAREAD = 0x03;
	
	static final int OUTPUT = 1;

	static final int LOW = 0;
	static final int HIGH = 1;

	static final int _cs = 8;
	static final int _rst = 0;
	
	private byte command;

	private Spi spi;
	private DigitalOutput cs;
	private DigitalOutput rst;
	private Context context;
    private SpiBus spiBus;
	private SpiChipSelect chipSelect;

	private boolean debug = false;

	public PN532Spi(Context pi4j) {
        this(pi4j,SpiBus.BUS_0,SpiChipSelect.CS_1);
    }

    public PN532Spi(Context pi4j, SpiBus spiBus, SpiChipSelect chipSelect) {
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

		SpiConfig spiConfig = Spi.newConfigBuilder(context)
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

		DigitalOutputConfigBuilder csConfig = DigitalOutput.newConfigBuilder(context)
		.address(_cs)
		.initial(DigitalState.HIGH)
		.provider("pigpio-digital-output");

		cs = context.create(csConfig);

		DigitalOutputConfigBuilder rstConfig = DigitalOutput.newConfigBuilder(context)
			.address(_rst)
			.provider("pigpio-digital-output");

		rst = context.create(rstConfig);
        LOG.info("PN532 Connected to spi");
	}

	@Override
	public void wakeup() {
		LOG.info("Waking SPI.");
		/*
		Gpio.digitalWrite(_cs, HIGH);
		Gpio.digitalWrite(rst, HIGH);
		Gpio.digitalWrite(_cs, LOW);
		*/
		cs.low();
		rst.high();
		delay(2);
		cs.high();
	}

	@Override
	public CommandStatus writeCommand(byte[] header, byte[] body) throws InterruptedException {

		LOG.info("Medium.writeCommand(" + getByteString(header) + " " + (body != null ? getByteString(body) : "") + ")");
		
		command = header[0];
		
		byte checksum;
		byte cmdlen_1;
		byte i;
		byte checksum_1;

		byte cmd_len = (byte) header.length;

		cmd_len++;

		cs.low();
		delay(2); 

		writeByte(PN532_SPI_DATAWRITE); 

		checksum = PN532_PREAMBLE + PN532_STARTCODE1 + PN532_STARTCODE2;
		writeByte(PN532_PREAMBLE); 
		writeByte(PN532_STARTCODE1); 
		writeByte(PN532_STARTCODE2); 

		writeByte(cmd_len); 
		cmdlen_1 = (byte) (~cmd_len + 1);
		writeByte(cmdlen_1); 

		writeByte(PN532_HOSTTOPN532); 
		checksum += PN532_HOSTTOPN532;

		for (i = 0; i < cmd_len - 1; i++) {
			writeByte(header[i]);
			checksum += header[i];
		}

		checksum_1 = (byte) ~checksum;
		writeByte(checksum_1);
		writeByte(PN532_POSTAMBLE);
		cs.high();

		return waitForAck(5000);
	}

	@Override
	public CommandStatus writeCommand(byte[] header) throws InterruptedException {
		return writeCommand(header, null);
	}

	@Override
	public int readResponse(byte[] buffer, int expectedLength, int timeout) throws InterruptedException {
		LOG.info("Medium.readResponse(..., " + expectedLength + ", " + timeout + ")");

		cs.low();
		delay(2);
		writeByte(PN532_SPI_DATAREAD);

		byte[] first = new byte[] {readByte(),readByte(),readByte()};
		byte length;
		byte com_length;

		if (PN532_PREAMBLE != first[0]  || PN532_STARTCODE1 != first[1] || PN532_STARTCODE2 != first[2]) {

			LOG.info("pn532spi.readResponse bad starting bytes found");

			if (first[0] == PN532_STARTCODE2)
			{
				length=first[1];
				com_length = length;
				com_length += first[2];

				LOG.info("pn532spi.readResponse but hey it's ok!");
			}
			else if (first[0] == PN532_STARTCODE1 && first[1] == PN532_STARTCODE2)
			{
				length=first[2];
				com_length = length;
				com_length += readByte();

				LOG.info("pn532spi.readResponse but hey it's ok!");
			}
			else
				return -1;
		}
		else
		{
			length = readByte();
			com_length = length;
			com_length += readByte();
		}

		if (com_length != 0) {
			LOG.info("pn532spi.readResponse bad length checksum: "+com_length);
			return -1;
		}

		byte cmd = 1;
		cmd += command;

		if (PN532_PN532TOHOST != readByte() || (cmd) != readByte()) {
			LOG.info("pn532spi.readResponse bad command check.");
			return -1;
		}

		length -= 2;
		if (length > expectedLength) {
			LOG.info("pn532spi.readResponse not enough space");
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
			LOG.info("pn532spi.readResponse bad checksum");
			return -1;
		}
		
		readByte(); //POSTAMBLE
    
		cs.high();
		
		return length;
	}

	@Override
	public int readResponse(byte[] buffer, int expectedLength) throws InterruptedException {
		return readResponse(buffer, expectedLength, 1000);
	}

	private CommandStatus waitForAck(int timeout) throws InterruptedException {
		LOG.info("Medium.waitForAck()");

		int timer = 0;
		while (readSpiStatus() != PN532_SPI_READY) {
			if (timeout != 0) {
				timer += 10;
				if (timer > timeout) {
					return CommandStatus.TIMEOUT;
				}
			}
			delay(10);
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
			delay(10);
		}
		return CommandStatus.OK; 
	}
//	
//	@Override
//	public int getOffsetBytes() {
//	  return 7;
//	}

	private byte readSpiStatus() throws InterruptedException {
		// LOG.info("Medium.readSpiStatus()");
		byte status;

		cs.low();
		delay(2);
		writeByte(PN532_SPI_STATREAD);
		status = readByte();
		cs.high();
		return status;
	}

	private boolean checkSpiAck(int timeout) throws InterruptedException {
		LOG.info("Medium.checkSpiAck()");
		byte ackbuff[] = new byte[6];
		byte PN532_ACK[] = new byte[] { 0, 0, (byte) 0xFF, 0, (byte) 0xFF, 0 };

		cs.low();
		delay(2);
		writeByte(PN532_SPI_DATAREAD);

		int read = spi.read(ackbuff, 0, 6);
		if (read > 0) {
			LOG.info("pn532i2c.waitForAck Read " + read + " bytes.");
		}
		
		for (int i = 0; i < ackbuff.length; i++) {
			if (ackbuff[i] != PN532_ACK[i]) {
				LOG.info("pn532i2c.waitForAck Invalid Ack.");
				return false;
			}
			// else 
			//	LOG.info("pn532i2c.waitForAck ok.");
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
	  	delay(1);
		byte[] data = new byte[1];
		data[0] = 0;
		spi.read(data, 1);
		//LOG.info("Medium.read dritto() = "+Integer.toHexString((int)(data[0] & 0xff)));
		data[0] = reverseByte(data[0]);
		// LOG.info("Medium.read reversed() = "+Integer.toHexString((int)(data[0] & 0xff)));
		
		return data[0];
	}

	private String getByteString(byte[] arr) {
		String output = "[";
		for (int i = 0; i < arr.length; i++) {
			output += Integer.toHexString(arr[i]) + " ";
		}
		return output.trim() + "]";
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
	
	private static void delay(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

}