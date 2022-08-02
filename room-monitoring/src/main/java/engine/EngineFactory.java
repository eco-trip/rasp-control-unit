package engine;

import java.util.concurrent.*;
import java.util.function.Function;

public class EngineFactory {
    public static <T> Engine<T> createScheduledExecutor() {
        return new Engine<>() {
            private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            /**
             *
             * @param toBeScheduled is the job which has to be scheduled
             * @param repeatEvery time after which the runnable must be rescheduled
             */
            @Override
            public void schedule(Runnable toBeScheduled, final long repeatEvery) {
                var f = executor.scheduleWithFixedDelay(toBeScheduled,0, repeatEvery, TimeUnit.SECONDS);
                getBackgroundJobs().add(f);
            }

            @Override
            public void submit(Runnable job) {
                executor.submit(job);
            }

            @Override
            public CompletableFuture<T> submitAndRepeat(final Function<CompletableFuture<T>, CompletableFuture<T>> job,
                                                        final CompletableFuture<T> accumulator,
                                                        final int repetitions, final int delayInSeconds) {
                var delayedExecutor = CompletableFuture.delayedExecutor(delayInSeconds, TimeUnit.SECONDS, executor);
                if(repetitions > 0) {
                    return CompletableFuture.supplyAsync(() -> job.apply(accumulator), delayedExecutor)
                            .thenCompose(t -> submitAndRepeat(job, t,repetitions-1, delayInSeconds));
                }
                return accumulator;
            }
        };
    }
}
