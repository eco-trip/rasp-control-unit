package com.pi4j.example;

public class Utils {
    public static void safeSleep(long milliseconds) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleepOneMillisecond() {
        safeSleep(1);
    }

    public static void sleepOneSecond() {
        safeSleep(1000);
    }
}
