package io.github.ecotrip.adapter;

import java.util.concurrent.CompletableFuture;

/**
 * The bridge with the outside.
 * @param <T> the message group.
 * @param <M> the message to send.
 */
public interface OutputAdapter<T, M> {
    CompletableFuture<Void> sendMessage(final T topic, final M message);
}
