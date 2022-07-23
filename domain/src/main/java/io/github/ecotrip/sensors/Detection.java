package io.github.ecotrip.sensors;

import io.github.ecotrip.Entity;
import io.github.ecotrip.measures.Measure;

import java.time.Instant;

public class Detection<ID, M extends Measure<?>> extends Entity<ID> {
    private final Instant detectionTime;
    private final M measure;

    private Detection(final ID identifier, final Instant detectionTime, final M value) {
        super(identifier);
        this.detectionTime = detectionTime;
        this.measure = value;
    }

    public static <ID, M extends Measure<?>> Detection<ID, M> of(final ID identifier, final M value) {
        return new Detection<>(identifier, Instant.now(), value);
    }

    public static <ID, M extends Measure<?>> Detection<ID, M> empty(final ID identifier) {
        return new Detection<>(identifier, Instant.now(), null);
    }

    public Instant getDetectionTime() {
        return detectionTime;
    }

    public M getMeasure() {
        return measure;
    }

    @Override
    public String toString() {
        return "Detection{" +
                "identifier=" + getIdentifier() +
                ", detectionTime=" + detectionTime +
                ", measure=" + measure +
                '}';
    }
}
