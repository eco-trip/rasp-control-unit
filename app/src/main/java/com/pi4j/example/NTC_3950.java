package com.pi4j.example;

import com.pi4j.example.ADS1115.AnalogChannel;

public class NTC_3950 {
    private static final double KELVIN = 273.15;
    private static final int R1 = 10100; // 10K ohm + 1%
    private static final double VCC = 3.3;
    private static final int BC = 3950; 
    private static final int TEMP_NOMINAL = 25;
    private static final int R_NTC = 50000; // 50K ohm - 1% 
    private final AnalogChannel channel;

    public NTC_3950(final AnalogChannel channel) {
        this.channel = channel;
    }

    public double getTemperature() {
        double voltage = channel.getData();
        return computeNtcInCelsius(voltage, R1, VCC, BC, TEMP_NOMINAL, R_NTC);
    }

    private static double computeNtcInCelsius(double voltage, int R1, double Vcc, int Bc, int Tnom, int Rntc) {
        double R2=voltage*R1/(Vcc-voltage);
        double steinhart = R2 / Rntc;
        steinhart = Math.log(steinhart);
        steinhart /= Bc;
        steinhart += 1 / (Tnom + KELVIN);
        steinhart = 1 / steinhart;
        return steinhart - KELVIN;
    }

}
