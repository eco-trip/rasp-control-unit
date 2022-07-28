package com.pi4j.example.PN532;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PN532 {

	private static final Logger LOG = LoggerFactory.getLogger(PN532.class);

	static final byte PN532_COMMAND_GETFIRMWAREVERSION 	  = 0x02;
	static final byte PN532_COMMAND_SAMCONFIGURATION 	  = 0x14;
	static final byte PN532_COMMAND_INLISTPASSIVETARGET   = 0x4A;
	static final byte PN532_COMMAND_INRELEASE 			  = 0x52;
	public static final byte PN532_COMMAND_TGINITASTARGET = (byte) 0x8C;
	static final byte PN532_COMMAND_TGSETGENERALBYTES     = (byte) 0x92;
	static final byte PN532_COMMAND_TGGETDATA             = (byte) 0x86;
	static final byte PN532_COMMAND_TGSETDATA             = (byte) 0x8E;
	static final byte PN532_COMMAND_TGSETMETADATA         = (byte) 0x94;
	static final byte PN532_COMMAND_TGGETINITIATORCOMMAND = (byte) 0x88;
	static final byte PN532_COMMAND_TGRESPONSETOINITIATOR = (byte) 0x90;
	static final byte PN532_COMMAND_TGGETTARGETSTATUS     = (byte) 0x8A;

	private IPN532Interface medium;
	private byte[] pn532_packetbuffer;

	public PN532(IPN532Interface medium) {
		this.medium = medium;
		this.pn532_packetbuffer = new byte[40]; // instead of 64 for iphone compatibility in readResponse()
	}

	public void begin() {
		medium.begin();
		medium.wakeup();
	}

	public long getFirmwareVersion() throws InterruptedException {
		long response;

		byte[] command = new byte[1];
		command[0] = PN532_COMMAND_GETFIRMWAREVERSION;

		if (medium.writeCommand(command) != CommandStatus.OK) {
			System.out.print("getFirmwareVersion error!\n");
			return 0;
		}

		// read data packet
		int status = medium.readResponse(pn532_packetbuffer, 12);
		if (status < 0) {
			return 0;
		}
		
		int offset = 0; //medium.getOffsetBytes();

		response = pn532_packetbuffer[offset + 0];
		response <<= 8;
		response |= pn532_packetbuffer[offset + 1];
		response <<= 8;
		response |= pn532_packetbuffer[offset + 2];
		response <<= 8;
		response |= pn532_packetbuffer[offset + 3];

		return response;
	}

	public boolean SAMConfig() throws InterruptedException {
		byte[] command = new byte[4];
		command[0] = PN532_COMMAND_SAMCONFIGURATION;
		command[1] = 0x01; // normal mode;
		command[2] = 0x14; // timeout 50ms * 20 = 1 second
		command[3] = 0x01; // use IRQ pin!

		if (medium.writeCommand(command) != CommandStatus.OK) {
			System.out.print("SAMConfig error!\n");
			return false;
		}

		Arrays.fill(pn532_packetbuffer, (byte)0);
		int status = medium.readResponse(pn532_packetbuffer, 8);

		System.out.print("SAMConfig read response:\n");

		for (int x=0;x<pn532_packetbuffer.length;x++)
		{
			System.out.print(Integer.toHexString((int)(pn532_packetbuffer[x] & 0xff))+" ");
		}

		System.out.print("\n\n");

		return status > 0;
	}

	public int readPassiveTargetID(byte cardbaudrate, byte[] buffer) throws InterruptedException {
		byte[] command = new byte[3];
		command[0] = PN532_COMMAND_INLISTPASSIVETARGET;
		command[1] = 1; // max 1 cards at once (we can set this to 2 later)
		command[2] = (byte) cardbaudrate;

		if (medium.writeCommand(command) != CommandStatus.OK) {
			return -1; // command failed
		}

		// read data packet
//		if (medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length) < 0) {
		Arrays.fill(pn532_packetbuffer, (byte)0);
		if (medium.readResponse(pn532_packetbuffer, 20) < 0) {
			return -1;
		}

		// check some basic stuff
		/*
		 * ISO14443A card response should be in the following format:
		 * 
		 * byte Description -------------
		 * ------------------------------------------ b0 Tags Found b1 Tag
		 * Number (only one used in this example) b2..3 SENS_RES b4 SEL_RES b5
		 * NFCID Length b6..NFCIDLen NFCID
		 */
		
		int offset = 0; //medium.getOffsetBytes();

		if (pn532_packetbuffer[offset + 0] != 1) {
			return -1;
		}
		// int sens_res = pn532_packetbuffer[2];
		// sens_res <<= 8;
		// sens_res |= pn532_packetbuffer[3];

		// DMSG("ATQA: 0x"); DMSG_HEX(sens_res);
		// DMSG("SAK: 0x"); DMSG_HEX(pn532_packetbuffer[4]);
		// DMSG("\n");

		/* Card appears to be Mifare Classic */
		int uidLength = pn532_packetbuffer[offset + 5];

		for (int i = 0; i < uidLength; i++) {
			buffer[i] = pn532_packetbuffer[offset + 6 + i];
		}

		return uidLength;
	}

	public int readDataPacket(byte cardbaudrate, byte[] buffer) throws InterruptedException {
		byte[] command = new byte[3];
		command[0] = PN532_COMMAND_INLISTPASSIVETARGET;
		command[1] = 1; // max 1 cards at once (we can set this to 2 later)
		command[2] = (byte) cardbaudrate;

		if (medium.writeCommand(command) != CommandStatus.OK) {
			return -1; // command failed
		}

		// read data packet
		Arrays.fill(pn532_packetbuffer, (byte)0);
		if (medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length) < 0) {
//		if (medium.readResponse(pn532_packetbuffer, 20) < 0) {
			return -1;
		}

		// check some basic stuff
		/*
		 * ISO14443A card response should be in the following format:
		 *
		 * byte Description -------------
		 * ------------------------------------------ b0 Tags Found b1 Tag
		 * Number (only one used in this example) b2..3 SENS_RES b4 SEL_RES b5
		 * NFCID Length b6..NFCIDLen NFCID
		 */

		int offset = 0; //medium.getOffsetBytes();

		if (pn532_packetbuffer[offset + 0] != 1) {
			return -1;
		}
		// int sens_res = pn532_packetbuffer[2];
		// sens_res <<= 8;
		// sens_res |= pn532_packetbuffer[3];

		// DMSG("ATQA: 0x"); DMSG_HEX(sens_res);
		// DMSG("SAK: 0x"); DMSG_HEX(pn532_packetbuffer[4]);
		// DMSG("\n");

		/* Card appears to be Mifare Classic */
		int uidLength = pn532_packetbuffer[offset + 5];

		for (int i = 0; i < uidLength; i++) {
			buffer[i] = pn532_packetbuffer[offset + 6 + i];
		}

		return uidLength;
	}

	public int inRelease() throws InterruptedException {
		return this.inRelease((byte) 0);
	}

	public int inRelease(byte relevantTarget) throws InterruptedException {

		LOG.info("inRelease!");

		byte[] command = new byte[2];
		command[0] = PN532_COMMAND_INRELEASE;
		command[1] = relevantTarget;

		if (medium.writeCommand(command) != CommandStatus.OK) {
			LOG.error("inRelease: writeCommand failed");
			return -1; 
		}
		Arrays.fill(pn532_packetbuffer, (byte)0);
		return medium.readResponse(pn532_packetbuffer,pn532_packetbuffer.length);
	}

	public boolean tgInitAsTarget(byte[] command) throws InterruptedException {
		if (medium.writeCommand(command) != CommandStatus.OK) {
			LOG.error("tgInitAsTarget: writeCommand failed");
			return false; 
		}
		
		Arrays.fill(pn532_packetbuffer, (byte)0);
		int status = medium.readResponse(pn532_packetbuffer,pn532_packetbuffer.length,0);
		if (status < 0) {
			LOG.error("tgInitAsTarget: readResponse failed -> "+status);
			return false;
		}

		System.out.print("tgInitAsTarget write response:\n");

		for (int x=0;x<pn532_packetbuffer.length;x++)
		{
			System.out.print(Integer.toHexString((int)(pn532_packetbuffer[x] & 0xff))+" ");
		}

		System.out.print("\n\n");

		return true;
	}

	/**
	 * Peer to Peer
	 */
	public boolean tgInitAsTarget() throws InterruptedException {
		byte[] command = new byte[]{
			PN532_COMMAND_TGINITASTARGET,
			0,
			0x00, 0x00,         //SENS_RES
			0x00, 0x00, 0x00,   //NFCID1
			0x40,               //SEL_RES

			0x01, (byte) 0xFE, 0x0F, (byte) 0xBB, (byte) 0xBA, (byte) 0xA6, (byte) 0xC9, (byte) 0x89, // POL_RES
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			(byte) 0xFF, (byte) 0xFF,

			0x01, (byte) 0xFE, 0x0F, (byte) 0xBB, (byte) 0xBA, (byte) 0xA6, (byte) 0xC9, (byte) 0x89, 0x00, 0x00, //NFCID3t: Change this to desired value

			0x0a, 0x46,  0x66, 0x6D, 0x01, 0x01, 0x10, 0x02, 0x02, 0x00, (byte) 0x80, // LLCP magic number, version parameter and MIUX
			0x00
		};

		return this.tgInitAsTarget(command);
	}

	public int tgGetData(byte[] buffer) throws InterruptedException {
		byte[] command = new byte[1];
		command[0] = PN532_COMMAND_TGGETDATA;

		if (medium.writeCommand(command) != CommandStatus.OK) {
			LOG.error("tgGetData: writeCommand failed");
			return -1; 
		}

		Arrays.fill(pn532_packetbuffer, (byte)0);
		int status = medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length, 3000);

		if (0 >= status) {
			LOG.error("tgGetData: readResponse failed -> "+status);
			return status;
		}

		int length = status - 1;

		if (pn532_packetbuffer[0] != 0) {
			LOG.error("tgGetData: status is not ok -> "+Integer.toHexString((int)(pn532_packetbuffer[0] & 0xff)));

			System.out.print("tgGetData read response:\n");

			for (int x=0;x<pn532_packetbuffer.length;x++)
			{
				System.out.print(Integer.toHexString((int)(pn532_packetbuffer[x] & 0xff))+" ");
			}

			System.out.print("\n\n");
			return -(int)(pn532_packetbuffer[0] & 0xff);
		}
		
		for (int i = 0; i < length; i++) {
			buffer[i] = pn532_packetbuffer[i + 1];
		}

		return length;
	}

	public boolean tgSetData(byte[] header) throws InterruptedException {
		return this.tgSetData(header,new byte[0]);
	}

	public boolean tgSetData(byte[] header,byte[] body) throws InterruptedException {
		
		byte[] command = new byte[1 + header.length];
		command[0] = PN532_COMMAND_TGSETDATA;

		for (int i = header.length - 1; i >= 0; i--){
			command[i + 1] = header[i];
		}

		System.out.print("tgSetData write command:\n");

		for (int x=0;x<command.length;x++)
		{
			System.out.print(Integer.toHexString((int)(command[x] & 0xff))+" ");
		}

		System.out.print("\n\n");

		if (body.length>0)
		{
			if (medium.writeCommand(command,body) != CommandStatus.OK) {
				LOG.error("tgSetData: writeCommand failed");
				return false; 
			}
		}
		else
		{
			if (medium.writeCommand(command) != CommandStatus.OK) {
				LOG.error("tgSetData: writeCommand failed");
				return false; 
			}
		}

		Arrays.fill(pn532_packetbuffer, (byte)0);
		if (0 >  medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length, 3000)) {
			LOG.error("tgSetData: readResponse failed");
			return false;
		}

		if (0 != pn532_packetbuffer[0]) {
			LOG.error("tgSetData: status is not ok -> "+pn532_packetbuffer[0]);
			return false;
		}

		System.out.print("tgSetData write response:\n");

		for (int x=0;x<pn532_packetbuffer.length;x++)
		{
			System.out.print(Integer.toHexString((int)(pn532_packetbuffer[x] & 0xff))+" ");
		}

		System.out.print("\n\n");
	
		return true;
	
	}

}