package com.pi4j.example;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalMultipurpose;
import com.pi4j.io.gpio.digital.DigitalMultipurposeConfig;
import com.pi4j.io.gpio.digital.DigitalMultipurposeConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalMode;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.util.Console;
import com.pi4j.plugin.pigpio.PiGpioPlugin;

public class DHT22v2 {
    private static final int MAXTIMINGS = 85;
    private final int[] data = { 0, 0, 0, 0, 0 };
    private final DigitalMultipurposeConfigBuilder cfg;
    private final Context ctx;
    private DigitalMultipurpose sensor;

    public DHT22v2(final Context ctx, final int pin) {
        this.ctx = ctx;

        cfg = DigitalMultipurpose.newConfigBuilder(ctx)
        .id("transfer")
        .name("transfer")
        .address(pin)
        .mode(DigitalMode.OUTPUT)
        .initial(DigitalState.HIGH)
        // .pull(PullResistance.PULL_UP)
        .provider(PiGpioPlugin.DIGITAL_MULTIPURPOSE_PROVIDER_ID);
        
        /* 
        cfgOut = DigitalOutput.newConfigBuilder(ctx)
            .address(pin)
            .initial(DigitalState.HIGH)
            .shutdown(DigitalState.HIGH)
            .provider("pigpio-digital-output");
        cfgIn = DigitalInput.newConfigBuilder(ctx)
            .address(pin)
            .pull(PullResistance.OFF)
            .provider("pigpio-digital-input");
        */
    }

    public void printTemperatureAndHumidity() {
        DigitalState laststate = DigitalState.HIGH;
        int j = 0;
        data[0] = data[1] = data[2] = data[3] = data[4] = 0;
        var console = new Console();

        long now, start, lowSent, highSent, delay15;

        if (sensor == null) sensor = ctx.create(cfg); else sensor.initialize(ctx);

        start = java.lang.System.nanoTime();

        sensor.low();

        lowSent = java.lang.System.nanoTime();
        
        delay(16);

        delay15 = java.lang.System.nanoTime();

        sensor.high();

        highSent = java.lang.System.nanoTime();

        // output.shutdown(ctx);
        sensor.mode(DigitalMode.INPUT);

        long readyForInput = java.lang.System.nanoTime();
        
        now = System.nanoTime();
		DigitalState state = sensor.state();
        DigitalState next = state;
		long val = 0, lastHi = now;
		int read = 0;
        boolean isOne = false;

        // boolean firstStateHigh = state == DigitalState.HIGH; // DEBUG
        // int stateChanges = 0;                                // DEBUG

		//active polling for 10ms (5.5ms is enough according to datasheet)
		while (System.nanoTime()-now < 7000000)
		{
			next = sensor.state();
			//edge detection
			if (state != next)
			{
                 // stateChanges++;

				//if this is the beginning of a high interval
				if (next == DigitalState.HIGH)
					lastHi = System.nanoTime();
				//otherwise end of interval so measure length and store bit
				else
				{
                    isOne = (System.nanoTime()-lastHi)/1000 > 55;
					val = (val << 1);
					//if bit is 1
					if (isOne)
						val++;

                    read++;
                    if (read>=41)
                        break;
				}
				state = next;
			}
		}

		sensor.shutdown(ctx);

        // console.println("---------------------------");
        // console.println("Low sent = "+Math.round((lowSent-start)/1000)+" us");
        // console.println("Delay 15ms = "+Math.round((delay15-lowSent)/1000)+" us");
        // console.println("High sent = "+Math.round((highSent-delay15)/1000)+" us");
        // console.println("Ready for input = "+Math.round((readyForInput-highSent)/1000)+" us");
        // console.println("Primo stato letto = "+(firstStateHigh?"HIGH":"LOW"));
        // console.println("Ultimo stato letto = "+(next == DigitalState.HIGH?"HIGH":"LOW"));
        // console.println("Numero cambiamenti rilevati = "+stateChanges);
        // console.println("Reads = "+read);
        
		
        // check we read 40 bits (8bit x 5 ) + verify checksum in the last byte
		// should be 41 (40 + 1 at start) but the first few bits are often missed and often equal 0
		if (read >= 38)
		{
			int hi = (int)((val & 0xff00000000L) >> 32), hd = (int)((val & 0xff000000L) >> 24),
				ti = (int)((val & 0xff0000) >> 16), td = (int)((val & 0xff00) >> 8), 
				cs = (int)(val & 0xff);
			//checksum
			if (cs == ((hi+hd+ti+td) & 0xff))
			{
				double temperature = ((((ti & 0x7f) << 8)+td)/10.)*((ti & 0x80) != 0 ? -1 : 1);
				double humidity = ((hi << 8)+hd)/10.;
				console.println("Humidity = " + humidity + " Temperature = " + temperature);
			} else {
                console.println("Data not good, skip");
            }
		}
    }

    private static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void delayMicroseconds(int time) {
        long start = System.nanoTime();
        long end=0;
        do{
            end = System.nanoTime();
        }while(start + time*1000 >= end);
    }
}