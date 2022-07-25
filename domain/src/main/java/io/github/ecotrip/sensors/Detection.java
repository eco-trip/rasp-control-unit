package io.github.ecotrip.sensors;

import io.github.ecotrip.Entity;
import io.github.ecotrip.measures.Measure;

import java.time.Instant;

public class Detection<ID> extends Entity<ID> {
    private final Instant detectionTime;
    private final Measure measure;

    private Detection(final ID identifier, final Instant detectionTime, final Measure value) {
        super(identifier);
        this.detectionTime = detectionTime;
        this.measure = value;
    }

    public static <ID> Detection<ID> of(final ID identifier, final Measure value) {
        return new Detection<>(identifier, Instant.now(), value);
    }

    public static <ID> Detection<ID> empty(final ID identifier) {
        return new Detection<>(identifier, Instant.now(), null);
    }

    public Instant getDetectionTime() {
        return detectionTime;
    }

    public Measure getMeasure() {
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
