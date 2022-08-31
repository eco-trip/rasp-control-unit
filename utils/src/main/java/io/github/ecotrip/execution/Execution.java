package io.github.ecotrip.execution;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Utils class relative to {@link Thread} and execution times.
 */
public class Execution {
    public static final int SECOND_IN_MICRO = 1000000;
    public static final int SECOND_IN_MILLIS = 1000;
    private static final Logger LOG = Logger.getLogger(Execution.class.toString());

    public static double instantInMicros() {
        return System.nanoTime() / (double) SECOND_IN_MILLIS;
    }

    /**
     * Register how much time the {@link Runnable} will take.
     * @param runnable to perform and analyzed.
     * @return the computational time required by the {@link Runnable}.
     */
    public static double getComputationalTimeInMillis(final Runnable runnable) {
        final double start = System.currentTimeMillis();
        runnable.run();
        final double end = System.currentTimeMillis();
        return end - start;
    }

    /**
     * Gets the current {@link Thread} sleep.
     * @param sleepTimeInMillis is the waiting time before awake the {@link Thread}.
     * @param exceptionHandler used to consume a possible {@link Exception}.
     */
    public static void safeSleep(final long sleepTimeInMillis, final Consumer<Exception> exceptionHandler) {
        try {
            Thread.sleep(sleepTimeInMillis);
        } catch (InterruptedException e) {
            exceptionHandler.accept(e);
        }
    }

    /**
     * Gets the current {@link Thread} sleep.
     * @param sleepTimeInMillis is the waiting time before awaking the {@link Thread}.
     */
    public static void safeSleep(final long sleepTimeInMillis) {
        safeSleep(sleepTimeInMillis, e -> {
            throw new RuntimeException(e);
        });
    }

    /**
     * Blocks the current {@link Thread} thanks a busy waiting for performance reasons.
     * @param delayTime is the waiting before realising the current {@link Thread}.
     */
    public static void delayMicroseconds(int delayTime) {
        long start = System.nanoTime();
        long end;
        do {
            end = System.nanoTime();
        } while(start + (long) delayTime * SECOND_IN_MILLIS >= end);
    }

    @SafeVarargs
    public static <T> void logsInfo(final T ... elements) {
        Arrays.stream(elements).forEach(d -> LOG.info(d.toString()));
    }

    @SafeVarargs
    public static <T> void logsError(final T ... errors) {
        Arrays.stream(errors).forEach(e -> LOG.info(e.toString()));
    }
}
