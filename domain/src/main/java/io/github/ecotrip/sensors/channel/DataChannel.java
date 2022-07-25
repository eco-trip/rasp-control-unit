package io.github.ecotrip.sensors.channel;

import java.util.concurrent.CompletableFuture;

public interface DataChannel<T> {
    CompletableFuture<T> getRawData();
}
