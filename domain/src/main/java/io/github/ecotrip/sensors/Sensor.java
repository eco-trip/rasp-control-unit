package io.github.ecotrip.sensors;

import io.github.ecotrip.Entity;
import io.github.ecotrip.measures.Measure;

import java.util.concurrent.CompletableFuture;

public abstract class Sensor<ID> extends Entity<ID> {
    private final DetectionFactory<ID> detectionFactory;

    protected Sensor(final ID identifier, final DetectionFactory<ID> detectionFactory) {
        super(identifier);
        this.detectionFactory = detectionFactory;
    }

    public CompletableFuture<Detection<ID>> detect() {
        final CompletableFuture<Measure> measure = measure();
        return measure.thenApply(m -> {
            if(!isMeasureValid(m)) {
                return detectionFactory.createEmpty();
            }
            return detectionFactory.create(m);
        });
    }

    protected abstract CompletableFuture<Measure> measure();

    protected abstract boolean isMeasureValid(Measure measure);
}
