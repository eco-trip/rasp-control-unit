package io.github.ecotrip;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.sensor.Sensor;

/**
 * Class use to change the abstract methods' visibility from protected to public
 */
public abstract class MockedSensor extends Sensor<UUID> {
    public MockedSensor(UUID identifier, DetectionFactory<UUID> detectionFactory) {
        super(identifier, detectionFactory);
    }

    @Override
    public abstract CompletableFuture<List<Measure>> measure();

    @Override
    public abstract boolean isMeasureValid(Measure measure);

    public void setMockedBehaviour(final List<Measure> measures) {
        when(this.measure()).thenReturn(CompletableFuture.completedFuture(measures));
        when(this.isMeasureValid(any(Measure.class))).thenReturn(true);
    }
}
