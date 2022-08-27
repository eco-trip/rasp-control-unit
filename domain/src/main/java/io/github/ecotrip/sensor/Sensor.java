package io.github.ecotrip.sensor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.github.ecotrip.Entity;
import io.github.ecotrip.measure.Measure;

/**
 * Represents a sensor installed inside the hotel room.
 * @param <ID> uniquely identifies the sensor.
 */
public abstract class Sensor<ID> extends Entity<ID> {
    private final DetectionFactory<ID> detectionFactory;

    protected Sensor(final ID identifier, final DetectionFactory<ID> detectionFactory) {
        super(identifier);
        this.detectionFactory = detectionFactory;
    }

    /**
     * Async function used to retrieve the sensor's measurements.
     * @return the measures wrapped inside a specif Detection object.
     */
    public CompletableFuture<Detection<ID>> detect() {
        return measure().thenApply(m -> {
            var validMeasures = m.stream()
                    .filter(this::isMeasureValid)
                    .collect(Collectors.toUnmodifiableList());
            if (validMeasures.isEmpty()) {
                return detectionFactory.createEmpty();
            }
            return detectionFactory.create(validMeasures);
        });
    }

    protected abstract CompletableFuture<List<Measure>> measure();

    protected abstract boolean isMeasureValid(Measure measure);
}
