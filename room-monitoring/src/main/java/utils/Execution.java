package utils;

import java.util.function.Consumer;

public class Execution {
    public static double calculateExecutionTimeInMillis(final Runnable runnable) {
        final double start = System.currentTimeMillis();
        runnable.run();
        final double end = System.currentTimeMillis();
        return end - start;
    }

    public static void safeSleep(final long sleepTimeInMillis, final Consumer<Exception> consumer) {
        try {
            Thread.sleep(sleepTimeInMillis);
        } catch (InterruptedException e) {
            consumer.accept(e);
        }
    }

    public static void safeSleep(final long sleepTimeInMillis) {
        safeSleep(sleepTimeInMillis, e -> {
            throw new RuntimeException(e);
        });
    }
}