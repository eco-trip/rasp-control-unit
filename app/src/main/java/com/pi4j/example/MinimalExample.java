package com.pi4j.example;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: EXAMPLE  :: Sample Code
 * FILENAME      :  MinimalExample.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2021 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.example.PN532.IPN532Interface;
import com.pi4j.example.PN532.PN532;
import com.pi4j.example.PN532.PN532I2C;
import com.pi4j.example.PN532.Ndef.NdefMessage;
import com.pi4j.example.PN532.Ndef.NdefRecord;
import com.pi4j.example.ACS712.ScaleFactor;
import com.pi4j.example.ADS1115.Channel;
import com.pi4j.util.Console;


/**
 * <p>This example fully describes the base usage of Pi4J by providing extensive comments in each step.</p>
 *
 * @author Frank Delporte (<a href="https://www.webtechie.be">https://www.webtechie.be</a>)
 * @version $Id: $Id
 */
public class MinimalExample {

    // Command APDU
    static final byte C_APDU_CLA = 0;
    static final byte C_APDU_INS = 1;  // instruction
    static final byte C_APDU_P1 = 2;   // parameter 1
    static final byte C_APDU_P2 = 3;   // parameter 2
    static final byte C_APDU_LC = 4;   // length command
    static final byte C_APDU_DATA = 5; 

    static final byte C_APDU_P1_SELECT_BY_ID = 0x00;
    static final byte C_APDU_P1_SELECT_BY_NAME = 0x04;

    // Response APDU
    static final byte R_APDU_SW1_COMMAND_COMPLETE = (byte) 0x90;
    static final byte R_APDU_SW2_COMMAND_COMPLETE = 0x00;

    static final byte R_APDU_SW1_NDEF_TAG_NOT_FOUND = 0x6a;
    static final byte R_APDU_SW2_NDEF_TAG_NOT_FOUND = (byte) 0x82;

    static final byte R_APDU_SW1_FUNCTION_NOT_SUPPORTED = 0x6A;
    static final byte R_APDU_SW2_FUNCTION_NOT_SUPPORTED = (byte) 0x81;

    static final byte R_APDU_SW1_MEMORY_FAILURE = 0x65;
    static final byte R_APDU_SW2_MEMORY_FAILURE = (byte) 0x81;

    static final byte R_APDU_SW1_END_OF_FILE_BEFORE_REACHED_LE_BYTES = 0x62;
    static final byte R_APDU_SW2_END_OF_FILE_BEFORE_REACHED_LE_BYTES = (byte) 0x82;

    // ISO7816-4 commands
    static final byte ISO7816_SELECT_FILE = (byte) 0xA4;
    static final byte ISO7816_READ_BINARY = (byte) 0xB0;
    static final byte ISO7816_UPDATE_BINARY = (byte) 0xD6;

    public enum ResponseCommand
    {
        COMMAND_COMPLETE,
        TAG_NOT_FOUND,
        FUNCTION_NOT_SUPPORTED,
        MEMORY_FAILURE,
        END_OF_FILE_BEFORE_REACHED_LE_BYTES
    }

    public enum TagFile
    {
        NONE,
        CC,
        NDEF
    }

