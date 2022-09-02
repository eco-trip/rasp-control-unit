package io.github.ecotrip.adapter.builder;

import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.sensor.Sensor;

/**
 * Builder used to construct {@link Sensor} instances.
 * @param <ID>
 */
public abstract class SensorBuilder<ID> {
    private ID identifier;
    private DetectionFactory<ID> detectionFactory;

    public SensorBuilder<ID> setIdentifier(final ID identifier) {
        this.identifier = identifier;
        return this;
    }

    public SensorBuilder<ID> setDetectionFactory(DetectionFactory<ID> detectionFactory) {
        this.detectionFactory = detectionFactory;
        return this;
    }

    public abstract Sensor<ID> build();

    protected ID getIdentifier() {
        return identifier;
    }

    protected DetectionFactory<ID> getDetectionFactory() {
        return detectionFactory;
    }
}
