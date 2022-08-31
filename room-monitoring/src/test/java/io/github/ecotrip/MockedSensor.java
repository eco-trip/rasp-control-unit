package io.github.ecotrip;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.mockito.Mockito;

import io.github.ecotrip.measure.Measure;
import io.github.ecotrip.sensor.DetectionFactory;
import io.github.ecotrip.sensor.Sensor;

/**
 * Class use to change the abstract methods' visibility from protected to public
 */
public abstract class MockedSensor extends Sensor<UUID> {
    protected MockedSensor(UUID identifier, DetectionFactory<UUID> detectionFactory) {
        super(identifier, detectionFactory);
    }

    @Override
    public abstract CompletableFuture<List<Measure>> measure();

    @Override
    public abstract boolean isMeasureValid(Measure measure);

    public MockedSensor setMockedBehaviour(final List<Measure> measures) {
        when(this.measure()).thenReturn(CompletableFuture.completedFuture(measures));
        when(this.isMeasureValid(any(Measure.class))).thenReturn(true);
        return this;
    }

    /**
     * Helper method, used to avoid boilerplate code.
     * @param identifier uniquely identify the sensor.
     * @param detectionFactory simply the detection creation.
     * @return the mocked sensor.
     */
    public static MockedSensor of(UUID identifier, DetectionFactory<UUID> detectionFactory) {
        return Mockito.mock(MockedSensor.class, Mockito.withSettings()
                .useConstructor(identifier, detectionFactory)
                .defaultAnswer(Mockito.CALLS_REAL_METHODS)
        );
    }
}