    static final byte PN532_MIFARE_ISO14443A = 0x00;
    /**
     * This application blinks a led and counts the number the button is pressed. The blink speed increases with each
     * button press, and after 5 presses the application finishes.
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(String[] args) throws Exception {
        // Create Pi4J console wrapper/helper
        // (This is a utility class to abstract some of the boilerplate stdin/stdout code)
        final var console = new Console();

        // Print program title/header
        console.title("<-- The Pi4J Project -->", "Minimal Example project");

        // ************************************************************
        //
        // WELCOME TO Pi4J:
        //
        // Here we will use this getting started example to
        // demonstrate the basic fundamentals of the Pi4J library.
        //
        // This example is to introduce you to the boilerplate
        // logic and concepts required for all applications using
        // the Pi4J library.  This example will do use some basic I/O.
        // Check the pi4j-examples project to learn about all the I/O
        // functions of Pi4J.
        //
        // ************************************************************

        // ------------------------------------------------------------
        // Initialize the Pi4J Runtime Context
        // ------------------------------------------------------------
        // Before you can use Pi4J you must initialize a new runtime
        // context.
        //
        // The 'Pi4J' static class includes a few helper context
        // creators for the most common use cases.  The 'newAutoContext()'
        // method will automatically load all available Pi4J
        // extensions found in the application's classpath which
        // may include 'Platforms' and 'I/O Providers'
        var pi4j = Pi4J.newAutoContext();

        // ------------------------------------------------------------
        // Output Pi4J Context information
        // ------------------------------------------------------------
        // The created Pi4J Context initializes platforms, providers
        // and the I/O registry. To help you to better understand this
        // approach, we print out the info of these. This can be removed
        // from your own application.
        // OPTIONAL
        PrintInfo.printLoadedPlatforms(console, pi4j);
        PrintInfo.printDefaultPlatform(console, pi4j);
        PrintInfo.printProviders(console, pi4j);

        // Here we will create I/O interfaces for a (GPIO) digital output

        // var buttonConfig = DigitalInput.newConfigBuilder(pi4j)
        //         .id("button")
        //         .name("Press button")
        //         .address(PIN_BUTTON)
        //         .pull(PullResistance.PULL_DOWN)
        //         .debounce(3000L)
        //         .provider("pigpio-digital-input");
        // var button = pi4j.create(buttonConfig);
        // button.addListener(e -> {
        //     if (e.state() == DigitalState.LOW) {
        //         pressCount++;
        //         console.println("Button was pressed for the " + pressCount + "th time");
        //     }
        // });

        // OPTIONAL: print the registry
        PrintInfo.printRegistry(console, pi4j);

        // while (pressCount < 5) {
        //     if (led.equals(DigitalState.HIGH)) {
        //         console.println("LED low");
        //         led.low();
        //     } else {
        //         console.println("LED high");
        //         led.high();
        //     }
        //     Thread.sleep(500 / (pressCount + 1));
        // }

        final DHT22v2 dht = new DHT22v2(pi4j, 17);

        /*
        final BH1750 bh1 = new BH1750(pi4j, 0x5c, 1, "@bh1750-a");
        final BH1750 bh2 = new BH1750(pi4j, "@bh1750-b");
        final ADS1115 ads = new ADS1115(pi4j);
        final CHY7 chy7 = new CHY7(pi4j, 27);
        final NCT_3950 nct = new NCT_3950(ads);
        final ACSensor acs = new ACS712(ScaleFactor.ACS_20A, () -> ads.getDataByAnalogInput(Channel.A1_IN));
        */

        nfcEmulate(pi4j);

        /* 
        for (int i = 0; i < 1000; i++) {
            console.println("luminosity bh1: " + bh1.getLightIntensity());
            console.println("luminosity bh2: " + bh2.getLightIntensity());
            console.println("Liter per minute: " + chy7.getLiterPerMinute());
            console.println("Temperature (NTC): " + nct.getTemperature());
            console.println("Current: " + acs.getCurrent());
            dht.printTemperatureAndHumidity();
        }   
        */

        // System.out.println("Done!!");

        // ------------------------------------------------------------
        // Terminate the Pi4J library
        // ------------------------------------------------------------
        // We we are all done and want to exit our application, we must
        // call the 'shutdown()' function on the Pi4J static helper class.
        // This will ensure that all I/O instances are properly shutdown,
        // released by the the system and shutdown in the appropriate
        // manner. Terminate will also ensure that any background
        // threads/processes are cleanly shutdown and any used memory
        // is returned to the system.

