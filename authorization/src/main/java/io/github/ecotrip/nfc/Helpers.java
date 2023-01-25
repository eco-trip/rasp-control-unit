package io.github.ecotrip.nfc;

import java.io.ByteArrayOutputStream;
// import java.io.FileOutputStream;
import java.io.IOException;

import io.github.ecotrip.Generated;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.nfc.ndef.NdefMessage;
import io.github.ecotrip.nfc.ndef.NdefRecord;

/**
 * PN532 helpers
 */
@Generated
public class Helpers {
    // Helpers APDU
    public static final byte C_APDU_CLA = 0;
    public static final byte C_APDU_INS = 1; // instruction
    public static final byte C_APDU_P1 = 2; // parameter 1
    public static final byte C_APDU_P2 = 3; // parameter 2
    public static final byte C_APDU_LC = 4; // length command
    public static final byte C_APDU_DATA = 5;

    public static final byte C_APDU_P1_SELECT_BY_ID = 0x00;
    public static final byte C_APDU_P1_SELECT_BY_NAME = 0x04;

    // Response APDU
    public static final byte R_APDU_SW1_COMMAND_COMPLETE = (byte) 0x90;
    public static final byte R_APDU_SW2_COMMAND_COMPLETE = 0x00;

    public static final byte R_APDU_SW1_NDEF_TAG_NOT_FOUND = 0x6a;
    public static final byte R_APDU_SW2_NDEF_TAG_NOT_FOUND = (byte) 0x82;

    public static final byte R_APDU_SW1_FUNCTION_NOT_SUPPORTED = 0x6A;
    public static final byte R_APDU_SW2_FUNCTION_NOT_SUPPORTED = (byte) 0x81;

    public static final byte R_APDU_SW1_MEMORY_FAILURE = 0x65;
    public static final byte R_APDU_SW2_MEMORY_FAILURE = (byte) 0x81;

    public static final byte R_APDU_SW1_END_OF_FILE_BEFORE_REACHED_LE_BYTES = 0x62;
    public static final byte R_APDU_SW2_END_OF_FILE_BEFORE_REACHED_LE_BYTES = (byte) 0x82;

    // ISO7816-4 commands
    public static final byte ISO7816_SELECT_FILE = (byte) 0xA4;
    public static final byte ISO7816_READ_BINARY = (byte) 0xB0;
    public static final byte ISO7816_UPDATE_BINARY = (byte) 0xD6;

    /**
     * Response Commands
     */
    @Generated
    public enum ResponseCommand {
        COMMAND_COMPLETE,
        TAG_NOT_FOUND,
        FUNCTION_NOT_SUPPORTED,
        MEMORY_FAILURE,
        END_OF_FILE_BEFORE_REACHED_LE_BYTES
    }

    /**
     * Tag mode
     */
    @Generated
    public enum TagFile {
        NONE,
        CC,
        NDEF
    }

    /**
     * Command Status
     */
    @Generated
    public enum CommandStatus {
        OK, TIMEOUT, INVALID_ACK
    }

    public static final byte PN532_MIFARE_ISO14443A = 0x00;

    public static final byte[] TG_INIT_AS_TARGET_CMD = new byte[] {
        Pn532Controller.PN532_COMMAND_TGINITASTARGET,
        5, // MODE: PICC only, Passive only

        0x04, 0x00, // SENS_RES
        0x12, 0x34, 0x56, // NFCID1 -> default 0,0,0
        0x20, // SEL_RES

        // FELICA PARAMS
        0x01, (byte) 0xFE, /*  NFCID2t (8 bytes)
                           // https://github.com/adafruit/Adafruit-PN532/blob/master/Adafruit_PN532.cpp
                           // FeliCa NEEDS TO BEGIN WITH 0x01 0xFE!
                           */

        0x05, 0x01, (byte) 0x86,
        0x04, 0x02, 0x02,
        0x03, 0x00, // PAD (8 bytes)
        0x4B, 0x02, 0x4F,
        0x49, (byte) 0x8A, 0x00,
        (byte) 0xFF, (byte) 0xFF, // System code (2 bytes)

        0x01, 0x01, 0x66, // NFCID3t (10 bytes)
        0x6D, 0x01, 0x01, 0x10,
        0x02, 0x00, 0x00,

        0x00, // length of general bytes
        0x00 // length of historical bytes
    };

