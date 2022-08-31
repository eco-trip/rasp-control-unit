package io.github.ecotrip.execution;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utils class for Java {@link CompletableFuture}.
 */
public class Futures {

    public static <T> CompletableFuture<Void> mergeFutures(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public static <T, U> CompletableFuture<U> thenAll(List<CompletableFuture<T>> futures, Function<List<T>, U> fn) {
        return thenAll(futures, fn, Integer.MAX_VALUE);
    }

    /**
     * Wait for all futures to end and then execution a provided function.
     * @param futures to wait.
     * @param fn is the function to execution as last action.
     * @param timeoutInMillis if exceed it will complete the future with an exception.
     * @param <T> specify the type of futures' results.
     * @param <U> specify the type of returned {@link CompletableFuture}.
     * @return the possible result wrapped inside a {@link CompletableFuture}.
     */
    public static <T, U> CompletableFuture<U> thenAll(List<CompletableFuture<T>> futures, Function<List<T>, U> fn,
                                                      final int timeoutInMillis) {
        return CompletableFuture.supplyAsync(() -> {
            var results = futures.stream()
                    .map(f -> safeGet(f, timeoutInMillis))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableList());
            return fn.apply(results);
        });
    }

    /**
     * Reduce the boilerplate code necessary to extract the result from a {@link CompletableFuture}.
     * @param future to wait.
     * @param timeoutInMillis is the maximum waiting time.
     * @param <T> the type of future's value to get.
     * @return the future's result.
     */
    public static <T> T safeGet(CompletableFuture<T> future, int timeoutInMillis) {
        try {
            return future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.completeExceptionally(e);
            return null;
        }
    }
}
