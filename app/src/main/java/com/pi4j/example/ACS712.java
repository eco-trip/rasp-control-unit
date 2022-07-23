package com.pi4j.example;

import java.util.OptionalDouble;
import java.util.stream.IntStream;

import com.pi4j.example.ADS1115.AnalogChannel;

public class ACS712 implements ACSensor {
    private static final int AVG_ITERATIONS = 100;
    private static final int ADC_MAX_VALUE = 1023;
    private static final int V_REF = 5000;
    private static final int V_ZERO = 2500;
    private static final int MAX_TOLERANCE = 10;  
    private final AnalogChannel channel;
    private final ScaleFactor scaleFactor;
    private final double voltageAtZeroCurrent;

    public enum ScaleFactor {
        ACS_30A(66), 
        ACS_20A(100), 
        ACS_5A(185); 

        private int value;
        
        ScaleFactor(final int value) {
            this.value = value;
        }
    }

    public ACS712(final ScaleFactor scaleFactor, final AnalogChannel channel) {
        this.channel = channel;
        this.scaleFactor = scaleFactor;
        this.voltageAtZeroCurrent = getCalibratedVoltage();
    }

    public double getCurrent() {
        double outVoltage = computeOutputVoltage();
        return computeCurrentInA(outVoltage);
    }

    // For stable measurement we take 100 measurements and average it
    private OptionalDouble computeAvgOfRawData() {
        return IntStream.of(AVG_ITERATIONS).mapToDouble(i -> {
            Utils.sleepOneMillisecond();
            return channel.getData();
        }).average();
    }

    private double computeOutputVoltage() {
        return (computeAvgOfRawData().getAsDouble() / ADC_MAX_VALUE) * V_REF;
    }

    private double computeCurrentInA(final double outVoltage) {
        return (outVoltage - voltageAtZeroCurrent) / scaleFactor.value;
    }

    private double getCalibratedVoltage() {
        double outVoltage = computeOutputVoltage();
        return V_ZERO - outVoltage <= MAX_TOLERANCE ? outVoltage : V_ZERO; 
    }
}
 