package io.github.ecotrip.sensor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.github.ecotrip.Entity;
import io.github.ecotrip.measure.Measure;

/**
 * Represents the instance of a measure done at some point.
 * @param <ID> uniquely identifies the Detection
 */
public class Detection<ID> extends Entity<ID> {
    private final Instant detectionTime;
    private final List<Measure> measures;

    private Detection(final ID identifier, final Instant detectionTime, final List<Measure> values) {
        super(identifier);
        this.detectionTime = detectionTime;
        this.measures = values;
    }

    public static <ID> Detection<ID> of(final ID identifier, final List<Measure> values) {
        return new Detection<>(identifier, Instant.now(), values);
    }

    public static <ID> Detection<ID> empty(final ID identifier) {
        return new Detection<>(identifier, Instant.now(), List.of());
    }

    public Instant getDetectionTime() {
        return Instant.from(detectionTime);
    }

    public List<Measure> getMeasures() {
        return new ArrayList<>(measures);
    }

    @Override
    public String toString() {
        return "Detection{"
                + "identifier=" + getIdentifier()
                + ", detectionTime=" + detectionTime
                + ", measures=" + measures
                + '}';
    }
}
