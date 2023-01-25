package io.github.ecotrip.nfc;

import static io.github.ecotrip.nfc.Helpers.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import io.github.ecotrip.Generated;
import io.github.ecotrip.adapter.NfcAdapter;
import io.github.ecotrip.execution.Execution;
import io.github.ecotrip.token.Token;

/**
 * PN532 adapter
 */
@Generated
public class Pn532NfcAdapter implements NfcAdapter {
    private static final int INIT_TIMEOUT_IN_MS = 1000;
    private static final int GENERAL_ERROR_CODE = -1;
    private static final String BASE_URL = "ecotrip.meblabs.dev/";
    private final Pn532Controller nfcReader;

    private Pn532NfcAdapter(final Pn532Controller nfcReader) {
        this.nfcReader = nfcReader;
        nfcReader.begin();
        Execution.safeSleep(INIT_TIMEOUT_IN_MS);
        checkNfcDeviceAvailability();
    }

    public static Pn532NfcAdapter of(final Pn532Controller reader) {
        return new Pn532NfcAdapter(reader);
    }

    @Override
    public CompletableFuture<Void> initTagAndWaitForNearbyDevice() {
        return CompletableFuture.runAsync(() -> {
            try {
                while (!nfcReader.tgInitAsTarget(TG_INIT_AS_TARGET_CMD));
            } catch (InterruptedException e) {
                Execution.logsError(e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> reset() {
        return CompletableFuture.runAsync(() -> {
            try {
                nfcReader.inRelease();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> transmit(final Token token) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return startNfcHandshake(token);
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * @param token
     * @return
     */
    private int startNfcHandshake(final Token token) throws InterruptedException, IOException {
        boolean tagWriteable = false;
        var rwbuf = new byte[128];
        byte[] responseCommand = null;
        var currentFile = TagFile.NONE;

        while (true) {
            Execution.logsInfo("tgGetData!\n");
            int status = nfcReader.tgGetData(rwbuf);
            if (status < 0) {
                Execution.logsError("tgGetData failed! ->" + status);
                return status;
            }

            /*
            for (byte b : rwbuf) {
                Execution.logsInfo(Integer.toHexString(b & 0xff) + " ");
            }
            */

            byte p1 = rwbuf[C_APDU_P1];
            byte p2 = rwbuf[C_APDU_P2];
            byte lc = rwbuf[C_APDU_LC];
            int p1p2Length = ((p1 & 0xff) << 8) + (p2 & 0xff);

            System.out.println("p1: " + (p1 & 0xff));
            System.out.println("p2: " + (p2 & 0xff));
            System.out.println("p1p2_length: " + p1p2Length);
            System.out.println("lc: " + (lc & 0xff));

            switch(rwbuf[C_APDU_INS]) {
            case ISO7816_SELECT_FILE:
                switch (p1) {
                case C_APDU_P1_SELECT_BY_ID:
                    if (p2 != 0x0c) {
                        Execution.logsInfo("C_APDU_P2 != 0x0c\n");
                        responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE);
                    } else if (lc == 2 && rwbuf[C_APDU_DATA] == (byte) 0xE1 && (rwbuf[C_APDU_DATA + 1] == 0x03
                            || rwbuf[C_APDU_DATA + 1] == 0x04)) {
                        responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE);
                        if (rwbuf[C_APDU_DATA + 1] == 0x03) {
                            currentFile = TagFile.CC;
                        } else if (rwbuf[C_APDU_DATA + 1] == 0x04) {
                            currentFile = TagFile.NDEF;
                        }
                        System.out.println("C_APDU_P1_SELECT_BY_ID -> currentFile: " + currentFile + "\n");
                    } else {
                        System.out.println("C_APDU_P1_SELECT_BY_ID -> TAG NOT FOUND\n");
                        responseCommand = setResponse(ResponseCommand.TAG_NOT_FOUND);
                    }
                    break;
                case C_APDU_P1_SELECT_BY_NAME:
                    System.out.println("check ndef_tag_application_name_v2\n");
                    byte[] ndefTagApplicationNameV2 =
                        {0, 0x7, (byte) 0xD2, 0x76, 0x00, 0x00, (byte) 0x85, 0x01, 0x01};
                    boolean ok = true;
                    for (int x = 0; x < ndefTagApplicationNameV2.length; x++) {
                        System.out.println(
                            "tag app name v2: " + ndefTagApplicationNameV2[x] + " -> " + rwbuf[C_APDU_P2 + x] + "\n"
                        );
                        if (ndefTagApplicationNameV2[x] != rwbuf[C_APDU_P2 + x]) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE);
                    } else {
                        System.out.print("Function not supported\n");
                        responseCommand = setResponse(ResponseCommand.FUNCTION_NOT_SUPPORTED);
                    }
                    break;
                default:
                    break;
                }
                break;
            case ISO7816_READ_BINARY:
                System.out.print("Read Binary\n");
                switch (currentFile) {
                case NONE:
                    System.out.print("ALERT 3\n");
                    responseCommand = setResponse(ResponseCommand.TAG_NOT_FOUND);
                    break;
                case CC:
                    if (p1p2Length > NDEF_MAX_LENGTH) {
                        System.out.print("ALERT 2\n");
                        responseCommand = setResponse(ResponseCommand.END_OF_FILE_BEFORE_REACHED_LE_BYTES);
                    } else {
                        System.out.print("Send compatibility_container\n");
                        // memcpy(rwbuf, compatibility_container + p1p2_length, lc);

                        byte[] toSend = new byte[lc];
                        for (int x = 0; x < lc; x++) {
                            if (p1p2Length + x > COMPATIBILITY_CONTAINER.length - 1) {
                                break;
                            }
                            toSend[x] = COMPATIBILITY_CONTAINER[p1p2Length + x];
                        }

                        responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE, toSend);
                        // setResponse(ResponseCommand.COMMAND_COMPLETE, rwbuf + lc, lc);
                    }
                    break;
                case NDEF:
                    if (p1p2Length > NDEF_MAX_LENGTH) {
                        System.out.print("ALERT 1\n");
                        responseCommand = setResponse(ResponseCommand.END_OF_FILE_BEFORE_REACHED_LE_BYTES);
                    } else {
                        System.out.print("SEND BINARY NDEF " + p1p2Length + "-" + lc + "\n");

                        var toSend = new byte[lc];
                        byte[] messageToSend = createMessage(BASE_URL + token.getValue());
                        for (int x = 0; x < lc; x++) {
                            if (p1p2Length + x > messageToSend.length - 1) {
                                break;
                            }
                            toSend[x] = messageToSend[p1p2Length + x];
                        }
                        responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE, toSend);
                    }
                    break;
                default:
                    break;
                }
                break;
            case ISO7816_UPDATE_BINARY:
                if (!tagWriteable) {
                    System.out.print("ALERT 5\n");
                    responseCommand = setResponse(ResponseCommand.FUNCTION_NOT_SUPPORTED);
                } else {
                    if (p1p2Length > NDEF_MAX_LENGTH) {
                        System.out.print("ALERT 6\n");
                        responseCommand = setResponse(ResponseCommand.MEMORY_FAILURE);
                    } else {
                        System.out.print("TODO 3\n");
                        /*
                        memcpy(ndef_file + p1p2_length, rwbuf + C_APDU_DATA, lc);
                        responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE);
                        tagWrittenByInitiator = true;

                        int ndef_length = (ndef_file[0] << 8) + ndef_file[1];
                        if ((ndef_length > 0) && (updateNdefCallback != 0))
                        {
                            updateNdefCallback(ndef_file + 2, ndef_length);
                        }
                        */
                    }
                }
                break;
            default:
                System.out.print("Command not supported! " + rwbuf[C_APDU_INS] + "\n");
                responseCommand = setResponse(ResponseCommand.FUNCTION_NOT_SUPPORTED);
            }

            if (!nfcReader.tgSetData(responseCommand)) {
                System.out.print("tgSetData failed\n!");
                return GENERAL_ERROR_CODE;
            }
        }
    }

    private void checkNfcDeviceAvailability() {
        try {
            long versionData = nfcReader.getFirmwareVersion();
            if (versionData == 0) {
                Execution.logsInfo("Didn't find PN53x board");
                return;
            }
            // Got ok data, print it out!
            Execution.logsInfo("Found chip PN5");
            Execution.logsInfo(Long.toHexString((versionData >> 24) & 0xFF));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] createMessage(String message) throws IOException {
        var messageEncoded = Helpers.buildUriNdefMessage(message);
        var outputStream = new ByteArrayOutputStream();
        outputStream.write(new byte[] { 0x00, (byte) messageEncoded.length });
        outputStream.write(messageEncoded);
        return outputStream.toByteArray();
    }
}