    public static final byte[] COMPATIBILITY_CONTAINER = new byte[]{
        0, 0x0F,
        0x20,
        0, 0x54,
        0, (byte) 0xFF,
        0x04, // T
        0x06, // L
        (byte) 0xE1, 0x04, // File identifier
        (byte) 0xFF, (byte) 0xFE, /* maximum NDEF file size (byte)
                                  //((NDEF_MAX_LENGTH & 0xFF00) >> 8), (byte) (NDEF_MAX_LENGTH & 0xFF)
                                  */
        0x00, // read access 0x0 = granted
        (byte) 0xFF // write access 0x0 = granted | 0xFF = deny
    };

    public static final int NDEF_MAX_LENGTH = 255;

    /**
     * build URI Message
     */
    public static byte[] buildUriNdefMessage(String uriToEncode) throws IOException {
        var prefix = new byte[] { (byte) 0x04 }; // https://
        var uri = uriToEncode.getBytes();

        var outputStream = new ByteArrayOutputStream();
        outputStream.write(prefix);
        outputStream.write(uri);

        var payload = outputStream.toByteArray();

        var message = new NdefMessage(new NdefRecord[] {
            new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[]{}, payload)
        });

        byte[] messageEncoded = message.toByteArray();

        String messageLog = "";
        for (byte b : messageEncoded) {
            messageLog += Integer.toHexString((b & 0xff)).concat(" ");
        }

        Execution.logsInfo("ENCODED MESSAGE: " + messageLog);

        // try (var fos = new FileOutputStream("/root/ndef-file")) {
        //    fos.write(messageEncoded);
        // }

        return messageEncoded;
    }

    public static byte[] setResponse(ResponseCommand cmd) {
        return setResponse(cmd, null);
    }

    public static byte[] setResponse(ResponseCommand cmd, byte[] data) {
        var cmdOffset = 0;
        byte[] command = null;

        switch (cmd) {
        case COMMAND_COMPLETE:
            if (data == null) {
                command = new byte[2];
            } else {
                command = new byte[data.length + 2];
                cmdOffset = data.length;
                System.arraycopy(data, 0, command, 0, data.length);
            }

            command[cmdOffset] = R_APDU_SW1_COMMAND_COMPLETE;
            command[cmdOffset + 1] = R_APDU_SW2_COMMAND_COMPLETE;
            break;
        case TAG_NOT_FOUND:
            command = new byte[2];
            command[0] = R_APDU_SW1_NDEF_TAG_NOT_FOUND;
            command[1] = R_APDU_SW2_NDEF_TAG_NOT_FOUND;
            // *sendlen = 2;
            break;
        case FUNCTION_NOT_SUPPORTED:
            command = new byte[2];
            command[0] = R_APDU_SW1_FUNCTION_NOT_SUPPORTED;
            command[1] = R_APDU_SW2_FUNCTION_NOT_SUPPORTED;
            // *sendlen = 2;
            break;
        case MEMORY_FAILURE:
            command = new byte[2];
            command[0] = R_APDU_SW1_MEMORY_FAILURE;
            command[1] = R_APDU_SW2_MEMORY_FAILURE;
            // *sendlen = 2;
            break;
        case END_OF_FILE_BEFORE_REACHED_LE_BYTES:
            command = new byte[2];
            command[0] = R_APDU_SW1_END_OF_FILE_BEFORE_REACHED_LE_BYTES;
            command[1] = R_APDU_SW2_END_OF_FILE_BEFORE_REACHED_LE_BYTES;
            // *sendlen = 2;
            break;
        default:
            break;
        }

        return command;
    }
}
