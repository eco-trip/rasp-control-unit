package io.github.ecotrip.nfc;

import java.util.Arrays;

import io.github.ecotrip.Generated;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.nfc.Helpers.CommandStatus;
import io.github.ecotrip.nfc.channel.Pn532Channel;

/**
 * PN532 sensor controller
 */
@Generated
public class Pn532Controller {
    public static final byte PN532_COMMAND_GETFIRMWAREVERSION = 0x02;
    public static final byte PN532_COMMAND_SAMCONFIGURATION = 0x14;
    public static final byte PN532_COMMAND_INLISTPASSIVETARGET = 0x4A;
    public static final byte PN532_COMMAND_INRELEASE = 0x52;
    public static final byte PN532_COMMAND_TGINITASTARGET = (byte) 0x8C;
    public static final byte PN532_COMMAND_TGSETGENERALBYTES = (byte) 0x92;
    public static final byte PN532_COMMAND_TGGETDATA = (byte) 0x86;
    public static final byte PN532_COMMAND_TGSETDATA = (byte) 0x8E;
    public static final byte PN532_COMMAND_TGSETMETADATA = (byte) 0x94;
    public static final byte PN532_COMMAND_TGGETINITIATORCOMMAND = (byte) 0x88;
    public static final byte PN532_COMMAND_TGRESPONSETOINITIATOR = (byte) 0x90;
    public static final byte PN532_COMMAND_TGGETTARGETSTATUS = (byte) 0x8A;
    private static final int RESPONSE_TIMEOUT = 3000;

    private final Pn532Channel channel;
    private final byte[] pn532Packetbuffer;

    private Pn532Controller(Pn532Channel channel) {
        this.channel = channel;
        this.pn532Packetbuffer = new byte[40]; // instead of 64 for iphone compatibility in readResponse()
    }

    public static Pn532Controller of(Pn532Channel channel) {
        return new Pn532Controller(channel);
    }

    /**
     * sensor init
     */
    public void begin() {
        channel.begin();
        channel.wakeup();
    }

    public long getFirmwareVersion() throws InterruptedException {
        long response;

        byte[] command = new byte[1];
        command[0] = PN532_COMMAND_GETFIRMWAREVERSION;

        if (channel.writeCommand(command) != CommandStatus.OK) {
            System.out.print("getFirmwareVersion error!\n");
            return 0;
        }

        // read data packet
        int status = channel.readResponse(pn532Packetbuffer, 12);
        if (status < 0) {
            return 0;
        }

        int offset = 0; //medium.getOffsetBytes();

        response = pn532Packetbuffer[offset];
        response <<= 8;
        response |= pn532Packetbuffer[offset + 1];
        response <<= 8;
        response |= pn532Packetbuffer[offset + 2];
        response <<= 8;
        response |= pn532Packetbuffer[offset + 3];

        return response;
    }

    /**
     * Init the sensor in SAM mode
     * @return success or fail
     */
    public boolean samConfig() throws InterruptedException {
        byte[] command = new byte[4];
        command[0] = PN532_COMMAND_SAMCONFIGURATION;
        command[1] = 0x01; // normal mode;
        command[2] = 0x14; // timeout 50ms * 20 = 1 second
        command[3] = 0x01; // use IRQ pin!

        if (channel.writeCommand(command) != CommandStatus.OK) {
            System.out.print("SAMConfig error!\n");
            return false;
        }

        Arrays.fill(pn532Packetbuffer, (byte) 0);
        int status = channel.readResponse(pn532Packetbuffer, 8);

        System.out.print("SAMConfig read response:\n");

        for (byte b : pn532Packetbuffer) {
            System.out.print(Integer.toHexString((int) (b & 0xff)) + " ");
        }

        System.out.print("\n\n");

        return status > 0;
    }