        // Shutdown Pi4J
        pi4j.shutdown();
    }
    static public boolean nfcRead(Context pi4j) throws Exception
    {
        IPN532Interface pn532Interface = new PN532I2C(pi4j);
        final PN532 nfc = new PN532(pn532Interface);

        nfc.begin();
		Thread.sleep(1000);

		long versiondata = nfc.getFirmwareVersion();
		if (versiondata == 0) {
			System.out.println("Didn't find PN53x board");
			return false;
		}
		// Got ok data, print it out!
		System.out.print("Found chip PN5");
		System.out.println(Long.toHexString((versiondata >> 24) & 0xFF));

		System.out.print("Firmware ver. ");
		System.out.print(Long.toHexString((versiondata >> 16) & 0xFF));
		System.out.print('.');
		System.out.println(Long.toHexString((versiondata >> 8) & 0xFF));

		// configure board to read RFID tags
		nfc.SAMConfig();

		System.out.println("Waiting for an ISO14443A Card ...");

		byte[] buffer = new byte[64];
		for (int x = 0; x < 10000; x++) {
			int readLength = nfc.readDataPacket(PN532_MIFARE_ISO14443A,
					buffer);

			if (readLength > 0) {
				System.out.println("Found a card");

				System.out.print("  Data Length ");
				System.out.print(readLength);
				System.out.println(" bytes");

				System.out.print("  UID Value: [");
				for (int i = 0; i < readLength; i++) {
					System.out.print(Integer.toHexString(buffer[i]));
				}
				for (int i = 0; i < readLength; i++){
					System.out.print((char)(buffer[i]));
				}
				System.out.println("]");
			}

            if (readLength>0)
			    break;
            else
                Thread.sleep(1000);
		}    

        return true;
    }
    static public boolean nfcEmulate(Context pi4j) throws Exception
    {
        IPN532Interface pn532Interface = new PN532I2C(pi4j);
        final PN532 nfc = new PN532(pn532Interface);

        NdefMessage message = new NdefMessage(new NdefRecord[]{new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_URI,"id value".getBytes(),"http://www.meblabs.com".getBytes())});
        byte[] messageEncoded = message.toByteArray();

        System.out.print("NDEF message: ");
        for (int x=0;x<messageEncoded.length;x++)
        {
            System.out.print(Integer.toHexString((int)(messageEncoded[x] & 0xff))+" ");
        }

        System.out.print("\n\n");


        nfc.begin();
        Thread.sleep(1000);

        long versiondata = nfc.getFirmwareVersion();
		if (versiondata == 0) {
			System.out.println("Didn't find PN53x board");
			return false;
		}
		// Got ok data, print it out!
		System.out.print("Found chip PN5");
		System.out.println(Long.toHexString((versiondata >> 24) & 0xFF));
        nfc.SAMConfig();

        // nfc.inRelease(); // precaution

        /*
        byte[] command = new byte[]{
            PN532.PN532_COMMAND_TGINITASTARGET,
            5, // MODE: PICC only, Passive only
      
            0x04, 0x00,       // SENS_RES
            0x00, 0x00, 0x00, // NFCID1
            0x20,             // SEL_RES
      
            // FELICA PARAMS
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, // FeliCaParams
            0, 0,
        
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // NFCID3t
      
            0, // length of general bytes
            0  // length of historical bytes
        };
        */
        byte[] command = new byte[]{
            PN532.PN532_COMMAND_TGINITASTARGET,
            5, // MODE: PICC only, Passive only
      
            0x04, 0x00,       // SENS_RES
            0x00, 0x00, 0x00, // NFCID1
            0x20,             // SEL_RES
      
            // FELICA PARAMS
            0x01, (byte) 0xFE,         // NFCID2t (8 bytes) https://github.com/adafruit/Adafruit-PN532/blob/master/Adafruit_PN532.cpp FeliCa NEEDS TO BEGIN WITH 0x01 0xFE!
            0x05, 0x01, (byte) 0x86,
            0x04, 0x02, 0x02,
            0x03, 0x00,         // PAD (8 bytes)
            0x4B, 0x02, 0x4F, 
            0x49, (byte) 0x8A, 0x00,   
            (byte) 0xFF, (byte) 0xFF,         // System code (2 bytes)

            0x01, 0x01, 0x66,   // NFCID3t (10 bytes)
            0x6D, 0x01, 0x01, 0x10,
            0x02, 0x00, 0x00,
      
            0x00, // length of general bytes
            0x00  // length of historical bytes
        };

        byte[] uid = new byte[]{0x12, 0x34, 0x56};
        for (int i=0;i<=2;i++)
        {
            command[4+i]=uid[i];
        }

        if (!nfc.tgInitAsTarget(command))
        {
            // tgInitAsTarget failed or timed out!
            nfc.inRelease();
            return false;
        }

        int NDEF_MAX_LENGTH = 128;

        byte compatibility_container[] = new byte[]{
            0, 0x0F,
            0x20,
            0, 0x54,
            0, (byte) 0xFF,
            0x04,                                                        // T
            0x06,                                                        // L
            (byte) 0xE1, 0x04,                                                  // File identifier
            (byte) ((NDEF_MAX_LENGTH & 0xFF00) >> 8), (byte) (NDEF_MAX_LENGTH & 0xFF), // maximum NDEF file size
            0x00,                                                        // read access 0x0 = granted
            0x00                                                         // write access 0x0 = granted | 0xFF = deny
        };

        boolean tagWriteable = false;

        if (tagWriteable == false)
        {
            compatibility_container[14] = (byte) 0xFF;
        }
      
        boolean tagWrittenByInitiator = false;

        byte[] rwbuf = new byte[128];
        byte[] responseCommand = null;
        byte sendlen;
        int status;
        TagFile currentFile = TagFile.NONE;
        int cc_size = compatibility_container.length;
        boolean runLoop = true;

        while (runLoop)
        {
            System.out.print("tgGetData!\n");
            status = nfc.tgGetData(rwbuf);
            if (status < 0)
            {
                System.out.print("tgGetData failed!\n");
                nfc.inRelease();
                return true;
            }

            System.out.print("Buffer: ");
            for (int x=0;x<rwbuf.length;x++)
            {
                System.out.print(Integer.toHexString((int)(rwbuf[x] & 0xff))+" ");
            }

            System.out.print("\n\n");

            byte p1 = rwbuf[C_APDU_P1];
            byte p2 = rwbuf[C_APDU_P2];
            byte lc = rwbuf[C_APDU_LC];
            int p1p2_length = ((int)(p1 & 0xff) << 8) + (int)(p2 & 0xff);

            System.out.println("p1: " + (int)(p1 & 0xff));
            System.out.println("p2: " + (int)(p2 & 0xff));
            System.out.println("p1p2_length: " + p1p2_length);
            System.out.println("lc: " + (int)(lc & 0xff));

            switch (rwbuf[C_APDU_INS])
            {
                case ISO7816_SELECT_FILE:
                    switch (p1)
                    {
                        case C_APDU_P1_SELECT_BY_ID:
                            
                            if (p2 != 0x0c)
                            {
                                System.out.print("C_APDU_P2 != 0x0c\n");
                                responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE);
                            }
                            else if (lc == 2 && rwbuf[C_APDU_DATA] == (byte) 0xE1 && (rwbuf[C_APDU_DATA + 1] == 0x03 || rwbuf[C_APDU_DATA + 1] == 0x04))
                            {
                                responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE);

                                if (rwbuf[C_APDU_DATA + 1] == 0x03)
                                    currentFile = TagFile.CC;
                                else if (rwbuf[C_APDU_DATA + 1] == 0x04)
                                    currentFile = TagFile.NDEF;

                                System.out.print("C_APDU_P1_SELECT_BY_ID -> currentFile: "+currentFile+"\n");
                            }
                            else
                            {
                                System.out.print("C_APDU_P1_SELECT_BY_ID -> TAG NOT FOUND\n");
                                responseCommand = setResponse(ResponseCommand.TAG_NOT_FOUND);
                            }
                            break;
                        case C_APDU_P1_SELECT_BY_NAME:

                            System.out.print("check ndef_tag_application_name_v2\n");

                            byte ndef_tag_application_name_v2[] = {0, 0x7, (byte) 0xD2, 0x76, 0x00, 0x00, (byte) 0x85, 0x01, 0x01};

                            boolean ok=true;
                            for (int x=0;x<ndef_tag_application_name_v2.length;x++)
                            {
                                System.out.print("tag app name v2: "+ndef_tag_application_name_v2[x]+" -> "+rwbuf[C_APDU_P2+x]+"\n");

                                if (ndef_tag_application_name_v2[x]!=rwbuf[C_APDU_P2+x])
                                {
                                    ok = false;
                                    break;
                                }
                            }
                            if (ok)
                            {
                                responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE);
                            }
                            else
                            {
                                System.out.print("Function not supported\n");
                                responseCommand = setResponse(ResponseCommand.FUNCTION_NOT_SUPPORTED);
                            }
                        break;
                    }
                break;
                case ISO7816_READ_BINARY:
                    System.out.print("Read Binary\n");
                    switch (currentFile)
                    {
                        case NONE:
                            System.out.print("ALERT 3\n");
                            responseCommand = setResponse(ResponseCommand.TAG_NOT_FOUND);
                        break;
                        case CC:
                            if (p1p2_length > NDEF_MAX_LENGTH)
                            {
                                System.out.print("ALERT 2\n");
                                responseCommand = setResponse(ResponseCommand.END_OF_FILE_BEFORE_REACHED_LE_BYTES);
                            }
                            else
                            {
                                System.out.print("Send compatibility_container\n");
                                // memcpy(rwbuf, compatibility_container + p1p2_length, lc);

                                byte[] toSend = new byte[lc];
                                for (int x=0;x<lc;x++)
                                {
                                    if (p1p2_length+x > compatibility_container.length -1)
                                        break;
                                    toSend[x] = compatibility_container[p1p2_length+x];
                                }

                                responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE,toSend);
                                // setResponse(ResponseCommand.COMMAND_COMPLETE, rwbuf + lc, lc);
                            }
                            break;
                        case NDEF:
                            if (p1p2_length > NDEF_MAX_LENGTH)
                            {
                                System.out.print("ALERT 1\n");
                                responseCommand = setResponse(ResponseCommand.END_OF_FILE_BEFORE_REACHED_LE_BYTES);
                            }
                            else
                            {
                                System.out.print("SEND BINARY NDEF "+p1p2_length+"-"+lc+"\n");

                                byte[] toSend = new byte[lc];
                                for (int x=0;x<lc;x++)
                                {
                                    if (p1p2_length+x > messageEncoded.length -1)
                                        break;
                                    toSend[x] = messageEncoded[p1p2_length+x];
                                }

                                responseCommand = setResponse(ResponseCommand.COMMAND_COMPLETE,toSend);
                            }
                            break;
                    }
                break;
                case ISO7816_UPDATE_BINARY:
                    if (!tagWriteable)
                    {
                        System.out.print("ALERT 5\n");
                        responseCommand = setResponse(ResponseCommand.FUNCTION_NOT_SUPPORTED);
                    }
                    else
                    {
                        if (p1p2_length > NDEF_MAX_LENGTH)
                        {
                            System.out.print("ALERT 6\n");
                            responseCommand = setResponse(ResponseCommand.MEMORY_FAILURE);
                        }
                        else
                        {
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
                    System.out.print("Command not supported! "+rwbuf[C_APDU_INS]+"\n");
                    responseCommand = setResponse(ResponseCommand.FUNCTION_NOT_SUPPORTED);
            }

            if (!nfc.tgSetData(responseCommand))
            {
                System.out.print("tgSetData failed\n!");
                nfc.inRelease();
                return true;
            }
        }

        nfc.inRelease();
        return true;
    }

    static public byte[] setResponse(ResponseCommand cmd)
    {
        return setResponse(cmd,null);
    }
    static public byte[] setResponse(ResponseCommand cmd, byte[] data)
    {
        int cmdOffset = 0;
        byte[] command = null;

        switch (cmd)
        {
            case COMMAND_COMPLETE:
                if (data==null)
                    command = new byte[2];
                else
                {
                    command = new byte[data.length+2];
                    cmdOffset = data.length;

                    for (int x=0;x<data.length;x++)
                    {
                        command[x]=data[x];   
                    }

                }

                command[cmdOffset+0] = R_APDU_SW1_COMMAND_COMPLETE;
                command[cmdOffset+1] = R_APDU_SW2_COMMAND_COMPLETE;
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
        }

        return command;
    }
}
