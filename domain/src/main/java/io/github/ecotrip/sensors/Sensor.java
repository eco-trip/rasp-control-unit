package io.github.ecotrip.sensors;

import io.github.ecotrip.Entity;
import io.github.ecotrip.measures.Measure;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class Sensor<ID> extends Entity<ID> {
    private final DetectionFactory<ID> detectionFactory;

    protected Sensor(final ID identifier, final DetectionFactory<ID> detectionFactory) {
        super(identifier);
        this.detectionFactory = detectionFactory;
    }

    public CompletableFuture<Detection<ID>> detect() {
        final CompletableFuture<List<Measure>> measures = measure();
        return measures.thenApply(m -> {
            var validMeasures = m.stream()
                    .filter(this::isMeasureValid)
                    .collect(Collectors.toUnmodifiableList());
            if(validMeasures.isEmpty()) {
                return detectionFactory.createEmpty();
            }
            return detectionFactory.create(validMeasures);
        });
    }

    protected abstract CompletableFuture<List<Measure>> measure();

    protected abstract boolean isMeasureValid(Measure measure);
}
