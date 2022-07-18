package com.pi4j.example;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.util.Console;

public class CHY7 {
    private static int DEVICE_FREQUENCY = 11;
    private static int SEC_PER_MINUTES = 60;
    private int pulses = 0;
    private boolean enabledForReading = false;
    private final DigitalInput input;

    public CHY7(Context pi4j, int pin, Console console) {

        var config = DigitalInput.newConfigBuilder(pi4j)
            .id("chy7_waterflow")
            .name("chy7 waterflow")
            .address(pin)
            .pull(PullResistance.PULL_UP)
            .provider("pigpio-digital-input");
        
        input = pi4j.din().create(config);
        input.addListener(e -> {
            pulses++;
            console.println("Something has changed");
            if (e.state() == DigitalState.HIGH && enabledForReading) {
                pulses++;
            }
        });
    }

    public double getLiterPerMinute() throws InterruptedException {
        enabledForReading = true;
        Thread.sleep(1000);
        enabledForReading = false;
        double flow = pulses * SEC_PER_MINUTES / DEVICE_FREQUENCY;
        pulses = 0;
        return flow;
    }
}