    /**
     * read passive target identification
     * @param cardbaudrate card speed
     * @param buffer result
     * @return uid length
     * @throws InterruptedException
     */
    public int readPassiveTargetID(byte cardbaudrate, byte[] buffer) throws InterruptedException {
        byte[] command = new byte[3];
        command[0] = PN532_COMMAND_INLISTPASSIVETARGET;
        command[1] = 1; // max 1 cards at once (we can set this to 2 later)
        command[2] = (byte) cardbaudrate;

        if (channel.writeCommand(command) != CommandStatus.OK) {
            return -1; // command failed
        }

        // read data packet
        // if (medium.readResponse(pn532Packetbuffer, pn532Packetbuffer.length) < 0) {
        Arrays.fill(pn532Packetbuffer, (byte) 0);
        if (channel.readResponse(pn532Packetbuffer, 20) < 0) {
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

        if (pn532Packetbuffer[offset] != 1) {
            return -1;
        }
        // int sens_res = pn532Packetbuffer[2];
        // sens_res <<= 8;
        // sens_res |= pn532Packetbuffer[3];

        // DMSG("ATQA: 0x"); DMSG_HEX(sens_res);
        // DMSG("SAK: 0x"); DMSG_HEX(pn532Packetbuffer[4]);
        // DMSG("\n");

        /* Card appears to be Mifare Classic */
        int uidLength = pn532Packetbuffer[offset + 5];

        for (int i = 0; i < uidLength; i++) {
            buffer[i] = pn532Packetbuffer[offset + 6 + i];
        }

        return uidLength;
    }

    /**
     * read data
     * @param cardbaudrate card speed
     * @param buffer result
     * @return uid length
     * @throws InterruptedException
     */
    public int readDataPacket(byte cardbaudrate, byte[] buffer) throws InterruptedException {
        byte[] command = new byte[3];
        command[0] = PN532_COMMAND_INLISTPASSIVETARGET;
        command[1] = 1; // max 1 cards at once (we can set this to 2 later)
        command[2] = (byte) cardbaudrate;

        if (channel.writeCommand(command) != CommandStatus.OK) {
            return -1; // command failed
        }

        // read data packet
        Arrays.fill(pn532Packetbuffer, (byte) 0);
        if (channel.readResponse(pn532Packetbuffer, pn532Packetbuffer.length) < 0) {
            // if (medium.readResponse(pn532Packetbuffer, 20) < 0) {
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

        if (pn532Packetbuffer[offset] != 1) {
            return -1;
        }
        // int sens_res = pn532Packetbuffer[2];
        // sens_res <<= 8;
        // sens_res |= pn532Packetbuffer[3];

        // DMSG("ATQA: 0x"); DMSG_HEX(sens_res);
        // DMSG("SAK: 0x"); DMSG_HEX(pn532Packetbuffer[4]);
        // DMSG("\n");

        /* Card appears to be Mifare Classic */
        int uidLength = pn532Packetbuffer[offset + 5];

        for (int i = 0; i < uidLength; i++) {
            buffer[i] = pn532Packetbuffer[offset + 6 + i];
        }

        return uidLength;
    }

    public int inRelease() throws InterruptedException {
        return this.inRelease((byte) 0);
    }

    /**
     * realease comunication
     * @param relevantTarget
     * @return response
     * @throws InterruptedException
     */
    public int inRelease(byte relevantTarget) throws InterruptedException {

        Execution.logsInfo("inRelease!");

        byte[] command = new byte[2];
        command[0] = PN532_COMMAND_INRELEASE;
        command[1] = relevantTarget;

        if (channel.writeCommand(command) != CommandStatus.OK) {
            Execution.logsError("inRelease: writeCommand failed");
            return -1;
        }
        Arrays.fill(pn532Packetbuffer, (byte) 0);
        return channel.readResponse(pn532Packetbuffer, pn532Packetbuffer.length);
    }

    /**
     * Init pn532 as tag emulation mode
     * @param command config
     * @return success
     * @throws InterruptedException
     */
    public boolean tgInitAsTarget(byte[] command) throws InterruptedException {
        if (channel.writeCommand(command) != CommandStatus.OK) {
            Execution.logsError("tgInitAsTarget: writeCommand failed");
            return false;
        }

        Arrays.fill(pn532Packetbuffer, (byte) 0);
        int status = channel.readResponse(pn532Packetbuffer, pn532Packetbuffer.length, 0);
        if (status < 0) {
            Execution.logsError("tgInitAsTarget: readResponse failed -> " + status);
            return false;
        }

        System.out.print("tgInitAsTarget write response:\n");

        for (byte b : pn532Packetbuffer) {
            System.out.print(Integer.toHexString(b & 0xff) + " ");
        }

        System.out.print("\n\n");

        return true;
    }

    /**
     * Init pn532 as tag emulation mode - p2p mode
     * @return success
     * @throws InterruptedException
     */
    public boolean tgInitAsTarget() throws InterruptedException {
        byte[] command = new byte[]{
            PN532_COMMAND_TGINITASTARGET,
            0,
            0x00, 0x00, // SENS_RES
            0x00, 0x00, 0x00, // NFCID1
            0x40, // SEL_RES

            0x01, (byte) 0xFE, 0x0F, (byte) 0xBB, (byte) 0xBA, (byte) 0xA6, (byte) 0xC9, (byte) 0x89, // POL_RES
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xFF,

            // NFCID3t: Change this to desired value
            0x01, (byte) 0xFE, 0x0F, (byte) 0xBB, (byte) 0xBA, (byte) 0xA6, (byte) 0xC9, (byte) 0x89,
            0x00, 0x00,

            // LLCP magic number, version parameter and MIUX
            0x0a, 0x46, 0x66, 0x6D, 0x01, 0x01, 0x10, 0x02, 0x02, 0x00, (byte) 0x80,
            0x00
        };

        return this.tgInitAsTarget(command);
    }

    /**
     * get data from connected device
     * @param buffer data
     * @return length
     * @throws InterruptedException
     */
    public int tgGetData(byte[] buffer) throws InterruptedException {
        byte[] command = new byte[1];
        command[0] = PN532_COMMAND_TGGETDATA;

        if (channel.writeCommand(command) != CommandStatus.OK) {
            Execution.logsError("tgGetData: writeCommand failed");
            return -1;
        }

        Arrays.fill(pn532Packetbuffer, (byte) 0);
        int status = channel.readResponse(pn532Packetbuffer, pn532Packetbuffer.length, RESPONSE_TIMEOUT);

        if (0 >= status) {
            Execution.logsError("tgGetData: readResponse failed -> " + status);
            return status;
        }

        int length = status - 1;

        if (pn532Packetbuffer[0] != 0) {
            Execution.logsError("tgGetData: status is not ok -> " + Integer.toHexString(pn532Packetbuffer[0] & 0xff));

            System.out.print("tgGetData read response:\n");

            for (byte b : pn532Packetbuffer) {
                System.out.print(Integer.toHexString(b & 0xff) + " ");
            }

            System.out.print("\n\n");
            return -(int) (pn532Packetbuffer[0] & 0xff);
        }

        System.arraycopy(pn532Packetbuffer, 1, buffer, 0, length);

        return length;
    }

    public boolean tgSetData(byte[] header) throws InterruptedException {
        return this.tgSetData(header, new byte[0]);
    }

    /**
     * send data to connected device
     * @param header data
     * @param body data
     * @return length
     * @throws InterruptedException
     */
    public boolean tgSetData(byte[] header, byte[] body) throws InterruptedException {

        byte[] command = new byte[1 + header.length];
        command[0] = PN532_COMMAND_TGSETDATA;

        System.arraycopy(header, 0, command, 1, header.length - 1 + 1);

        System.out.print("tgSetData write command:\n");

        for (byte b : command) {
            System.out.print(Integer.toHexString((int) (b & 0xff)) + " ");
        }

        System.out.print("\n\n");

        if (body.length > 0 && channel.writeCommand(command, body) != CommandStatus.OK) {
            Execution.logsError("tgSetData: writeCommand failed");
            return false;
        } else if (channel.writeCommand(command) != CommandStatus.OK) {
            Execution.logsError("tgSetData: writeCommand failed");
            return false;
        }

        Arrays.fill(pn532Packetbuffer, (byte) 0);
        if (0 > channel.readResponse(pn532Packetbuffer, pn532Packetbuffer.length, 3000)) {
            Execution.logsError("tgSetData: readResponse failed");
            return false;
        }

        if (0 != pn532Packetbuffer[0]) {
            Execution.logsError("tgSetData: status is not ok -> " + pn532Packetbuffer[0]);
            return false;
        }

        System.out.print("tgSetData write response:\n");

        for (byte b : pn532Packetbuffer) {
            System.out.print(Integer.toHexString((int) (b & 0xff)) + " ");
        }

        System.out.print("\n\n");

        return true;

    }
}
