package io.github.ecotrip.execution.engine;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Represents a context used to run async task.
 * */
public interface Engine {
    CompletableFuture<Void> schedule(Runnable job, long repeatEvery);

    CompletableFuture<Void> submit(final Runnable job);

    <T> CompletableFuture<T> submitAndRepeat(final Function<CompletableFuture<T>, CompletableFuture<T>> job,
                                                         final CompletableFuture<T> accumulator,
                                                         final int repetitions, final int delayInSeconds);

    Executor getContext();
}
