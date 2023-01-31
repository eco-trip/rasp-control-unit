package io.github.ecotrip.execution.engine;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Utils class which simplify the {@link Engine} construction.
 */
public class EngineFactory {
    /**
     * Creates a scheduled engine.
     * @param threadPoolSize used by the engine.
     * @param <T> type of the object to be accumulated.
     * @return the engine.
     * */
    public static <T> Engine createScheduledEngine(int threadPoolSize) {
        return new Engine() {
            private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(threadPoolSize);

            @Override
            public CompletableFuture<Void> schedule(Runnable toBeScheduled, final long repeatEveryInMillis) {
                var scheduledJob = executor.scheduleWithFixedDelay(toBeScheduled, 0,
                        repeatEveryInMillis, TimeUnit.MILLISECONDS);
                return CompletableFuture.runAsync(() -> {
                    try {
                        scheduledJob.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            public CompletableFuture<Void> submit(Runnable job) {
                return CompletableFuture.runAsync(job, executor);
            }

            @Override
            public <T> CompletableFuture<T> submitAndRepeat(final Function<CompletableFuture<T>,
                    CompletableFuture<T>> job,
                                                        final CompletableFuture<T> accumulator,
                                                        final int repetitions, final int delayInSeconds) {
                if (repetitions > 0) {
                    return CompletableFuture.supplyAsync(() -> job.apply(accumulator), executor)
                            .thenCompose(t -> submitAndRepeat(job, t, repetitions - 1, delayInSeconds));
                }
                return accumulator;
            }

            @Override
            public Executor getContext() {
                return executor;
            }
        };
    }
}
