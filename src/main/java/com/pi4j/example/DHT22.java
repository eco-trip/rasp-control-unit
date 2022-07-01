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
            .initial(DigitalState.HIGH)
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

        long now, start, lowSent, highSent, delay15;

        if (output == null) output = ctx.create(cfgOut); else output.initialize(ctx);

        start = java.lang.System.nanoTime();

        output.low();

        lowSent = java.lang.System.nanoTime();
        
        delay(15);

        delay15 = java.lang.System.nanoTime();

        output.high();

        highSent = java.lang.System.nanoTime();

        output.shutdown(ctx);

        if (input == null) input = ctx.create(cfgIn); else input.initialize(ctx);

        long readyForInput = java.lang.System.nanoTime();
        
        now = System.nanoTime();
		DigitalState state = input.state();
		long val = 0, lastHi = now;
		int read = 0;

		//active polling for 10ms (5.5ms is enough according to datasheet)
		while (System.nanoTime()-now < 10000000)
		{
			DigitalState next = input.state();
			//edge detection
			if (state != next)
			{
				//if this is the beginning of a high interval
				if (next == DigitalState.HIGH)
					lastHi = System.nanoTime();
				//otherwise end of interval so measure length and store bit
				else
				{
					val = (val << 1);
					read++;
					//if bit is 1
					if ((System.nanoTime()-lastHi)/1000 > 48)
						val++;
				}
				state = next;
			}
		}
		input.shutdown(ctx);

        System.out.println("Low sent = "+Math.round((lowSent-start)/1000)+" us");
        System.out.println("Delay 15ms = "+Math.round((delay15-lowSent)/1000)+" us");
        System.out.println("High sent = "+Math.round((highSent-delay15)/1000)+" us");
        System.out.println("Ready for input = "+Math.round((readyForInput-highSent)/1000)+" us");
		
        // check we read 40 bits (8bit x 5 ) + verify checksum in the last byte
		// should be 40 but the first few bits are often missed and often equal 0
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
				System.out.println("Humidity = " + humidity + " Temperature = " + temperature);
			} else {
                System.out.println("Data not good, skip");
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