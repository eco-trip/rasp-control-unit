package io.github.ecotrip.sensors;

import io.github.ecotrip.Entity;
import io.github.ecotrip.measures.Measure;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public abstract class Sensor<ID, M extends Measure<?>> extends Entity<ID> {
    private final DetectionFactory<ID, M> detectionFactory;

    protected Sensor(final ID identifier, final DetectionFactory<ID, M> detectionFactory) {
        super(identifier);
        this.detectionFactory = detectionFactory;
    }

    public CompletableFuture<Detection<ID, M>> detect() {
        final CompletableFuture<M> measure = measure();
        return measure.thenApply(m -> {
            if(!isMeasureValid(m)) {
                return detectionFactory.createEmpty();
            }
            return detectionFactory.create(m);
        });
    }

    protected abstract CompletableFuture<M> measure();

    protected abstract boolean isMeasureValid(M measure);
}
