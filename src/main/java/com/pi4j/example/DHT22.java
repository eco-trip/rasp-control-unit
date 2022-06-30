package com.pi4j.example;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.util.Console;

public class DHT22 {
    private static final int MAXTIMINGS = 85;
    private final int[] data = { 0, 0, 0, 0, 0 };
    private final DigitalOutputConfigBuilder cfgOut;
    private final DigitalInputConfigBuilder cfgIn;
    private final Context ctx;
    private DigitalInput input;
    private DigitalOutput output;


    public DHT22(final Context ctx, final int pin) {
        this.ctx = ctx;
        cfgOut = DigitalOutput.newConfigBuilder(ctx)
            .address(pin)
            .initial(DigitalState.LOW)
            .shutdown(DigitalState.HIGH)
            .provider("pigpio-digital-output");
        cfgIn = DigitalInput.newConfigBuilder(ctx)
            .address(pin)
            .pull(PullResistance.OFF)
            .provider("pigpio-digital-input");
    }

    public void printTemperatureAndHumidity() {
        DigitalState laststate = DigitalState.HIGH;
        int j = 0;
        data[0] = data[1] = data[2] = data[3] = data[4] = 0;
        var console = new Console();

        if (output == null) output = ctx.create(cfgOut); else output.initialize(ctx);

        output.low();
        delay(18);
        output.high();
        delayMicroseconds(40);
        output.shutdown(ctx);

        if (input == null) input = ctx.create(cfgIn); else input.initialize(ctx);
        
        for (int i = 0; i < MAXTIMINGS; i++) {
            int counter = 0;
            while (input.state().equals(laststate)) {
                counter++;
                delayMicroseconds(2);
                if (counter == 255) {
                    console.println("break");
                    break;
                }
            }

            laststate = input.state();

            if (counter == 255) {
                break;
            }

            /* ignore first 3 transitions */
            if (i >= 4 && i % 2 == 0) {
                /* shove each bit into the storage bytes */
                data[j / 8] <<= 1;
                if (counter > 16) {
                    data[j / 8] |= 1;
                }
                j++;
            }
        }
        input.shutdown(ctx);
        // check we read 40 bits (8bit x 5 ) + verify checksum in the last
        // byte
        if (j >= 40 && checkParity()) {
            System.out.println("Humidity = " + data[0] + " Temperature = " + data[2]);
        } else {
            System.out.println("Data not good, skip");
        }
    }

    private boolean checkParity() {
        return data[4] == (data[0] + data[1] + data[2] + data[3] & 0xFF);
    }

    private static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void delayMicroseconds(int time) {
        try {
            Thread.sleep(0, time * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}