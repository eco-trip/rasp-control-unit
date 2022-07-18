package com.pi4j.example;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;

public class CHY7 {
    private static int DEVICE_FREQUENCY = 11;
    private AtomicInteger pulses = new AtomicInteger();
    private AtomicBoolean enabledForReading = new AtomicBoolean();
    private final DigitalInput input;

    public CHY7(Context pi4j, int pin) {

        var config = DigitalInput.newConfigBuilder(pi4j)
            .id("chy7_waterflow")
            .name("chy7 waterflow")
            .address(pin)
            .pull(PullResistance.PULL_UP)
            .provider("pigpio-digital-input")
            .build();

        input = pi4j.create(config);
        input.addListener(e -> {
            if (e.state() == DigitalState.HIGH && enabledForReading.get()) {
                pulses.incrementAndGet();
            }
        });

        pulses.set(0);
        enabledForReading.set(false);
    }

    public double getLiterPerMinute() throws InterruptedException {
        double start = java.lang.System.nanoTime();
        pulses.set(0);
        enabledForReading.set(true);
        Thread.sleep(1000);
        enabledForReading.set(false);
        double end = java.lang.System.nanoTime();
        double effective_time = (end - start) / 1000000; //ms
        double effective_pulses = (pulses.get() * 1000 / effective_time);
        double flow = effective_pulses / DEVICE_FREQUENCY; // P:t1 = Px:1000 ; t1 >= 1000
        return flow;
    }
}
